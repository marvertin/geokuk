package cz.geokuk.plugins.mrizky;

import java.awt.*;

import javax.swing.BorderFactory;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.BeanSubtype;

public class JMeritkoSlide extends JSingleSlide0 implements AfterInjectInit {

	private static final long serialVersionUID = -5858146658366237217L;
	private JMeritko meritko;

	public JMeritkoSlide() {
		setOpaque(false);
		setCursor(null);
		initComponents();
	}

	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JMeritkoSlide();
	}

	@Override
	public void initAfterInject() {
		factory.init(meritko);
		// for (Component comp : getComponents()) {
		// factory.init(comp);
		// comp.setMaximumSize(new Dimension(50, 50));
		// }
	}

	@BeanSubtype("Meritkovnik")
	public void onEvent(final MrizkaEvent event) {
		setVisible(event.onoff);
	}

	@Override
	public void render(final Graphics gg) throws InterruptedException {
		// paint(g);
		final Graphics2D g = (Graphics2D) gg.create();
		// g.translate(0, 500);
		// meritko.setMaximalniSirkaMeritka(getWidth() * 3 / 4);
		// meritko.setPixluNaMetr(getSoord().getPixluNaMetr());
		final Coord soord = getSoord();
		meritko.setMaximalniSirkaMeritka(soord.getWidth() * 3 / 4);
		meritko.setPixluNaMetr(soord.getPixluNaMetr());
		meritko.setSize(meritko.getPreferredSize());
		g.translate((soord.getWidth() - meritko.getSize().width) / 2, soord.getHeight() - meritko.getSize().getHeight() - 20);
		meritko.paintComponent(g);

	}

	@Override
	protected void onVyrezChanged() {
		meritko.setMaximalniSirkaMeritka(getSoord().getWidth() * 3 / 4);
		meritko.setPixluNaMetr(getSoord().getPixluNaMetr());
	}

	@SuppressWarnings("unused")
	private void drawLine(final Graphics g, final int xx1, final int yy1, final int xx2, final int yy2) {
		final Point p1 = getSoord().transform(new Mou(xx1, yy1));
		final Point p2 = getSoord().transform(new Mou(xx2, yy2));
		g.drawLine(p1.x, p1.y, p2.x, p2.y);

	}

	/**
	 *
	 */
	private void initComponents() {
		setLayout(new BorderLayout());
		meritko = new JMeritko();
		meritko.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
		add(meritko, BorderLayout.SOUTH);
		// //add(new JMeritko(), BorderLayout.SOUTH);
		// add(new JMeritko(), BorderLayout.WEST);
		// add(new JMeritko(), BorderLayout.EAST);
		// add(new JMeritko(), BorderLayout.NORTH);
		// add(new JMeritko());

	}

}
