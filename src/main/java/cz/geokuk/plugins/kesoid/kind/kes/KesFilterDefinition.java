package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.Preferenceble;
import lombok.*;

@Preferenceble
@Data
@AllArgsConstructor
public class KesFilterDefinition {
	@With private int prahHodnoceni;
	@With private int prahBestOf;
	@With private int prahFavorit;
	@With private boolean jenDoTerenuUNenalezenych;
	@With private boolean jenFinalUNalezenych;

	public KesFilterDefinition() {
		prahBestOf = 0;
		prahFavorit = 0;
		prahHodnoceni = 0;
		jenDoTerenuUNenalezenych = true;
		jenFinalUNalezenych = true;
	}
}
