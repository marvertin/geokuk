package cz.geokuk.plugins.mapy.kachle.gui;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * Slabé logo GeoKuk uprostřed výřezu, které je vidět jen tam, kde mapa ještě nedorazila. Zmenšené logo se drží v paměti, takže překreslení je jedno vykreslení hotového obrázku.
 */
@Slf4j
public final class LogoNacitani {

	private static final String ZDROJ = "/img/geokuk-logo.png";
	/** Podíl kratší strany výřezu, který logo zabere. */
	private static final double PODIL_STRANY = 0.4;
	private static final int NEJVETSI_LOGICKA_VELIKOST = 500;
	/** Krytí loga na světlé ploše; logo je plné, proto nízké, aby působilo jemně. */
	static final float KRYTI_SVETLE = 0.13f;
	/** Krytí loga na tmavé ploše; zelená na tmavé šedé má nízký kontrast, proto vyšší než na světlé. */
	static final float KRYTI_TMAVE = 0.32f;

	private static BufferedImage zdroj;
	private static boolean zdrojNacten;

	private BufferedImage zmensene;
	private int velikostZmenseneho;

	static float kryti(final boolean tmave) {
		return tmave ? KRYTI_TMAVE : KRYTI_SVETLE;
	}

	/** Kolik pixelů zařízení má mít logo pro daný výřez; 0, když se nemá kreslit. */
	static int velikost(final int sirka, final int vyska, final double meritko) {
		final double logicka = Math.min(NEJVETSI_LOGICKA_VELIKOST, PODIL_STRANY * Math.min(sirka, vyska));
		return logicka < 48 ? 0 : (int) Math.round(logicka * meritko);
	}

	/** Nakreslí logo doprostřed {@code 0,0,sirka,vyska}, ale jen do oblasti {@code kamSmi}; null znamená všude. */
	void kresli(final Graphics2D g, final int sirka, final int vyska, final Area kamSmi, final boolean tmave) {
		final double sx = g.getTransform().getScaleX();
		final int velikost = velikost(sirka, vyska, sx);
		if (velikost == 0) {
			return;
		}
		final BufferedImage logo = zmensene(velikost);
		if (logo == null) {
			return;
		}
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			if (kamSmi != null) {
				g2.clip(kamSmi);
			}
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, kryti(tmave)));
			// Zmenšené logo má pixely zařízení, proto se kreslí bez měřítka displeje.
			final double sy = g2.getTransform().getScaleY();
			g2.scale(1 / sx, 1 / sy);
			g2.drawImage(logo, (int) Math.round((sirka * sx - velikost) / 2), (int) Math.round((vyska * sy - velikost) / 2), null);
		} finally {
			g2.dispose();
		}
	}

	private BufferedImage zmensene(final int velikost) {
		if (zmensene != null && velikostZmenseneho == velikost) {
			return zmensene;
		}
		final BufferedImage z = zdroj();
		if (z == null) {
			return null;
		}
		BufferedImage obr = z;
		int w = obr.getWidth();
		while (w / 2 >= velikost) {
			w /= 2;
			obr = zmenseni(obr, w);
		}
		if (w != velikost) {
			obr = zmenseni(obr, velikost);
		}
		zmensene = obr;
		velikostZmenseneho = velikost;
		return obr;
	}

	private static BufferedImage zmenseni(final BufferedImage obr, final int velikost) {
		final BufferedImage v = new BufferedImage(velikost, velikost, BufferedImage.TYPE_INT_ARGB_PRE);
		final Graphics2D g = v.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(obr, 0, 0, velikost, velikost, null);
		} finally {
			g.dispose();
		}
		return v;
	}

	/** Logo se dekóduje předem vedle EDT, ať první vykreslení mapy nečeká. */
	public static void predehrej() {
		zdroj();
	}

	private static synchronized BufferedImage zdroj() {
		if (!zdrojNacten) {
			zdrojNacten = true;
			try (InputStream is = LogoNacitani.class.getResourceAsStream(ZDROJ)) {
				if (is != null) {
					zdroj = ImageIO.read(is);
				}
			} catch (final IOException | RuntimeException e) {
				log.warn("Logo pro načítání mapy se nepodařilo načíst", e);
			}
		}
		return zdroj;
	}
}
