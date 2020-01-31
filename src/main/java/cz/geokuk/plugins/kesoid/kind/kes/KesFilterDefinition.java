package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.Preferenceble;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

@Preferenceble
@Data
@AllArgsConstructor
public class KesFilterDefinition {
	@Wither final private int prahHodnoceni;
	@Wither final private int prahBestOf;
	@Wither final private int prahFavorit;
	@Wither final private boolean jenDoTerenuUNenalezenych;
	@Wither final private boolean jenFinalUNalezenych;

	public KesFilterDefinition() {
		prahBestOf = 0;
		prahFavorit = 0;
		prahHodnoceni = 0;
		jenDoTerenuUNenalezenych = true;
		jenFinalUNalezenych = true;
	}

}
