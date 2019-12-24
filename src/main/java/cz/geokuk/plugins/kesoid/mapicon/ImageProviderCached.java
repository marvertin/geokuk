package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;

import cz.geokuk.util.pocitadla.*;

public class ImageProviderCached implements ImageProvider {

	private static Pocitadlo pocitSourceImaguZasah = new PocitadloRoste("Zdrojové obrázky - zásah cache", "");
	private final Pocitadlo pocitSourceImagu = new PocitadloMalo("Zdrojové obrázky - počet", "Kolik vlastně máme typů konkrétních vzhledů ikon");

	private final Map<URL, BufferedImage> sourceImageCache = Collections.synchronizedMap(new HashMap<URL, BufferedImage>());

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
