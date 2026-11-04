package cz.geokuk.util.gui;

import java.util.function.LongSupplier;

/**
 * Kdy je vidět legenda posuvníků nad mapou: po najetí myší (s malým zpožděním), při tažení, při změně kolečkem nebo klávesou a při fokusu. Objeví se plynule, po skončení
 * interakce se drží a plynule zmizí, časy jsou společné s {@link BublinkaHodnoty}. Animaci řídí časovač na EDT, který se zastaví, když se nic neděje.
 */
public final class ViditelnostLegendy {

	/** Jak dlouho musí myš zůstat nad posuvníky, než se legenda začne ukazovat. */
	public static final int ZPOZDENI_NAJETI_MS = 200;

	private final LongSupplier hodinyMs;
	private final BublinkaHodnoty.Casovac casovac;
	private final Runnable prekresli;
	private boolean mys;
	private long mysOd;
	private boolean tahne;
	private boolean fokus;
	/** Legenda se ukazuje nebo mizí. */
	private boolean aktivni;
	private long zacatek;
	/** Kdy interakce skončila, -1 dokud trvá. */
	private long skoncila = -1;
	private float posledniKreslena = -1f;

	public ViditelnostLegendy(final Runnable prekresli) {
		this.hodinyMs = () -> System.nanoTime() / 1_000_000;
		this.prekresli = prekresli;
		this.casovac = BublinkaHodnoty.swingovyCasovac(this::tik);
	}

	ViditelnostLegendy(final LongSupplier hodinyMs, final BublinkaHodnoty.Casovac casovac, final Runnable prekresli) {
		this.hodinyMs = hodinyMs;
		this.casovac = casovac;
		this.prekresli = prekresli;
	}

	/** Myš je (nebo už není) nad posuvníky. */
	public void mys(final boolean uvnitr) {
		if (uvnitr && !mys) {
			mysOd = hodinyMs.getAsLong();
		}
		mys = uvnitr;
		vyhodnot();
	}

	public void tahne(final boolean tahne) {
		this.tahne = tahne;
		vyhodnot();
	}

	public void fokus(final boolean fokus) {
		this.fokus = fokus;
		vyhodnot();
	}

	/** Změna hodnoty kolečkem nebo klávesou: legenda se krátce ukáže. */
	public void dotyk() {
		final long ted = hodinyMs.getAsLong();
		aktivuj(ted, ted);
		skoncila = ted;
		zajistiCasovac(ted);
		prekresli.run();
	}

	/** Okamžitě skryje legendu a zastaví časovač, třeba když se vrstva skryje. */
	public void zrus() {
		mys = false;
		tahne = false;
		fokus = false;
		aktivni = false;
		skoncila = -1;
		posledniKreslena = -1f;
		casovac.zastav();
		prekresli.run();
	}

	/** Viditelnost legendy 0 až 1. */
	public float viditelnost() {
		if (!aktivni) {
			return 0f;
		}
		final long ted = hodinyMs.getAsLong();
		return BublinkaHodnoty.viditelnost(ted - zacatek, skoncila < 0 ? -1 : ted - skoncila);
	}

	public boolean jeAktivni() {
		return aktivni;
	}

	public boolean casovacBezi() {
		return casovac.bezi();
	}

	/** Krok animace: překreslí, když se viditelnost změnila, a zastaví časovač, když se nic neděje. */
	void tik() {
		final long ted = hodinyMs.getAsLong();
		vyhodnotBezCasovace(ted);
		final float v = viditelnost();
		if (v != posledniKreslena) {
			posledniKreslena = v;
			prekresli.run();
		}
		final boolean ustaleno = aktivni && skoncila < 0 && v >= 1f;
		if ((!aktivni && !ceka(ted)) || ustaleno) {
			casovac.zastav();
		}
	}

	private boolean ceka(final long ted) {
		return mys && ted - mysOd < ZPOZDENI_NAJETI_MS;
	}

	private boolean chce(final long ted) {
		if (tahne || fokus) {
			return true;
		}
		// Nad už viditelnou legendou nečeká nic, nad skrytou počká zpoždění.
		return mys && (ted - mysOd >= ZPOZDENI_NAJETI_MS || viditelnost() > 0f);
	}

	/** Začátek náběhu: při najetí myší se počítá od uplynutí zpoždění, i když to časovač zjistí o něco později. */
	private void aktivuj(final long ted, final long odKdy) {
		if (!aktivni) {
			zacatek = odKdy;
		} else if (skoncila >= 0) {
			zacatek = ted - Math.round(viditelnost() * BublinkaHodnoty.ZACATEK_MS);
		}
		aktivni = true;
		skoncila = -1;
	}

	private void vyhodnotBezCasovace(final long ted) {
		if (aktivni && skoncila >= 0 && ted - skoncila >= BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS) {
			aktivni = false;
			skoncila = -1;
		}
		if (chce(ted)) {
			aktivuj(ted, tahne || fokus ? ted : Math.min(ted, mysOd + ZPOZDENI_NAJETI_MS));
		} else if (aktivni && skoncila < 0) {
			skoncila = ted;
		}
	}

	private void vyhodnot() {
		final long ted = hodinyMs.getAsLong();
		vyhodnotBezCasovace(ted);
		zajistiCasovac(ted);
		final float v = viditelnost();
		if (v != posledniKreslena) {
			posledniKreslena = v;
			prekresli.run();
		}
	}

	private void zajistiCasovac(final long ted) {
		if ((aktivni || ceka(ted)) && !casovac.bezi()) {
			casovac.spust();
		}
	}
}
