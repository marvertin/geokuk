package cz.geokuk.plugins.kesoid;

import java.util.*;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.genetika.*;
import cz.geokuk.plugins.kesoid.genetika.Genom.CitacAlel;
import cz.geokuk.plugins.kesoid.importek.InformaceOZdrojich;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;
import cz.geokuk.util.lang.CounterMap;

/**
 * Kešbag drží všechny kešoidy a jejich waypointy a to i zaindexované podle pozice.
 * Vznikají dvě instance:
 *   1. Načtené kešoidy.
 *   2. Vyfiltroivané kešoidy (bude jich míň).
 *
 * Kešoid dostane genom, který pak sdílý. Obě instance sdílejí stejný genom.
 * Během plnění KesBag se může plnit i genom o nové alely, geny a geny vstupují do druhů.
 *
 * Postup je:
 *
 * <pre>
 *    new KesBag(genom)
 *    v cyklu:
 *       add(wpt)
 *    done()
 * </pre>
 * Teprve po done() je připrven k poskytování informací.
 * Musí se do něj vložit všechny waypointy už provázané do kešoidů, kešoidy si odvodí sám.
 * Může tedy obsahovat jen některé waypointy kešoidů, což se často děje právě při filtrování.
 *
 * @author veverka
 *
 */
public class KesBag implements WptReceiver {
	//

	private final List<Wpt> wpts = new ArrayList<>();
	private Set<Kesoid> kesoidyset;
	private List<Kesoid> kesoidy;

	private CounterMap<Alela> poctyAlel;

	private Indexator<Wpt> indexator;


	private int maximalniBestOf = 0;
	private int maximalniHodnoceni;
	private int maximalniFavorit;
	private final Genom genom;

	private final CitacAlel citacAlel;

	private InformaceOZdrojich iInformaceOZdrojich;

	private boolean indexatorOdevzdan = false;

	public KesBag(final Genom genom) {
		this.genom = genom;
		indexator = new Indexator<>(BoundingRect.ALL);
		kesoidyset = new HashSet<>();
		citacAlel = genom.createCitacAlel();
	}

	@Override
	public void expose(final Wpt wpt) {
		if (indexatorOdevzdan) {
			throw new IllegalStateException("Indexator uz byl odevztdan");
		}
		// Následné volání má vedlejší efekt spočívající ve výpočtu genotypu a schování ve Wpt.
		// Přitom ovšem může docházet ke vniku alel, genů a přidávání genů do druhů.
		// Tento efekt je důležitý, dělat to líně by bylo divné.
		wpt.computeGenotypIfNotExistsForAllRing(genom);
		if (wpt.hasEmptyCoords()) {
			// On se sice nepřidá do bagu, takže přímo nebudezobrazen, ale přesto je v ringu kešoidových waypointů.
			// a za určité situace se na něj dostaneme, nejspíš bude vidět v seznamu keší.
			// TODO Udělat ak, aby nebyl ani v ringu od začátku.
			wpt.removeMeFromRing();
			return;
		}

		final Genotyp genotyp = wpt.getGenotyp();

		final Mou mou = wpt.getMou();
		indexator = indexator.add(mou.xx, mou.yy, wpt);
		final Kesoid kesoid = wpt.getKesoid();
		kesoidyset.add(kesoid);
		wpts.add(wpt);
		if (kesoid instanceof Kes) {
			final Kes kes = (Kes) kesoid;
			maximalniBestOf = Math.max(maximalniBestOf, kes.getBestOf());
			maximalniHodnoceni = Math.max(maximalniHodnoceni, kes.getHodnoceni());
			maximalniFavorit = Math.max(maximalniFavorit, kes.getFavorit());
		}
		genotyp.countTo(citacAlel);
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
		indexatorOdevzdan = true;
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
