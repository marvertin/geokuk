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

			if (! aWpt.getKesoidPlugin().filter(aWpt)) {
				return false; // Plugin si nepřeje, aby byl waypoint viděn
			}


			if (!vyletFilter(aWpt)) {
				return false;  // výlet nám to dává pryč
			}
			return true;
		} catch (final Exception e) {
			throw new RuntimeException("Filtrovani waypointu: " + aWpt, e);

		}

	}

	private boolean vyletFilter(final Wpt wpt) {
		if (vyletModel == null) {
			System.out.println("Vylet mode neni");
			return true; // nemáme model nemůžeme odfiltrovávat
		}
		switch (filterDefinition.getPrahVyletu()) {
		case VSECHNY: return true; // všechny tam máme mít
		case BEZ_VYNECHANYCH: { // nemáme tam mít vynechané, ale jednotlivé waypointy
			final EVylet evylKes = vyletModel.get(wpt);
			return evylKes != EVylet.NE; // takže když neříkám ne, nechám totam
		}
		case JEN_LOVENE: {// jen lovené, ale zobrazme vše, co s tím souvisí
			for (final Wpt wtpx : wpt.getRelatedWpts()) {
				final EVylet evylKes = vyletModel.get(wtpx);
				if (evylKes == EVylet.ANO) {
					return true;
				}
			}
			return false;
		}
		default: return true; // raději zobrazit, když je to neco jiného
		}
	}

	private synchronized Set<Alela> nechteneAlely(final Genom genom) {
		if (this.nechteneAlely == null) {
			this.nechteneAlely = genom.searchAlelasByQualNames(jmenaNechtenychAlel);
		}
		return this.nechteneAlely;
	}

}
