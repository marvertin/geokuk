package cz.geokuk.core.program;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.render.JRenderSlide;
import cz.geokuk.framework.*;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.cesty.JCestySlide;
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
		setIconImages(imgs);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Jen kvůli tomu, aby se nezničilo a chodili eventy
		// TODO Na menu by neměly být asi události
		final JGeokukToolbar jGeokukToolbar = factory.init(new JGeokukToolbar());
		menux = factory.init(new Menu(this, jGeokukToolbar));

		// Settings.prefbag = new PreferencableBag(new Runnable() {
		// @Override
		// public void run() {
		// detailCoord.setMoustred(coord.getMoustred()); // iniciální nastavení náhledu na střed
		// }
		// });
		// Nahraje hodnoty preferencí a také již posílá eventy
		// Settings.loadCurrentPreferences();
		menux.makeMenu();

		final JComponent srohama = new JSRohamaPrekryvnik();
		srohama.setLayout(new CornerLayoutManager());
		// srohama.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 5));
		srohama.add(createDetailRoh(), ERoh.JZ);
		srohama.add(createKesInfoRoh(), ERoh.JV);

		final JPresCeleMysovani mysovani = new JPresCeleMysovani();
		{
			final JComponent smrizema = new JPresCelePrekryvnik();
			smrizema.setLayout(new OverlayLayout(smrizema));

			final JComponent renderSlide = new JRenderSlide();
			final JComponent zoomovaciObdelnik = new JZoomovaciObdelnik();
			final JComponent meritkovnik = new JMeritkoSlide();
			final JComponent mrizkaDdMmMmm = new JMrizkaDdMmMmm();
			final JComponent mrizkaDdMmSs = new JMrizkaDdMmSs();
			final JComponent mrizkaUtm = new JMrizkaUtm();
			// final JComponent mrizkaS42 = new JMrizkaS42(coord);
			// final JComponent mrizkaJtsk = new JMrizkaJTSK(coord);
			final JCestySlide cesty = new JCestySlide();

			final JComponent popisyKesiNaMape = new JPopiskySlide();
			final JComponent kesky = new JKesoidySlide(false);
			final JComponent pozicovnik = new JPozicovnikSlide();
			final JComponent zvyraznovaciKruhy = new JZvyraznovaciKruhySlide();
			final JComponent obsazenik = new JObsazenost();
			final JComponent kachlovnik = new JKachlovnikPresCele();
			final JComponent pozadi = new JSlidePozadi();

			smrizema.add(mysovani);
			smrizema.add(renderSlide);
			smrizema.add(zoomovaciObdelnik);
			smrizema.add(meritkovnik);
			smrizema.add(mrizkaDdMmMmm);
			smrizema.add(mrizkaDdMmSs);
			smrizema.add(mrizkaUtm);
			// smrizema.add(mrizkaS42);
			// smrizema.add(mrizkaJtsk);
			smrizema.add(cesty);
			smrizema.add(popisyKesiNaMape);
			smrizema.add(kesky);
			smrizema.add(pozicovnik);
			smrizema.add(zvyraznovaciKruhy);
			smrizema.add(obsazenik);
			smrizema.add(kachlovnik);
			smrizema.add(pozadi);

			mysovani.addKeyListener(mysovani);

			factory.init(mysovani);
			factory.init(renderSlide);
			factory.init(zoomovaciObdelnik);
			factory.init(meritkovnik);
			factory.init(mrizkaDdMmMmm);
			factory.init(mrizkaDdMmSs);
			factory.init(mrizkaUtm);
			// factory.init(mrizkaS42);
			// factory.init(mrizkaJtsk);
			factory.init(cesty);
			factory.init(popisyKesiNaMape);
			factory.init(kesky);
			factory.init(pozicovnik);
			factory.init(zvyraznovaciKruhy);
			factory.init(obsazenik);
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

			smrizema.add(mysovani, smrizema.getComponentCount());

			factory.init(smrizema);
			srohama.add(smrizema);
		}

		getContentPane().add(srohama);

		// Menu a toolbar
		setJMenuBar(menux.getMenuBar());
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
		if (!jeRozumneNaObrazovce(oknoStatus)) {
			// Implicitní veliksot a umístění
			final Toolkit toolkit = getToolkit();
			final Dimension screenSize = toolkit.getScreenSize();
			setSize(new Dimension(screenSize.width * 3 / 4, screenSize.height * 3 / 4));
			setLocation(new Point(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2));
			// event.getModel().setOknoUmisteni(oknoUmisteni); // a aktualizovat model
		} else {
			setLocation(oknoStatus.getPozice());
			setSize(oknoStatus.getVelikost());
		}
		setExtendedState(event.getStavOkna());
	}

	public void setFullScreen(final boolean fs) {
		// JMenuBar menubar = getJMenuBar();
		// if (fs) {
		// menubar.setVisible(false);
		// menubar.setPreferredSize(new Dimension(0, 0));
		// menubar.setVisible(true);
		// statusbar.setVisible(false);
		// toolBar.setVisible(false);
		// //setUndecorated(true);
		// } else {
		// menubar.setVisible(false);
		// menubar.setPreferredSize(null);
		// menubar.setVisible(true);
		// statusbar.setVisible(true);
		// toolBar.setVisible(true);
		// //setUndecorated(false);
		// }

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

	private boolean jeRozumneNaObrazovce(final OknoUmisteniDto u) {
		final Toolkit toolkit = getToolkit();
		final Dimension screenSize = toolkit.getScreenSize();
		final Dimension velikost = u.getVelikost();
		final Point pozice = u.getPozice();
		if (velikost.width < 200 || velikost.width >= screenSize.width) {
			return false; // moc uzke nebo siroke
		}
		if (velikost.height < 200 || velikost.height >= screenSize.height) {
			return false; // moc nizke nebo vysoke
		}
		if (pozice.x + velikost.width < 200) {
			return false; // prilis vlevo
		}
		if (screenSize.width - pozice.x < 200) {
			return false; // prilis vpravo
		}
		if (pozice.y < 0) {
			return false; // prilis nahore
		}
		if (screenSize.height - pozice.y < 200) {
			return false; // prilis dole
		}
		return true;

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
