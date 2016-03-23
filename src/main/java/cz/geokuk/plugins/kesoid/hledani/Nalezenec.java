package cz.geokuk.plugins.kesoid.hledani;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.Nalezenec0;
import cz.geokuk.plugins.kesoid.Kesoid;

public class Nalezenec extends Nalezenec0 {
	private Kesoid	kesoid;

	private String	kdeNalezeno;
	// Indexy místa v řetězci, kde byl text nalezen
	private int		poc;
	private int		kon;

	public String getKdeNalezeno() {
		return kdeNalezeno;
	}

	public Kesoid getKes() {
		return kesoid;
	}

	public int getKon() {
		return kon;
	}

	public int getPoc() {
		return poc;
	}

	@Override
	public Wgs getWgs() {
		return kesoid.getMainWpt().getWgs();
	}

	public void setKdeNalezeno(final String kdeNalezeno) {
		this.kdeNalezeno = kdeNalezeno;
	}

	public void setKes(final Kesoid kes) {
		kesoid = kes;
	}

	public void setKon(final int kon) {
		this.kon = kon;
	}

	public void setPoc(final int poc) {
		this.poc = poc;
	}
}
