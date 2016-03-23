package cz.geokuk.img;

import java.awt.*;

import javax.swing.Icon;

/**
 * The "missing icon" is a white box with a black border and a red x. It's used to display something when there are issues loading an icon from an external location.
 *
 * @author Collin Fagan
 */
public class MissingIcon implements Icon {

	private final int			width	= 32;
	private final int			height	= 32;

	private final BasicStroke	stroke	= new BasicStroke(4);

	@Override
	public int getIconHeight() {
		return height;
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		final Graphics2D g2d = (Graphics2D) g.create();

		g2d.setColor(Color.WHITE);
		g2d.fillRect(x + 1, y + 1, width - 2, height - 2);

		g2d.setColor(Color.BLACK);
		g2d.drawRect(x + 1, y + 1, width - 2, height - 2);

		g2d.setColor(Color.RED);

		g2d.setStroke(stroke);
		g2d.drawLine(x + 10, y + 10, x + width - 10, y + height - 10);
		g2d.drawLine(x + 10, y + height - 10, x + width - 10, y + 10);

		g2d.dispose();
	}
}
