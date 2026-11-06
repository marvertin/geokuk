package cz.geokuk.core.program;

import java.lang.reflect.Field;
import java.util.*;

import javax.swing.Action;
import javax.swing.KeyStroke;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.ESmer;
import cz.geokuk.core.napoveda.*;
import cz.geokuk.core.ovladani.DalkoveOvladaniAction;
import cz.geokuk.core.render.RenderAction;
import cz.geokuk.framework.NapovedaAction;
import cz.geokuk.plugins.cesty.akce.*;
import cz.geokuk.plugins.cesty.akce.bod.RozdelitCestuVBoduAction;
import cz.geokuk.plugins.cesty.akce.bod.ZnovuSpojitCestyAction;
import cz.geokuk.plugins.cesty.akce.cesta.*;
import cz.geokuk.plugins.cesty.akce.doc.CestyZoomAction;
import cz.geokuk.plugins.cesty.akce.doc.PromazatJednobodoveAPrazdneCesty;
import cz.geokuk.plugins.cesty.akce.doc.PrepocitatUsekyPoCesteAction;
import cz.geokuk.plugins.cesty.akce.soubor.*;
import cz.geokuk.plugins.geocoding.GeocodingAdrAction;
import cz.geokuk.plugins.kesoid.EZobrazeniKesi;
import cz.geokuk.plugins.kesoid.filtr.FiltrIkonyAction;
import cz.geokuk.plugins.kesoid.importek.TypZdroje;
import cz.geokuk.plugins.kesoid.kind.kes.JenDoTerenuUNenalezenychAction;
import cz.geokuk.plugins.kesoid.kind.kes.JenFinalUNalezenychAction;
import cz.geokuk.plugins.kesoid.mapicon.*;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.plugins.kesoidkruhy.JednotkoveKruhyAction;
import cz.geokuk.plugins.kesoidkruhy.KruhyOnoffAction;
import cz.geokuk.plugins.kesoidkruhy.NastavParametryZvyraznovacichKruhuAction;
import cz.geokuk.plugins.kesoidobsazenost.ObsazenostOnoffAction;
import cz.geokuk.plugins.kesoidpopisky.*;
import cz.geokuk.plugins.mapy.MapyAction0;
import cz.geokuk.plugins.mapy.kachle.OnlineModeAction;
import cz.geokuk.plugins.mapy.kachle.PrekryvTrasAction;
import cz.geokuk.plugins.mapy.kachle.podklady.UkladatMapyNaDiskAction;
import cz.geokuk.plugins.mapy.stahovac.KachleOflinerAction;
import cz.geokuk.plugins.mrizky.*;
import cz.geokuk.plugins.refbody.BezDomuAction;
import cz.geokuk.plugins.refbody.TadyJsemDomaAction;
import cz.geokuk.plugins.vylety.*;

public class Akce {
	public final HledejKesAction hledejKesAction = new HledejKesAction();
	public final GeocodingAdrAction geocodingAdrAction = new GeocodingAdrAction();
	public final BezNaPoziciAction bezNaPoziciAction = new BezNaPoziciAction();
	public final BezNaStredAction bezNaStredAction = new BezNaStredAction();
	public final BezDomuAction bezDomuAction = new BezDomuAction();
	public final BezNaSouradniceAction bezNaSouradnice = new BezNaSouradniceAction();
	public final PopiskyOnAction popiskyOnAction = new PopiskyOnAction();
	public final PopiskyOffAction popiskyOffAction = new PopiskyOffAction();
	public final PopiskyNastavParametryAction popiskyNastavParametryAction = new PopiskyNastavParametryAction(null);
	public final PopiskyOnoffAction popiskyOnoffAction = new PopiskyOnoffAction();
	public final PriblizMapuAction priblizMapuAction = new PriblizMapuAction();
	public final OddalMapuAction oddalMapuAction = new OddalMapuAction();

	public final CestyZoomAction cestyZoomAction = new CestyZoomAction(null);
	public final PridatDoCestyAction pridatDoCestyAction = new PridatDoCestyAction(null);
	public final OdebratZCestyAction odebratZCestyAction = new OdebratZCestyAction(null);
	public final CestyPresClipboardDoGeogetuAction cestyPresClipboardDoGeogetuAction = new CestyPresClipboardDoGeogetuAction();
	public final KruhyOnoffAction kruhyOnoffAction = new KruhyOnoffAction();
	public final ZobrazeniKesiAction zobrazeniKesiAutomatickyAction = new ZobrazeniKesiAction(EZobrazeniKesi.AUTOMATICKY, "Tečky při oddálení",
			"Na oddálené mapě (zoom 12 a menší) keše jako barevné tečky, při přiblížení ikony.");
	public final ZobrazeniKesiAction zobrazeniKesiIkonyAction = new ZobrazeniKesiAction(EZobrazeniKesi.IKONY, "Vždy ikony", "Keše vždy jako ikony.");
	public final ZobrazeniKesiAction zobrazeniKesiTeckyAction = new ZobrazeniKesiAction(EZobrazeniKesi.TECKY, "Vždy tečky", "Keše vždy jako barevné tečky.");
	public final LimityKresleniAction limityKresleniAction = new LimityKresleniAction();
	public final JednotkoveKruhyAction jednotkoveKruhyAction = new JednotkoveKruhyAction();
	public final NastavParametryZvyraznovacichKruhuAction nastavParametryZvyraznovacichKruhuAction = new NastavParametryZvyraznovacichKruhuAction();
	public final ObsazenostOnoffAction obsazenostOnoffAction = new ObsazenostOnoffAction();

	public final cz.geokuk.plugins.challenge.ChallengeNastaveniAction challengeNastaveniAction = new cz.geokuk.plugins.challenge.ChallengeNastaveniAction();
	public final cz.geokuk.plugins.challenge.ChallengeZapnoutAction challengeZapnoutAction = new cz.geokuk.plugins.challenge.ChallengeZapnoutAction();
	public final cz.geokuk.plugins.challenge.ChallengeDalsiAction challengeDalsiAction = new cz.geokuk.plugins.challenge.ChallengeDalsiAction();
	public final cz.geokuk.plugins.challenge.ChallengePredchoziAction challengePredchoziAction = new cz.geokuk.plugins.challenge.ChallengePredchoziAction();
	public final cz.geokuk.plugins.challenge.ChallengeJenKandidatiAction challengeJenKandidatiAction = new cz.geokuk.plugins.challenge.ChallengeJenKandidatiAction();

	public final MrizkaDdAction mrizkaDdAction = new MrizkaDdAction();
	public final MrizkaDdMmMmmAction mrizkaDdMmMmmAction = new MrizkaDdMmMmmAction();
	public final MrizkaDdMmSsAction mrizkaDdMmSsAction = new MrizkaDdMmSsAction();
	public final MrizkaS42Action mrizkaS42Action = new MrizkaS42Action();
	public final MrizkaJTSKAction mrizkaJTSKAction = new MrizkaJTSKAction();
	public final MrizkaUtmAction mrizkaUtmAction = new MrizkaUtmAction();

	public final MeritkovnikAction meritkovnikAction = new MeritkovnikAction();
	public final ZhasniVsechnyMrizkyAction zhasniVsechnyMrizkyAction = new ZhasniVsechnyMrizkyAction();
	public final KachleOflinerAction kachleOflinerAction = new KachleOflinerAction();

	public final ZkontrolovatAktualizaceAction zkontrolovatAktualizaceAction = new ZkontrolovatAktualizaceAction();

	public final UkladatMapyNaDiskAction ukladatMapyNaDiskAction = new UkladatMapyNaDiskAction();
	public final cz.geokuk.plugins.mapy.kachle.podklady.OptimalizaceDatabazeAction optimalizaceDatabazeAction = new cz.geokuk.plugins.mapy.kachle.podklady.OptimalizaceDatabazeAction();
	public final OnlineModeAction onlineModeAction = new OnlineModeAction();
	public final PrekryvTrasAction prekryvTrasAction = new PrekryvTrasAction();
	public final cz.geokuk.plugins.mapy.kachle.ReliefCuzkAction reliefCuzkAction = new cz.geokuk.plugins.mapy.kachle.ReliefCuzkAction();
	public final cz.geokuk.plugins.mapy.sprava.OfflineMapyAction offlineMapyAction = new cz.geokuk.plugins.mapy.sprava.OfflineMapyAction();
	public final cz.geokuk.plugins.mapy.pruvodce.PruvodceMapAction pruvodceMapAction = new cz.geokuk.plugins.mapy.pruvodce.PruvodceMapAction();
	public final TadyJsemDomaAction tadyJsemDomaAction = new TadyJsemDomaAction();

	public final ImplicitniVyberZobrazenychKesiAction implicitniVyberZobrazenychKesi = new ImplicitniVyberZobrazenychKesiAction();
	public final JenFinalUNalezenychAction jenFinalUNalezenychAction = new JenFinalUNalezenychAction();
	public final JenDoTerenuUNenalezenychAction jenDoTerenuUNenalezenychAction = new JenDoTerenuUNenalezenychAction();
	public final FiltrIkonyAction filtrIkonyAction = new FiltrIkonyAction();

	public final FenotypIkonyAction fenotypIkonyAction = new FenotypIkonyAction();

	public final ZobrazServisniOknoAction zobrazServisniOknoAction = new ZobrazServisniOknoAction();
	public final DalkoveOvladaniAction dalkoveOvladaniAction = new DalkoveOvladaniAction();
	public final VytvoritZastupceAction vytvoritZastupceAction = new VytvoritZastupceAction();
	public final PametProgramuAction pametProgramuAction = new PametProgramuAction();
	public final ZobrazeniProgramuAction zobrazeniProgramuAction = new ZobrazeniProgramuAction();
	public final BetaKanalAction betaKanalAction = new BetaKanalAction();
	public final UkazatelVykonuAction ukazatelVykonuAction = new UkazatelVykonuAction();

	public final RefreshIkonAction refreshIkonAction = new RefreshIkonAction();
	public final DebugIkonyAction debugIkonyAction = new DebugIkonyAction();

	public final UrlToClipboardForGeogetAction urlToClipboardForGeogetAction = new UrlToClipboardForGeogetAction(null);
	public final UrlToListingForGeogetAction urlToListingForGeogetAction = new UrlToListingForGeogetAction(null);


	public final WebovaStrankaAction webovaStrankaAction = new WebovaStrankaAction();
	public final OProgramuAction oProgramuAction = new OProgramuAction();
	public final CoJeNovehoAction coJeNovehoAction = new CoJeNovehoAction();
	public final FullScreenAction fullScreenAction = new FullScreenAction();
	public final NastavMapuCeskaAction nastavMapuCeskaAction = new NastavMapuCeskaAction();
	public final ZobrazVsechnyKeseAction zobrazVsechnyKeseAction = new ZobrazVsechnyKeseAction();
	public final CloseAction closeAction = new CloseAction();
	public final NastaveniAction nastaveniAction = new NastaveniAction();
	public final UmisteniSouboruAction umisteniSouboruAction = new UmisteniSouboruAction(null);

	public final NickEditAction nickEditAction = new NickEditAction();

	public final InformaceoZdrojichAction informaceoZdrojichAction = new InformaceoZdrojichAction();
	public final RenderAction renderAction = new RenderAction();

	public final KesoidyOnoffAction kesoidyOnoffAction = new KesoidyOnoffAction();
	public final PrepniZdrojAction prepniZdrojGpxAction = new PrepniZdrojAction(TypZdroje.GPX);
	public final PrepniZdrojAction prepniZdrojGeogetAction = new PrepniZdrojAction(TypZdroje.GEOGET);
	public final PrepniZdrojAction prepniZdrojGsakAction = new PrepniZdrojAction(TypZdroje.GSAK);
	public final PrepniZdrojAction prepniZdrojOpensakAction = new PrepniZdrojAction(TypZdroje.OPENSAK);
	public final PrejdiNaKesoidZeSchrankyAction prejdiNaKesoidZeSchrankyAction = new PrejdiNaKesoidZeSchrankyAction();

	public final NapovedaAction napovedaAction = new NapovedaAction(null);
	public final ZadatProblemAction zadatProblemAction = new ZadatProblemAction();
	public final PrehledProblemuAction prehledProblemuAction = new PrehledProblemuAction();
	public final DiagnostikaAction diagnostikaAction = new DiagnostikaAction();

	public final OtevriAction nactiAction = new OtevriAction();
	public final UlozAction ulozAction = new UlozAction();
	public final UlozJakoAction ulozJakoAction = new UlozJakoAction();
	public final UlozKopiiAction ulozKopiiAction = new UlozKopiiAction();
	public final ZavriAction zavriAction = new ZavriAction();
	public final ImportujAction importujAction = new ImportujAction();
	public final ExportujDoGgtAction exportujDoGgtAction = new ExportujDoGgtAction();

	public final BezNaZacatekCestyAction bezNaZacatekCestyAction = new BezNaZacatekCestyAction();
	public final BezNaKonecCestyAction bezNaKonecCestyAction = new BezNaKonecCestyAction();
	public final BezNaBodVpredAction bezNaBodVpredAction = new BezNaBodVpredAction();
	public final BezNaBodVzadAction bezNaBodVzadAction = new BezNaBodVzadAction();

	public final RozdelitCestuVBoduAction rozdelitCestuAction = new RozdelitCestuVBoduAction(null);
	public final ZnovuSpojitCestyAction znovuSpojitCestyAction = new ZnovuSpojitCestyAction(null);
	public final PospojovatVzdusneUseky pospojovatVzdusneUsekyAction = new PospojovatVzdusneUseky(null);

	public final ObratitCestuAction obratitCestuAction = new ObratitCestuAction(null);
	public final SmazatCestuAction smazatCestuAction = new SmazatCestuAction(null);
	public final UzavritCestuAction uzavritCestuAction = new UzavritCestuAction(null);
	public final ZoomovatCestuAction zoomovatCestuAction = new ZoomovatCestuAction(null);
	public final PromazatJednobodoveAPrazdneCesty promazatJednobodoveAPrazdneCesty = new PromazatJednobodoveAPrazdneCesty(null);
	public final PrepocitatUsekyPoCesteAction prepocitatUsekyPoCesteAction = new PrepocitatUsekyPoCesteAction(null);
	public final PanelCestyAction panelCestyAction = new PanelCestyAction();

	public final PosouvejSipkamiAction posouvejSipkamiActionSEVER = new PosouvejSipkamiAction(ESmer.SEVER, false);
	public final PosouvejSipkamiAction posouvejSipkamiActionVYCHOD = new PosouvejSipkamiAction(ESmer.VYCHOD, false);
	public final PosouvejSipkamiAction posouvejSipkamiActionJIH = new PosouvejSipkamiAction(ESmer.JIH, false);
	public final PosouvejSipkamiAction posouvejSipkamiActionZAPAD = new PosouvejSipkamiAction(ESmer.ZAPAD, false);

	public final PosouvejSipkamiAction posouvejSipkamiRychleSEVER = new PosouvejSipkamiAction(ESmer.SEVER, true);
	public final PosouvejSipkamiAction posouvejSipkamiRychleVYCHOD = new PosouvejSipkamiAction(ESmer.VYCHOD, true);
	public final PosouvejSipkamiAction posouvejSipkamiRychleJIH = new PosouvejSipkamiAction(ESmer.JIH, true);
	public final PosouvejSipkamiAction posouvejSipkamiRychleZAPAD = new PosouvejSipkamiAction(ESmer.ZAPAD, true);

	public final SouradniceDoClipboarduAction souradniceDoClipboarduAction = new SouradniceDoClipboarduAction(null);

	public final VyletZoomAction vyletZoomAction = new VyletZoomAction();
	public final VyletAnoAction vyletAnoAction = new VyletAnoAction(null);
	public final VyletNeAction vyletNeAction = new VyletNeAction(null);
	public final VyletNevimAction vyletNevimAction = new VyletNevimAction(null);
	public final VyletPresClipboardDoGeogetuAction vyletPresClipboardDoGeogetuAction = new VyletPresClipboardDoGeogetuAction();
	public final VyletSmazAnoAction vyletSmazAnoAction = new VyletSmazAnoAction();
	public final VyletSmazNeAction vyletSmazNeAction = new VyletSmazNeAction();

	public final List<MapyAction0> mapoakce = new LinkedList<>();

	/** Klávesové zkratky akcí a jejich názvy. */
	public Map<KeyStroke, String> zkratky() {
		final Map<KeyStroke, String> zkratky = new HashMap<>();
		for (final Field f : Akce.class.getFields()) {
			try {
				if (f.get(this) instanceof Action) {
					final Action a = (Action) f.get(this);
					if (a.getValue(Action.ACCELERATOR_KEY) != null) {
						zkratky.put((KeyStroke) a.getValue(Action.ACCELERATOR_KEY), "akce " + a.getValue(Action.NAME));
					}
				}
			} catch (final IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		for (final PrepniZdrojAction zdroj : new PrepniZdrojAction[] { prepniZdrojGpxAction, prepniZdrojGeogetAction, prepniZdrojGsakAction, prepniZdrojOpensakAction }) {
			final KeyStroke numpad = ZkratkyZdroju.zkratkaNumpad(zdroj.getTyp());
			if (numpad != null) {
				zkratky.put(numpad, "akce " + zdroj.getValue(Action.NAME));
			}
		}
		// Referenční body dostávají Alt+číslo postupně až při sestavení menu.
		for (int i = 1; i <= 9; i++) {
			zkratky.putIfAbsent(KeyStroke.getKeyStroke("alt " + i), "referenční bod");
		}
		return zkratky;
	}

}
