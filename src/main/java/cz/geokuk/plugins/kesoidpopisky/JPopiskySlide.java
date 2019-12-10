package cz.geokuk.plugins.kesoidpopisky;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Box;

import cz.geokuk.core.coord.EJakOtacetPriRendrovani;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.program.FConst;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.util.index2d.FlatVisitor;
import cz.geokuk.util.index2d.Indexator;

public class JPopiskySlide extends JSingleSlide0 {

	private static final long serialVersionUID = -5858146658366237217L;

	private Indexator<Wpt> iIndexator;

	private PopiskySettings pose;

	private PopiskyModel popiskyModel;

	public JPopiskySlide() {
		setOpaque(false);
		setCursor(null);
		initComponents();
	}

	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JPopiskySlide();
	}

	public void inject(final PopiskyModel popiskyModel) {
		this.popiskyModel = popiskyModel;
	}

	@Override
	public EJakOtacetPriRendrovani jakOtacetProRendrovani() {
		return EJakOtacetPriRendrovani.COORD;
	}

	public void onEvent(final KeskyVyfiltrovanyEvent event) {
		iIndexator = event.getFiltrovane().getIndexator();
		repaint();
	}

	public void onEvent(final PopiskyOnoffEvent event) {
		setVisible(event.isOnoff());
		repaint();
	}

	public void onEvent(final PopiskyPreferencesChangeEvent event) {
		pose = event.pose;
		repaint();
	}

	@Override
	public void paintComponent(final Graphics aG) {
		if (iIndexator == null) {
			return;
		}
		if (pose == null) {
			return;
		}
		final boolean prekrocenLimit = iIndexator.count(getSoord().getBoundingRect()) > FConst.MAX_POC_WPT_NA_MAPE;
		if (prekrocenLimit) {
			return;
		}

		final Graphics2D g = (Graphics2D) aG;
		// final Color barvaTextu = iSlidovnikText.getColor();
		// final Color barvaPodkladu = iSlidovnikPodklad.getColor();
		// g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

		g.setFont(pose.font);
		final Color barvaTextu = pose.foreground;
		final Color barvaPodkladu = pose.background;
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int height2 = fontMetrics.getHeight();
		final int posuny = fontMetrics.getDescent() - height2;
		final EnumMap<EKesoidKind, SestavovacPopisku> sestavmapa = new EnumMap<>(EKesoidKind.class);
		for (final Map.Entry<EKesoidKind, String> entry : popiskyModel.getData().getPatterns().asMap().entrySet()) {
			sestavmapa.put(entry.getKey(), new SestavovacPopisku(entry.getValue()));
		}

		iIndexator.visit(getSoord().getBoundingRect(), (FlatVisitor<Wpt>) aSheet -> {
			final Wpt wpt = aSheet.get();
			if (!wpt.isMainWpt()) {
				return;
			}
			final Mou mou = new Mou(aSheet.getXx(), aSheet.getYy());
			final Point p = getSoord().transform(mou);
			p.x -= 10;
			p.y += 25;
			g.setColor(barvaPodkladu);
			final String[] popisky = sestavmapa.get(wpt.getKesoid().getKesoidKind()).sestavPopisek(wpt);
			int stringWidth = 0;
			for (final String popisek1 : popisky) {
				stringWidth = Math.max(fontMetrics.stringWidth(popisek1), stringWidth);
			}
			g.fillRect(p.x + pose.posuX, p.y + posuny + pose.posuY, stringWidth, height2 * popisky.length);

			// g.drawString(str, x, y)
			g.setBackground(Color.YELLOW);
			g.setColor(barvaTextu);
			int yOffset = pose.posuY;
			for (final String popisek2 : popisky) {
				g.drawString(popisek2, p.x + pose.posuX, p.y + yOffset);
				yOffset += height2;
			}
			// g.fillOval(p.x -r, p.y - r, d, d);
		});

	}

	private void initComponents() {
		setLayout(new BorderLayout());
		final Box box = Box.createVerticalBox();

		add(box, BorderLayout.WEST);
	}
}
