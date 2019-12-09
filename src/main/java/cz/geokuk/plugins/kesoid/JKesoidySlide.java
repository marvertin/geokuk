package cz.geokuk.plugins.kesoid;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.*;
import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.cesty.CestyModel;
import cz.geokuk.plugins.cesty.akce.OdebratZCestyAction;
import cz.geokuk.plugins.cesty.akce.PridatDoCestyAction;
import cz.geokuk.plugins.kesoid.mapicon.*;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.plugins.vylety.*;
import cz.geokuk.util.index2d.*;
import cz.geokuk.util.pocitadla.PocitadloNula;
import cz.geokuk.util.process.BrowserOpener;

public class JKesoidySlide extends JSingleSlide0 implements AfterEventReceiverRegistrationInit {

	private static class PaintovaciVlakno extends Thread {

		private final WeakReference<JKesoidySlide> wrKesoidy;

		public PaintovaciVlakno(final JKesoidySlide jKesoidy) {
			super("Paintovani");
			final ReferenceQueue<JKesoidySlide> refqueue = new ReferenceQueue<>();
			wrKesoidy = new WeakReference<>(jKesoidy, refqueue);
			new Thread("Zabijec paintovaciho vlakna") {
				@Override
				public void run() {
					try {
						refqueue.remove();
					} catch (final InterruptedException e) { // když přeruší, tak také přeruším
					}
					PaintovaciVlakno.this.interrupt();
				}
			}.start();
		}

		@Override
		public void run() {
			JKesoidySlide jKesoidy = wrKesoidy.get();
			if (jKesoidy == null) {
				return; // slide kešoidů už je pryč, takže ani nemá cenu dál něco řešit
			}
			for (;;) {
				try {
					final BlockingQueue<WptPaintRequest> frontaWaypointu2 = jKesoidy.frontaWaypointu;
					jKesoidy = null; // přičekání na požadavek ve froně nesmím držet slide waypointů, aby mohl být garbage collectorem sebrán
					WptPaintRequest wpr = frontaWaypointu2.poll(10000, TimeUnit.MILLISECONDS); // aby umělo vlákno skončit, musí být timeout
					jKesoidy = wrKesoidy.get();
					if (jKesoidy == null) {
						return; // slide kešoidů už je pryč, takže ani nemá cenu dál něco řešit
					}
					if (wpr == null) {
						continue;
					}
					final MouRect mouRect = new MouRect();
					while (wpr != null) {
						if (wpr.wpt.getSklivec() == null) {
							spocitejSklivece(wpr.wpt, jKesoidy);
						}
						mouRect.add(wpr.mou);
						wpr = jKesoidy.frontaWaypointu.poll();
						pocitVelikostFrontyWaypointu.set(jKesoidy.frontaWaypointu.size());
					}
					vyvolejVykresleni(mouRect, jKesoidy);
				} catch (final InterruptedException e) {
					// System.out.println("Paintovaci vlakno konci diky intrerupci.");
					return;
				}
			}
		}

		/**
		 * Asynchronně volaná metoda provýpočet sklivce
		 *
		 * @param wpt
		 */
		private void spocitejSklivece(final Wpt wpt, final JKesoidySlide jKesoidy) {
			jKesoidy.computeSklivec(wpt);
		}

		private void vyvolejVykresleni(final MouRect aMouRect, final JKesoidySlide jKesoidy) {
			if (aMouRect.isEmpty()) {
				return;
			}
			SwingUtilities.invokeLater(() -> {
				final Insets bigiestIconInsets = jKesoidy.ikonBag.getSada().getBigiestIconInsets();
				final Rectangle rect = jKesoidy.getSoord().transform(aMouRect, bigiestIconInsets);
				jKesoidy.repaint(rect);
			});
		}

		// @Override
		// public void finalize() {
		// System.out.println("Paintovací vlákno fanalizováno");
		// }
	}

	private static class WptPaintRequest {

		private final Wpt wpt;
		private final Mou mou;

		public WptPaintRequest(final Wpt wpt, final Mou mou) {
			this.wpt = wpt;
			this.mou = mou == null ? wpt.getWgs().toMou() : mou;
		}

	}

	// static int POLOMER_KESE = 15; // je to v pixlech
	private static final int POLOMER_CITLIVOSTI = 10;

	private static final PocitadloNula pocitVelikostFrontyWaypointu = new PocitadloNula("Velikost vykreslovací waypointové fronty", "Kolik waypointů čeká na vykreslení.");

	private static final long serialVersionUID = -5858146658366237217L;

	private static final double scale = 1;

	private final BlockingQueue<WptPaintRequest> frontaWaypointu = new LinkedBlockingQueue<>();
	private Indexator<Wpt> indexator;

	private CestyModel cestyModel;

	// private List<Rectangle> repaintovaneCtverce = new ArrayList<Rectangle>();
	// private List<Rectangle> klipovanci = new ArrayList<Rectangle>();

	private Kesoid kesoidPodMysi;

	private Wpt wptPodMysi;

	private final JLabel jakoTooltip = new JLabel();

	private IkonBag ikonBag;

	private Set<Alela> fenotypoveZakazaneAlely;

	private Set<String> iJmenaAlel;

	private Factory factory;

	private PoziceModel poziceModel;

	private KesoidModel kesoidModel;

	private final boolean vykreslovatOkamtiteAleDlouho;

	private KesBag vsechny;

	private VyletModel vyletModel;

	public static double getScale() {
		return scale;
	}

	public JKesoidySlide(final boolean vykreslovatOkamtiteAleDlouho) {
		this.vykreslovatOkamtiteAleDlouho = vykreslovatOkamtiteAleDlouho;
		setLayout(null);
		setOpaque(false);

		jakoTooltip.setVisible(false);
		add(jakoTooltip);

		repaint();

		if (!vykreslovatOkamtiteAleDlouho) {
			new PaintovaciVlakno(this).start();
		}
	}

	@Override
	public void addPopouItems(final JPopupMenu popupMenu, final MouseGestureContext ctx) {
		if (wptPodMysi != null) {
			initPopupMenuItems(popupMenu, wptPodMysi, vsechny);
		}
		chain().addPopouItems(popupMenu, ctx);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.core.coord.JSingleSlide0#createRenderableSlide()
	 */
	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JKesoidySlide(true);
	}

	@Override
	public Cursor getMouseCursor(final boolean pressed) {
		if (wptPodMysi != null && !pressed) {
			final Cursor cursor = FKurzory.NAD_WAYPOINTEM;
			return cursor;
		} else {
			return super.getMouseCursor(pressed);
		}
	}

	/**
	 * Vrátí pozici nmyši, pokud je v blízkosti kříže
	 *
	 * @return
	 */
	@Override
	public Mouable getUpravenaMys() {
		if (wptPodMysi != null) {
			return wptPodMysi;
		} else {
			return chain().getUpravenaMys();
		}
	}

	@Override
	public Wpt getWptPodMysi() {
		return wptPodMysi;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
	 */
	@Override
	public void initAfterEventReceiverRegistration() {
		registerEvents();
	}

	public void inject(final CestyModel cestyModel) {
		this.cestyModel = cestyModel;
	}

	@Override
	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}
	// public void inject(VyrezModel vyrezModel) {
	// this.vyrezModel = vyrezModel;
	// }

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void inject(final VyletModel vyletModel) {
		this.vyletModel = vyletModel;
	}

	@Override
	public EJakOtacetPriRendrovani jakOtacetProRendrovani() {
		return EJakOtacetPriRendrovani.COORD;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(final MouseEvent e, final MouseGestureContext ctx) {
		if (wptPodMysi != null) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				poziceModel.setPozice(wptPodMysi);
			}
			final Kesoid kesoid = wptPodMysi.getKesoid();
			if (e.getClickCount() >= 3 && kesoid.getUrlPrint() != null) {
				BrowserOpener.displayURL(kesoid.getUrlPrint());
			} else if (e.getClickCount() >= 2 && kesoid.getUrlShow() != null) {
				BrowserOpener.displayURL(kesoid.getUrlShow());
			}
			e.consume();
		} else {
			chain().mouseClicked(e, ctx);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(final MouseEvent e, final MouseGestureContext ctx) {
		final Wpt wpt = najdiWptVBlizkosti(new Point(e.getX(), e.getY()));
		wptPodMysi = wpt;
		final Kesoid kes = wpt == null ? null : wpt.getKesoid();

		if (kes != kesoidPodMysi) {
			final Kesoid staraKes = kesoidPodMysi;
			kesoidPodMysi = kes;
			// repaint();
			repaintKes(staraKes);
			repaintKes(kesoidPodMysi);
		}

		if (wptPodMysi != null) {
			// setCursor(cursor);
			jakoTooltip.setText(wptPodMysi.textToolTipu());
			jakoTooltip.setOpaque(true);
			jakoTooltip.setBackground(Color.WHITE);
			jakoTooltip.setSize(jakoTooltip.getPreferredSize());

			final Point p = getSoord().transform(wptPodMysi.getWgs().toMou());
			p.y -= jakoTooltip.getHeight();
			jakoTooltip.setLocation(p);
			jakoTooltip.setVisible(true);
			// System.out.println("TEXTIK ZDE: " + p);
		} else {
			jakoTooltip.setVisible(false);
			// System.out.println("TEXTIK PRYC: ");
		}
		// setToolTipText();

	}

	public void onEvent(final FenotypPreferencesChangedEvent aEvent) {
		iJmenaAlel = aEvent.getJmenaNefenotypovanychAlel();
		repaintIfVse();
	}

	public void onEvent(final IkonyNactenyEvent aEvent) {
		ikonBag = aEvent.getBag();
		repaintIfVse();
	}

	public void onEvent(final KeskyNactenyEvent event) {
		vsechny = event.getVsechny();
	}

	public void onEvent(final KeskyVyfiltrovanyEvent aEvent) {
		indexator = aEvent.getFiltrovane().getIndexator();
		repaint();
	}

	public void onEvent(final KesoidOnoffEvent event) {
		setVisible(event.isOnoff());
	}

	public void onEvent(final PoziceSeMaMenitEvent event) {
		// TODO-vylet Tato metoda by měla být na modelu, pak je otázkou s jakou citlivostí vybírat
		final int polomerCitlivosi = POLOMER_CITLIVOSTI;
		final Point p = getSoord().transform(event.mou);
		final Rectangle rect = new Rectangle(p.x - polomerCitlivosi, p.y - polomerCitlivosi, polomerCitlivosi * 2, polomerCitlivosi * 2);
		final Sheet<Wpt> swpt = indexator == null ? null : indexator.locateNearestOne(getSoord().transforToBounding(rect), event.mou.xx, event.mou.yy);
		final Wpt wpt = swpt == null ? null : swpt.get();
		if (wpt != null && wpt.getMou().equals(event.mou)) { // je to přesně on
			int priorita = 40;
			if (wpt.isMainWpt()) {
				priorita = 50;
			}
			priorita -= wpt.getKesoid().getKesoidKind().ordinal();
			event.add(wpt, priorita);
		}
	}

	public void onEvent(final VyletChangeEvent aEvent) {
		if (aEvent.isVelkaZmena()) {
			Wpt.invalidateAllSklivec();
			repaint();
		} else {
			repaintWpt(aEvent.getKes().getMainWpt());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.core.coord.JSingleSlide0#render(java.awt.Graphics)
	 */
	@Override
	public void render(final Graphics g) {
		paintComponent(g);
	}

	@Override
	protected void paintComponent(final Graphics ag) {
		final Graphics2D gg = (Graphics2D) ag;

		if (indexator == null) {
			return;
		}
		if (ikonBag == null) {
			return;
		}
		// Nevykresluju. kdyz je prekrocen limit, ale jen kdyz kreslim na obrazovku
		final boolean prekrocenLimit = !vykreslovatOkamtiteAleDlouho && indexator.count(getSoord().getBoundingRect()) > FConst.MAX_POC_WPT_NA_MAPE;
		SwingUtilities.invokeLater(() -> kesoidModel.setPrekrocenLimitWaypointuVeVyrezu(prekrocenLimit));

		// vytvoření prázdných seznamů
		final EnumMap<Wpt.EZOrder, List<Sheet<Wpt>>> mapa = new EnumMap<>(Wpt.EZOrder.class);
		for (final Wpt.EZOrder zorder : Wpt.EZOrder.values()) {
			mapa.put(zorder, new ArrayList<Sheet<Wpt>>(10000));
		}

		// Roztřídit waypointy podle pořadí vykreslování
		if (!prekrocenLimit) {
			final BoundingRect hranice = coVykreslovat(gg);
			indexator.visit(hranice, (FlatVisitor<Wpt>) aSheet -> {
				final Wpt wpt = aSheet.get();
				mapa.get(wpt.getZorder()).add(aSheet);
			});
		}

		final List<SkloAplikant> skloAplikanti = ikonBag.getSada().getSkloAplikanti();
		for (int i = 0; i < skloAplikanti.size(); i++) {
			final SkloAplikant skloAplikant = skloAplikanti.get(i);
			if (skloAplikant.aplikaceSkla == EAplikaceSkla.VSE) { // jen na skla, na kterych je vsechno
				if (!prekrocenLimit) {
					for (final List<Sheet<Wpt>> list : mapa.values()) {
						for (final Sheet<Wpt> swpt : list) {
							final Wpt wpt = swpt.get();
							final Mou mou = new Mou(swpt.getXx(), swpt.getYy());
							paintWaypoint(gg, wpt, mou, i);
						}
					}
				}
			}
			// Vykreslení zvýrazněných
			if (kesoidPodMysi != null) {
				for (final Wpt wpt : kesoidPodMysi.getWpts()) {
					paintWaypoint(gg, wpt, null, i);
				}
			}
		}
	}

	private Genotyp computeGenotyp(final Wpt wpt) {
		final Genom genom = ikonBag.getGenom();
		final Genotyp g = wpt.getGenotyp(genom);
		// switch (cestyModel.get(wpt.getKesoid())) {
		// // case ANO: g.put(ikonBag.getGenom().ALELA_lovime); break;
		// case NE: g.put(ikonBag.getGenom().ALELA_ignoru); break;
		// }
		switch (vyletModel.get(wpt.getKesoid())) {
		case ANO:
			g.put(genom.ALELA_lovime);
			break;
		case NE:
			g.put(genom.ALELA_ignoru);
			break;
		case NEVIM:
			break;
		}
		g.put(cestyModel.isOnVylet(wpt) ? genom.ALELA_nacestejsou : genom.ALELA_mimocesticu);

		if (wpt == wptPodMysi) {
			g.put(genom.ALELA_mouseon);
		}
		if (wpt.getKesoid() == kesoidPodMysi) {
			g.put(genom.ALELA_mousean);
		}
		g.removeAll(fenotypoveZakazaneAlely);
		return g;
	}

	private void computeSklivec(final Wpt wpt) {
		final Sklivec sklivec = ikonBag.getSada().getSklivec(computeGenotyp(wpt));
		wpt.setSklivec(sklivec);
	}

	private BoundingRect coVykreslovat(final Graphics gg) {
		final Insets bii = ikonBag.getSada().getBigiestIconInsets();
		Rectangle rect = gg.getClipBounds();
		if (vykreslovatOkamtiteAleDlouho) {
			System.out.println("Omezeni: " + rect);
		}
		if (rect == null) {
			rect = new Rectangle(getSize());
		}
		// klipovanci.add(rect);
		// to right a bottom je v poradku, protoze zvetsujeme obdelnik,
		// abychom chytli vsechy kese, ktere k nam mohou zapadnout
		final Rectangle rect2 = new Rectangle(rect.x - bii.right, rect.y - bii.bottom, rect.width + bii.left + bii.right, rect.height + bii.top + bii.bottom);
		final BoundingRect br = getSoord().transforToBounding(rect2);
		return br;
	}

	private void initPopupMenuItems(final JPopupMenu p, final Wpt mysNadWpt, final KesBag vsechny) {
		// TODO : these should be based on the waypoint type
		// Přidat zhasínače
		final JMenu zhasinace = new JMenu("Zhasni");
		p.add(zhasinace);
		final Genotyp genotyp = mysNadWpt.getGenotyp(vsechny.getGenom());
		for (final Alela alela : genotyp.getAlely()) {
			if (alela.getGen().isVypsatelnyVeZhasinaci() && !alela.isVychozi()) {
				zhasinace.add(factory.init(new ZhasniKeseUrciteAlelyAction(alela)));
			}
		}

		///
		final Kesoid kesoid = mysNadWpt.getKesoid();
		p.add(factory.init(new ZoomKesAction(kesoid)));
		final JMenuItem item = new JMenuItem(factory.init(new CenterWaypointAction(mysNadWpt)));
		item.setText("Centruj");
		// TODO Dát ikonu středování
		item.setIcon(null);
		p.add(item);
		if (kesoid.getUrlShow() != null) {
			p.add(factory.init(new ZobrazNaGcComAction(kesoid)));
		}
		if (kesoid.getUrlPrint() != null) {
			p.add(factory.init(new TiskniNaGcComAction(kesoid)));
		}
		if (kesoid.getUrlShow() != null) {
			p.add(factory.init(new UrlToClipboardForGeogetAction(kesoid)));
		}
		if (kesoid.getUrlPrint() != null) {
			p.add(factory.init(new UrlToListingForGeogetAction(kesoid)));
		}
		final File kesoidSourceFile = kesoid.getSourceFile();
		if (kesoidSourceFile != null) {
			p.add(factory.init(new OpenFileAction(kesoidSourceFile)));
		}
		if (kesoid.getIdentifier() != null) {
			p.add(factory.init(new KesoidCodeToClipboard(kesoid)));
		}
		p.addSeparator();
		p.add(factory.init(new PridatDoCestyAction(mysNadWpt)));
		p.add(factory.init(new OdebratZCestyAction(mysNadWpt)));
		p.addSeparator();
		p.add(factory.init(new VyletAnoAction(kesoid)));
		p.add(factory.init(new VyletNeAction(kesoid)));
		p.add(factory.init(new VyletNevimAction(kesoid)));
		p.addSeparator();
		for (final Wpt wpt : kesoid.getWpts()) {
			p.add(factory.init(new CenterWaypointAction(wpt)));
		}
	}

	private Wpt najdiWptVBlizkosti(final Point p) {
		// System.out.println("POSUNOVACKA TO ASI BUDE: " + e);
		final int polomerCitlivosi = POLOMER_CITLIVOSTI;
		final Rectangle rect = new Rectangle(p.x - polomerCitlivosi, p.y - polomerCitlivosi, polomerCitlivosi * 2, polomerCitlivosi * 2);
		final Sheet<Wpt> swpt = indexator == null ? null : indexator.locateAnyOne(getSoord().transforToBounding(rect));
		final Wpt wpt = swpt == null ? null : swpt.get();
		return wpt;
	}

	/**
	 * @param g
	 * @param wpt
	 * @param mou
	 * @param i
	 */
	private void paintWaypoint(Graphics2D g, final Wpt wpt, Mou mou, final int i) {
		if (mou == null) {
			mou = wpt.getMou(); // z vykonnostnich duvodu se to vetsinou predava
		}
		Sklivec sklivec = wpt.getSklivec();

		// sklivec = null; // pro ucely testovani vzdfy vykreslujeme

		if (sklivec == null) { // nemame sklivce
			if (vykreslovatOkamtiteAleDlouho) {
				computeSklivec(wpt);
			} else {
				zaplanujNaplneniSklivce(wpt, mou);
			}
			sklivec = wpt.getSklivec(); // kdyby se náhodou už zaplnilo
			if (sklivec == null) {
				return;
			}
		}
		// Waypointů se může zobraztoat hodně, tak raději nekonvertujeme z Wgs
		final Point p = getSoord().transform(mou);

		final Imagant imagant = sklivec.imaganti.get(i);
		if (imagant == null) {
			return; // neni co vykreslovat
			// if (vykreslovatOkamtiteAleDlouho) {
			// System.out.println("Malujem kesika " + x + " " + y + " --- " +imagant.getImage().getWidth() + " " + imagant.getImage().getHeight());
			// }
		}

		// getScale() vrací vždy 1, ale tento kód zde chceme, protože se někdy zřejmě hodil
		if (getScale() == 1) {
			final int x = p.x + imagant.getXpos();
			final int y = p.y + imagant.getYpos();
			g.drawImage(imagant.getImage(), x, y, null);
		} else {
			g = (Graphics2D) g.create();
			g.translate(p.x, p.y);
			g.scale(scale, scale);
			g.translate(imagant.getXpos(), imagant.getYpos());
			g.drawImage(imagant.getImage(), 0, 0, null);
		}
	}

	/**
	 *
	 */
	private void registerEvents() {
	}

	private void repaintIfVse() {
		if (iJmenaAlel == null) {
			return;
		}
		if (ikonBag == null) {
			return;
		}
		fenotypoveZakazaneAlely = ikonBag.getGenom().namesToAlely(iJmenaAlel);
		Wpt.invalidateAllSklivec();
		repaint();
	}

	// @Override
	// public void finalize() {
	// System.out.println("Kesoidy finalizovány");
	// }

	private void repaintKes(final Kesoid kes) {
		if (kes == null) {
			return;
		}
		for (final Wpt wpt : kes.getWpts()) {
			repaintWpt(wpt);
		}
	}

	private void repaintWpt(final Wpt wpt) {
		wpt.invalidate();

		// Point p = coord.transform(wpt.getWgs().toMou());
		// Rectangle rect1 = new Rectangle(p.x - POLOMER_KESE, p.y - POLOMER_KESE,
		// POLOMER_KESE * 2, POLOMER_KESE * 2);

		frontaWaypointu.add(new WptPaintRequest(wpt, null));
		// Repaintanger repaintanger = new Repaintanger();
		// repaintanger.includeInsetsOnly(wpt);
		// repaintanger.include(wpt.getWgs().toMou());
		//
		// Rectangle rect2 = repaintanger.computeRectangle(coord);
		// // new Rectangle(p.x - POLOMER_KESE, p.y - POLOMER_KESE,
		// // POLOMER_KESE * 2, POLOMER_KESE * 2);
		// // repaintovaneCtverce .add(rect);
		// // System.out.println("REKTANGLE-1: " + rect1 + " -- " + p);
		// // System.out.println("REKTANGLE-2: " + rect2);
		// repaint(rect2);
	}

	private void zaplanujNaplneniSklivce(final Wpt wpt, final Mou mou) {

		frontaWaypointu.add(new WptPaintRequest(wpt, mou));
		pocitVelikostFrontyWaypointu.set(frontaWaypointu.size());
		// spocitejSklivece(wpt);
	}

}
