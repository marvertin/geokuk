package cz.geokuk.util.procak;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

/**
 * Dipečuje zpracování procákům, které v sobě dostal a zařizuje, aby se správně interpretovaly návratové kódy.
 * @author veverka
 *
 */

public class ProcakDispatcher<T> {

	/** Procáci, kterří budou zpracovávat v uvedeném pořadí */
	private final List<Procak<T>> procaci;

	private final Kolo kolo1;
	/**
	 * Klient dává objekt ke zpracování.
	 * @param obj
	 */
	public void dispatch(final T obj) {
		kolo1.dispatch( new Drzak(obj, procaci));
	}

	public ProcakDispatcher(final List<Procak<T>> procaci) {
		this.procaci = Collections.unmodifiableList(procaci);
		this.kolo1 = new Kolo();
	}



	private class Kolo {
		/** Sem se sbírájí neporpanci, to jsou ty, kteří nesmějí být posláni dál, ale musejí přijít znovu, ty seznamy musí být synchronizované, */
		private final Map<Procak<T>, List<Drzak>> nepropanci  = Collections.unmodifiableMap(procaci.stream().collect(Collectors.toMap(Function.identity(),
				__ -> Collections.synchronizedList(new LinkedList<>()),
				(u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
				() ->  new IdentityHashMap<>()
				)));
		/** Sem se posílají ti, kteří zporacování nebyli, a když projdou bzudou posláni znovu */
		private final List<Drzak> drzaci = Collections.synchronizedList(new LinkedList<>());
		/** Celkový počet objektů verzus procáci, kteří mají být poslánni, tedy kolikrát se bude ještě volat procak.proces.
		 * Eviduje se, aby se už nedělala další kola, když už není kam */
		private int pocet = 0;


		private void dispatch(final Drzak aDrzak) {
			final Drzak drzakx = new Drzak(aDrzak.obj);
			Drzak drzak = null;
			cyklus: for (final Procak<T> procak : aDrzak.procaci) {
				if (drzak != null) {
					// Když se nastavilo na null, znamená to, že do dalších procáků to nedáváme, ale evidujeme je na zpracování v daším kole
					drzak.zpracovatVPristimKole(procak);
					pocet ++;
					continue;
				}
				final EProcakResult result = procak.process(aDrzak.obj);
				switch (result) {
				case DONE: return;
				case NEVER: continue cyklus;
				case NEXT_ROUND:
					drzakx.zpracovatVPristimKole(procak);
					continue cyklus;
				case PROBABLY_MY_BUT_NEXT_ROUND:
					drzak = new Drzak(aDrzak.obj);
					drzak.zpracovatVPristimKole(procak);
					nepropanci.get(procak).add(drzak);
					pocet += drzak.getPocet();
				}
			};
			if (drzakx.procaci != null) {
				drzaci.add(drzakx);
				pocet += drzakx.getPocet();
			}
		}

		/**
		 * Dispačne kolo do tohoto kola s tím, že postupně uzavírá kolo předchozí.
		 * @param kolo
		 */
		private void dispatch (final Kolo kolo) {

			for (final Procak<T> procak : procaci) {
				// tady jedeme přes všechny procáky, aby se zachovalo pořadí
				procak.roundDone(); // uzavřeme jeho kolo, protože to, co dostane je v novém kole, postupně uzavřeme všechna kola
				// všechny, které máme schoané dipečneme všem. Těm nahoře a komut to patří je to v následném kole
				// následujícím je to v původním kole, protože to ještě neviděli a když to tentokrát propadlo, mohou to dostat.
				kolo.nepropanci.get(procak)
				.forEach(this::dispatch);

			};
			// už je to určitě nové kolo, můžeme propadat
			kolo.drzaci.forEach(this::dispatch);

		}

	}

	/**
	 * Klient oznamuje, že je vše zpracováno a že mohou začít další kola.
	 */
	public void done() {
		Kolo koloA = null; // jakoby předkolo, které nebylo
		Kolo koloB = kolo1; // kolo právě proběhlo, ale jinak
		do {
			koloA = koloB; // a vyměníme kola
			koloB = new Kolo();
			koloB.dispatch(koloA);
		}
		while (koloB.pocet < koloA.pocet);

	}


	/** Drží objekt a procáky, které ho budoi zpracovávat další kolo. */
	@RequiredArgsConstructor
	private class Drzak {
		private final T obj;

		private List<Procak<T>> procaci;

		void zpracovatVPristimKole(final Procak<T> procak) {
			if (procaci == null) {
				// většinou asi nebude potřeba protož se to zpracuje hned, tak vyrábíme až v případě potřeby.
				procaci = new LinkedList<Procak<T>>();
			}
			// přidáme ho, víme, že když není null, jedentam bude
			procaci.add(procak);
		}

		public Drzak(final T obj, final List<Procak<T>> procaci) {
			super();
			this.obj = obj;
			this.procaci = procaci;
		}

		public int getPocet() {
			return procaci == null ? 0 : procaci.size();
		}

	}



}
