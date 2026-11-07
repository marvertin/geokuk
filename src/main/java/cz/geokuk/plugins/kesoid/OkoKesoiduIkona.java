package cz.geokuk.plugins.kesoid;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;

/**
 * Oko GeoKuku (oko s řasami z ikony programu) na tlačítku „Zobrazovat kešoidy (F5)“. Barva je z písma tlačítka, takže sedí v každém tématu; skryté kešoidy: oko zesvětlené
 * a přeškrtnuté červenou čarou.
 */
public class OkoKesoiduIkona implements Icon {

	private static final int VELIKOST = 16;
	/** Obrys oka a řas ve čtvercovém prostoru 256 × 256, jako v ikona-24.svg v grafika/zdroje. */
	private static final String OKO = "M24.11 153.55C24.56 152.59 25.16 151.69 25.91 150.88C51.82 121.7 88.97 105 128 105C167.03 105 204.18 121.7 230.09 150.88C230.84 151.69 231.44 152.59 231.89 153.55C232.61 155.04 232.96 156.67 232.96 158.28C232.96 159.9 232.61 161.52 231.89 163.02C231.44 163.97 230.84 164.88 230.09 165.69C204.18 194.86 167.03 211.56 128 211.56C88.97 211.56 51.82 194.86 25.91 165.69C25.16 164.88 24.56 163.97 24.11 163.02C23.39 161.52 23.04 159.9 23.04 158.28C23.04 156.67 23.39 155.04 24.11 153.55ZM128 189.56C157.38 189.56 185.49 178.28 206.66 158.28C185.49 138.28 157.38 127 128 127C98.62 127 70.51 138.28 49.34 158.28C70.51 178.28 98.62 189.56 128 189.56Z";
	private static final String RASY = "M45.76 103.64L33.05 78.69C30.38 73.44 32.46 67.02 37.71 64.34C42.96 61.67 49.39 63.76 52.06 69L64.77 93.95C67.45 99.2 65.36 105.62 60.11 108.3C54.86 110.97 48.44 108.89 45.76 103.64ZM191.23 93.95L203.94 69C206.61 63.76 213.04 61.67 218.29 64.34C223.54 67.02 225.62 73.44 222.95 78.69L210.24 103.64C207.56 108.89 201.14 110.97 195.89 108.3C190.64 105.62 188.55 99.2 191.23 93.95ZM117.33 81.33L117.33 53.33C117.33 47.44 122.11 42.67 128 42.67C133.89 42.67 138.67 47.44 138.67 53.33L138.67 81.33C138.67 87.22 133.89 92 128 92C122.11 92 117.33 87.22 117.33 81.33Z";
	/** Viditelná část kresby v prostoru 256 × 256. */
	private static final double OD_X = 20;
	private static final double OD_Y = 38;
	private static final double SIRKA = 216;

	private static final Path2D OKO_TVAR = cesta(OKO);
	private static final Path2D RASY_TVAR = cesta(RASY);

	/** Cesta z podmnožiny SVG (M, L, C, Z, absolutní souřadnice), která stačí pro ikonu programu. */
	static Path2D cesta(final String d) {
		final Path2D.Double p = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		final String[] t = d.replaceAll("([MLCZ])", " $1 ").trim().split("\\s+");
		int i = 0;
		while (i < t.length) {
			switch (t[i++]) {
			case "M":
				p.moveTo(Double.parseDouble(t[i++]), Double.parseDouble(t[i++]));
				break;
			case "L":
				p.lineTo(Double.parseDouble(t[i++]), Double.parseDouble(t[i++]));
				break;
			case "C":
				p.curveTo(Double.parseDouble(t[i++]), Double.parseDouble(t[i++]), Double.parseDouble(t[i++]), Double.parseDouble(t[i++]), Double.parseDouble(t[i++]), Double.parseDouble(t[i++]));
				break;
			case "Z":
				p.closePath();
				break;
			default:
				throw new IllegalArgumentException(d);
			}
		}
		return p;
	}

	/** Barva písma je světlá (tmavé téma), takže červená čára musí být světlejší. */
	static boolean jeSvetle(final Color c) {
		return 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue() > 150;
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		final boolean zobrazuji = !(c instanceof AbstractButton) || ((AbstractButton) c).isSelected();
		final boolean povoleno = c.isEnabled();
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g2.translate(x, y);
			final double meritko = VELIKOST / SIRKA;
			g2.translate(0, (VELIKOST - 170 * meritko) / 2);
			g2.scale(meritko, meritko);
			g2.translate(-OD_X, -OD_Y);
			final Color barva = povoleno ? c.getForeground() : Color.GRAY;
			final java.awt.geom.Area oko = new java.awt.geom.Area(RASY_TVAR);
			oko.add(new java.awt.geom.Area(OKO_TVAR));
			oko.add(new java.awt.geom.Area(new Ellipse2D.Double(128 - 21, 158.28 - 21, 42, 42)));
			final java.awt.geom.Line2D.Double cara = new java.awt.geom.Line2D.Double(48, 205, 208, 62);
			if (!zobrazuji) {
				oko.subtract(new java.awt.geom.Area(new BasicStroke(46f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND).createStrokedShape(cara)));
				g2.setComposite(AlphaComposite.SrcOver.derive(0.45f));
			}
			g2.setColor(barva);
			g2.fill(oko);
			if (!zobrazuji) {
				g2.setComposite(AlphaComposite.SrcOver);
				g2.setColor(jeSvetle(c.getForeground()) ? new Color(255, 110, 110) : new Color(200, 30, 30));
				g2.setStroke(new BasicStroke(26f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2.draw(cara);
			}
		} finally {
			g2.dispose();
		}
	}

	@Override
	public int getIconWidth() {
		return VELIKOST;
	}

	@Override
	public int getIconHeight() {
		return VELIKOST;
	}
}
