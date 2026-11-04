package cz.geokuk.util.gui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Popisek přes mapu: bílý text na tmavém poloprůhledném podkladu s světlým obrysem, čitelný na světlé i tmavé mapě. */
public final class PopisekNaMape {

	private static final int VODOROVNE = 7;
	private static final int SVISLE = 2;
	private static final int PODKLAD = 190;
	private static final int OBRYS = 110;

	private PopisekNaMape() {}

	/** Obdélník popisku se zadaným pravým okrajem a svislým středem, i když se nekreslí. */
	public static Rectangle hranice(final FontMetrics fm, final String text, final int pravyOkraj, final int stredY) {
		final int w = fm.stringWidth(text) + 2 * VODOROVNE;
		final int h = fm.getHeight() + 2 * SVISLE;
		return new Rectangle(pravyOkraj - w, stredY - h / 2, w, h);
	}

	/** Nakreslí popisek s viditelností 0 až 1; vrací jeho obdélník, nebo null, když není vidět. */
	public static Rectangle nakresli(final Graphics2D g, final Font font, final String text, final int pravyOkraj, final int stredY, final float viditelnost) {
		if (viditelnost <= 0f) {
			return null;
		}
		final Graphics2D gg = (Graphics2D) g.create();
		try {
			gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			gg.setFont(font);
			final FontMetrics fm = gg.getFontMetrics();
			final Rectangle r = hranice(fm, text, pravyOkraj, stredY);
			final RoundRectangle2D.Float tvar = new RoundRectangle2D.Float(r.x, r.y, r.width - 1, r.height - 1, r.height, r.height);
			gg.setColor(new Color(20, 20, 20, Math.round(PODKLAD * viditelnost)));
			gg.fill(tvar);
			gg.setColor(new Color(255, 255, 255, Math.round(OBRYS * viditelnost)));
			gg.draw(tvar);
			gg.setColor(new Color(255, 255, 255, Math.round(255 * viditelnost)));
			gg.drawString(text, r.x + VODOROVNE, r.y + SVISLE + fm.getAscent());
			return r;
		} finally {
			gg.dispose();
		}
	}

	/** Značka osy: krátká vodorovná čárka. */
	public static void znacka(final Graphics2D g, final int x1, final int x2, final int y, final float viditelnost) {
		if (viditelnost <= 0f) {
			return;
		}
		final Graphics2D gg = (Graphics2D) g.create();
		try {
			gg.setColor(new Color(20, 20, 20, Math.round(PODKLAD * viditelnost)));
			gg.setStroke(new BasicStroke(3f));
			gg.drawLine(x1, y, x2, y);
			gg.setColor(new Color(255, 255, 255, Math.round(255 * viditelnost)));
			gg.setStroke(new BasicStroke(1f));
			gg.drawLine(x1, y, x2, y);
		} finally {
			gg.dispose();
		}
	}
}
