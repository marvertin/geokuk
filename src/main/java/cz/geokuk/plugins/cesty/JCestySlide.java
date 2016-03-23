package cz.geokuk.plugins.cesty;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.framework.FKurzory;
import cz.geokuk.framework.MouseGestureContext;
import cz.geokuk.plugins.cesty.akce.OdebratZCestyAction;
import cz.geokuk.plugins.cesty.akce.bod.*;
import cz.geokuk.plugins.cesty.akce.cesta.*;
import cz.geokuk.plugins.cesty.akce.usek.*;
import cz.geokuk.plugins.cesty.data.*;
import cz.geokuk.plugins.kesoid.Wpt;

public class JCestySlide extends JSingleSlide0 {

	private class PropojovaciAutomat {

		/**
		 * Stav v jakém je propojování cest. 0 ... nic zajimavého (posunuta myš nebo nesplněny podmínky pro drag 1 ... byla stisknuta myš nebo dragnuto a blizkyBousek NEma schopnost spojovani 2 ... bylo dragnuto a blizkyBousek ma schponost spojovani, tak při uvolnění spojujeme
		 */
		private int stavPropojovaniCest;

		void mouseDragged() {
			if (!jeMoznostSpojovani()) {
				stavPropojovaniCest = 1;
			} else {
				if (stavPropojovaniCest == 1) { // posouváme se dál
					stavPropojovaniCest = 2;
				}
			}
		}

		void mouseMoved() {
			stavPropojovaniCest = 0;
		}

		void mousePressed() {
			if (!jeMoznostSpojovani()) {
				stavPropojovaniCest = 1;
			}
		}

		void mouseReleased() {
			if (stavPropojovaniCest == 2 && jeMoznostSpojovani()) {
				System.out.println("Spojujeme !!!!!!!!!!!!!!!!!");
				cestyModel.spojCestyVPrekryvnemBode((Bod) blizkyBousek);
			}
			stavPropojovaniCest = 0;
		}

		private boolean jeMoznostSpojovani() {
			final boolean jeMoznostSpojeni = blizkyBousek != null && blizkyBousek.getKoncovyBodDruheCestyVhodnyProSpojeni() != null;
			if (jeMoznostSpojeni) {
				// System.out.println("MOZNOST SPOJENI JE ");
			}
			return jeMoznostSpojeni;
		}
	}

	private static final long serialVersionUID = -5858146658366237217L;

	private static final int MIRA_VZDALENOSTI_PRO_ZVYRAZNENI = 7;

	private Doc doc;
	private Mou moucur;

	private boolean dragujeme;

	/** Nejbližší bod nebo śek cesty ke kurozru myši, pokud nějaký vůbec blízko je */
	private Bousek0 blizkyBousek;

	private CestyModel cestyModel;

	private final JCestaTooltip cestaToolTip = new JCestaTooltip();
	/** Pokud je stisknut CTRL tak přidáváme body, to také znamená, že kreslíme čáru od posledního bodu cesty k aktuálnímu */
	private Mouable pridavanyBod1;

	private Mouable pridavanyBod2;

	private PoziceModel poziceModel;

	private Poziceq poziceq = new Poziceq();

	private final PropojovaciAutomat propojovaciAutmomat = new PropojovaciAutomat();

	public JCestySlide() {
		setOpaque(false);
		setCursor(null);
		initComponents();
	}

	@Override
	public void addPopouItems(final JPopupMenu popupMenu, final MouseGestureContext ctx) {

		if (blizkyBousek instanceof Usek) {
			final Usek usek = (Usek) blizkyBousek;
			addPopupItems(popupMenu, usek);
		}
		if (blizkyBousek instanceof Bod) {
			final Bod bod = (Bod) blizkyBousek;
			addPopupItems(popupMenu, bod);
		}
		if (blizkyBousek != null) {
			addPopupItems(popupMenu, blizkyBousek.getCesta());
		}

		super.addPopouItems(popupMenu, ctx);
	}

	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JCestySlide();
	}

	@Override
	public void ctrlKeyPressed(final MouseGestureContext ctx) {
		zahajPridavaniBodux();
	}

	@Override
	public void ctrlKeyReleased(final MouseGestureContext ctx) {
		zrusPridavaniBodu();
	}

	@Override
	public Cursor getMouseCursor(final boolean pressed) {
		final Mouable upravenaMys = getUpravenaMys();
		final boolean zachytavano = upravenaMys != null && !upravenaMys.getMou().equals(moucur);
		// boolean zachytavano = false;
		if (blizkyBousek != null) {
			if (pressed) {
				if (zachytavano) {
					return FKurzory.NAD_WAYPOINTEM_DRAGOVANI_BODU;
				} else {
					return FKurzory.BLIZKO_BOUSKU_DRAGOVANI;
				}
			} else {
				return FKurzory.BLIZKO_BOUSKU_NORMAL;
			}
		} else {
			if (pridavanyBod1 != null) {
				if (zachytavano) {
					return FKurzory.NAD_WAYPOINTEM_PRIDAVANI_BODU;
				} else {
					return FKurzory.PRIDAVANI_BODU;
				}
			} else {
				return super.getMouseCursor(pressed);
			}
		}
	}

	@Override
	public Mouable getUpravenaMys() {
		if (doc == null) {
			return super.getUpravenaMys();
		}

		// Ošetřit, že dosedne do nějaké cesty
		final long kvadratMaximalniVzdalenosti = getKvadratMaximalniVzdalenosti();
		for (final Cesta cesta : doc.getCesty()) {
			if (cesta == cestyModel.getCurta()) {
				// continue;
			}
			if (cesta.getStart() == null || cesta.getCil() == null) {
				continue;
			}
			// TODO -vylet hledat nejbližší cestu a ne libovolnou cestu splňuící podmínky
			if (cesta.getStart() != blizkyBousek && cesta.getStart().jeDoKvadratuVzdalenosti(moucur, kvadratMaximalniVzdalenosti)) {
				return cesta.getStart();
			}
			if (cesta.getCil() != blizkyBousek && cesta.getCil().jeDoKvadratuVzdalenosti(moucur, kvadratMaximalniVzdalenosti)) {
				return cesta.getCil();
			}
		}
		return super.getUpravenaMys();
	}

	public void inject(final CestyModel cestyModel) {
		this.cestyModel = cestyModel;
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	/**
	 * Invoked when the mouse has been clicked on a component.
	 */
	@Override
	public void mouseClicked(final MouseEvent e, final MouseGestureContext ctx) {
		final Mouable upravenaMys = getUpravenaMys();
		if (upravenaMys == null) {
			return; // důvěřuj, ale prověřuj
		}
		boolean propagovatDal = true;
		// System.out.println("UDALOST " + e);
		// kesky.mouseClicked(e);
		// if (e.isConsumed()) return;
		if (SwingUtilities.isRightMouseButton(e)) {
			cestyModel.setCurta(null);
		}
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (blizkyBousek instanceof Bod) {
				final Bod bod = (Bod) blizkyBousek;
				poziceModel.setPozice(bod);
				propagovatDal = false;
			}
		}

		if (SwingUtilities.isLeftMouseButton(e) && (e.getModifiers() & Event.CTRL_MASK) != 0) {
			if (pridavanyBod1 != null && pridavanyBod2 != null) {
				if (cestyModel.getCurta() != null) {
					final Bod bod = cestyModel.pridejBodNaKonec(upravenaMys);
					cestyModel.spojCestyVPrekryvnemBode(bod);
				} else {
					final Bod bod1 = cestyModel.pridejBodNaKonec(pridavanyBod1);
					final Bod bod2 = cestyModel.pridejBodNaKonec(pridavanyBod2);
					cestyModel.spojCestyVPrekryvnemBode(bod1);
					cestyModel.spojCestyVPrekryvnemBode(bod2);
				}
				zrusPridavaniBodu();
				zahajPridavaniBodux();
			}
			// Wpt wptPodMysi = getWptPodMysi();
			// Mouable mouable = wptPodMysi == null ? moucur : wptPodMysi;
			// je presně ten okamžik, kdy se má přidat bod
		}
		zobrazeniDalky();
		if (propagovatDal) {
			// Nepropagujeme dál, ale už ani nevím proč, ale to nevadí, protože
			// poziceModel.setPozice(bod) udělá vše, co udělat má.
			chain().mouseClicked(e, ctx);
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e, final MouseGestureContext ctx) {
		zrusPridavaniBoduKdyzKdyNeniCtrl(e.getModifiers());
		final Mouable mouNovy = getUpravenaMys();
		if (dragujeme && mouNovy != null) {
			// TODO Pro ty dole už to nemůže být dragování, ale jen m,ůvoání.
			// je to ale čisté?
			chain().mouseMoved(e, ctx);
			// System.out.println(e.getPoint());
			if (blizkyBousek instanceof Bod) {
				final Bod bb = (Bod) blizkyBousek;
				final Wpt wptPodMysi = getWptPodMysi();
				// Mouable mouable = wptPodMysi != null ? wptPodMysi : mouNovy;
				// Mouable mouable = wptPodMysi != null ? wptPodMysi : getUpravenaMys();
				cestyModel.presunBod(bb, mouNovy);
				final long kvadratOdklonu = bb.computeKvadratOdklonu();
				// System.out.println("1111: " + kvadratOdklonu + " -- " + getKvadratMaximalniVzdalenosti());
				if (kvadratOdklonu < getKvadratMaximalniVzdalenosti() && wptPodMysi == null) {
					if (!bb.isNaHraniciSegmentu()) {
						blizkyBousek = cestyModel.removeBod(bb);
					}
				}
				repaint();
			}
			if (blizkyBousek instanceof Usek) {
				// táhneme za úsek a možná budeme rozdělovat na dva
				final Usek usek = (Usek) blizkyBousek;
				final Mou mouact = getSoord().transform(e.getPoint());
				final long kvadratVzdalenosti = usek.computeKvadratVzdalenostiBoduKUsecce(mouact);
				if (kvadratVzdalenosti > getKvadratMaximalniVzdalenosti()) {
					blizkyBousek = cestyModel.rozdelUsekNaDvaNove(usek, mouNovy.getMou());
				}
				repaint();
			}
			propojovaciAutmomat.mouseDragged();
		} else {
			chain().mouseDragged(e, ctx);
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e, final MouseGestureContext ctx) {
		zrusPridavaniBoduKdyzKdyNeniCtrl(e.getModifiers());
		super.mouseEntered(e, ctx);
	}

	@Override
	public void mouseExited(final MouseEvent e, final MouseGestureContext ctx) {
		zrusPridavaniBoduKdyzKdyNeniCtrl(e.getModifiers());
		super.mouseExited(e, ctx);
	}

	@Override
	public void mouseMoved(final MouseEvent e, final MouseGestureContext ctx) {
		zahajCiZrsuPridavaniBoduNaZakladeMyssihoEventu(e.getModifiers());
		propojovaciAutmomat.mouseMoved();
		super.mouseMoved(e, ctx);
	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	@Override
	public void mousePressed(final MouseEvent e, final MouseGestureContext ctx) {
		zahajCiZrsuPridavaniBoduNaZakladeMyssihoEventu(e.getModifiers());
		dragujeme = blizkyBousek != null;
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (blizkyBousek != null) {
				cestyModel.setCurta(blizkyBousek.getCesta());
			}
		}
		chain().mousePressed(e, ctx);
		if (dragujeme) {
			e.consume();
		}
		propojovaciAutmomat.mousePressed();
	}

	@Override
	public void mouseReleased(final MouseEvent e, final MouseGestureContext ctx) {
		dragujeme = false;
		propojovaciAutmomat.mouseReleased();
		chain().mouseReleased(e, ctx);
	}

	public void onEvent(final CestyChangedEvent event) {
		doc = event.getDoc();
		// TODO repaintovat jen to, co je potřeba
		repaint();
	}

	public void onEvent(final PoziceChangedEvent event) {
		poziceq = event.poziceq;

	}

	public void onEvent(final ZmenaSouradnicMysiEvent event) {
		moucur = event.moucur;
		prepocitatBlizkehoBouska();
		zobrazeniDalky();
		// LATER-vylet repaintovat jen to, co je potřeba
		repaint();
	}

	@Override
	public void paintComponent(final Graphics aG) {
		final Graphics2D g = (Graphics2D) aG;
		// drawBodForDebug(g, pridavanyBod1, Color.BLACK);
		// drawBodForDebug(g, pridavanyBod2, Color.WHITE);
		if (doc == null) {
			return;
		}
		final MalovadloParams params = new MalovadloParams();
		params.doc = doc;
		params.curta = cestyModel.getCurta();
		params.blizkyBousek = blizkyBousek;
		params.soord = getSoord();
		params.mouPridavanyBod1 = pridavanyBod1 == null ? null : pridavanyBod1.getMou();
		params.mouPridavanyBod2 = pridavanyBod2 == null ? null : pridavanyBod2.getMou();
		params.mouDeliciNaBlizkemBousku = moucur;
		params.poziceMouable = poziceq.getPoziceMouable();
		final Malovadlo malovadlo = new Malovadlo(g, params);
		malovadlo.paint();
	}

	private void addPopupItems(final JPopupMenu popupMenu, final Bod bod) {
		popupMenu.add(factory.initNow(new OdebratZCestyAction(bod)));
		popupMenu.add(factory.initNow(new RozdelitCestuVBoduAction(bod)));
		popupMenu.add(factory.initNow(new UriznoutCestuVBoduAction(bod)));
		popupMenu.add(factory.initNow(new ZnovuSpojitCestyAction(bod)));
		popupMenu.add(factory.initNow(new PresunoutSemVychoziBodUzavreneCesty(bod)));
	}

	private void addPopupItems(final JPopupMenu popupMenu, final Cesta cesta) {
		popupMenu.add(factory.initNow(new ZoomovatCestuAction(cesta)));
		popupMenu.add(factory.initNow(new SmazatCestuAction(cesta)));
		popupMenu.add(factory.initNow(new ObratitCestuAction(cesta)));
		popupMenu.add(factory.initNow(new UzavritCestuAction(cesta)));
		popupMenu.add(factory.initNow(new PripojitVybranouCestu(cesta)));
		popupMenu.add(factory.initNow(new PredraditVybranouCestu(cesta)));
		popupMenu.add(factory.initNow(new PospojovatVzdusneUseky(cesta)));

	}

	private void addPopupItems(final JPopupMenu popupMenu, final Usek usek) {
		popupMenu.add(factory.initNow(new PrepniVzdusnostUseku(usek, moucur)));
		popupMenu.add(factory.initNow(new RozdelitCestuVUsekuAction(usek, moucur)));
		popupMenu.add(factory.initNow(new UriznoutCestuVUsekuAction(usek, moucur)));
		popupMenu.add(factory.initNow(new SmazatUsekAOtevritNeboRozdelitCestu(usek, moucur)));
	}

	@SuppressWarnings("unused")
	private void drawBodForDebug(final Graphics2D g, final Mouable mouable, final Color color) {
		if (mouable == null) {
			return;
		}
		final Mou mou = mouable.getMou();
		final Point p = getSoord().transform(mou);
		final Graphics gg = g.create();
		gg.setColor(color);
		gg.fillOval(p.x - 15, p.y - 15, 30, 30);
	}

	private long getKvadratMaximalniVzdalenosti() {
		// Využíváme transformace v jednom směru, možná by to chtělo speciální funkci.
		final long maximalniVzdalenost = getSoord().transformPoindDiff(MIRA_VZDALENOSTI_PRO_ZVYRAZNENI);
		final long kvadratMaximalniVzdalenosti = maximalniVzdalenost * maximalniVzdalenost;
		return kvadratMaximalniVzdalenosti;
	}

	/**
	 *
	 */
	private void initComponents() {
		cestaToolTip.setVisible(false);
		add(cestaToolTip);

	}

	private void napozicujCestaTolltip() {
		cestaToolTip.setOpaque(true);
		cestaToolTip.setBackground(Color.WHITE);
		cestaToolTip.setSize(cestaToolTip.getPreferredSize());
		final Point p = getSoord().transform(moucur);
		p.y -= cestaToolTip.getHeight() + 10;
		p.x -= cestaToolTip.getWidth() + 10;
		cestaToolTip.setLocation(p);
		cestaToolTip.setVisible(true);
	}

	private void prepocitatBlizkehoBouska() {
		if (!dragujeme) {// při dragování nechceme měnit bouska
			if (pridavanyBod1 != null) {
				blizkyBousek = null;
			} else {
				if (doc != null) {
					blizkyBousek = doc.locateNejblizsiDoKvadratuVzdalenosi(moucur, getKvadratMaximalniVzdalenosti(), cestyModel.getCurta(), true);
				} else {
					blizkyBousek = null;
				}
			}
		}
	}

	private void zahajCiZrsuPridavaniBoduNaZakladeMyssihoEventu(final int modifiers) {
		if ((modifiers & Event.CTRL_MASK) != 0) {
			zahajPridavaniBodux();
		} else {
			zrusPridavaniBodu();
		}
	}

	private void zahajPridavaniBodux() {
		if (pridavanyBod1 == null) {
			// ještě jsme nezačali přidávat, tak začínáme
			if (cestyModel.getCurta() == null) { // pokud není nic aktivní, tak hned jdeme na to
				if (poziceq.isNoPosition()) { // nemáme pozici, tak stváříme
					pridavanyBod1 = getUpravenaMys(); // kdyby byla null, nic nepřidváme
				} else {
					pridavanyBod1 = poziceq.getPoziceMouable();
				}
			} else { // máme curtu
				pridavanyBod1 = cestyModel.getCurta().getCil().getMouable();
			}
			pridavanyBod2 = pridavanyBod1; // kdyby byla null, nic nepřidváme
		} else { // jedničku už máme, dvojku doděláváme
			pridavanyBod2 = getUpravenaMys(); // kdyby byla null, nic nepřidváme
		}
		cestyModel.setPridavaniBodu(true);
	}

	private void zobrazeniDalky() {
		// Mouable mouable = getUpravenaMys();
		// Mou mou = mouable == null ? moucur : mouable.getMou();
		// LATER Takto nebude vzdálenost přesná v blízkosti uchopovanců, asi by to chtělo
		// uchopenou myš
		if (moucur == null) {
			return; // nemůžeme zobrazovat dálkoviny, když tam nemáme myš
		}
		final Cesta curta = cestyModel.getCurta();
		if (pridavanyBod1 != null && curta != null) {
			cestaToolTip.setPridavaciDalkoviny(curta, moucur);
			napozicujCestaTolltip();
		} else if (blizkyBousek != null) {
			cestaToolTip.setDalkoviny(blizkyBousek, moucur);
			napozicujCestaTolltip();
		} else {
			cestaToolTip.setVisible(false);
		}
	}

	private void zrusPridavaniBodu() {
		if (pridavanyBod1 != null) {
			prepocitatBlizkehoBouska();
			zobrazeniDalky();
			repaint();
		}
		pridavanyBod1 = null;
		pridavanyBod2 = null;
		cestyModel.setPridavaniBodu(false);
	}

	private void zrusPridavaniBoduKdyzKdyNeniCtrl(final int modifiers) {
		if ((modifiers & Event.CTRL_MASK) == 0) {
			zrusPridavaniBodu();
		}
	}

}
