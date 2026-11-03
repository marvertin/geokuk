package cz.geokuk.util.gui;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/** Popisek přes mapu: bílý text na tmavém poloprůhledném podkladu, čitelný na světlé i tmavé mapě. Značka je krátká čárka vlevo pro popisek osy. */
public class JPopisekNaMape extends JLabel {

	private static final long serialVersionUID = 1L;

	private static final int DELKA_ZNACKY = 6;

	private final boolean znacka;

	public JPopisekNaMape(final String text, final boolean znacka) {
		super(text);
		this.znacka = znacka;
		setOpaque(false);
		setForeground(Color.WHITE);
		setFont(getFont().deriveFont(Font.PLAIN, 11f));
		setBorder(BorderFactory.createEmptyBorder(1, 6 + (znacka ? DELKA_ZNACKY + 2 : 0), 1, 6));
	}

	public boolean maZnacku() {
		return znacka;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(20, 20, 20, 170));
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
			if (znacka) {
				g2.setColor(new Color(255, 255, 255, 220));
				final int y = getHeight() / 2;
				g2.drawLine(5, y, 5 + DELKA_ZNACKY, y);
			}
		} finally {
			g2.dispose();
		}
		super.paintComponent(g);
	}
}
