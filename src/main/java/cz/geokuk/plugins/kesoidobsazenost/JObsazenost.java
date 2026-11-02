package cz.geokuk.plugins.kesoidobsazenost;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.MouseGestureContext;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.util.gui.JBarvovyDvojSlider;
import cz.geokuk.util.index2d.Indexator;

public class JObsazenost extends JSingleSlide0 implements AfterEventReceiverRegistrationInit {

	// g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
	/**
	 * @author Martin Veverka
	 *
	 */
	private final class ChangeListenerImplementation implements ChangeListener {
		@Override
		public void stateChanged(final ChangeEvent aArg0) {
			final ObsazenostSettings data = obsazenostModel.getData();
			data.setColor(iSlidovnik.getColor());
			obsazenostModel.setData(data);
		}
	}

	private static final int POLOMER_OBSAZENOSTI = 161;
	/** Menší kroužky splývají s ikonami keší a obsazenost z nich není poznat. */
	private static final int MIN_POLOMER = 3;
	/** Víc kruhů by překreslení mapy zpomalilo pod plynulost. */
	static final int MAX_KRUHU = 20_000;

	private static final long serialVersionUID = -5858146658366237217L;

	private ObsazenostSettings obsazenost;

	private Indexator<Wpt> iIndexator;

	private JBarvovyDvojSlider iSlidovnik;

	private ObsazenostModel obsazenostModel;

	/** Maska kruhů, znovu použitá mezi kresleními. */
	private BufferedImage maska;

	public JObsazenost() {
		setOpaque(false);
		setCursor(null);
		initComponents();
	}

	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JObsazenost();
	}

	@Override
	public void initAfterEventReceiverRegistration() {
		registerEvents();
	}

	public void inject(final ObsazenostModel obsazenostModel) {
		this.obsazenostModel = obsazenostModel;
	}

	@Override
	public void mouseDragged(final MouseEvent e, final MouseGestureContext ctx) {
		// ctx.setMouseCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		super.mouseDragged(e, ctx);
	}

	public void onEvent(final KeskyNactenyEvent event) {
		iIndexator = event.getVsechny().getIndexator();
		repaint();
	}

	public void onEvent(final ObsazenostOnoffEvent event) {
		setVisible(event.isOnoff());
		repaint();
	}

	public void onEvent(final ObsazenostPreferencesChangeEvent event) {
		obsazenost = event.obsazenost;
		iSlidovnik.setColor(obsazenost.getColor());
		repaint();
	}

	@Override
	public void paintComponent(final Graphics aG) {
		if (iIndexator == null) {
			return;
		}
		final Graphics2D g = (Graphics2D) aG;
		final int r = polomerObsazenosti();
		final int d = 2 * r;
		if (r < MIN_POLOMER) {
			return;
		}
		if (prekrocenLimit(iIndexator.count(getSoord().getBoundingRect()))) {
			return;
		}
		final Rectangle clip = g.getClipBounds();
		final Rectangle oblast = clip != null ? clip : new Rectangle(getSoord().getDim());
		if (oblast.isEmpty()) {
			return;
		}
		// Kruhy se kreslí do masky plnou barvou a maska se položí jednou s průhledností, takže překryvy netmavnou.
		// Maska leží na celých pixelech zařízení, aby kresba po výřezech seděla i při zlomkovém zvětšení obrazovky.
		final AffineTransform transformace = g.getTransform();
		final Rectangle pixely = transformace.createTransformedShape(oblast).getBounds();
		if (pixely.isEmpty()) {
			return;
		}
		final int w = pixely.width;
		final int h = pixely.height;
		final BufferedImage m = maska(w, h);
		final Graphics2D gm = m.createGraphics();
		try {
			gm.setComposite(AlphaComposite.Clear);
			gm.fillRect(0, 0, w, h);
			gm.setComposite(AlphaComposite.SrcOver);
			gm.translate(-pixely.x, -pixely.y);
			gm.transform(transformace);
			final Color barva = obsazenost.getColor();
			gm.setColor(new Color(barva.getRGB() & 0xFFFFFF));
			iIndexator.bound(oblastKresleni(g, r + 1)).stream().forEach(wpt -> {
				if (!wpt.obsazujeOblast()) {
					return;
				}
				final Point p = getSoord().transform(wpt.getMou());
				gm.fillOval(p.x - r, p.y - r, d, d);
			});
		} finally {
			gm.dispose();
		}
		final Composite puvodni = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, obsazenost.getColor().getAlpha() / 255f));
		g.setTransform(new AffineTransform());
		g.drawImage(m, pixely.x, pixely.y, pixely.x + w, pixely.y + h, 0, 0, w, h, null);
		g.setTransform(transformace);
		g.setComposite(puvodni);
	}

	/** Kruhy se nekreslí, když je keší ve výřezu víc než {@link #MAX_KRUHU}. */
	boolean prekrocenLimit(final int pocet) {
		return pocet > MAX_KRUHU;
	}

	private BufferedImage maska(final int w, final int h) {
		if (maska == null || maska.getWidth() < w || maska.getHeight() < h) {
			maska = new BufferedImage(Math.max(w, maska == null ? 0 : maska.getWidth()), Math.max(h, maska == null ? 0 : maska.getHeight()), BufferedImage.TYPE_INT_ARGB);
		}
		return maska;
	}

	/**
	 *
	 */
	private void initComponents() {
		setLayout(new BorderLayout());
		iSlidovnik = new JBarvovyDvojSlider();
		add(iSlidovnik, BorderLayout.EAST);
		iSlidovnik.getBarvovnik().setToolTipText("Nastavení stupně šedi kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
		iSlidovnik.getPruhlednik().setToolTipText("Nastavení průhlednosti kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
	}

	/**
	 * @return
	 */
	private int polomerObsazenosti() {
		return (int) (getSoord().getPixluNaMetr() * POLOMER_OBSAZENOSTI);
	}

	/**
	 *
	 */
	private void registerEvents() {
		iSlidovnik.addChangeListener(new ChangeListenerImplementation());
	}
}
