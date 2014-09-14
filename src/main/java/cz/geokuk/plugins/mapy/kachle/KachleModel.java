/**
 * 
 */
package cz.geokuk.plugins.mapy.kachle;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import cz.geokuk.core.napoveda.NapovedaModel;
import cz.geokuk.core.napoveda.NapovedaModelChangedEvent;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.Model0;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboru;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboruChangedEvent;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloNula;

/**
 * @author veverka
 *
 */
public class KachleModel extends Model0 {

  private Factory factory;

  public final CacheNaKachleDisk cache;

  private final KachleCacheFolderHolder kachleCacheFolderHolder = new KachleCacheFolderHolder();

  public KachloDownloader kachloDownloader;

  public final Synchronizator<DlazebniPosilac> synchronizator;

  public final BlockingQueue<KaOneReq> downLoadQueue = new PriorityBlockingQueue<>(100);

  public final BlockingQueue<KaOneReq> fromFileQueue = new PriorityBlockingQueue<>(100); // a nové fronty

  public final BlockingQueue<Ka0Req> rozrazovaciQueue = new LinkedBlockingQueue<>();

  public Pocitadlo pocitRozrazovaciQueue = new PocitadloNula("Velikost rozřazovací fronty",
      "Ve frontě čekají požadavky na zjištění, zda jsou kachle na disku a stačí je zvednout nebo musejí být downloadnuty." +
      " Frotna je zpracována velmi rychle, požadavky jsou rozřazeny do dalších dvou front");

  private Object umisteniSouboru;

  private NapovedaModel napovedaModel;



  //  public boolean onlineMode = true;
  //  public boolean ukladatMapyNaDisk = true;

  /**
   * @param bb
   */
  public KachleModel() {
    cache = new CacheNaKachleDisk(this);
    synchronizator = new Synchronizator<>(cache);
    assert kachleCacheFolderHolder != null;
  }

  boolean isOnlineMode() {
    return napovedaModel.isOnlineMode();
  }

  /**
   * @return the ukladatMapyNaDisk
   */
  public boolean isUkladatMapyNaDisk() {
    //   return Settings.vseobecne.ukladatMapyNaDisk.isSelected();
    boolean b = currPrefe().getBoolean("ukladatMapyNaDisk", true);
    return b;
  }


  /**
   * @param ukladatMapyNaDisk the ukladatMapyNaDisk to set
   */
  public void setUkladatMapyNaDisk(boolean ukladatMapyNaDisk) {
    if (ukladatMapyNaDisk == isUkladatMapyNaDisk()) return;
    currPrefe().putBoolean("ukladatMapyNaDisk", ukladatMapyNaDisk);
    fire(new KachleModelChangeEvent());
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.Model0#initAndFire()
   */
  @Override
  protected void initAndFire() {
    setUmisteniSouboru(loadUmisteniSouboru());
    fire(new KachleModelChangeEvent());
    {
      Thread thread = new Thread(factory.init(new Rozrazovac()), "Rozřazovač");
      thread.setDaemon(true);
      thread.start();
    }
    {
      DotahovaciRunnable dotah = factory.init(new DotahovaciRunnable(fromFileQueue, "x"));
      Thread thread = new Thread(dotah, "Čteč z disku");
      thread.setDaemon(true);
      thread.start();
    }
    for (String server : kachloDownloader.getServers()) {
      DotahovaciRunnable dotah = factory.init(new DotahovaciRunnable(downLoadQueue, server));
      Thread thread = new Thread(dotah, "Download " + server);
      thread.setDaemon(true);
      thread.start();
    }

  }

  @Override
  public void inject(Factory factory) {
    this.factory = factory;

  }
  /**
   * @param umisteniSouboru the umisteniSouboru to set
   */
  public void setUmisteniSouboru(KachleUmisteniSouboru umisteniSouboru) {
    if (umisteniSouboru.equals(this.umisteniSouboru)) return;
    this.umisteniSouboru = umisteniSouboru;
    kachleCacheFolderHolder.setKachleCacheDir(umisteniSouboru.getKachleCacheDir());
    MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
    pref.putFilex(FPref.KACHLE_CACHE_DIR_value, umisteniSouboru.getKachleCacheDir());
    pref.remove("vyjimkyDir"); // mazat ze starych verzi
    fire(new KachleUmisteniSouboruChangedEvent(umisteniSouboru));
  }

  private KachleUmisteniSouboru loadUmisteniSouboru() {
    KachleUmisteniSouboru u = new KachleUmisteniSouboru();
    MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
    u.setKachleCacheDir ( pref.getFilex("kachleCacheDir", KachleUmisteniSouboru.KACHLE_CACHE_DIR));
    return u;
  }

  /**
   * @return the kachleCacheFolderHolder
   */
  public KachleCacheFolderHolder getKachleCacheFolderHolder() {
    assert kachleCacheFolderHolder != null;
    return kachleCacheFolderHolder;
  }

  public void inject(KachloDownloader kachloDownloader) {
    this.kachloDownloader = kachloDownloader;
  }

  public void inject(NapovedaModel napovedaModel) {
    this.napovedaModel = napovedaModel;
  }


  public void onEvent(NapovedaModelChangedEvent eve) {
    if (eve.getModel().isOnlineMode()) {
      cache.clearMemoryCache();
    }
  }
}
