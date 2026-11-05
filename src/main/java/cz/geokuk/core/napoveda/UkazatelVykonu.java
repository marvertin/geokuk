package cz.geokuk.core.napoveda;

import java.awt.*;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.Timer;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.MyPreferences;

/** Štítek s výkonem kreslení vlevo nahoře na mapě, je-li vidět pruh plánovače, pod ním; jen v beta kanálu a na přání. */
public final class UkazatelVykonu {

	static final String KLIC = "ukazatelVykonu";
	/** Štítek je jediný na celý program (jedno okno s mapou), proto i tento stav je společný; nastavuje ho pruh plánovače podle toho, jestli je vidět. */
	private static volatile boolean podPruhem;
	private static final Color POZADI = new Color(0, 0, 0, 150);

	private static volatile boolean zapnuty;
	private static JComponent mapa;
	private static Timer obnova;

	private UkazatelVykonu() {}

	/** Kde štítek leží; jeho překreslení se do doby kreslení mapy nepočítá. */
	public static Rectangle oblast() {
		return cz.geokuk.framework.RozmisteniPrekryvu.ukazatelVykonu(podPruhem);
	}

	/** Posune štítek pod pruh plánovače, nebo zpět nahoru. */
	public static void setPodPruhem(final boolean pod) {
		if (podPruhem == pod) {
			return;
		}
		final Rectangle stara = oblast();
		podPruhem = pod;
		if (mapa != null) {
			mapa.repaint(stara);
			mapa.repaint(oblast());
		}
	}

	static boolean ulozeno() {
		return MyPreferences.current().node(FPref.VSEOBECNE_node).getBoolean(KLIC, false);
	}

	/** Mapa, na které se štítek kreslí; zapne ho, když je zapnutý beta kanál i volba. */
	public static void sleduj(final JComponent naMape) {
		mapa = naMape;
		obnova = new Timer(500, e -> mapa.repaint(oblast()));
		nastav(Diagnostika.betaKanal() && ulozeno());
	}

	static void zapni(final boolean zapnout) {
		MyPreferences.current().node(FPref.VSEOBECNE_node).putBoolean(KLIC, zapnout);
		nastav(zapnout);
	}

	private static void nastav(final boolean zapnout) {
		zapnuty = zapnout;
		if (obnova != null) {
			if (zapnout) {
				obnova.start();
			} else {
				obnova.stop();
			}
			mapa.repaint(oblast());
		}
	}

	static String text() {
		final Vykon.Souhrn mapaMs = Vykon.souhrn(Vykon.Velicina.PREKRESLENI);
		final Vykon.Souhrn edt = Vykon.souhrn(Vykon.Velicina.EDT);
		return String.format(Locale.ROOT, "mapa %.0f ms · EDT max %.0f ms · %.0f dl/s", mapaMs.median, edt.max, Vykon.dlazdicZaSekundu());
	}

	public static void kresli(final Graphics g0) {
		if (!zapnuty) {
			return;
		}
		final Graphics2D g = (Graphics2D) g0.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setColor(POZADI);
			final Rectangle oblast = oblast();
			g.fillRoundRect(oblast.x, oblast.y, oblast.width, oblast.height, 6, 6);
			g.setColor(Color.WHITE);
			g.setFont(g.getFont().deriveFont(Font.PLAIN, 12f));
			g.drawString(text(), oblast.x + 6, oblast.y + oblast.height - 6);
		} finally {
			g.dispose();
		}
	}
}
