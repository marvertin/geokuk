package cz.geokuk.core.program;

import cz.geokuk.core.coord.BezNaPoziciAction;
import cz.geokuk.core.coord.BezNaSouradniceAction;
import cz.geokuk.core.coord.BezNaStredAction;
import cz.geokuk.core.coord.NastavMapuCeskaAction;
import cz.geokuk.core.coord.OddalMapuAction;
import cz.geokuk.core.coord.PosouvejSipkamiAction;
import cz.geokuk.core.coord.PriblizMapuAction;
import cz.geokuk.core.coordinates.ESmer;
import cz.geokuk.core.napoveda.OProgramuAction;
import cz.geokuk.core.napoveda.WebovaStrankaAction;
import cz.geokuk.core.napoveda.ZadatProblemAction;
import cz.geokuk.core.napoveda.ZkontrolovatAktualizaceAction;
import cz.geokuk.core.profile.UlozitNastaveniKProgramuAction;
import cz.geokuk.core.render.RenderAction;
import cz.geokuk.framework.NapovedaAction;
import cz.geokuk.plugins.geocoding.GeocodingAdrAction;
import cz.geokuk.plugins.kesoid.filtr.FiltrIkonyAction;
import cz.geokuk.plugins.kesoid.mapicon.DebugIkonyAction;
import cz.geokuk.plugins.kesoid.mapicon.FenotypIkonyAction;
import cz.geokuk.plugins.kesoid.mapicon.RefreshIkonAction;
import cz.geokuk.plugins.kesoid.mvc.HledejKesAction;
import cz.geokuk.plugins.kesoid.mvc.ImplicitniVyberZobrazenychKesiAction;
import cz.geokuk.plugins.kesoid.mvc.InformaceoZdrojichAction;
import cz.geokuk.plugins.kesoid.mvc.JenDoTerenuUNenalezenychAction;
import cz.geokuk.plugins.kesoid.mvc.JenFinalUNalezenychAction;
import cz.geokuk.plugins.kesoid.mvc.KesoidyOnoffAction;
import cz.geokuk.plugins.kesoid.mvc.NickEditAction;
import cz.geokuk.plugins.kesoid.mvc.UrlToClipboardForGeogetAction;
import cz.geokuk.plugins.kesoid.mvc.UrlToListingForGeogetAction;
import cz.geokuk.plugins.kesoidkruhy.KruhyOnoffAction;
import cz.geokuk.plugins.kesoidkruhy.NastavParametryZvyraznovacichKruhuAction;
import cz.geokuk.plugins.kesoidobsazenost.ObsazenostOnoffAction;
import cz.geokuk.plugins.kesoidpopisky.PopiskyNastavParametryAction;
import cz.geokuk.plugins.kesoidpopisky.PopiskyOffAction;
import cz.geokuk.plugins.kesoidpopisky.PopiskyOnAction;
import cz.geokuk.plugins.kesoidpopisky.PopiskyOnoffAction;
import cz.geokuk.plugins.mapy.Army2PodkladAction;
import cz.geokuk.plugins.mapy.BaseNPodkladAction;
import cz.geokuk.plugins.mapy.HybridDekoraceAction;
import cz.geokuk.plugins.mapy.Ophot0203PodkladAction;
import cz.geokuk.plugins.mapy.OphototPodkladAction;
import cz.geokuk.plugins.mapy.ReliefDekoraceAction;
import cz.geokuk.plugins.mapy.TcykloDekoraceAction;
import cz.geokuk.plugins.mapy.TturDekoraceAction;
import cz.geokuk.plugins.mapy.TuristPokladAction;
import cz.geokuk.plugins.mapy.ZadnePodkladAction;
import cz.geokuk.plugins.mapy.kachle.OnlineModeAction;
import cz.geokuk.plugins.mapy.kachle.UkladatMapyNaDiskAction;
import cz.geokuk.plugins.mapy.stahovac.KachleOflinerAction;
import cz.geokuk.plugins.mrizky.MeritkovnikAction;
import cz.geokuk.plugins.mrizky.MrizkaDdMmMmmAction;
import cz.geokuk.plugins.mrizky.MrizkaDdMmSsAction;
import cz.geokuk.plugins.mrizky.MrizkaJTSKAction;
import cz.geokuk.plugins.mrizky.MrizkaS42Action;
import cz.geokuk.plugins.mrizky.MrizkaUtmAction;
import cz.geokuk.plugins.mrizky.ZhasniVsechnyMrizkyAction;
import cz.geokuk.plugins.refbody.BezDomuAction;
import cz.geokuk.plugins.refbody.TadyJsemDomaAction;
import cz.geokuk.plugins.vylety.akce.BezNaBodVpredAction;
import cz.geokuk.plugins.vylety.akce.BezNaBodVzadAction;
import cz.geokuk.plugins.vylety.akce.BezNaKonecCestyAction;
import cz.geokuk.plugins.vylety.akce.BezNaZacatekCestyAction;
import cz.geokuk.plugins.vylety.akce.VyletAnoAction;
import cz.geokuk.plugins.vylety.akce.VyletNeAction;
import cz.geokuk.plugins.vylety.akce.VyletNevimAction;
import cz.geokuk.plugins.vylety.akce.VyletPresClipboardDoGeogetuAction;
import cz.geokuk.plugins.vylety.akce.VyletSmazAnoAction;
import cz.geokuk.plugins.vylety.akce.VyletSmazNeAction;
import cz.geokuk.plugins.vylety.akce.bod.RozdelitCestuVBoduAction;
import cz.geokuk.plugins.vylety.akce.bod.ZnovuSpojitCestyAction;
import cz.geokuk.plugins.vylety.akce.cesta.ObratitCestuAction;
import cz.geokuk.plugins.vylety.akce.cesta.PospojovatVzdusneUseky;
import cz.geokuk.plugins.vylety.akce.cesta.SmazatCestuAction;
import cz.geokuk.plugins.vylety.akce.cesta.UzavritCestuAction;
import cz.geokuk.plugins.vylety.akce.cesta.ZoomCestuAction;
import cz.geokuk.plugins.vylety.akce.doc.PromazatJednobodoveAPrazdneCesty;
import cz.geokuk.plugins.vylety.akce.doc.VyletZoomAction;
import cz.geokuk.plugins.vylety.akce.soubor.ExportujDoGgtAction;
import cz.geokuk.plugins.vylety.akce.soubor.ImportujAction;
import cz.geokuk.plugins.vylety.akce.soubor.OtevriAction;
import cz.geokuk.plugins.vylety.akce.soubor.UlozAction;
import cz.geokuk.plugins.vylety.akce.soubor.UlozJakoAction;
import cz.geokuk.plugins.vylety.akce.soubor.UlozKopiiAction;
import cz.geokuk.plugins.vylety.akce.soubor.ZavriAction;

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

  public final VyletZoomAction vyletZoomAction = new VyletZoomAction(null);
  public final VyletAnoAction vyletAnoAction = new VyletAnoAction(null);
  public final VyletNeAction vyletNeAction = new VyletNeAction(null);
  public final VyletNevimAction vyletNevimAction = new VyletNevimAction(null);
  public final VyletPresClipboardDoGeogetuAction vyletPresClipboardDoGeogetuAction = new VyletPresClipboardDoGeogetuAction();
  public final VyletSmazAnoAction vyletSmazAnoAction = new VyletSmazAnoAction();
  public final VyletSmazNeAction vyletSmazNeAction = new VyletSmazNeAction();
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

  public final BaseNPodkladAction baseNPodkladAction = new BaseNPodkladAction();
  public final TuristPokladAction turistPokladAction = new TuristPokladAction();
  public final OphototPodkladAction ophototPodkladAction = new OphototPodkladAction();
  public final Army2PodkladAction army2PodkladAction = new Army2PodkladAction();
  public final Ophot0203PodkladAction ophot0203PodkladAction = new Ophot0203PodkladAction();
  public final ZadnePodkladAction zadnePodkladAction = new ZadnePodkladAction();

  public final TturDekoraceAction tturDekoraceAction = new TturDekoraceAction();
  public final TcykloDekoraceAction tcykloDekoraceAction = new TcykloDekoraceAction();
  public final ReliefDekoraceAction reliefDekoraceAction = new ReliefDekoraceAction();
  public final HybridDekoraceAction hybridDekoraceAction = new HybridDekoraceAction();

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
  public final FullScreenAction fullScreenAction = new FullScreenAction();
  public final NastavMapuCeskaAction nastavMapuCeskaAction = new NastavMapuCeskaAction();
  public final CloseAction closeAction = new CloseAction();
  public final NastaveniAction nastaveniAction = new NastaveniAction();
  public final UmisteniSouboruAction umisteniSouboruAction = new UmisteniSouboruAction(null);

  public final NickEditAction nickEditAction = new NickEditAction();

  public final InformaceoZdrojichAction informaceoZdrojichAction = new InformaceoZdrojichAction();
  public final RenderAction renderAction = new RenderAction();

  public final KesoidyOnoffAction kesoidyOnoffAction = new KesoidyOnoffAction();

  public final NapovedaAction  napovedaAction = new NapovedaAction(null);
  public final ZadatProblemAction zadatProblemAction = new ZadatProblemAction();

  public final OtevriAction nactiAction = new OtevriAction();
  public final UlozAction ulozAction = new UlozAction();
  public final UlozJakoAction ulozJakoAction = new UlozJakoAction();
  public final UlozKopiiAction ulozKopiiAction = new UlozKopiiAction();
  public final ZavriAction zavriAction = new ZavriAction();
  public final ImportujAction importujAction = new ImportujAction();
  public final ExportujDoGgtAction exportujDoGgtAction = new ExportujDoGgtAction();

  public final BezNaZacatekCestyAction bezNaZacatekCestyAction = new BezNaZacatekCestyAction();
  public final BezNaKonecCestyAction    bezNaKonecCestyAction = new BezNaKonecCestyAction();
  public final BezNaBodVpredAction bezNaBodVpredAction = new BezNaBodVpredAction();
  public final BezNaBodVzadAction bezNaBodVzadAction = new BezNaBodVzadAction();

  public final RozdelitCestuVBoduAction rozdelitCestuAction = new RozdelitCestuVBoduAction(null);
  public final ZnovuSpojitCestyAction znovuSpojitCestyAction = new ZnovuSpojitCestyAction(null);
  public final PospojovatVzdusneUseky pospojovatVzdusneUsekyAction = new PospojovatVzdusneUseky(null);

  public final ObratitCestuAction obratitCestuAction = new ObratitCestuAction(null);
  public final SmazatCestuAction smazatCestuAction = new SmazatCestuAction(null);
  public final UzavritCestuAction uzavritCestuAction = new UzavritCestuAction(null);
  public final ZoomCestuAction zoomCestuAction = new ZoomCestuAction(null);
  public final PromazatJednobodoveAPrazdneCesty promazatJednobodoveAPrazdneCesty = new PromazatJednobodoveAPrazdneCesty(null);


  public final PosouvejSipkamiAction posouvejSipkamiActionSEVER = new PosouvejSipkamiAction(ESmer.SEVER, false);
  public final PosouvejSipkamiAction posouvejSipkamiActionVYCHOD = new PosouvejSipkamiAction(ESmer.VYCHOD, false);
  public final PosouvejSipkamiAction posouvejSipkamiActionJIH = new PosouvejSipkamiAction(ESmer.JIH, false);
  public final PosouvejSipkamiAction posouvejSipkamiActionZAPAD = new PosouvejSipkamiAction(ESmer.ZAPAD, false);

  public final PosouvejSipkamiAction posouvejSipkamiRychleSEVER = new PosouvejSipkamiAction(ESmer.SEVER, true);
  public final PosouvejSipkamiAction posouvejSipkamiRychleVYCHOD = new PosouvejSipkamiAction(ESmer.VYCHOD, true);
  public final PosouvejSipkamiAction posouvejSipkamiRychleJIH = new PosouvejSipkamiAction(ESmer.JIH, true);
  public final PosouvejSipkamiAction posouvejSipkamiRychleZAPAD = new PosouvejSipkamiAction(ESmer.ZAPAD, true);

}
