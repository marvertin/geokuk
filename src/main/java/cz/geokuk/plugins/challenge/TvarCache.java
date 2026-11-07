package cz.geokuk.plugins.challenge;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

/**
 * Poslední podklad kreslení sdílený oběma vrstvami plánovače. S vláknem pro stavbu se podklad (neměnný) staví mimo EDT a vrstva se po dokončení překreslí; bez něj (testy) se staví hned.
 * Z EDT; rozpracovaná stavba staršího stavu se zruší, když přijde novější.
 */
final class TvarCache {

	private static final Logger LOG = Logger.getLogger(TvarCache.class.getName());

	private static final class Polozka {
		final PrehledChallenge prehled;
		final VybranyKandidat vybrany;
		final CompletableFuture<TvarPlanovace> budouci = new CompletableFuture<>();
		volatile boolean zruseno;
		/** Vrstvy, které už čekají na dokončení této položky. */
		final Set<Runnable> cekajici = new HashSet<>();
		boolean selhaniZalogovano;

		Polozka(final PrehledChallenge prehled, final VybranyKandidat vybrany) {
			this.prehled = prehled;
			this.vybrany = vybrany;
		}
	}

	private static Polozka posledni;
	/** Kolik rozpracovaných staveb se zrušilo, protože přišel novější stav (testy). */
	static final java.util.concurrent.atomic.AtomicInteger ZRUSENYCH = new java.util.concurrent.atomic.AtomicInteger();

	private TvarCache() {
	}

	/**
	 * Hotový podklad pro přehled a výběr, nebo null, dokud se staví nebo když stavba selhala (viz {@link #selhalo}); po dokončení se jednou zavolá {@code poHotovo} na EDT.
	 */
	static TvarPlanovace dej(final PrehledChallenge prehled, final VybranyKandidat vybrany, final Executor stavba, final Runnable poHotovo) {
		Polozka p = posledni;
		if (p == null || p.prehled != prehled || p.vybrany != vybrany) {
			if (p != null) {
				p.zruseno = true;
			}
			final Polozka nova = new Polozka(prehled, vybrany);
			p = nova;
			posledni = nova;
			final Runnable stav = () -> {
				try {
					nova.budouci.complete(TvarPlanovace.z(prehled, vybrany, () -> nova.zruseno));
				} catch (final CancellationException e) {
					ZRUSENYCH.incrementAndGet();
					nova.budouci.cancel(false);
				} catch (final RuntimeException e) {
					nova.budouci.completeExceptionally(e);
				}
			};
			if (stavba == null) {
				stav.run();
			} else {
				stavba.execute(stav);
			}
		}
		if (p.budouci.isDone()) {
			if (p.budouci.isCompletedExceptionally()) {
				zalogujSelhani(p);
				return null;
			}
			return p.budouci.join();
		}
		if (p.cekajici.add(poHotovo)) {
			p.budouci.whenComplete((v, e) -> SwingUtilities.invokeLater(poHotovo));
		}
		return null;
	}

	/** Stavba podkladu pro poslední přehled skončila chybou nebo byla zrušena; nic se nekreslí. */
	static boolean selhalo() {
		final Polozka p = posledni;
		return p != null && p.budouci.isCompletedExceptionally();
	}

	/** Je podklad zapomenut (testy). */
	static boolean prazdna() {
		return posledni == null;
	}

	/** Zapomene poslední podklad (plánovač se vypnul), a zruší rozpracovanou stavbu. */
	static void vymaz() {
		final Polozka p = posledni;
		if (p != null) {
			p.zruseno = true;
		}
		posledni = null;
	}

	private static void zalogujSelhani(final Polozka p) {
		if (!p.selhaniZalogovano && !p.zruseno) {
			p.selhaniZalogovano = true;
			Throwable t = null;
			try {
				p.budouci.join();
			} catch (final CompletionException | CancellationException e) {
				t = e.getCause() != null ? e.getCause() : e;
			}
			LOG.log(Level.WARNING, "Podklad plánovače na mapě se nepodařilo sestavit, kreslení se vynechá.", t);
		}
	}
}
