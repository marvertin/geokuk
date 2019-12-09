package cz.geokuk.plugins.kesoid.mapicon;

import java.util.*;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.plugins.kesoid.genetika.Jedinec;
import cz.geokuk.util.pocitadla.*;

public class Sklo {

	private static Pocitadlo pocitImangantuZasah = new PocitadloRoste("Imagant - zásahy cache", "");
	private final Pocitadlo pocitImangantu = new PocitadloMalo("Imagant - počet", "Kolik vlastně máme typů konkrétních vzhledů ikon");

	List<Vrstva> vrstvy = new ArrayList<>();

	private final Map<Jedinec, Imagant> cache = new HashMap<>();

	private final String iName;

	// TODO zjistit proč je keš definována ve skle. Cožpak je ji nutno uvolňovat?
	private final ImageProvider imageProvider = new ImageProviderCached();

	/**
	 *
	 */
	public Sklo(final String name) {
		iName = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return iName;
	}

	/**
	 * Vrací sklivec pro daný genotyp s tím, že se hrabe v keši, aby se jednak zvýšila rychnlost, druhak, aby se šetřila paměť
	 *
	 * @param genotyp
	 * @return
	 */
	public synchronized Imagant getRenderedImage(final Jedinec genotyp) {
		Imagant imagant = cache.get(genotyp);
		if (!cache.containsKey(genotyp)) { // může tam být totiž null
			imagant = render(genotyp);
			cache.put(genotyp, imagant);
			pocitImangantu.set(cache.size());
		} else {
			pocitImangantuZasah.inc();
		}
		return imagant;
	}

	/**
	 * Vyrendruje ikonu pro sklo pro daný genotyp.
	 *
	 * @param genotyp
	 * @return
	 */
	Imagant render(final Jedinec genotyp) {
		// Vyrendrovat jednotlivé vrstvy samostatně
		final Deque<Imagant> imaganti = new ArrayDeque<>();
		for (final Vrstva vrstva : vrstvy) {
			final IconDef iconDef = vrstva.locate(genotyp);
			if (iconDef != null) {
				render(iconDef.idp, imaganti);
			}
		}

		final List<Imagant> list = new ArrayList<>();
		for (final Iterator<Imagant> it = imaganti.descendingIterator(); it.hasNext();) {
			final Imagant ima = it.next();
			list.add(ima);
		}

		return Imagant.prekresliNaSebe(list);

	}

	/**
	 * @param aIdp
	 * @param aImaganti
	 */
	private void render(final IkonDrawingProperties idp, final Deque<Imagant> aImaganti) {

		idp.vykreslovac.draw(aImaganti);
	}

	public ImageProvider getImageProvider() {
		return imageProvider;
	}

}
