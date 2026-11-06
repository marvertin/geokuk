package cz.geokuk.core.program;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.*;

import cz.geokuk.core.lookandfeel.LafSupport;
import cz.geokuk.core.napoveda.Diagnostika;
import cz.geokuk.plugins.kesoid.kind.kes.*;
import cz.geokuk.plugins.kesoid.mapicon.JMenuIkony;
import cz.geokuk.plugins.kesoid.mapicon.JMenuZvyrazneni;
import cz.geokuk.plugins.kesoid.mvc.JVybiracVyletu;
import cz.geokuk.plugins.kesoid.mvc.PrepniZdrojAction;
import cz.geokuk.plugins.mapy.MapyAction0;
import cz.geokuk.plugins.mapy.PodkladAction;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.mapy.kachle.gui.JTemaOfflineMapyMenu;
import cz.geokuk.plugins.refbody.RefbodyModel;
import cz.geokuk.util.gui.MenuStrujce;

public class Menu extends MenuStrujce {

	private Akce akce;
	private RefbodyModel refbodyModel;
	private KachleModel kachleModel;
	private final JMainFrame jMainFrame;

	public Menu(final JMainFrame jMainFrame, final JGeokukToolbar geokukToolbar) {
		super(new JMenuBar(), geokukToolbar);
		this.jMainFrame = jMainFrame;
	}

	public JToolBar getToolBar() {
		return tb;
	}

	public void inject(final Akce akce) {
		this.akce = akce;
	}

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	public void inject(final RefbodyModel refbodyModel) {
		this.refbodyModel = refbodyModel;
	}

	public void makeMapSubmenuPart(final Akce akce) {
		final ButtonGroup mapPodkladButtonGroup = new ButtonGroup();
		boolean uzivatelske = false;
		for (final MapyAction0 mapoakce1 : akce.mapoakce) {
			if (mapoakce1 instanceof PodkladAction) {
				if (!uzivatelske && ((PodkladAction) mapoakce1).getPodklad().isUzivatelska()) {
					uzivatelske = true;
					separator();
					hlavicka("Uživatelské mapy");
				}
				item(mapoakce1, mapPodkladButtonGroup);
			}
		}
		if (menu.getMenuComponentCount() > 0) {
			separator();
		}
		hlavicka("Přes mapu");
		item(akce.prekryvTrasAction);
		item(akce.reliefCuzkAction);

		separator();
		item(akce.offlineMapyAction);
		menu.add(new JTemaOfflineMapyMenu(kachleModel));
		separator();
	}

	/** Neaktivní položka s nadpisem skupiny položek. */
	private void hlavicka(final String text) {
		final JMenuItem nadpis = new JMenuItem(text);
		nadpis.setEnabled(false);
		menu.add(nadpis);
	}

	/** Menu Soubor: práce se soubory, nastavením a údržbou dat programu. */
	void sestavMenuSoubor() {
		menu("Soubor", "Práce se soubory a nastaveními.");
		menu.setMnemonic(KeyEvent.VK_S);

		item(akce.informaceoZdrojichAction);
		item(akce.umisteniSouboruAction);
		item(akce.pruvodceMapAction);
		item(akce.optimalizaceDatabazeAction);
		separator();
		item(akce.nickEditAction);
		item(akce.tadyJsemDomaAction);
		separator();
		item(akce.pametProgramuAction);
		item(akce.zobrazeniProgramuAction);
		item(akce.vytvoritZastupceAction);
		item(akce.dalkoveOvladaniAction);
		separator();
		item(akce.zobrazServisniOknoAction);
		if (!FConst.ZAKAZAT_PRIPRAVOVANOU_FUNKCIONALITU) {
			separator();
			item(akce.renderAction);
		}

		separator();
		item(akce.fullScreenAction);
		separator();
		item(akce.closeAction);
	}

	/** Podnabídka Zdroje keší: zapnutí a vypnutí typů zdrojů klávesami Ctrl+číslo, na číselné klávesnici také. */
	private void sestavSubmenuZdroju() {
		final JMenu hlavni = menu;
		final JMenu zdroje = new JMenu("Zdroje keší");
		zdroje.setToolTipText("Zapne nebo vypne načítání typu zdroje; zkratky Ctrl+1 až Ctrl+4.");
		hlavni.add(zdroje);
		menu = zdroje;
		final PrepniZdrojAction[] vsechny = { akce.prepniZdrojGpxAction, akce.prepniZdrojGeogetAction, akce.prepniZdrojGsakAction, akce.prepniZdrojOpensakAction };
		for (final PrepniZdrojAction zdroj : vsechny) {
			item(zdroj);
		}
		PrepniZdrojAction.navazNumpad(jMainFrame.getRootPane(), vsechny);
		menu = hlavni;
	}

	private void sestavSubmenuChallenge() {
		final JMenu hlavni = menu;
		final JMenu challenge = new JMenu("Challenge");
		challenge.setToolTipText("Plánovač challenge: lišta na mapě, další a předchozí chybějící, jen kandidáti, kreslení sektorů a štítků.");
		hlavni.add(challenge);
		menu = challenge;
		item(akce.challengeZapnoutAction);
		separator();
		item(akce.challengeNastaveniAction);
		item(akce.challengeDalsiAction);
		item(akce.challengePredchoziAction);
		item(akce.challengeJenKandidatiAction);
		item(akce.challengeSektoryAction);
		item(akce.challengeStitkyAction);
		separator();
		item(akce.challengeSkokSever);
		item(akce.challengeSkokVychod);
		item(akce.challengeSkokJih);
		item(akce.challengeSkokZapad);
		menu = hlavni;
	}

	/** Menu Mapy: podklady, překryvy, správce offline map, pohyb po mapě a volby ukládání map. */
	void sestavMenuMapy() {
		// Ovládat mapy
		menu("Mapy", "Řízení zobrazení součástí map");
		menu.setMnemonic(KeyEvent.VK_M);

		makeMapSubmenuPart(akce);

		item(akce.priblizMapuAction);
		item(akce.oddalMapuAction);
		item(akce.nastavMapuCeskaAction);
		item(akce.zobrazVsechnyKeseAction);

		separator();
		item(akce.onlineModeAction);
		item(akce.ukladatMapyNaDiskAction);
		item(akce.kachleOflinerAction);
	}

	@Override
	protected void makeMenu() {

		sestavMenuSoubor();

		// Jít
		menu("Jít", "Přesun aktuálního bodu a mapy na zadané místo.");
		menu.setMnemonic(KeyEvent.VK_J);
		item(akce.bezNaSouradnice);
		item(akce.hledejKesAction);
		item(akce.geocodingAdrAction);
		item(akce.bezDomuAction);
		separator();
		item(akce.bezNaPoziciAction);
		item(akce.bezNaStredAction);
		separator();
		item(akce.bezNaZacatekCestyAction);
		item(akce.bezNaKonecCestyAction);
		item(akce.bezNaBodVpredAction);
		item(akce.bezNaBodVzadAction);
		separator();
		item(akce.souradniceDoClipboarduAction);

		// Body z GeoGetu se doplní, až se přečtou; menu se kvůli nim nestaví déle.
		final JMenu jit = menu;
		final int pozice = jit.getItemCount();
		refbodyModel.nactiNaPozadi(body -> {
			if (body.isEmpty()) {
				return;
			}
			jit.insertSeparator(pozice);
			for (int i = 0; i < body.size(); i++) {
				jit.insert(new JMenuItem(body.get(i)), pozice + 1 + i);
			}
		});

		separator();
		item(akce.posouvejSipkamiActionSEVER);
		item(akce.posouvejSipkamiActionVYCHOD);
		item(akce.posouvejSipkamiActionJIH);
		item(akce.posouvejSipkamiActionZAPAD);
		item(akce.posouvejSipkamiRychleSEVER);
		item(akce.posouvejSipkamiRychleVYCHOD);
		item(akce.posouvejSipkamiRychleJIH);
		item(akce.posouvejSipkamiRychleZAPAD);

		sestavMenuMapy();

		// Ovládání zobrazení keší
		menu("Kešoidy", "Řízení zobrazení keší a waypointů.");
		menu.setMnemonic(KeyEvent.VK_K);
		// ////////////////////////////// keškový filtr
		item(akce.kesoidyOnoffAction);
		item(akce.filtrIkonyAction);
		final javax.swing.JToggleButton kesoidyTlacitko = new javax.swing.JToggleButton(new cz.geokuk.plugins.kesoid.OkoKesoiduIkona());
		kesoidyTlacitko.setFocusable(false);
		akce.kesoidyOnoffAction.join(kesoidyTlacitko);
		kesoidyTlacitko.setHideActionText(true);
		kesoidyTlacitko.setIcon(new cz.geokuk.plugins.kesoid.OkoKesoiduIkona());
		tb.add(kesoidyTlacitko);
		tb.add(akce.filtrIkonyAction).setFocusable(false);
		tb.add(akce.renderAction).setFocusable(false);

		separator();
		final ButtonGroup zobrazeniKesi = new ButtonGroup();
		item(akce.zobrazeniKesiAutomatickyAction, zobrazeniKesi);
		item(akce.zobrazeniKesiIkonyAction, zobrazeniKesi);
		item(akce.zobrazeniKesiTeckyAction, zobrazeniKesi);
		item(akce.limityKresleniAction);
		menu.add(factory.init(new JMenuZvyrazneni()));
		separator();
		sestavSubmenuZdroju();
		separator();
		item(akce.popiskyOnoffAction);
		item(akce.popiskyNastavParametryAction);
		item(akce.popiskyOnAction);
		item(akce.popiskyOffAction);

		separator();
		tb.addSeparator();

		item(akce.jenDoTerenuUNenalezenychAction);
		item(akce.jenFinalUNalezenychAction);

		separator();
		item(akce.obsazenostOnoffAction);
		item(akce.kruhyOnoffAction);
		item(akce.jednotkoveKruhyAction);

		item(akce.nastavParametryZvyraznovacichKruhuAction);

		separator();
		sestavSubmenuChallenge();
		separator();
		item(akce.implicitniVyberZobrazenychKesi);
		item(akce.urlToClipboardForGeogetAction);
		item(akce.urlToListingForGeogetAction);
		separator();
		item(akce.prejdiNaKesoidZeSchrankyAction);

		/////////////////////////////////////////////////////
		JLabel lbl;

		final JVybiracHodnoceni jVybiracHodnoceni = factory.init(new JVybiracHodnoceni());
		tb.add(jVybiracHodnoceni);

		final JVybiracBestOf jVybiraceBestOf = factory.init(new JVybiracBestOf());
		tb.add(jVybiraceBestOf);

		final JVybiracFavorit jVybiraceFavorit = factory.init(new JVybiracFavorit());
		tb.add(jVybiraceFavorit);

		final JVybiracVyletu iVybiracVyletu = factory.init(new JVybiracVyletu());
		lbl = new JLabel("Zobrazit: ");
		lbl.setLabelFor(iVybiracVyletu);
		tb.add(lbl);
		tb.add(iVybiracVyletu);
		menu("Výlety", "Plánování výletů");
		menu.setMnemonic(KeyEvent.VK_V);
		item(akce.vyletAnoAction);
		item(akce.vyletNeAction);
		item(akce.vyletNevimAction);
		separator();
		item(akce.vyletZoomAction);
		item(akce.vyletPresClipboardDoGeogetuAction);
		separator();
		item(akce.vyletSmazAnoAction);
		item(akce.vyletSmazNeAction);

		menu("Cesty", "Plánování cest");
		menu.setMnemonic(KeyEvent.VK_C);
		item(akce.nactiAction);
		item(akce.importujAction);
		separator();
		item(akce.ulozAction);
		item(akce.ulozJakoAction);
		item(akce.ulozKopiiAction);
		item(akce.exportujDoGgtAction);
		item(akce.zavriAction);
		separator();
		item(akce.pridatDoCestyAction);
		item(akce.odebratZCestyAction);
		item(akce.obratitCestuAction);
		item(akce.uzavritCestuAction);
		item(akce.rozdelitCestuAction);
		item(akce.znovuSpojitCestyAction);
		item(akce.pospojovatVzdusneUsekyAction);
		item(akce.smazatCestuAction);
		separator(); // nad celým výletem
		item(akce.cestyZoomAction);
		item(akce.zoomovatCestuAction);
		item(akce.cestyPresClipboardDoGeogetuAction);
		item(akce.promazatJednobodoveAPrazdneCesty);
		item(akce.prepocitatUsekyPoCesteAction);
		separator();
		item(akce.panelCestyAction);

		menu("Mřížky", "Různé dekorace na mapě");
		menu.setMnemonic('Y');

		item(akce.meritkovnikAction);

		separator();

		item(akce.mrizkaDdAction);
		item(akce.mrizkaDdMmMmmAction);
		item(akce.mrizkaDdMmSsAction);

		item(akce.mrizkaUtmAction);
		// item(kontejner.mrizkaS42Action);
		// item(kontejner.mrizkaJTSKAction);

		separator();
		item(akce.zhasniVsechnyMrizkyAction);

		// Look&Feel
		getMenuBar().add(LafSupport.getLafMenu());
		///// Nápověda

		final JMenuIkony menuIkony = factory.init(new JMenuIkony());
		menuBar.add(menuIkony);
		menu = menuIkony;

		separator();
		item(akce.fenotypIkonyAction);
		separator();
		item(akce.refreshIkonAction);
		item(akce.debugIkonyAction);

		menu("Nápověda", "Nápověda, odkazy na web, kontrola aktualizací");
		menu.setMnemonic(KeyEvent.VK_N);
		item(akce.napovedaAction);
		item(akce.webovaStrankaAction);
		item(akce.coJeNovehoAction);
		separator();
		item(akce.zkontrolovatAktualizaceAction);
		item(akce.betaKanalAction);
		separator();
		item(akce.zadatProblemAction);
		item(akce.prehledProblemuAction);
		item(akce.diagnostikaAction);
		if (Diagnostika.betaKanal()) {
			item(akce.ukazatelVykonuAction);
		}
		separator();
		item(akce.oProgramuAction);

		menuBar.add(Box.createHorizontalGlue());
		final JButton verze = new JButton(akce.diagnostikaAction);
		verze.setIcon(null);
		verze.setForeground(new Color(0xC0, 0x50, 0x00));
		verze.setBorderPainted(false);
		verze.setContentAreaFilled(false);
		verze.setFocusable(false);
		nastavStitekVerze(verze);
		Diagnostika.poZmeneBetaKanalu(() -> SwingUtilities.invokeLater(() -> nastavStitekVerze(verze)));
		menuBar.add(verze);

		tb.addSeparator();
		tb.addOvladaceAlel();
		tb.add(Box.createHorizontalGlue());

	}

	static void nastavStitekVerze(final AbstractButton verze) {
		verze.setText(Diagnostika.popisVerze());
		verze.setVisible(Diagnostika.zobrazovatVerzi());
	}

}
