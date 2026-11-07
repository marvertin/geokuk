package cz.geokuk.plugins.challenge;

/** Které druhy keší se do challenge počítají (nálezy i kandidáti). */
public final class PravidlaTypu {

	/** Nejvíc kandidátů na jedno políčko, pokud se nenastaví jinak. */
	public static final int VYCHOZI_MAX_KANDIDATU = 50;

	private final boolean lab;
	private final boolean locationless;
	private final int maxKandidatu;
	private final int opakovani;

	private PravidlaTypu(final boolean lab, final boolean locationless, final int maxKandidatu) {
		this(lab, locationless, maxKandidatu, 1);
	}

	private PravidlaTypu(final boolean lab, final boolean locationless, final int maxKandidatu, final int opakovani) {
		this.opakovani = opakovani;
		this.lab = lab;
		this.locationless = locationless;
		this.maxKandidatu = maxKandidatu;
	}

	/** D/T matice: Locationless se počítá, Lab nemá D/T. */
	public static PravidlaTypu proMatici() {
		return new PravidlaTypu(false, true, VYCHOZI_MAX_KANDIDATU);
	}

	/** Kalendář: bez Lab a Locationless. */
	public static PravidlaTypu proKalendar() {
		return new PravidlaTypu(false, false, VYCHOZI_MAX_KANDIDATU);
	}

	public PravidlaTypu sLab(final boolean zapocitat) {
		return new PravidlaTypu(zapocitat, locationless, maxKandidatu, opakovani);
	}

	public PravidlaTypu sLocationless(final boolean zapocitat) {
		return new PravidlaTypu(lab, zapocitat, maxKandidatu, opakovani);
	}

	/** Kolik kandidátů se nejvýš vrátí na jedno políčko: v kalendáři s nejbližším datem, při shodě i v matici nejbližších referenčnímu bodu. */
	public PravidlaTypu sMaxKandidatu(final int max) {
		if (max < 1) {
			throw new IllegalArgumentException("Počet kandidátů na políčko musí být alespoň 1: " + max);
		}
		return new PravidlaTypu(lab, locationless, max, opakovani);
	}

	/** Kolikrát se má každé políčko naplnit; výchozí 1. */
	public PravidlaTypu sOpakovanim(final int kolikrat) {
		if (kolikrat < 1) {
			throw new IllegalArgumentException("Opakování musí být alespoň 1: " + kolikrat);
		}
		return new PravidlaTypu(lab, locationless, maxKandidatu, kolikrat);
	}

	int opakovani() {
		return opakovani;
	}

	int maxKandidatu() {
		return maxKandidatu;
	}

	boolean lzePocitat(final VstupKes k) {
		return (lab || !k.lab()) && (locationless || !k.locationless());
	}

	/** Nález se počítá, pokud gc.com nevylučuje jeho log. */
	static boolean platnyNalez(final VstupKes k) {
		return k.nalezena() && k.logNaGc() != VstupKes.StavLogu.NE;
	}

	/** Nenalezená keš, kterou lze jít hledat. */
	static boolean hledatelna(final VstupKes k) {
		return !k.nalezena() && !k.vlastni() && !k.neaktivni() && !k.mimoKandidaty();
	}
}
