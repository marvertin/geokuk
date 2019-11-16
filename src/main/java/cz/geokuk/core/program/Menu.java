package cz.geokuk.core.program;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.*;

import cz.geokuk.core.lookandfeel.LafSupport;
import cz.geokuk.plugins.kesoid.mapicon.JMenuIkony;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.plugins.mapy.MapyAction0;
import cz.geokuk.plugins.mapy.PodkladAction;
import cz.geokuk.plugins.refbody.NaKonkretniBodAction;
import cz.geokuk.plugins.refbody.RefbodyModel;
import cz.geokuk.util.gui.MenuStrujce;

public class Menu extends MenuStrujce {

	private Akce akce;
	private RefbodyModel refbodyModel;
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

	public void inject(final RefbodyModel refbodyModel) {
		this.refbodyModel = refbodyModel;
	}

	public void makeMapSubmenuPart(final Akce akce) {
		final ButtonGroup mapPodkladButtonGroup = new ButtonGroup();
		for (final MapyAction0 mapoakce1 : akce.mapoakce) {
			if (mapoakce1 instanceof PodkladAction) {
				item(mapoakce1, mapPodkladButtonGroup);
			}
		}

		separator();

	}

	@Override
	protected void makeMenu() {

		menu("Soubor", "Práce se soubory a nastaveními.");
		menu.setMnemonic(KeyEvent.VK_S);

		item(akce.informaceoZdrojichAction);
		item(akce.umisteniSouboruAction);
		item(akce.nickEditAction);
		item(akce.ulozitNastaveniKProgramuAction);
		item(akce.tadyJsemDomaAction);
		item(akce.zobrazServisniOknoAction);
		if (!FConst.ZAKAZAT_PRIPRAVOVANOU_FUNKCIONALITU) {
			item(akce.renderAction);
		}

		separator();
		item(akce.fullScreenAction);
		separator();
		item(akce.closeAction);

		// Jít
		menu("Jít", "Přesun aktuálního bodu a mapy na zadané místo.");
		menu.setMnemonic(KeyEvent.VK_J);
		item(akce.bezNaSouradnice);
		item(akce.bezDomuAction);
		item(akce.hledejKesAction);
		item(akce.geocodingAdrAction);
		item(akce.bezNaPoziciAction);
		item(akce.bezNaStredAction);
		item(akce.bezNaZacatekCestyAction);
		item(akce.bezNaKonecCestyAction);
		item(akce.bezNaBodVpredAction);
		item(akce.bezNaBodVzadAction);
		item(akce.souradniceDoClipboarduAction);

		final List<NaKonkretniBodAction> geoHomeBodyAction = refbodyModel.nacti();
		if (geoHomeBodyAction.size() > 0) {
			separator();
			for (final Action a : geoHomeBodyAction) {
				item(a);
			}
		}

		separator();
		item(akce.posouvejSipkamiActionSEVER);
		item(akce.posouvejSipkamiActionVYCHOD);
		item(akce.posouvejSipkamiActionJIH);
		item(akce.posouvejSipkamiActionZAPAD);
		item(akce.posouvejSipkamiRychleSEVER);
		item(akce.posouvejSipkamiRychleVYCHOD);
		item(akce.posouvejSipkamiRychleJIH);
		item(akce.posouvejSipkamiRychleZAPAD);

		// Ovládat mapy
		menu("Mapy", "Řízení zobrazení součástí map");
		menu.setMnemonic(KeyEvent.VK_M);

		makeMapSubmenuPart(akce);

		separator();
		item(akce.priblizMapuAction);
		item(akce.oddalMapuAction);
		item(akce.nastavMapuCeskaAction);

		separator();
		item(akce.onlineModeAction);
		item(akce.ukladatMapyNaDiskAction);
		item(akce.kachleOflinerAction);

		// Ovládání zobrazení keší
		menu("Kešoidy", "Řízení zobrazení keší a waypointů.");
		menu.setMnemonic(KeyEvent.VK_K);
		// ////////////////////////////// keškový filtr
		item(akce.filtrIkonyAction);
		tb.add(akce.filtrIkonyAction);
		tb.add(akce.renderAction);

		item(akce.kesoidyOnoffAction);
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

		item(akce.nastavParametryZvyraznovacichKruhuAction);

		item(akce.jednotkoveKruhyAction);

		separator();
		item(akce.implicitniVyberZobrazenychKesi);
		item(akce.urlToClipboardForGeogetAction);
		item(akce.urlToListingForGeogetAction);

		/////////////////////////////////////////////////////
		JLabel lbl;

		final JVybiracHodnoceni jVybiracHodnoceni = factory.init(new JVybiracHodnoceni());
		tb.add(jVybiracHodnoceni);

		final JVybiracBestOf jVybiraceBestOf = factory.init(new JVybiracBestOf());
		tb.add(jVybiraceBestOf);

		final JVybiracFavorit jVybiraceFavorit = factory.init(new JVybiracFavorit());
		tb.add(jVybiraceFavorit);

		final JVybiracVyletu iVybiracVyletu = factory.init(new JVybiracVyletu());
		lbl = new JLabel("Výlet: ");
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
		menu.setMnemonic(KeyEvent.VK_V);
		item(akce.pridatDoCestyAction);
		item(akce.odebratZCestyAction);
		separator(); // nad celým výletem
		item(akce.cestyZoomAction);
		item(akce.zoomovatCestuAction);
		item(akce.cestyPresClipboardDoGeogetuAction);
		item(akce.promazatJednobodoveAPrazdneCesty);
		separator();
		item(akce.nactiAction);
		item(akce.importujAction);
		item(akce.ulozAction);
		item(akce.ulozJakoAction);
		item(akce.ulozKopiiAction);
		item(akce.zavriAction);
		item(akce.exportujDoGgtAction);
		separator();
		item(akce.obratitCestuAction);
		item(akce.smazatCestuAction);
		item(akce.uzavritCestuAction);
		item(akce.pospojovatVzdusneUsekyAction);
		separator();
		item(akce.rozdelitCestuAction);
		item(akce.znovuSpojitCestyAction);

		menu("Mřížky", "Různo dekorace na mapě");
		menu.setMnemonic('Y');

		item(akce.meritkovnikAction);

		separator();

		item(akce.mrizkaDdMmMmmAction);
		item(akce.mrizkaDdMmSsAction);

		item(akce.mrizkaUtmAction);
		// item(kontejner.mrizkaS42Action);
		// item(kontejner.mrizkaJTSKAction);

		separator();
		item(akce.zhasniVsechnyMrizkyAction);

		// Look&Feel
		getMenuBar().add(LafSupport.getLafMenu());
		LafSupport.setFrame(jMainFrame);
		///// Nápověda

		final JMenuIkony menuIkony = factory.init(new JMenuIkony());
		menuBar.add(menuIkony);
		menu = menuIkony;

		separator();
		item(akce.fenotypIkonyAction);
		separator();
		item(akce.refreshIkonAction);
		item(akce.debugIkonyAction);

		menu("Nápověda", "Nápověda, odkazy na web, kotnrola aktualiozací");
		menu.setMnemonic(KeyEvent.VK_N);
		item(akce.napovedaAction);
		item(akce.webovaStrankaAction);
		item(akce.zadatProblemAction);
		item(akce.zkontrolovatAktualizaceAction);
		item(akce.zpravyUzivatelumAction);
		separator();
		item(akce.oProgramuAction);

		tb.addSeparator();
		tb.addOvladaceAlel();
		tb.add(Box.createHorizontalGlue());

	}

}
