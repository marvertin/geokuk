package cz.geokuk.core.render;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

public class JIkonkaPapiru extends JComponent {

	private static final long	serialVersionUID	= 8533258621624761103L;
	private PapirovaMetrika		metrika;

	public JIkonkaPapiru() {
		final Dimension dim = new Dimension(80, 80);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		setBorder(BorderFactory.createEtchedBorder());
	}

	@Override
	protected void paintComponent(final Graphics aG) {
		if (metrika == null) {
			return;
			// System.out.println();
			// System.out.printf("METRIKA- sirka:%.0f vyska:%.0f \n",
			// metrika.xsize *1000, metrika.ysize *1000);
		}

		final Graphics2D g = (Graphics2D) aG.create();
		final double kvoc = getWidth() / metrika.getDelsiStrana() * 0.8;
		g.translate(getWidth() / 2, getHeight() / 2);
		final double delsi = metrika.getDelsiStrana() * kvoc;
		final double kratsi = metrika.getKratsiStrana() * kvoc;
		if (metrika.naSirku) {
			g.setColor(Color.WHITE);
			g.fillRect((int) (-delsi / 2), (int) -kratsi / 2, (int) delsi, (int) kratsi);
			g.setColor(Color.BLACK);
			g.drawRect((int) (-delsi / 2), (int) -kratsi / 2, (int) delsi, (int) kratsi);
		}
		if (metrika.naVysku) {
			g.setColor(Color.WHITE);
			g.fillRect((int) (-kratsi / 2), (int) -delsi / 2, (int) kratsi, (int) delsi);
			g.setColor(Color.BLACK);
			g.drawRect((int) (-kratsi / 2), (int) -delsi / 2, (int) kratsi, (int) delsi);
		}
		g.setColor(Color.DARK_GRAY);
		g.setFont(getFont().deriveFont(Font.BOLD, 2));
		g.setFont(Font.decode("Arial-BOLD-18"));
		final String formatStr = "A" + metrika.format;
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int charsWidth = fontMetrics.stringWidth(formatStr);
		g.drawString(formatStr, -charsWidth / 2, fontMetrics.getHeight() / 2 - fontMetrics.getDescent());

		g.setColor(Color.MAGENTA);
		final double sirka = metrika.xsize * kvoc;
		final double vyska = metrika.ysize * kvoc;
		g.drawRect((int) (-sirka / 2), (int) -vyska / 2, (int) sirka, (int) vyska);
	}

	public void setMetrikia(final PapirovaMetrika metrika) {
		this.metrika = metrika;
		repaint();
	}
}
