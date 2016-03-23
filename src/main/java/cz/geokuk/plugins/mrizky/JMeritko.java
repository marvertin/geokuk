/**
 *
 */
package cz.geokuk.plugins.mrizky;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JPanel;

/**
 * @author veverka
 *
 */
public class JMeritko extends JPanel {

	private static final int	MINIMALNI_SIRKA_DILKU	= 20;
	private static final int	ODSTUP_POPISKU_OD_CARKY	= 3;
	// private static final int MINIMALNI_SIRKA_DILKU = 50;

	private final static int	tloustka				= 6;
	private final static int	vyskaCarky				= 6;

	private static final long	serialVersionUID		= -4801191981059574701L;
	private double				pixluNaMetr				= 1;
	private double				metruNaDilek;
	private int					pixluNaDilek;
	private int					pocetDilku;
	private int					sirka;
	private int					vyska;
	private double				maximalniSirkaMeritka	= 400;
	private Font				font;
	private FontMetrics			fontMetrics;

	public JMeritko() {
		// setPreferredSize(new Dimension(1600, 40));
		spocitejMetriky();
		// setBorder(BorderFactory.createLoweredBevelBorder());
	}

	@Override
	public Dimension getPreferredSize() {
		final Insets insets = getInsets();
		return new Dimension(sirka + insets.left + insets.right, vyska + insets.top + insets.bottom);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g = g.create();
		final Insets insets = getInsets();
		g.translate(insets.left, insets.top);
		if (pixluNaMetr <= 0 || Double.isNaN(pixluNaMetr)) {
			return;
		}
		g.setFont(font);

		final int offset = (getWidth() - sirka) / 2;
		final int pocatekY = fontMetrics.getHeight() + vyskaCarky + ODSTUP_POPISKU_OD_CARKY;
		for (int i = 0; i < pocetDilku; i++) {
			final double metruOdZacatku = i * metruNaDilek;
			final int pixluOdZacatku = offset + (int) (metruOdZacatku * pixluNaMetr);
			g.setColor(i % 2 == 0 ? Color.BLACK : Color.WHITE);
			g.fillRect(pixluOdZacatku, pocatekY, pixluNaDilek, tloustka);
			g.setColor(Color.BLACK);
			g.drawRect(pixluOdZacatku, pocatekY, pixluNaDilek, tloustka);
			g.setColor(Color.WHITE);
			g.drawLine(pixluOdZacatku, pocatekY - 1, pixluOdZacatku + pixluNaDilek, pocatekY - 1);
			g.setColor(Color.BLACK);
			g.fillRect(pixluOdZacatku, pocatekY - vyskaCarky, 2, vyskaCarky + tloustka);
			g.setColor(Color.WHITE);
			g.drawLine(pixluOdZacatku + 2, pocatekY - 1, pixluOdZacatku + 2, pocatekY - vyskaCarky);

			g.setColor(i % 2 == 0 ? Color.WHITE : Color.BLACK);
			// teď písmenka
			g.setColor(Color.BLACK);

			g.drawString(popisek(metruOdZacatku), pixluOdZacatku, pocatekY - vyskaCarky - ODSTUP_POPISKU_OD_CARKY);
		}
		final double metruOdZacatku = pocetDilku * metruNaDilek;
		final int pixluOdZacatku = offset + (int) (metruOdZacatku * getPixluNaMetr());
		g.drawString(jednotka(metruNaDilek), pixluOdZacatku, pocatekY - vyskaCarky - ODSTUP_POPISKU_OD_CARKY);
	}

	private void spocitejMetriky() {
		metruNaDilek = 1;
		pixluNaDilek = 0;
		while (pixluNaDilek < MINIMALNI_SIRKA_DILKU) {
			metruNaDilek *= 10;
			pixluNaDilek = (int) (pixluNaMetr * metruNaDilek);
		}
		pocetDilku = (int) Math.min(8.0, getMaximalniSirkaMeritka() / pixluNaDilek);

		final Map<TextAttribute, Object> map = new Hashtable<>();
		// map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
		map.put(TextAttribute.BACKGROUND, Color.WHITE);
		map.put(TextAttribute.SWAP_COLORS, TextAttribute.SWAP_COLORS_ON);
		map.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
		font = Font.decode("ARIAL-BOLD-12").deriveFont(map);
		fontMetrics = getFontMetrics(font);
		final int naJednotkuNaKonci = fontMetrics.stringWidth(jednotka(metruNaDilek));

		sirka = pixluNaDilek * pocetDilku + naJednotkuNaKonci;
		vyska = vyskaCarky + tloustka + ODSTUP_POPISKU_OD_CARKY + fontMetrics.getHeight();
	}

	private String popisek(double d) {
		if (d >= 1000) {
			d = d / 1000;
		}
		return Math.round(d) + "";
	}

	private String jednotka(final double d) {
		if (d >= 1000) {
			return "km";
		} else {
			return "m";
		}
	}

	public void setPixluNaMetr(final double pixluNaMetr) {
		if (pixluNaMetr == this.pixluNaMetr) {
			return;
		}
		this.pixluNaMetr = pixluNaMetr;
		spocitejMetriky();
		revalidate();
		repaint();
	}

	public void setMaximalniSirkaMeritka(final double maximalniSirkaMeritka) {
		if (this.maximalniSirkaMeritka == maximalniSirkaMeritka) {
			return;
		}
		this.maximalniSirkaMeritka = maximalniSirkaMeritka;
		spocitejMetriky();
		revalidate();
		repaint();
	}

	public double getPixluNaMetr() {
		return pixluNaMetr;
	}

	public double getMaximalniSirkaMeritka() {
		return maximalniSirkaMeritka;
	}

}
