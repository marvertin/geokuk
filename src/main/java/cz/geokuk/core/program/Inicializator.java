/**
 * 
 */
package cz.geokuk.core.program;

import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.hledani.HledaciSluzba;
import cz.geokuk.core.napoveda.NapovedaModel;
import cz.geokuk.core.profile.ProfileModel;
import cz.geokuk.core.render.RenderModel;
import cz.geokuk.framework.BeanBag;
import cz.geokuk.framework.EventManager;
import cz.geokuk.framework.Prefe;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.cesty.CestyModel;
import cz.geokuk.plugins.cesty.CestyNacitaniKesoiduWatchDog;
import cz.geokuk.plugins.cesty.CestyZperzistentnovac;
import cz.geokuk.plugins.geocoding.GeocodingModel;
import cz.geokuk.plugins.kesoid.KesFilter;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoidkruhy.KruhyModel;
import cz.geokuk.plugins.kesoidobsazenost.ObsazenostModel;
import cz.geokuk.plugins.kesoidpopisky.PopiskyModel;
import cz.geokuk.plugins.mapy.MapyModel;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.mapy.kachle.KachloDownloader;
import cz.geokuk.plugins.mrizky.MrizkaModel;
import cz.geokuk.plugins.refbody.HlidacReferencnihoBodu;
import cz.geokuk.plugins.refbody.RefbodyModel;
import cz.geokuk.plugins.vylety.VyletModel;
import cz.geokuk.plugins.vylety.VyletNacitaniKesoiduWatchDog;
import cz.geokuk.plugins.vylety.VyletovyZperzistentnovac;

/**
 * @author veverka
 *
 */
public class Inicializator {

  private final MainFrameHolder mainFrameHolder = new MainFrameHolder();
  private NapovedaModel napovedaModel;

  public void inicializace() {
    BeanBag bb = new BeanBag();
    bb.registerSigleton(bb);
    bb.registerSigleton(new EventManager());
    bb.registerSigleton(new Prefe());


    bb.registerSigleton(new CestyZperzistentnovac());
    bb.registerSigleton(new VyletovyZperzistentnovac());


    // modely
    bb.registerSigleton(new PoziceModel());
    bb.registerSigleton(new VyrezModel());
    bb.registerSigleton(new CestyModel());
    bb.registerSigleton(new VyletModel());
    bb.registerSigleton(new CestyNacitaniKesoiduWatchDog());
    bb.registerSigleton(new VyletNacitaniKesoiduWatchDog());
    bb.registerSigleton(new KesFilter());
    bb.registerSigleton(new PopiskyModel());
    bb.registerSigleton(new KruhyModel());
    bb.registerSigleton(new ObsazenostModel());

    bb.registerSigleton(new MrizkaModel("DdMmMmm", true));
    bb.registerSigleton(new MrizkaModel("DdMmSs", false));
    bb.registerSigleton(new MrizkaModel("Utm", false));
    bb.registerSigleton(new MrizkaModel("S42", false));
    bb.registerSigleton(new MrizkaModel("JTSK", false));
    bb.registerSigleton(new MrizkaModel("Meritkovnik", true));

    bb.registerSigleton(new RefbodyModel());
    bb.registerSigleton(new KachleModel());
    bb.registerSigleton(new KachloDownloader());

    bb.registerSigleton(new MapyModel());
    bb.registerSigleton(new KesoidModel());

    bb.registerSigleton(new HlidacReferencnihoBodu());
    bb.registerSigleton(new ProfileModel());
    bb.registerSigleton(new ProgressModel());
    bb.registerSigleton(new OknoModel());
    bb.registerSigleton(new RenderModel());
    napovedaModel = bb.registerSigleton(new NapovedaModel());
    bb.registerSigleton(new GeocodingModel());

    bb.registerSigleton(new HledaciSluzba());

    // akce
    Akce akce = new Akce();
    bb.registerSigleton(akce);
    bb.registrFieldsAsSingleton(akce);

    //
    bb.registerSigleton(mainFrameHolder);
    bb.init();
    //Board.eveman = eveman;
  }

  public void setMainFrame(JMainFrame frame) {
    mainFrameHolder.setMainFrame(frame);
  }

  public void zkontrolovatAktualizace() {
    napovedaModel.zkontrolujNoveAktualizace(false);
  }
}
