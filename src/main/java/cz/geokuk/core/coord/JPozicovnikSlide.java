package cz.geokuk.core.coord;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.*;

import cz.geokuk.core.coordinates.*;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.MouseGestureContext;
import cz.geokuk.plugins.cesty.akce.PridatDoCestyAction;
import cz.geokuk.plugins.geocoding.GeocodingBorowserXmlAction;
import cz.geokuk.util.lang.FUtil;

public class JPozicovnikSlide extends JSingleSlide0 {

	/**
	 *
	 */
	private static final int R_KRIZE = 50;

	/**
	 *
	 */
	private static final int R_VNEJSI_KRUZNICE = 30;

	/**
	 *
	 */
	private static final int R_VNITRNI_KRUZNICE = 20;

	private static final long serialVersionUID = -5858146658366237217L;

	private Poziceq poziceq = new Poziceq();

	private boolean mysJePoblizKrize;

	private PoziceModel poziceModel;

	private VyrezModel vyrezModel;

	private Factory factory;

	private Point pointcur;

	public JPozicovnikSlide() {
		setOpaque(false);
		setCursor(null);
		// Board.eveman.registerWeakly(this);
	}

	@Override
	public void addPopouItems(final JPopupMenu popupMenu, final MouseGestureContext ctx) {
		if (mysJePoblizKrize) {
			final Wgs wgs = poziceq.getWgs();
			// add(new ZoomKesAction(kesoid));
			if (wgs != null) {
				final JMenuItem item = new JMenuItem(factory.init(new CenterPoziceAction()));
				// item.setText("Centruj");
				// TODO Dát ikonu středování
				// item.setIcon(null);

				add(item);
			}

			popupMenu.add(new JMenuItem(factory.init(new ZoomPoziceAction(wgs))));
			popupMenu.add(new JMenuItem(factory.init(new OdstranKrizAction())));
			popupMenu.add(new JMenuItem(factory.init(new PridatDoCestyAction(poziceq.getPoziceMouable()))));
			popupMenu.add(new JMenuItem(factory.init(new GeocodingBorowserXmlAction(wgs))));
			popupMenu.add(new JMenuItem(factory.init(new SouradniceDoClipboarduAction(wgs))));
			popupMenu.add(new JMenuItem(factory.init(new OpenStreetViewAction(wgs))));
		}
		chain().addPopouItems(popupMenu, ctx);
	}

	/**
	 * Vrátí pozici nmyši, pokud je v blízkosti kříže
	 *
	 * @return
	 */
	@Override
	public Mouable getUpravenaMys() {
		if (mysJePoblizKrize) {
			return poziceq.getPoziceMou();
		} else {
			return chain().getUpravenaMys();
		}
	}

	@Override
	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	/**
	 * Invoked when the mouse has been clicked on a component.
	 */
	@Override
	public void mouseClicked(final MouseEvent e, final MouseGestureContext ctx) {
		// System.out.println("UDALOST " + e);
		// kesky.mouseClicked(e);
		// if (e.isConsumed()) return;
		if (SwingUtilities.isRightMouseButton(e)) {
			// Board.eveman.fire(new PoziceChangedEvent(new Pozice(), false));
			poziceModel.clearPozice();
		} else {
			if ((e.getModifiers() & Event.CTRL_MASK) == 0) { // jen když není stisknut control
				final boolean vystredovat = e.getClickCount() >= 2;
				poziceModel.setPozice(getSoord().transform(e.getPoint()).toWgs());
				if (vystredovat) {
					vyrezModel.vystredovatNaPozici();
				}
			}
		}
		chain().mouseClicked(e, ctx);
	}

	public void onEvent(final PoziceChangedEvent aEvent) {
		if (FUtil.equalsa(poziceq, aEvent.poziceq)) {
			return;
		}
		poziceq = aEvent.poziceq;
		prepocitatBlizkostKrize();
		repaint();
	}

	public void onEvent(final ZmenaSouradnicMysiEvent aEvent) {
		pointcur = aEvent.pointcur;
		prepocitatBlizkostKrize();
	}

	@Override
	public void paintComponent(final Graphics aG) {

		if (poziceq.isNoPosition()) {
			return; // není co kreslit
		}
		final Mou mou = poziceq.getPoziceMou();
		final Point p = getSoord().transform(mou);
		final int ra = R_VNITRNI_KRUZNICE;
		final int rb = R_VNEJSI_KRUZNICE;
		final int rk = R_KRIZE;

		final Graphics2D g = (Graphics2D) aG;
		g.setStroke(new BasicStroke(mysJePoblizKrize ? 3 : 1));
		g.setColor(Color.RED);
		g.drawLine(p.x - rk, p.y, p.x + rk, p.y);
		g.drawLine(p.x, p.y - rk, p.x, p.y + rk);
		g.drawOval(p.x - ra, p.y - ra, 2 * ra, 2 * ra);
		g.drawOval(p.x - rb, p.y - rb, 2 * rb, 2 * rb);
	}

	private void prepocitatBlizkostKrize() {
		if (!poziceq.isNoPosition() && pointcur != null) {
			final double dalka = getSoord().pixleDalka(getSoord().transform(pointcur), poziceq.getWgs().toMou());
			final boolean pobliz = dalka < 20;
			if (pobliz != mysJePoblizKrize) {
				mysJePoblizKrize = pobliz;
				repaintKriz();
			}
		}
	}

	/**
	 *
	 */
	private void repaintKriz() {
		if (poziceq.isNoPosition()) {
			return; // není co kreslit
		}
		final Mou mou = poziceq.getPoziceMou();
		final Point p = getSoord().transform(mou);
		repaint(p.x - R_KRIZE, p.y - R_KRIZE, R_KRIZE * 2, R_KRIZE * 2);
	}

}
