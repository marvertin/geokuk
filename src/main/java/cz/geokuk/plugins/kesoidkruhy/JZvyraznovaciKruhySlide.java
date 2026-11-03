package cz.geokuk.plugins.kesoidkruhy;

import java.awt.*;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.LimityKresleni;
import cz.geokuk.plugins.kesoid.ViditelnostSKesoidy;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.plugins.kesoid.mvc.LimityKresleniEvent;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.util.index2d.Indexator;

public class JZvyraznovaciKruhySlide extends JSingleSlide0 {
	private static final long serialVersionUID = -5858146658366237217L;
	private volatile int limitIkon = LimityKresleni.VYCHOZI_IKON;
	private static final int MINIMALNI_JEDNOTKOVY_KRUH = 25;

	private Indexator<Wpt> iIndexator;

	private EKaType podklad;
	private KruhySettings kruhy;
	private final ViditelnostSKesoidy viditelnost = new ViditelnostSKesoidy(this);

	public JZvyraznovaciKruhySlide() {
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
		return new JZvyraznovaciKruhySlide();
	}

	public void onEvent(final LimityKresleniEvent event) {
		limitIkon = event.getLimity().getIkon();
		repaint();
	}

	public void onEvent(final KeskyVyfiltrovanyEvent event) {
		iIndexator = event.getFiltrovane().getIndexator();
		repaint();
	}

	public void onEvent(final KesoidOnoffEvent event) {
		viditelnost.setKesoidy(event.isOnoff());
	}

	public void onEvent(final KruhyPreferencesChangeEvent event) {
		viditelnost.setVlastni(event.kruhy.isOnoff());
		kruhy = event.kruhy;
		repaint();
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		podklad = event.getKatype();
		provazModely();
	}

	@Override
	public void paintComponent(final Graphics aG) {
		if (iIndexator == null) {
			return;
		}
		final int r;
		final boolean jednotkove = kruhy.isJednotkovaVelikost();
		if (jednotkove) {
			r = vypoctiJednotkovyPolomer();
		} else {
			r = kruhy.getVelikost();
		}
		final int pocet = iIndexator.count(getSoord().getBoundingRect());
		if (pocet > limitIkon || prilisHuste(pocet, Math.PI * r * r)) {
			return;
		}

		final Graphics2D g = (Graphics2D) aG;
		final int d = 2 * r;
		final Color barva = kruhy.getBarva();
		final Stroke prerus = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 5.0f, 5.0f }, 0);
		g.setColor(barva);
		iIndexator.bound(oblastKresleni(g, r + 1)).stream().forEach(wpt -> {
			final Mou mou = wpt.getMou();
			final Point p = getSoord().transform(mou);
			g.setStroke(prerus);
			g.setColor(barva);
			g.fillOval(p.x - r, p.y - r, d, d);
			if (jednotkove) {
				g.setColor(Color.WHITE);
				g.drawOval(p.x - r, p.y - r, d, d);

			}
		});

	}

	/**
	 *
	 */
	private void initComponents() {
		provazModely();
	}

	private void provazModely() {
		if (podklad != null) {
			// barvaAVElikostKruhuModel = Settings.barvyAVelikostiKruhu.get(podklad);
		}
	}

	private int vypoctiJednotkovyPolomer() {
		final double pixluNaMetr = getSoord().getPixluNaMetr();
		int metru = 1;
		int pixlu;
		do {
			metru = metru * 10;
			pixlu = (int) (metru * pixluNaMetr);
		} while (pixlu < MINIMALNI_JEDNOTKOVY_KRUH);
		return pixlu;
	}

}
