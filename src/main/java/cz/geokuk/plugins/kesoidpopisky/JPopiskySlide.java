package cz.geokuk.plugins.kesoidpopisky;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;

import cz.geokuk.core.coord.EJakOtacetPriRendrovani;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.LimityKresleni;
import cz.geokuk.plugins.kesoid.ViditelnostSKesoidy;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.LimityKresleniEvent;
import cz.geokuk.util.index2d.Indexator;

public class JPopiskySlide extends JSingleSlide0 {

	private static final long serialVersionUID = -5858146658366237217L;
	private volatile int limitIkon = LimityKresleni.VYCHOZI_IKON;
	/** Nejširší popisek, se kterým se při kreslení po částech počítá, v pixelech. */
	private static final int OKRAJ_POPISKU = 600;

	private Indexator<Wpt> iIndexator;

	private PopiskySettings pose;

	private PopiskyModel popiskyModel;

	private final ViditelnostSKesoidy viditelnost = new ViditelnostSKesoidy(this);

	public JPopiskySlide() {
		setOpaque(false);
		setCursor(null);
		initComponents();
	}

	@Override
	protected boolean jeNadMapouKNacitani() {
		return true;
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

	public void onEvent(final LimityKresleniEvent event) {
		limitIkon = event.getLimity().getIkon();
		repaint();
	}

	public void onEvent(final KeskyVyfiltrovanyEvent event) {
		iIndexator = event.getFiltrovane().getIndexator();
		repaint();
	}

	/** Popisky jsou vidět, jen když jsou zapnuté kešoidy i vlastní vypínač popisků; vypínač popisků se zobrazením kešoidů nemění. */
	public void onEvent(final PopiskyOnoffEvent event) {
		viditelnost.setVlastni(event.isOnoff());
		repaint();
	}

	public void onEvent(final KesoidOnoffEvent event) {
		viditelnost.setKesoidy(event.isOnoff());
		repaint();
	}

	public void onEvent(final PopiskyPreferencesChangeEvent event) {
		pose = event.pose;
		repaint();
	}

	private final SirkaTextuCache sirkyTextu = new SirkaTextuCache();

	@Override
	public void paintComponent(final Graphics aG) {
		if (iIndexator == null) {
			return;
		}
		if (pose == null) {
			return;
		}
		final Graphics2D g = (Graphics2D) aG;

		g.setFont(pose.font);
		final Color barvaTextu = pose.foreground;
		final Color barvaPodkladu = pose.background;
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int height2 = fontMetrics.getHeight();
		final int pocet = iIndexator.count(getSoord().getBoundingRect());
		// Odhad popisku: dva řádky po zhruba patnácti znacích.
		if (pocet > limitIkon || prilisHuste(pocet, 2.0 * height2 * 8 * height2)) {
			return;
		}
		final int posuny = fontMetrics.getDescent() - height2;
		final Map<Kepodr, SestavovacPopisku> sestavmapa = new HashMap<>();
		// Popisek je vpravo dole od bodu a jeho šířku předem neznáme, proto velkorysý okraj.
		final int okraj = OKRAJ_POPISKU + Math.abs(pose.posuX) + Math.abs(pose.posuY);
		iIndexator.bound(oblastKresleni(g, okraj)).stream().forEach(wpt -> {
			if (!wpt.isMainWpt()) {
				return;
			}
			final Mou mou = wpt.getMou();
			final Point p = getSoord().transform(mou);
			p.x -= 10;
			p.y += 25;
			g.setColor(barvaPodkladu);
			sestavmapa.computeIfAbsent(wpt.getKepodr(), kepodr ->
			new SestavovacPopisku(popiskyModel.getData().getPatterns().asMap().get(kepodr),
					wpt
					.getKesoidPlugin()
					.getPopiskyDef(kepodr)
					.getNahrazovace())
					);
			final String[] popisky = sestavmapa.get(wpt.getKepodr()).sestavPopisek(wpt);
			int stringWidth = 0;
			for (final String popisek1 : popisky) {
				stringWidth = Math.max(sirkyTextu.sirka(fontMetrics, popisek1), stringWidth);
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
