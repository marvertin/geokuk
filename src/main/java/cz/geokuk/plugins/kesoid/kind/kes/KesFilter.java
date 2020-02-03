package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoid.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KesFilter {

	private final KesFilterDefinition filterDefinition;


	public boolean filter(final Wpt aWpt) {
		final Kesoid kesoid = aWpt.getKesoid();

		if (filterDefinition.isJenFinalUNalezenych()) {
			if (kesoid.getVztah() == EKesVztah.FOUND || kesoid.getVztah() == EKesVztah.OWN) {
				if (aWpt != kesoid.getMainWpt()) {
					return false;
				}
			}
		}

		final Kes kes = (Kes) kesoid;

		if (kes.getVztah() == EKesVztah.NORMAL) { // jen u nenalezených
			if (kes.hasValidFinal() && filterDefinition.isJenDoTerenuUNenalezenych() && !nutnyKLusteni(aWpt) && !Wpt.TRADITIONAL_CACHE.equals(kes.getFirstWpt().getSym())) {
				return false;
			}
		}

		if (kes.getHodnoceni() != Kes.NENI_HODNOCENI) {
			if (kes.getHodnoceni() < filterDefinition.getPrahHodnoceni()) {
				return false;
			}
		}
		if (kes.getBestOf() != Kes.NENI_HODNOCENI) {
			if (kes.getBestOf() < filterDefinition.getPrahBestOf()) {
				return false;
			}
		}
		if (kes.getFavorit() != Kes.NENI_HODNOCENI) {
			if (kes.getFavorit() < filterDefinition.getPrahFavorit()) {
				return false;
			}
		}
		return true;
	}


	private boolean nutnyKLusteni(final Wpt wpt) {
		final EKesWptType wptType = EKesWptType.decode(wpt.getSym());
		return wpt.isMainWpt() || wptType == EKesWptType.QUESTION_TO_ANSWER || wptType == EKesWptType.STAGES_OF_A_MULTICACHE;
	}



}
