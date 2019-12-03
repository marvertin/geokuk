package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Deque;

import cz.geokuk.api.mapicon.Drawer0;
import cz.geokuk.api.mapicon.Imagant;

public class DefaultVykreslovac extends Drawer0 {

	private final ImageProvider imageProvider;

	public DefaultVykreslovac(final ImageProvider imageProvider) {
		this.imageProvider = imageProvider;
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
		final BufferedImage image = imageProvider.getImage(url);
		assert image != null;
		final Imagant imagant = new Imagant(image, getXoffset(), getYoffset());

		aImaganti.addFirst(imagant);

	}

}
