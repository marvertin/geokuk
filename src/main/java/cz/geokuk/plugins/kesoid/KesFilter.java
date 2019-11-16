package cz.geokuk.plugins.kesoid;

import java.util.HashSet;
import java.util.Set;

import cz.geokuk.plugins.kesoid.mapicon.*;
import cz.geokuk.plugins.vylety.EVylet;
import cz.geokuk.plugins.vylety.VyletModel;

public class KesFilter {

	private FilterDefinition filterDefinition;
	private Set<Alela> nechteneAlely;
	private Set<String> jmenaNechtenychAlel;

	private VyletModel vyletModel;

	public KesFilter() {}

	// public void onEvent(IgnoreListChangedEvent event) {
	// if (filterDefinition.getPrahVyletu() != EVylet.VSECHNY) {
	// kesoidModel.spustFiltrovani();
	// }
	//
	// }

	// public void onEvent(CestyChangedEvent aEvent) {
	// EVylet evylPrah = filterDefinition.getPrahVyletu();
	//
	// if (evylPrah == EVylet.JEN_V_CESTE) {
	// Set<Wpt> wpts = new HashSet<Wpt>();
	// FUtil.addAll(wpts, aEvent.getDoc().getWpts());
	// if (! wpts.equals(jenTytoVyletoveWaypointyZobrazit)) {
	// jenTytoVyletoveWaypointyZobrazit = wpts;
	// kesoidModel.spustFiltrovani();
	// }
	//
	// }
	// else {
	// jenTytoVyletoveWaypointyZobrazit = null;
	// }
	// }
	//

	/**
	 *
	 */
	public void done() {
		nechteneAlely = null;
	}

	/**
	 * @return the filterDefinition
	 */
	public FilterDefinition getFilterDefinition() {
		return filterDefinition;
	}

	/**
	 * @return the jmenaNechtenychAlel
	 */
	public Set<String> getJmenaNechtenychAlel() {
		return jmenaNechtenychAlel;
	}

	/**
	 *
	 */
	public void init() {
		nechteneAlely = null;
	}

	public void inject(final VyletModel vyletModel) {
		this.vyletModel = vyletModel;
	}

	public boolean isFiltered(final Wpt aWpt, final Genom genom, Genotyp genotyp) {
		try {
			if (genotyp == null) { // to je zde jen z důvodu optimalizace
				genotyp = aWpt.getGenotyp(genom);
			}
			final Set<Alela> alelygenotypu = genotyp.getAlely();
			if (jmenaNechtenychAlel != null) {
				if (nechteneAlely == null) {
					nechteneAlely = genom.namesToAlelyIgnorujNeexistujici(jmenaNechtenychAlel);
				}
				final Set<Alela> alely = new HashSet<>(nechteneAlely);
				alely.retainAll(alelygenotypu);
				if (alely.size() > 0) {
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

	public void setDefaults() {
		// Atom.of(AWptType.FINAL_LOCATION);
		filterDefinition = new FilterDefinition();
	}

	/**
	 * @param filterDefinition
	 *            the filterDefinition to set
	 */
	public void setFilterDefinition(final FilterDefinition filterDefinition) {
		this.filterDefinition = filterDefinition;
	}

	/**
	 * @param jmenaNechtenychAlel
	 *            the jmenaNechtenychAlel to set
	 */
	public void setJmenaNechtenychAlel(final Set<String> jmenaNechtenychAlel) {
		this.jmenaNechtenychAlel = jmenaNechtenychAlel;
		nechteneAlely = null;
	}

}
