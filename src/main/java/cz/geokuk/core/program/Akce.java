package cz.geokuk.core.program;

import java.util.LinkedList;
import java.util.List;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.ESmer;
import cz.geokuk.core.napoveda.*;
import cz.geokuk.core.profile.UlozitNastaveniKProgramuAction;
import cz.geokuk.core.render.RenderAction;
import cz.geokuk.framework.NapovedaAction;
import cz.geokuk.plugins.cesty.akce.*;
import cz.geokuk.plugins.cesty.akce.bod.RozdelitCestuVBoduAction;
import cz.geokuk.plugins.cesty.akce.bod.ZnovuSpojitCestyAction;
import cz.geokuk.plugins.cesty.akce.cesta.*;
import cz.geokuk.plugins.cesty.akce.doc.CestyZoomAction;
import cz.geokuk.plugins.cesty.akce.doc.PromazatJednobodoveAPrazdneCesty;
import cz.geokuk.plugins.cesty.akce.soubor.*;
import cz.geokuk.plugins.geocoding.GeocodingAdrAction;
import cz.geokuk.plugins.kesoid.filtr.FiltrIkonyAction;
import cz.geokuk.plugins.kesoid.mapicon.*;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.plugins.kesoidkruhy.KruhyOnoffAction;
import cz.geokuk.plugins.kesoidkruhy.NastavParametryZvyraznovacichKruhuAction;
import cz.geokuk.plugins.kesoidobsazenost.ObsazenostOnoffAction;
import cz.geokuk.plugins.kesoidpopisky.*;
import cz.geokuk.plugins.mapy.MapyAction0;
import cz.geokuk.plugins.mapy.kachle.OnlineModeAction;
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
	public final JenDoTerenuUNenalezenychAction jednotkoveKruhyAction = new JenDoTerenuUNenalezenychAction();
	public final NastavParametryZvyraznovacichKruhuAction nastavParametryZvyraznovacichKruhuAction = new NastavParametryZvyraznovacichKruhuAction();
	public final ObsazenostOnoffAction obsazenostOnoffAction = new ObsazenostOnoffAction();

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
	public final OnlineModeAction onlineModeAction = new OnlineModeAction();
	public final TadyJsemDomaAction tadyJsemDomaAction = new TadyJsemDomaAction();

	public final ImplicitniVyberZobrazenychKesiAction implicitniVyberZobrazenychKesi = new ImplicitniVyberZobrazenychKesiAction();
	public final JenFinalUNalezenychAction jenFinalUNalezenychAction = new JenFinalUNalezenychAction();
	public final JenDoTerenuUNenalezenychAction jenDoTerenuUNenalezenychAction = new JenDoTerenuUNenalezenychAction();
	public final FiltrIkonyAction filtrIkonyAction = new FiltrIkonyAction();

	public final FenotypIkonyAction fenotypIkonyAction = new FenotypIkonyAction();

	public final ZobrazServisniOknoAction zobrazServisniOknoAction = new ZobrazServisniOknoAction();

	public final RefreshIkonAction refreshIkonAction = new RefreshIkonAction();
	public final DebugIkonyAction debugIkonyAction = new DebugIkonyAction();

	public final UrlToClipboardForGeogetAction urlToClipboardForGeogetAction = new UrlToClipboardForGeogetAction(null);
	public final UrlToListingForGeogetAction urlToListingForGeogetAction = new UrlToListingForGeogetAction(null);

	public final UlozitNastaveniKProgramuAction ulozitNastaveniKProgramuAction = new UlozitNastaveniKProgramuAction();

	public final WebovaStrankaAction webovaStrankaAction = new WebovaStrankaAction();
	public final OProgramuAction oProgramuAction = new OProgramuAction();
	public final ZpravyUzivatelumAction zpravyUzivatelumAction = new ZpravyUzivatelumAction();
	public final FullScreenAction fullScreenAction = new FullScreenAction();
	public final NastavMapuCeskaAction nastavMapuCeskaAction = new NastavMapuCeskaAction();
	public final CloseAction closeAction = new CloseAction();
	public final NastaveniAction nastaveniAction = new NastaveniAction();
	public final UmisteniSouboruAction umisteniSouboruAction = new UmisteniSouboruAction(null);

	public final NickEditAction nickEditAction = new NickEditAction();

	public final InformaceoZdrojichAction informaceoZdrojichAction = new InformaceoZdrojichAction();
	public final RenderAction renderAction = new RenderAction();

	public final KesoidyOnoffAction kesoidyOnoffAction = new KesoidyOnoffAction();

	public final NapovedaAction napovedaAction = new NapovedaAction(null);
	public final ZadatProblemAction zadatProblemAction = new ZadatProblemAction();

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

}
