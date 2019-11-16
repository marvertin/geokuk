package cz.geokuk.plugins.kesoid;

import java.util.*;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.importek.InformaceOZdrojich;
import cz.geokuk.plugins.kesoid.mapicon.*;
import cz.geokuk.plugins.kesoid.mapicon.Genom.CitacAlel;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;
import cz.geokuk.util.lang.CounterMap;

public class KesBag {
	//private static final Logger log = LogManager.getLogger(KesBag.class.getSimpleName());

	private final List<Wpt> wpts = new ArrayList<>();
	private Set<Kesoid> kesoidyset;
	private List<Kesoid> kesoidy;

	private CounterMap<Alela> poctyAlel;

	private final Indexator<Wpt> indexator;

	private int maximalniBestOf = 0;
	private int maximalniHodnoceni;
	private int maximalniFavorit;
	private final Genom genom;

	private final CitacAlel citacAlel;

	private InformaceOZdrojich iInformaceOZdrojich;

	public KesBag(final Genom genom) {
		this.genom = genom;
		indexator = new Indexator<>(BoundingRect.ALL);
		kesoidyset = new HashSet<>();
		citacAlel = genom.createCitacAlel();
	}

	public void add(final Wpt wpt, Genotyp genotyp) {
		if (genotyp == null) { // to je zde jen z důvodu optimalizace
			genotyp = wpt.getGenotyp(genom);
		}
		final Mou mou = wpt.getMou();
		indexator.vloz(mou.xx, mou.yy, wpt);
		final Kesoid kesoid = wpt.getKesoid();
		kesoidyset.add(kesoid);
		wpts.add(wpt);
		if (kesoid instanceof Kes) {
			final Kes kes = (Kes) kesoid;
			maximalniBestOf = Math.max(maximalniBestOf, kes.getBestOf());
			maximalniHodnoceni = Math.max(maximalniHodnoceni, kes.getHodnoceni());
			maximalniFavorit = Math.max(maximalniFavorit, kes.getFavorit());
		}
		for (final Alela alela : genotyp.getAlely()) {
			assert alela != null;
			citacAlel.add(alela);
		}
	}

	public void done() {
		kesoidy = new ArrayList<>(kesoidyset.size());
		kesoidy.addAll(kesoidyset);
		kesoidyset = null;
		poctyAlel = citacAlel.getCounterMap();
		// System.out.println(poctyAlel);
	}

	/**
	 * @return the genom
	 */
	public Genom getGenom() {
		return genom;
	}

	public Indexator<Wpt> getIndexator() {
		return indexator;
	}

	/**
	 * @return the informaceOZdrojich
	 */
	public InformaceOZdrojich getInformaceOZdrojich() {
		return iInformaceOZdrojich;
	}

	public List<Kesoid> getKesoidy() {
		if (kesoidy == null) {
			throw new RuntimeException("Jeste neni kesBag vytvoren");
		}
		return kesoidy;
	}

	/**
	 * @return the maximalniBestOf
	 */
	public int getMaximalniBestOf() {
		return maximalniBestOf;
	}

	public int getMaximalniFavorit() {
		return maximalniFavorit;
	}

	public int getMaximalniHodnoceni() {
		return maximalniHodnoceni;
	}

	/**
	 * @return the poctyAlel
	 */
	public CounterMap<Alela> getPoctyAlel() {
		return poctyAlel;
	}

	public Set<Alela> getPouziteAlely() {
		// TODO optimalizovat
		return poctyAlel.getMap().keySet();
	}

	public List<Wpt> getWpts() {
		if (kesoidy == null) {
			throw new RuntimeException("Jeste neni kesBag vytvoren");
		}
		return wpts;
	}

	/**
	 * @param informaceOZdrojich
	 *            the informaceOZdrojich to set
	 */
	public void setInformaceOZdrojich(final InformaceOZdrojich informaceOZdrojich) {
		iInformaceOZdrojich = informaceOZdrojich;
	}
}
