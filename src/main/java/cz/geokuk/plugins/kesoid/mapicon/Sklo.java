package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp.Otisk;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.pocitadla.*;

public class Sklo implements ImagantCache {

	private final Pocitadlo						pocitImangantu			= new PocitadloMalo("Imagant - počet", "Kolik vlastně máme typů konkrétních vzhledů ikon");
	private static Pocitadlo					pocitImangantuZasah		= new PocitadloRoste("Imagant - zásahy cache", "");
	private final Pocitadlo						pocitSourceImagu		= new PocitadloMalo("Zdrojové obrázky - počet", "Kolik vlastně máme typů konkrétních vzhledů ikon");
	private static Pocitadlo					pocitSourceImaguZasah	= new PocitadloRoste("Zdrojové obrázky - zásah cache", "");

	List<Vrstva>								vrstvy					= new ArrayList<>();

	private final Map<Genotyp.Otisk, Imagant>	cache					= new HashMap<>();
	private final Map<URL, BufferedImage>		sourceImageCache		= Collections.synchronizedMap(new HashMap<URL, BufferedImage>());

	private final String						iName;

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

		return prekresliNaSebe(list);

	}

	public static Imagant prekresliNaSebe(final List<Imagant> imaganti) {
		if (imaganti.isEmpty()) {
			return null;
		}
		final BoundingRect br = Sklo.sjednoceni(imaganti);

		if (br == null) {
			// TODO log.warn()
			return null;
		}

		// vytvořit cílový brázek ve správné velikosti
		final int width = br.xx2 - br.xx1;
		final int height = br.yy2 - br.yy1;
		Imagant imagant;
		final BufferedImage resultbi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics g = resultbi.getGraphics();
		try {
			g.translate(-br.xx1, -br.yy1); // nastavit správný střed

			// vykreslení obrázků
			for (final Imagant ima : imaganti) {
				if (ima != null) {
					g.drawImage(ima.getImage(), ima.getXpos(), ima.getYpos(), null);
				}
			}

			// a vyvorit výsledek
			imagant = new Imagant(resultbi);
			imagant.setXoffset(br.xx1 - (-width / 2));
			imagant.setYoffset(br.yy1 - (-height / 2));
		} finally {
			g.dispose();
		}
		return imagant;
	}

	/**
	 * @param aIdp
	 * @param aImaganti
	 */
	private void render(final IkonDrawingProperties idp, final Deque<Imagant> aImaganti) {

		idp.vykreslovac.draw(aImaganti);
	}

	public static BoundingRect sjednoceni(final List<Imagant> imaganti) {
		// Spočítat hranice
		int xx1 = 0, xx2 = 0, yy1 = 0, yy2 = 0;
		for (final Imagant imagant : imaganti) {
			if (imagant == null) {
				continue;
			}
			final int x1 = imagant.getXpos();
			final int y1 = imagant.getYpos();
			final int x2 = x1 + imagant.getImage().getWidth();
			final int y2 = y1 + imagant.getImage().getHeight();
			xx1 = Math.min(xx1, x1);
			xx2 = Math.max(xx2, x2);
			yy1 = Math.min(yy1, y1);
			yy2 = Math.max(yy2, y2);
		}
		if (xx2 - xx1 == 0 || yy2 - yy1 == 0) {
			return null;
		}
		final BoundingRect br = new BoundingRect(xx1, yy1, xx2, yy2);
		return br;
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

}
