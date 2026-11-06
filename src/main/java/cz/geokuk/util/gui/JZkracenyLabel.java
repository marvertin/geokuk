package cz.geokuk.util.gui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

/**
 * Popisek s jedním řádkem textu, který se zkrátí „…“ podle skutečné šířky; plný text je v tooltipu, když se nevešel. Požadovaná šířka je nejvýš {@code nejvetsiSirka}, takže dlouhý
 * text nezvětší okno ani popup.
 */
public class JZkracenyLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	private final int nejvetsiSirka;
	private String plnyText = "";

	public JZkracenyLabel(final int nejvetsiSirka) {
		this.nejvetsiSirka = nejvetsiSirka;
		setToolTipText("");
	}

	/** Plný text; zkrácení se určí při kreslení podle šířky. */
	public void setPlnyText(final String text) {
		plnyText = text == null ? "" : text;
		super.setText(plnyText);
	}

	public String getPlnyText() {
		return plnyText;
	}

	private int dostupnaSirka() {
		final Insets in = getInsets();
		return getWidth() - in.left - in.right;
	}

	/** Text, který se při dané šířce zobrazí. */
	String zobrazeny() {
		return ZkraceniTextu.zkrat(plnyText, getFontMetrics(getFont()), dostupnaSirka());
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension d = super.getPreferredSize();
		d.width = Math.min(d.width, nejvetsiSirka);
		return d;
	}

	@Override
	public Dimension getMinimumSize() {
		final Dimension d = super.getPreferredSize();
		final FontMetrics fm = getFontMetrics(getFont());
		final Insets in = getInsets();
		d.width = Math.min(d.width, fm.stringWidth("M…") + in.left + in.right);
		return d;
	}

	@Override
	public String getToolTipText(final MouseEvent event) {
		return getWidth() > 0 && !zobrazeny().equals(plnyText) ? plnyText : null;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setFont(getFont());
			g2.setColor(isEnabled() ? getForeground() : java.awt.Color.GRAY);
			final FontMetrics fm = g2.getFontMetrics();
			final Insets in = getInsets();
			final int y = in.top + (getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent();
			g2.drawString(zobrazeny(), in.left, y);
		} finally {
			g2.dispose();
		}
	}
}
