package cz.geokuk.plugins.mapy.kachle;



import java.awt.Image;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import cz.geokuk.plugins.mapy.kachle.KachloDownloader.EPraznyObrazek;
import cz.geokuk.plugins.mapy.kachle.KachloStav.EFaze;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloNula;
import cz.geokuk.util.pocitadla.PocitadloRoste;



public class DotahovaciRunnable implements Runnable {

  private static Pocitadlo pocitPocetParalenichy = new PocitadloNula("Počet paralelně získávaných dlaždic",
  "Ty, které jsou ve třídě Synchronization, která zajišťuje, aby se nic nezískávalo vícekrát.");

  private static Pocitadlo pocitNectenychProtozePozadavekZastaral= new PocitadloRoste("Počet zastaralýh požadavků",
  "Počet požadavků, které dřív, než se stačili splnit zastaraly a už je nikdo nechtěl, takže nebyly plněny.");

  private static Pocitadlo pocitZiskananychNekymJinym = new PocitadloRoste("Počet získávaných někým jiným",
  "Počet dlaždic, u kterých se zjistilo, že je zrovna získává také jiné vlákno, tak se to na něm nechalo a nařídilo se mu přidat je všude tam, kde jsou potřeba");

  private KachleModel kachleModel;

  //private List<JKachle> kachliky = Collections.sy new ArrayList<JKachle>();
  //	private Stack<KachDvoj> stack = new Stack<KachDvoj>();


  // Zpracovavana fronta, null se sem nastavi, kdyz uz ja nebudu
  // nebo z venku nechci dal pracovat
  private final BlockingQueue<KaOneReq> queue;

  private final Pocitadlo pocitadloFrony;
  
  public DotahovaciRunnable(BlockingQueue<KaOneReq> queue, Pocitadlo pocitadloFrony) {
    this.queue = queue;
    this.pocitadloFrony = pocitadloFrony;
  }

  @Override
  public void run() {

    try {
      for(;;) {
        zapocitejFronty();
        KaOneReq kaOne = queue.take();
        zapocitejFronty();
        KaOne ka = kaOne.getKa();
        if (! kaOne.jesteToChceme()) {
          pocitNectenychProtozePozadavekZastaral.inc();
          continue; // pro nikoho stejne nepracuji
        }
        Image img = kachleModel.synchronizator.chciZiskat(ka, kaOne.getPosilac());
        EFaze faze;
        Throwable thr = null;
        if (img == null) { // v paměti to nebylo
          List<DlazebniPosilac> dlazebniPosilace;
          try {
            kaOne.getPosilac().zacinamNacitatZDisku();
            img = kachleModel.cache.diskCachedImage(ka);
            //Thread.sleep(2000);
            byte[] data = null;
            if (img == null) {
              if (ka.getType() == EKaType._BEZ_PODKLADU) {
                ImageWithData imda = kachleModel.kachloDownloader.prazdnyObrazekBezDat(EPraznyObrazek.BEZ_PODKLADU);
                img = imda.img;
                data = null;
                dlazebniPosilace = kachleModel.synchronizator.ziskalJsem(ka, img, null);
                faze = null;
              } else if (!kachleModel.isOnlineMode()) {
                dlazebniPosilace = kachleModel.synchronizator.nepovedloSe(ka);
                faze = EFaze.OFFLINE_MODE;
              } else { // stahujeme
                URL url = ka.getType().getUrlBuilder().buildUrl(ka);
                kaOne.getPosilac().zacinamStahovat(url);
                ImageWithData imda = kachleModel.kachloDownloader.downloadImage(url);
                //Thread.sleep(3000);
                img = imda == null ? null : imda.img;
                data = imda == null ? null : imda.data;
                dlazebniPosilace = kachleModel.synchronizator.ziskalJsem(ka, img, data);
                faze = null;
              }
            } else { // už jsem měl, nemusel jsem stahovat
              dlazebniPosilace = kachleModel.synchronizator.ziskalJsem(ka, img, data);
              faze = null;
            }
          } catch (Exception e) {
            dlazebniPosilace = kachleModel.synchronizator.nepovedloSe(ka);
            faze = EFaze.CHYBA;
            thr = e;
          }
          for (DlazebniPosilac posilac : dlazebniPosilace) {
            if (img != null) posilac.add(ka.getType(), img);
            if (faze != null) posilac.neziskano(ka.getType(), faze, thr);
          }
        } else if (img == Synchronizator.NEZISKAVEJ) {
          pocitZiskananychNekymJinym.inc();
          // když získává jiný, nedělám nic
          //System.out.println("Nebudu získávat");
        } else { // má ji už
          kaOne.getPosilac().add(ka.getType() , img);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Chyba pri dotahovani", e);
    }
  }


  public void zapocitejFronty() {
    pocitadloFrony.set(queue.size());
    pocitPocetParalenichy.set(kachleModel.synchronizator.pocetZiskavancu());
  }

  public void inject(KachleModel kachleModel) {
    this.kachleModel = kachleModel;
  }


}
