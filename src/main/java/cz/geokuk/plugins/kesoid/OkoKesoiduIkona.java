package cz.geokuk.plugins.kesoid;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;

/** Oko na tlačítku „Zobrazovat kešoidy (F5)“: otevřené, když se kešoidy zobrazují, přeškrtnuté červenou čarou, když jsou skryté. Barva je z písma tlačítka, takže sedí v každém tématu. */
public class OkoKesoiduIkona implements Icon {

	private static final int VELIKOST = 20;

	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		final boolean zobrazuji = !(c instanceof AbstractButton) || ((AbstractButton) c).isSelected();
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.translate(x, y);
			final Color barva = c.isEnabled() ? c.getForeground() : Color.GRAY;
			g2.setColor(barva);
			g2.setStroke(new BasicStroke(1.6f));
			g2.draw(new Arc2D.Double(1, 3, VELIKOST - 2, 14, 25, 130, Arc2D.OPEN));
			g2.draw(new Arc2D.Double(1, 3, VELIKOST - 2, 14, 205, 130, Arc2D.OPEN));
			g2.fill(new Ellipse2D.Double(VELIKOST / 2.0 - 3, VELIKOST / 2.0 - 3, 6, 6));
			if (!zobrazuji) {
				g2.setColor(new Color(200, 30, 30));
				g2.setStroke(new BasicStroke(2.2f));
				g2.drawLine(3, VELIKOST - 3, VELIKOST - 3, 3);
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
