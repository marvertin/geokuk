package cz.geokuk.plugins.kesoid.kind.kes;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cz.geokuk.img.ImageLoader;

public class JFavorit extends JComponent {

	private static final long serialVersionUID = 8991499244324360406L;
	private static final Font sFont = new Font("SansSerif", Font.BOLD, 18);
	private int kolik = Integer.MIN_VALUE;

	public static void main(final String[] args) {
		final JFrame frm = new JFrame();
		final JPanel p = new JPanel();
		final JFavorit jFavorit = new JFavorit(-1);
		p.add(jFavorit);
		frm.add(p);
		frm.pack();
		frm.setVisible(true);

		jFavorit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		jFavorit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent mouseevent) {
				System.out.println("Prasteno do mysi " + jFavorit.kolik);
			}
		});

		new Timer(1000, new ActionListener() {
			int kolik = 0;

			@Override
			public void actionPerformed(final ActionEvent actionevent) {
				kolik++;
				jFavorit.setKolik(kolik);
			}
		}).start();
	}

	public JFavorit(final int kolik) {
		setKolik(kolik);
	}

	public void setKolik(final int kolik) {
		if (kolik == this.kolik) {
			return;
		}
		this.kolik = kolik;
		final String ss = String.valueOf(kolik);
		final int stringWidth = super.getFontMetrics(sFont).stringWidth(ss);
		final Dimension newPrefferedSize = new Dimension(33 + stringWidth, 30);
		setPreferredSize(newPrefferedSize);
		revalidate();
		repaint();
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		final int w = getWidth();
		final int h = getHeight();
		final int zaobleni = 20;

		// fill Ellipse2D.Double
		final GradientPaint redtowhite = new GradientPaint(w / 2, 0, new Color(242, 222, 158), w / 2, h, new Color(251, 251, 243));
		g2.setPaint(redtowhite);
		g2.fillRoundRect(0, 0, w, h, zaobleni, zaobleni);

		// g2.setStroke(new BasicStroke(2.0f));
		g2.setColor(Color.decode("#E9A24C"));
		g2.drawRoundRect(0, 0, w - 1, h - 1, zaobleni, zaobleni);

		g2.drawImage(ImageLoader.seekResImage("icon_fav.png"), 8, 7, null);

		final String ss = String.valueOf(kolik);
		g2.setFont(sFont);
		g2.setColor(Color.BLACK);
		g2.drawString(ss, 25, 22);
	}
}
