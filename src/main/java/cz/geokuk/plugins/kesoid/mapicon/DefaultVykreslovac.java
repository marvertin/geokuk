package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Deque;

import cz.geokuk.api.mapicon.Drawer0;
import cz.geokuk.api.mapicon.Imagant;

public class DefaultVykreslovac extends Drawer0 {

	private final ImagantCache imagantCache;

	public DefaultVykreslovac(final ImagantCache imagantCache) {
		this.imagantCache = imagantCache;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see geokuk.mapicon.Vykreslovac#draw(geokuk.mapicon.VykreslovaciKontext, java.util.List)
	 */
	@Override
	public void draw(final Deque<Imagant> aImaganti) {
		/**
		 * Načte obrázek, mělo by to jít, když je voláno.
		 *
		 * @return
		 */
		final URL url = getUrl();
		final BufferedImage image = imagantCache.getImage(url);
		assert image != null;
		final Imagant imagant = new Imagant(image, getXoffset(), getYoffset());

		aImaganti.addFirst(imagant);

	}

}
