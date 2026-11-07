package cz.geokuk.core.program;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.napoveda.Diagnostika;
import cz.geokuk.core.render.JRenderSlide;
import cz.geokuk.framework.*;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.cesty.JCestySlide;
import cz.geokuk.plugins.kesoid.JToast;
import cz.geokuk.plugins.cesty.JPanelCesty;
import cz.geokuk.plugins.kesoid.JKesoidySlide;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetailContainer;
import cz.geokuk.plugins.kesoid.detailroh.JDetailPrekryvnik;
import cz.geokuk.plugins.kesoid.detailroh.JZamernyKriz;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoidkruhy.JZvyraznovaciKruhySlide;
import cz.geokuk.plugins.kesoidobsazenost.JObsazenost;
import cz.geokuk.plugins.kesoidpopisky.JPopiskySlide;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.gui.*;
import cz.geokuk.plugins.mapy.sprava.gui.JObrysMapSlide;
import cz.geokuk.plugins.mrizky.*;
import cz.geokuk.util.gui.CornerLayoutManager;
import cz.geokuk.util.gui.ERoh;

public class JMainFrame extends JFrame implements SlideListProvider {

	private static final long serialVersionUID = 1L;
	private JStatusBar statusbar;
	private JToolBar toolBar;
	private Factory factory;
	private KesoidModel kesoidModel;
	private FullScreenAction fullScreenAction;
	private OknoModel oknoModel;
	private Menu menux;
	/** EventManager drží posluchače jen slabě, bez pole by o události přišel. */
	private cz.geokuk.plugins.challenge.SledovaniVybraneKese sledovaniVybraneKese;

	private List<JSingleSlide0> slideList;
	private CloseAction closeAction;

	JMainFrame() {
		// To je do globální proměnné kbůli dialogům a jen kvůli nim
		Dlg.natavMainFrameProMaleDialogy(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.core.coord.SlidListProvider#getSlides()
	 */
	@Override
	public List<JSingleSlide0> getSlides() {
		return slideList;
	}

	public void init() {
		// umistitOkno();
		setTitle("GeoKuk");
		final List<Image> imgs = new ArrayList<>();
		imgs.add(ImageLoader.seekResImage("geokuk32.png", 32, 32));
		imgs.add(ImageLoader.seekResImage("geokuk16.png", 16, 16));
		imgs.add(ImageLoader.seekResImage("geokuk48.png", 48, 48));
		imgs.add(ImageLoader.seekResImage("geokuk64.png", 64, 64));
		imgs.add(ImageLoader.seekResImage("geokuk128.png", 128, 128));
		imgs.add(ImageLoader.seekResImage("geokuk256.png", 256, 256));
		setIconImages(imgs);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Jen kvůli tomu, aby se nezničilo a chodili eventy
		// TODO Na menu by neměly být asi události
		final JGeokukToolbar jGeokukToolbar = factory.init(new JGeokukToolbar());
		menux = factory.init(new Menu(this, jGeokukToolbar));

		menux.makeMenu();

		final JComponent srohama = new JSRohamaPrekryvnik();
		srohama.setLayout(new CornerLayoutManager());
		// srohama.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 5));
		srohama.add(createDetailRoh(), ERoh.JZ);
		srohama.add(createKesInfoRoh(), ERoh.JV);
		srohama.add(factory.init(new JPanelCesty()), ERoh.SV);

		final JPresCeleMysovani mysovani = new JPresCeleMysovani();
		{
			final JComponent smrizema = new JPresCelePrekryvnik();
			smrizema.setLayout(new OverlayLayout(smrizema));

			final JComponent renderSlide = new JRenderSlide();
			final JComponent zoomovaciObdelnik = new JZoomovaciObdelnik();
			final JComponent meritkovnik = new JMeritkoSlide();
			final JComponent mrizkaDd = new JMrizkaDd();
			final JComponent mrizkaDdMmMmm = new JMrizkaDdMmMmm();
			final JComponent mrizkaDdMmSs = new JMrizkaDdMmSs();
			final JComponent mrizkaUtm = new JMrizkaUtm();
			// final JComponent mrizkaS42 = new JMrizkaS42(coord);
			// final JComponent mrizkaJtsk = new JMrizkaJTSK(coord);
			final JCestySlide cesty = new JCestySlide();

			final JComponent popisyKesiNaMape = new JPopiskySlide();
			final JComponent kesky = new JKesoidySlide(false);
			final cz.geokuk.plugins.challenge.JPlanovacNadKeskami planovacNad = new cz.geokuk.plugins.challenge.JPlanovacNadKeskami();
			final cz.geokuk.plugins.challenge.JPlanovacPodKeskami planovacPod = new cz.geokuk.plugins.challenge.JPlanovacPodKeskami();
			final JComponent pozicovnik = new JPozicovnikSlide();
			final JComponent zvyraznovaciKruhy = new JZvyraznovaciKruhySlide();
			final JObsazenost obsazenik = new JObsazenost();
			final JComponent legendaObsazenosti = new cz.geokuk.plugins.kesoidobsazenost.JLegendaObsazenosti(obsazenik);
			final JToast toast = new JToast();
			final cz.geokuk.plugins.challenge.JPruhPlanovace pruhPlanovace = new cz.geokuk.plugins.challenge.JPruhPlanovace();
			final cz.geokuk.plugins.challenge.JPopupPlanovace popupPlanovace = new cz.geokuk.plugins.challenge.JPopupPlanovace();
			pruhPlanovace.setNaKlikOdznaku(popupPlanovace::prepni);
			pruhPlanovace.setJinyPopupOtevreny(() -> popupPlanovace.jeOtevreno() || cz.geokuk.util.gui.PopupNadBlokem.jeNejakyOtevreny());
			final JKachlovnikPresCele kachlovnik = new JKachlovnikPresCele();
			final JComponent odkazRady = new JOdkazRadySlide(kachlovnik);
			final JKachlovnik kachlovnikTras = new JKachlovnikTras();
			final JKachlovnik kachlovnikReliefu = new cz.geokuk.plugins.mapy.kachle.gui.JKachlovnikReliefu();
			final JComponent obrysMap = new JObrysMapSlide();
			final JComponent indikatorNacitani = new JIndikatorNacitaniSlide(kachlovnik, kachlovnikTras, kachlovnikReliefu);
			final JComponent pozadi = new JSlidePozadi();

			smrizema.add(mysovani);
			smrizema.add(odkazRady);
			smrizema.add(indikatorNacitani);
			smrizema.add(renderSlide);
			smrizema.add(zoomovaciObdelnik);
			smrizema.add(meritkovnik);
			smrizema.add(mrizkaDd);
			smrizema.add(mrizkaDdMmMmm);
			smrizema.add(mrizkaDdMmSs);
			smrizema.add(mrizkaUtm);
			// smrizema.add(mrizkaS42);
			// smrizema.add(mrizkaJtsk);
			smrizema.add(toast);
			smrizema.add(legendaObsazenosti);
			smrizema.add(cesty);
			smrizema.add(popisyKesiNaMape);
			smrizema.add(planovacNad);
			smrizema.add(kesky);
			smrizema.add(planovacPod);
			smrizema.add(pozicovnik);
			smrizema.add(zvyraznovaciKruhy);
			smrizema.add(obsazenik);
			smrizema.add(obrysMap);
			smrizema.add(kachlovnikTras);
			smrizema.add(kachlovnikReliefu);
			smrizema.add(kachlovnik);
			smrizema.add(pozadi);

			mysovani.addKeyListener(mysovani);

			factory.init(mysovani);
			factory.init(odkazRady);
			factory.init(renderSlide);
			factory.init(zoomovaciObdelnik);
			factory.init(meritkovnik);
			factory.init(mrizkaDd);
			factory.init(mrizkaDdMmMmm);
			factory.init(mrizkaDdMmSs);
			factory.init(mrizkaUtm);
			// factory.init(mrizkaS42);
			// factory.init(mrizkaJtsk);
			factory.init(cesty);
			factory.init(popisyKesiNaMape);
			factory.init(kesky);
			factory.init(planovacNad);
			factory.init(planovacPod);
			sledovaniVybraneKese = factory.init(new cz.geokuk.plugins.challenge.SledovaniVybraneKese());
			factory.init(pozicovnik);
			factory.init(zvyraznovaciKruhy);
			factory.init(obsazenik);
			factory.init(toast);
			factory.init(pruhPlanovace);
			factory.init(popupPlanovace);
			factory.init(obrysMap);
			factory.init(kachlovnikTras);
			factory.init(kachlovnikReliefu);
			factory.init(kachlovnik);
			factory.init(pozadi);

			// Obezlička, aby myšování bylo podle myšího toku událostí nahoře, ale komponentně dole.
			// Musí být dole ,aby fungovaly slidery.
			smrizema.remove(mysovani);
			slideList = new ArrayList<>();
			for (final Component comp : smrizema.getComponents()) {
				if (comp instanceof JSingleSlide0) {
					final JSingleSlide0 slide = (JSingleSlide0) comp;
					slideList.add(0, slide);
				}
			}

			toast.setPrekazky(() -> RozmisteniPrekryvu.prekazky(srohama, smrizema, obsazenik.isVisible()));
			popupPlanovace.setPrekazky(() -> RozmisteniPrekryvu.prekazky(srohama, smrizema, obsazenik.isVisible()));
			planovacNad.setPrekazky(() -> RozmisteniPrekryvu.prekazky(srohama, smrizema, obsazenik.isVisible()));
			final java.util.concurrent.Executor stavbaPlanovace = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
				final Thread vlakno = new Thread(r, "Podklad plánovače");
				vlakno.setDaemon(true);
				return vlakno;
			});
			planovacNad.setStavba(stavbaPlanovace);
			planovacPod.setStavba(stavbaPlanovace);
			pruhPlanovace.setPrekazky(() -> RozmisteniPrekryvu.prekazky(srohama, smrizema, obsazenik.isVisible()));

			smrizema.add(pruhPlanovace, 0);
			smrizema.add(popupPlanovace, 0);
			smrizema.add(mysovani, smrizema.getComponentCount());

			factory.init(smrizema);
			srohama.add(smrizema);
		}

		getContentPane().add(srohama);

		// Menu a toolbar
		setJMenuBar(menux.getMenuBar());
		Diagnostika.sledujMenu(menux.getMenuBar());
		toolBar = menux.getToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);

		statusbar = factory.init(new JStatusBar());
		add(statusbar, BorderLayout.SOUTH);

		addWindowListener();

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F11"), "fullscreen");
		getRootPane().getActionMap().put("fullscreen", fullScreenAction);
		SwingUtilities.invokeLater(() -> mysovani.requestFocus());
	}

	public void inject(final CloseAction closeAction) {
		this.closeAction = closeAction;
	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void inject(final FullScreenAction fullScreenAction) {
		this.fullScreenAction = fullScreenAction;
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void inject(final OknoModel oknoModel) {
		this.oknoModel = oknoModel;
	}

	public void onEvent(final OknoStatusChangedEvent event) {
		final OknoUmisteniDto oknoStatus = event.getOknoStatus();
		final Rectangle naObrazovce = umistiNaObrazovku(oknoStatus);
		if (naObrazovce == null) {
			// Implicitní veliksot a umístění
			final Toolkit toolkit = getToolkit();
			final Dimension screenSize = toolkit.getScreenSize();
			setSize(new Dimension(screenSize.width * 3 / 4, screenSize.height * 3 / 4));
			setLocation(new Point(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2));
			// event.getModel().setOknoUmisteni(oknoUmisteni); // a aktualizovat model
		} else {
			setBounds(naObrazovce);
		}
		setExtendedState(event.getStavOkna());
	}

	public void setFullScreen(final boolean fs) {

	}

	private void addWindowListener() {
		addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(final ComponentEvent e) {
				ulozeStav();
			}

			@Override
			public void componentMoved(final ComponentEvent e) {
				ulozeStav();
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				ulozeStav();
			}

			@Override
			public void componentShown(final ComponentEvent e) {
				ulozeStav();
			}
		});
		addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowGainedFocus(final WindowEvent e) {
				ulozeStav();
			}

			@Override
			public void windowLostFocus(final WindowEvent e) {
				ulozeStav();
			}
		});

		addWindowStateListener(e -> ulozeStav());

		addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(final WindowEvent aE) {
				ulozeStav();
				zkontrolujALoadni();
			}

			@Override
			public void windowClosed(final WindowEvent aE) {
				ulozeStav();
				System.exit(0);
			}

			@Override
			public void windowClosing(final WindowEvent aE) {
				// System.out.println(aE);
				ulozeStav();
				closeAction.actionPerformed(null);
			}

			@Override
			public void windowDeactivated(final WindowEvent aE) {
				ulozeStav();
				// ulozeStav();
				// System.out.println(aE);
			}

			@Override
			public void windowDeiconified(final WindowEvent aE) {
				ulozeStav();
				zkontrolujALoadni();
				// System.out.println(aE);
			}

			@Override
			public void windowIconified(final WindowEvent aE) {
				ulozeStav();
				// System.out.println(aE);
			}

			@Override
			public void windowOpened(final WindowEvent aE) {
				zkontrolujALoadni();
				// System.out.println(aE);
			}
		});
	}

	private JComponent createDetailRoh() {
		JComponent detailRoh;
		// JComponent sv = new JPrekryvnik();
		detailRoh = factory.init(new JDetailPrekryvnik());
		// detailRoh.setBackground(new Color(0,255,120));
		detailRoh.setMinimumSize(new Dimension(100, 100));
		detailRoh.setPreferredSize(new Dimension(200, 200));
		detailRoh.setMaximumSize(new Dimension(300, 300));

		final JDetailMysovani mysovani = new JDetailMysovani();
		detailRoh.add(mysovani);
		detailRoh.add(new JZamernyKriz());
		final JKachlovnik detailKachlovnik = new JKachlovnikDoRohu();
		detailRoh.add(detailKachlovnik);
		factory.init(detailKachlovnik);
		factory.init(mysovani);
		detailKachlovnik.setKachloType(EKaType.OPHOTO_M);
		detailRoh.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		return detailRoh;
	}

	private JComponent createKesInfoRoh() {
		JComponent detailRoh;
		// JComponent sv = new JPrekryvnik();
		detailRoh = new JPanel();
		detailRoh.setLayout(new BorderLayout());

		JComponent kesDatail;
		kesDatail = factory.init(new JKesoidDetailContainer());
		detailRoh.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		detailRoh.add(kesDatail);
		return detailRoh;
	}

	/**
	 * Uložené umístění posunuté a zmenšené tak, aby bylo celé na monitoru, kterého se nejvíc dotýká (okno z většího nebo odpojeného monitoru), nebo null,
	 * když okno není vidět vůbec nebo je nesmyslně malé.
	 */
	private static Rectangle umistiNaObrazovku(final OknoUmisteniDto u) {
		final Dimension velikost = u.getVelikost();
		if (velikost.width < 200 || velikost.height < 200) {
			return null;
		}
		final Rectangle okno = new Rectangle(u.getPozice(), velikost);
		Rectangle nejlepsi = null;
		long nejvic = 0;
		for (final GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			final Rectangle plocha = JMyDialog0.pracovniPlocha(gd.getDefaultConfiguration());
			final Rectangle prunik = plocha.intersection(okno);
			final long obsah = prunik.isEmpty() ? 0 : (long) prunik.width * prunik.height;
			if (obsah > nejvic) {
				nejvic = obsah;
				nejlepsi = plocha;
			}
		}
		if (nejlepsi == null) {
			return null;
		}
		final int w = Math.min(okno.width, nejlepsi.width);
		final int h = Math.min(okno.height, nejlepsi.height);
		final int x = Math.max(nejlepsi.x, Math.min(okno.x, nejlepsi.x + nejlepsi.width - w));
		final int y = Math.max(nejlepsi.y, Math.min(okno.y, nejlepsi.y + nejlepsi.height - h));
		return new Rectangle(x, y, w, h);
	}

	private void ulozeStav() {
		final int extendedState = getExtendedState();
		if (extendedState == NORMAL) {
			// pozici a veliksot ukládáme jen v normálním stavu
			final OknoUmisteniDto oknoUmisteni = new OknoUmisteniDto();
			oknoUmisteni.setPozice(getLocation());
			oknoUmisteni.setVelikost(getSize());
			oknoModel.setOknoUmisteni(oknoUmisteni);
			// System.out.println("Ukladam stav: " + oknoUmisteni + " stav=" + extendedState);
		} else {
			// System.out.println("Ukladam stav: " + extendedState);
		}
		oknoModel.setStavOkna(extendedState);
	}

	private void zkontrolujALoadni() {
		kesoidModel.startIkonLoad(false);
	}

}
