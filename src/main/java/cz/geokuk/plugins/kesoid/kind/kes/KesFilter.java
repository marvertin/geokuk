package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoid.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KesFilter {

	private final KesFilterDefinition filterDefinition;


	public boolean filter(final Wpt aWptx) {
		final Wpti wpti = (Wpti)aWptx;
		final Kesoid0 kesoid = wpti.getKesoid();

		if (filterDefinition.isJenFinalUNalezenych()) {
			if (kesoid.getVztah() == EWptVztah.FOUND || kesoid.getVztah() == EWptVztah.OWN) {
				if (wpti != kesoid.getFirstWpt()) {
					return false;
				}
			}
		}

		final Kes kes = (Kes) kesoid;

		if (kes.getVztah() == EWptVztah.NORMAL) { // jen u nenalezených
			if (kes.hasValidFinal() && filterDefinition.isJenDoTerenuUNenalezenych() && !nutnyKLusteni(wpti) && !Wpti.TRADITIONAL_CACHE.equals(kes.getFirstWpt().getSym())) {
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


	private boolean nutnyKLusteni(final Wpti wpt) {
		final EKesWptType wptType = EKesWptType.decode(wpt.getSym());
		return wpt.isMainWpt() || wptType == EKesWptType.QUESTION_TO_ANSWER || wptType == EKesWptType.STAGES_OF_A_MULTICACHE;
	}



}
