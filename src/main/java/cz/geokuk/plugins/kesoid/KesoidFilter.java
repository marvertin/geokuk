package cz.geokuk.plugins.kesoid;

import java.util.Set;

import cz.geokuk.plugins.kesoid.genetika.*;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;
import cz.geokuk.plugins.vylety.EVylet;
import cz.geokuk.plugins.vylety.VyletModel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KesoidFilter {

	private final FilterDefinition filterDefinition;
	private final QualAlelaNames jmenaNechtenychAlel;
	private final VyletModel vyletModel;

	private Set<Alela> nechteneAlely;

	private final IndexMap<Genotyp, Boolean> uzFiltrovane  = new IndexMap<>();


	public boolean isFiltered(final Wpt aWpt) {
		try {
			final Genotyp genotyp = aWpt.getGenotyp();
			final Genom genom = genotyp.getGenom();
			if (jmenaNechtenychAlel != null) {
				final Set<Alela> nechteneAlely = nechteneAlely(genom);
				final boolean chceme = uzFiltrovane.computeIfAbsent(genotyp, g -> {
					// zbytečně se nepočítá znovu a znovu pro každý stejný genotyp
					return ! g.hasAny(nechteneAlely);
				});
				if (!chceme) {
					return false;
				}
			}

			// if (aWpt.getType() != AWptType.CACHE && ! wptTypes.contains(aWpt.getType())) return false;

			final Kesoid kesoid = aWpt.getKesoid();

			if (filterDefinition.isJenFinalUNalezenych()) {
				if (kesoid.getVztah() == EKesVztah.FOUND || kesoid.getVztah() == EKesVztah.OWN) {
					if (aWpt != kesoid.getMainWpt()) {
						return false;
					}
				}
			}

			if (kesoid instanceof Kes) {
				final Kes kes = (Kes) kesoid;

				if (kes.getVztah() == EKesVztah.NORMAL) { // jen u nenalezených
					if (kes.hasValidFinal() && filterDefinition.isJenDoTerenuUNenalezenych() && !aWpt.nutnyKLusteni() && !Wpt.TRADITIONAL_CACHE.equals(kes.getFirstWpt().getSym())) {
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
			}
			if (vyletModel != null) {
				final EVylet evylKes = vyletModel.get(kesoid);
				final EVylet evylPrah = filterDefinition.getPrahVyletu();
				if (evylKes.ordinal() < evylPrah.ordinal()) {
					return false;
				}
			}
			return true;
		} catch (final Exception e) {
			throw new RuntimeException("Filtrovani waypointu: " + aWpt, e);

		}

	}

	private synchronized Set<Alela> nechteneAlely(final Genom genom) {
		if (this.nechteneAlely == null) {
			this.nechteneAlely = genom.searchAlelasByQualNames(jmenaNechtenychAlel);
		}
		return this.nechteneAlely;
	}

}
