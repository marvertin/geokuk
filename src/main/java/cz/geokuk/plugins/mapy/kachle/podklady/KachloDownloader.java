package cz.geokuk.plugins.mapy.kachle.podklady;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.EnumMap;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloRoste;

public class KachloDownloader {

	/**
	 * Nepřejmenovávat hodnoty, odvozuje se z něj název resourcu
	 */
	public static enum EPraznyObrazek {
		OFFLINE, ERROR;

		/**
		 * @return
		 */
		public String getRecourceName() {
			return name() + ".png";
		}
	}

	private static final Logger log = LogManager.getLogger(KachloDownloader.class.getSimpleName());

	private final Pocitadlo pocitDownloadleDlazdice = new PocitadloRoste("Downloadlé dlaždice", "Počet dlaždic, které byly downloadovány.");

	private final EnumMap<EPraznyObrazek, Image> prazdneObrazky = new EnumMap<>(EPraznyObrazek.class);

	public KachloDownloader() {
	}

	public ImageWithData downloadImage(final URL url) throws IOException {
		log.debug("Loading kachle from URL: \"{}\"", url);
		// if (Math.random() > 0.5 && url.toString().contains("hybrid")) {
		// throw new IOException("Nasimulovaná chyba hybrid");
		// }

		final DataHoldingInputStream dhis = new DataHoldingInputStream(url.openStream());
		final Image img;
		try (InputStream stm = new BufferedInputStream(dhis)) {
			img = ImageIO.read(stm);
			if (img == null) {
				throw new RuntimeException("image is null");
			}
		}
		final ImageWithData imda = new ImageWithData(img, dhis.getData());
		pocitDownloadleDlazdice.inc();
		log.debug("Loaded {} bytes", imda.getData().length);
//		if (Math.random() > 0.9) {
//			throw new RuntimeException("Nepovedlo se");
//		}

		return imda;

	}

	/**
	 *
	 */
	public synchronized Image fejkovyObrazek(final EPraznyObrazek typPrazdnehoObrazku) {
		// TODO Offline image by měl být průhledný a rozhodně s lepší grafikou.

		Image image = prazdneObrazky.get(typPrazdnehoObrazku);
		InputStream istm = null;
		if (image == null) {
			try {
				istm = getClass().getResourceAsStream(typPrazdnehoObrazku.getRecourceName());
				if (istm != null) {
					image = ImageIO.read(istm);
				}
			} catch (final IOException e) {
				log.error("Nelze nacist prazdny obrazek! " + typPrazdnehoObrazku.getRecourceName(), e);
			} finally {
				if (istm != null) {
					try {
						istm.close();
					} catch (final IOException e) {
						log.error("Nelze zavrit stream pro " + typPrazdnehoObrazku.getRecourceName(), e);
					}
				}
			}
			if (image == null) {
				// K tomu pravděpodobně nedojde, ale co když ,tak vyplníme nesmyslem
				image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB_PRE);
				final Graphics2D g = (Graphics2D) image.getGraphics();
				g.setColor(new Color(128, 128, 128, 128));
				g.fillOval(30, 30, 196, 196);
			}

			prazdneObrazky.put(typPrazdnehoObrazku, image);
		}
		return image;
	}
}
