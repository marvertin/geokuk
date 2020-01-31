package cz.geokuk.plugins.kesoid;

import java.util.Set;

import cz.geokuk.plugins.kesoid.genetika.*;
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

			if (! aWpt.getKesoidPlugin().filter(aWpt)) {
				return false; // Plugin si nepřeje, aby byl waypoint viděn
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
