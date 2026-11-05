package cz.geokuk.plugins.challenge;

/** Nastavení challenge kalendář nad rámec {@link PravidlaTypu}. */
public final class PravidlaKalendare {

	/** Odkud se berou kandidáti. */
	public enum Kandidati {
		/** Eventy s datem v chybějícím dni, které ještě nenastaly. */
		JEN_EVENTY,
		/**
		 * Eventy a ostatní keše, jejichž datum zveřejnění připadá na chybějící den. Nenalezená běžná keš kalendář nezaplní, protože se loguje v den nálezu, ne v den
		 * zveřejnění; hodí se jen jako nápověda, UI to má říct.
		 */
		EVENTY_A_VYROCI
	}

	private final PravidlaTypu typy;
	private final boolean pozadovatPrestupny;
	private final Kandidati kandidati;

	private PravidlaKalendare(final PravidlaTypu typy, final boolean pozadovatPrestupny, final Kandidati kandidati) {
		this.typy = typy;
		this.pozadovatPrestupny = pozadovatPrestupny;
		this.kandidati = kandidati;
	}

	/** 365 dní bez 29. 2., jen eventy jako kandidáti, bez Lab a Locationless. */
	public static PravidlaKalendare vychozi() {
		return new PravidlaKalendare(PravidlaTypu.proKalendar(), false, Kandidati.JEN_EVENTY);
	}

	public PravidlaKalendare sTypy(final PravidlaTypu novaTypy) {
		return new PravidlaKalendare(novaTypy, pozadovatPrestupny, kandidati);
	}

	/** Zda se vyžaduje i nález 29. 2. (366 dní). */
	public PravidlaKalendare sPrestupnym(final boolean pozadovat) {
		return new PravidlaKalendare(typy, pozadovat, kandidati);
	}

	public PravidlaKalendare sKandidaty(final Kandidati novi) {
		return new PravidlaKalendare(typy, pozadovatPrestupny, novi);
	}

	PravidlaTypu typy() {
		return typy;
	}

	boolean pozadovatPrestupny() {
		return pozadovatPrestupny;
	}

	Kandidati kandidati() {
		return kandidati;
	}
}
