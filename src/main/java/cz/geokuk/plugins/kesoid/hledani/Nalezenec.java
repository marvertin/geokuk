package cz.geokuk.plugins.kesoid.hledani;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.Nalezenec0;
import cz.geokuk.plugins.kesoid.Wpt;
import lombok.*;

@Getter @Setter @ToString
public class Nalezenec extends Nalezenec0 {
	private Wpt wpt;

	private String kdeNalezeno;
	// Indexy místa v řetězci, kde byl text nalezen
	private int poc;
	private int kon;

	@Override
	public Wgs getWgs() {
		return wpt.getWgs();
	}

}
