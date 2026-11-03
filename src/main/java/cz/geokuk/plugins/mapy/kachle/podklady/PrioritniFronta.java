package cz.geokuk.plugins.mapy.kachle.podklady;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Fronta úloh pro {@link java.util.concurrent.ThreadPoolExecutor}, která úlohy vybírá podle pořadí vyhodnoceného až při výběru, ne při vložení. Pořadí závisí na
 * tom, co uživatel vidí právě teď, a mění se s každým posunem mapy. Úloha, kterou už nikdo nechce, se při výběru zahodí. Při stejném pořadí rozhoduje, která byla vložena dřív.
 * <p>
 * Pořadí se počítá pod zámkem fronty, úloha proto při jeho výpočtu nesmí brát žádný zámek, který drží vlákno zadávající úlohy.
 */
final class PrioritniFronta extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {

	/** Úloha s pořadím vyhodnocovaným při výběru. */
	interface Razena extends Runnable {

		/** Pořadí podle stavu v čase {@code tedNs}: menší číslo jde dřív, {@link ImageReceiver#ZASTARALA}, když už výsledek nikdo nechce. */
		long poradi(long tedNs);

		/** Úloha byla vyřazena jako zastaralá a nespustí se. Volá se mimo zámek fronty. */
		void zahod();
	}

	private final ReentrantLock zamek = new ReentrantLock();
	private final Condition neprazdna = zamek.newCondition();
	private final Condition prazdna = zamek.newCondition();
	private final List<Runnable> prvky = new ArrayList<>();

	/** Vybere nejlepší úlohu a odebere ji; zastaralé odebere také a přidá je do {@code zahodit}. Volá se pod zámkem. */
	private Runnable vyber(final List<Razena> zahodit) {
		final long ted = System.nanoTime();
		int nejlepsi = -1;
		long nejlepsiPoradi = Long.MAX_VALUE;
		for (int i = 0; i < prvky.size();) {
			final Runnable r = prvky.get(i);
			final long poradi = r instanceof Razena ? ((Razena) r).poradi(ted) : 0;
			if (poradi == ImageReceiver.ZASTARALA) {
				zahodit.add((Razena) r);
				prvky.remove(i);
				continue;
			}
			// Přísně menší: při shodě zůstává dřívější v seznamu.
			if (nejlepsi < 0 || poradi < nejlepsiPoradi) {
				nejlepsi = i;
				nejlepsiPoradi = poradi;
			}
			i++;
		}
		return nejlepsi < 0 ? null : prvky.remove(nejlepsi);
	}

	/** Probudí čekající na prázdnou frontu, je-li prázdná. Volá se pod zámkem po odebrání. */
	private void ohlasPrazdnou() {
		if (prvky.isEmpty()) {
			prazdna.signalAll();
		}
	}

	/** Počká, až ve frontě nebude žádná úloha. */
	void pockejNaPrazdnou() throws InterruptedException {
		zamek.lockInterruptibly();
		try {
			while (!prvky.isEmpty()) {
				prazdna.await();
			}
		} finally {
			zamek.unlock();
		}
	}

	private static void zahod(final List<Razena> zahodit) {
		for (final Razena r : zahodit) {
			r.zahod();
		}
	}

	private void pridej(final Runnable r) {
		if (r == null) {
			throw new NullPointerException();
		}
		zamek.lock();
		try {
			prvky.add(r);
			neprazdna.signal();
		} finally {
			zamek.unlock();
		}
	}

	@Override
	public boolean offer(final Runnable r) {
		pridej(r);
		return true;
	}

	@Override
	public boolean offer(final Runnable r, final long timeout, final TimeUnit unit) {
		pridej(r);
		return true;
	}

	@Override
	public void put(final Runnable r) {
		pridej(r);
	}

	@Override
	public Runnable take() throws InterruptedException {
		for (;;) {
			final List<Razena> zahodit = new ArrayList<>();
			Runnable vybrany;
			zamek.lockInterruptibly();
			try {
				while ((vybrany = vyber(zahodit)) == null && zahodit.isEmpty()) {
					neprazdna.await();
				}
				ohlasPrazdnou();
			} finally {
				zamek.unlock();
			}
			zahod(zahodit);
			if (vybrany != null) {
				return vybrany;
			}
		}
	}

	@Override
	public Runnable poll(final long timeout, final TimeUnit unit) throws InterruptedException {
		long zbyva = unit.toNanos(timeout);
		for (;;) {
			final List<Razena> zahodit = new ArrayList<>();
			Runnable vybrany;
			zamek.lockInterruptibly();
			try {
				while ((vybrany = vyber(zahodit)) == null && zahodit.isEmpty()) {
					if (zbyva <= 0) {
						return null;
					}
					zbyva = neprazdna.awaitNanos(zbyva);
				}
				ohlasPrazdnou();
			} finally {
				zamek.unlock();
			}
			zahod(zahodit);
			if (vybrany != null) {
				return vybrany;
			}
		}
	}

	@Override
	public Runnable poll() {
		final List<Razena> zahodit = new ArrayList<>();
		final Runnable vybrany;
		zamek.lock();
		try {
			vybrany = vyber(zahodit);
			ohlasPrazdnou();
		} finally {
			zamek.unlock();
		}
		zahod(zahodit);
		return vybrany;
	}

	/** Nejlepší úloha bez odebrání; zastaralé nechává být. */
	@Override
	public Runnable peek() {
		zamek.lock();
		try {
			final long ted = System.nanoTime();
			Runnable nejlepsi = null;
			long nejlepsiPoradi = Long.MAX_VALUE;
			for (final Runnable r : prvky) {
				final long poradi = r instanceof Razena ? ((Razena) r).poradi(ted) : 0;
				if (poradi != ImageReceiver.ZASTARALA && (nejlepsi == null || poradi < nejlepsiPoradi)) {
					nejlepsi = r;
					nejlepsiPoradi = poradi;
				}
			}
			return nejlepsi;
		} finally {
			zamek.unlock();
		}
	}

	@Override
	public int size() {
		zamek.lock();
		try {
			return prvky.size();
		} finally {
			zamek.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public int remainingCapacity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean remove(final Object o) {
		zamek.lock();
		try {
			final boolean odebrano = prvky.remove(o);
			ohlasPrazdnou();
			return odebrano;
		} finally {
			zamek.unlock();
		}
	}

	@Override
	public boolean contains(final Object o) {
		zamek.lock();
		try {
			return prvky.contains(o);
		} finally {
			zamek.unlock();
		}
	}

	@Override
	public int drainTo(final Collection<? super Runnable> c) {
		return drainTo(c, Integer.MAX_VALUE);
	}

	@Override
	public int drainTo(final Collection<? super Runnable> c, final int maxElements) {
		zamek.lock();
		try {
			int n = 0;
			while (n < maxElements && !prvky.isEmpty()) {
				c.add(prvky.remove(0));
				n++;
			}
			ohlasPrazdnou();
			return n;
		} finally {
			zamek.unlock();
		}
	}

	/** Iterátor nad kopií; {@code remove} odebere prvek z fronty. */
	@Override
	public Iterator<Runnable> iterator() {
		final List<Runnable> kopie;
		zamek.lock();
		try {
			kopie = new ArrayList<>(prvky);
		} finally {
			zamek.unlock();
		}
		final Iterator<Runnable> it = kopie.iterator();
		return new Iterator<Runnable>() {
			private Runnable posledni;

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Runnable next() {
				posledni = it.next();
				return posledni;
			}

			@Override
			public void remove() {
				if (posledni == null) {
					throw new IllegalStateException();
				}
				PrioritniFronta.this.remove(posledni);
				posledni = null;
			}
		};
	}
}
