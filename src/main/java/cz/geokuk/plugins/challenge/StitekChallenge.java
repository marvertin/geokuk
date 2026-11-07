package cz.geokuk.plugins.challenge;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.UIManager;

/** Oranžový štítek „Plánovač: jen kandidáti“ ve stavovém řádku; barva textu se přizpůsobí pozadí tématu, aby byl čitelný. */
public class StitekChallenge extends JLabel {

	private static final long serialVersionUID = 1L;
	private static final Color ORANZOVA = new Color(200, 100, 0);
	private static final double NEJMENSI_KONTRAST = 4.5;

	public StitekChallenge(final String text) {
		super(text);
		updateUI();
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setForeground(barva(UIManager.getColor("Panel.background")));
	}

	static double jas(final Color c) {
		final int[] v = { c.getRed(), c.getGreen(), c.getBlue() };
		final double[] k = new double[3];
		for (int i = 0; i < 3; i++) {
			final double x = v[i] / 255.0;
			k[i] = x <= 0.03928 ? x / 12.92 : Math.pow((x + 0.055) / 1.055, 2.4);
		}
		return 0.2126 * k[0] + 0.7152 * k[1] + 0.0722 * k[2];
	}

	static double kontrast(final Color a, final Color b) {
		final double x = jas(a);
		final double y = jas(b);
		return (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05);
	}

	private static Color smichej(final Color z, final Color na, final double t) {
		return new Color((int) Math.round(z.getRed() + (na.getRed() - z.getRed()) * t), (int) Math.round(z.getGreen() + (na.getGreen() - z.getGreen()) * t),
				(int) Math.round(z.getBlue() + (na.getBlue() - z.getBlue()) * t));
	}

	/** Oranžová, u které je text na daném pozadí čitelný: na světlém se ztmaví, na tmavém zesvětlí, dokud kontrast nedosáhne 4,5 : 1. */
	static Color barva(final Color pozadi) {
		if (pozadi == null || kontrast(ORANZOVA, pozadi) >= NEJMENSI_KONTRAST) {
			return ORANZOVA;
		}
		final Color cil = jas(pozadi) > 0.18 ? Color.BLACK : Color.WHITE;
		Color c = ORANZOVA;
		for (int i = 1; i <= 20 && kontrast(c, pozadi) < NEJMENSI_KONTRAST; i++) {
			c = smichej(ORANZOVA, cil, i / 20.0);
		}
		return c;
	}
}
