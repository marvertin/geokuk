package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp.Otisk;
import cz.geokuk.util.pocitadla.*;

public class Sklo implements ImagantCache {

	private static Pocitadlo pocitImangantuZasah = new PocitadloRoste("Imagant - zásahy cache", "");
	private static Pocitadlo pocitSourceImaguZasah = new PocitadloRoste("Zdrojové obrázky - zásah cache", "");
	private final Pocitadlo pocitImangantu = new PocitadloMalo("Imagant - počet", "Kolik vlastně máme typů konkrétních vzhledů ikon");
	private final Pocitadlo pocitSourceImagu = new PocitadloMalo("Zdrojové obrázky - počet", "Kolik vlastně máme typů konkrétních vzhledů ikon");

	List<Vrstva> vrstvy = new ArrayList<>();

	private final Map<Genotyp.Otisk, Imagant> cache = new HashMap<>();
	private final Map<URL, BufferedImage> sourceImageCache = Collections.synchronizedMap(new HashMap<URL, BufferedImage>());

	private final String iName;

	/**
	 *
	 */
	public Sklo(final String name) {
		iName = name;
	}

	@Override
	public BufferedImage getImage(final URL url) {
		try {
			BufferedImage bi = sourceImageCache.get(url);
			if (bi == null) {
				bi = ImageIO.read(url);
				sourceImageCache.put(url, bi);
				pocitSourceImagu.set(sourceImageCache.size());
			} else {
				pocitSourceImaguZasah.inc();
			}
			return bi;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

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
	public synchronized Imagant getRenderedImage(final Genotyp genotyp) {
		final Otisk otisk = genotyp.getOtisk();
		Imagant imagant = cache.get(otisk);
		if (!cache.containsKey(otisk)) { // může tam být totiž null
			imagant = render(genotyp);
			cache.put(otisk, imagant);
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
	Imagant render(final Genotyp genotyp) {
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

}
