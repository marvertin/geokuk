package cz.geokuk.core.program;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.geokuk.framework.FKurzory;
import lombok.extern.slf4j.Slf4j;

/** Při startu vedle EDT načte písma a třídy Swingu, které by si okno jinak načítalo na EDT samo. */
@Slf4j
final class PredehrevaniStartu {

	private static final String[] TRIDY = { "javax.swing.JFrame", "javax.swing.JMenuBar", "javax.swing.JMenu", "javax.swing.JMenuItem", "javax.swing.JPopupMenu", "javax.swing.JToolBar",
			"javax.swing.JButton", "javax.swing.JToggleButton", "javax.swing.JCheckBox", "javax.swing.JLabel", "javax.swing.JPanel", "javax.swing.JScrollPane", "javax.swing.JTable",
			"javax.swing.JTextField", "javax.swing.JSlider", "javax.swing.JComboBox", "javax.swing.JSpinner", "javax.swing.JTabbedPane", "javax.swing.JLayeredPane",
			"javax.swing.OverlayLayout", "javax.swing.text.html.HTMLEditorKit", "javax.swing.text.html.StyleSheet", "javax.swing.border.EmptyBorder" };

	/** Znaky textů v okně: každý znak nového písma si Windows poprvé vykreslí zvlášť, a to na EDT při prvním měření textu. */
	static final String ZNAKY = " 0123456789%.,:;-+/()[]°×–…„“\"'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
			+ "ÁČĎÉĚÍŇÓŘŠŤÚŮÝŽáčďéěíňóřšťúůýž";

	private PredehrevaniStartu() {
	}

	static void spust() {
		final Thread vlakno = new Thread(PredehrevaniStartu::predehrej, "Předehřátí startu");
		vlakno.setDaemon(true);
		vlakno.start();
	}

	/**
	 * Kontexty, se kterými Swing měří text: bez vyhlazování a s hinty plochy (třeba ClearType). Každá dvojice písma a kontextu má ve Windows vlastní sadu glyfů.
	 */
	static List<FontRenderContext> kontextyMereni(final Object hintyPlochy) {
		final List<FontRenderContext> kontexty = new ArrayList<>();
		kontexty.add(new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, RenderingHints.VALUE_FRACTIONALMETRICS_OFF));
		if (hintyPlochy instanceof Map) {
			final Map<?, ?> hinty = (Map<?, ?>) hintyPlochy;
			final Object vyhlazeni = hinty.get(RenderingHints.KEY_TEXT_ANTIALIASING);
			if (vyhlazeni != null) {
				final Object zlomkove = hinty.get(RenderingHints.KEY_FRACTIONALMETRICS);
				kontexty.add(new FontRenderContext(null, vyhlazeni, zlomkove != null ? zlomkove : RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT));
			}
		}
		return kontexty;
	}

	static void predehrej() {
		try {
			cz.geokuk.plugins.mapy.kachle.gui.LogoNacitani.predehrej();
			Toolkit.getDefaultToolkit();
			final FontRenderContext frc = new FontRenderContext(null, true, true);
			final Graphics2D g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
			for (final String rodina : new String[] { Font.DIALOG, Font.SANS_SERIF, Font.MONOSPACED }) {
				for (final int styl : new int[] { Font.PLAIN, Font.BOLD }) {
					final Font font = new Font(rodina, styl, 12);
					font.getLineMetrics("Ážq0", frc);
					g.getFontMetrics(font).stringWidth(Font.DIALOG.equals(rodina) ? ZNAKY : "Ážq0");
				}
			}
			g.dispose();
			for (final FontRenderContext kontext : kontextyMereni(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"))) {
				for (final int styl : new int[] { Font.PLAIN, Font.BOLD }) {
					new Font(Font.DIALOG, styl, 12).getStringBounds(ZNAKY, kontext);
				}
			}
			for (final String trida : TRIDY) {
				Class.forName(trida);
			}
			// Kurzory se kreslí při prvním použití třídy.
			Class.forName(FKurzory.class.getName());
		} catch (final Exception | LinkageError e) {
			log.debug("Předehřátí startu selhalo: {}", e.toString());
		}
	}
}
