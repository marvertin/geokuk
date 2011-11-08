package cz.geokuk.plugins.mapy.kachle;



import java.awt.Image;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.pocitadla.PocitadloNula;
import cz.geokuk.util.pocitadla.PocitadloRoste;



/**
 * To je keš na kachlice
 * @author tatinek
 */
class CacheNaKachleDisk {
  private static final int LIMIT_POZADAVKU_VE_FRONTE_PRO_NECEKANI = 20;
  private static final int MAXIMALNI_VELIKOST_FRONTY_ZAPISUJICI_NA_DISK = 100;

  private final PocitadloRoste pocitMinutiDiskoveKese = new PocitadloRoste("Počet minutí dlaždic v DISK cache",
      "Kolikrát se nepodařilo hledanou dlaždici v paměťové keši minout, co se dělo dál není tímto atributem určeno..");

  private final PocitadloRoste pocitZasahDiskoveKese = new PocitadloRoste("Počet zásahů dlaždic v DISK cache",
      "Kolikrát se podařilo hledanou dlaždici zasáhnout na disku, tedy naloadovat. Číslo stále roste a mělo by být ve srovnání s ostatními zásahy co největší.");

  private final PocitadloRoste pocitZapsanoNaDisk = new PocitadloRoste("Počet zapsaných dlaždic na disk",
      "Kolik dlaždic bylo úspěšně zapsáno na disk asynchronním zapisovačem.");

  private final PocitadloNula pocitVelikostZapisoveFronty = new PocitadloNula("Veliksot fronty pro zápisdlaždic na disk",
      "Kolik dlaždic je ještě ve frontě a čeká na zápis na disk.");


  private final KachleFileManager kfm;

  private final BlockingQueue<DiskSaveRequest> naZapsaniNaDisk = new LinkedBlockingQueue<DiskSaveRequest>(MAXIMALNI_VELIKOST_FRONTY_ZAPISUJICI_NA_DISK);



  private final CacheNaKachleMemory cache = new CacheNaKachleMemory();
  private final KachleModel kachleModel;


  public CacheNaKachleDisk(KachleModel kachleModel) {
    this.kachleModel = kachleModel;
    kfm = new KachleFileManager(kachleModel.getKachleCacheFolderHolder());
  }

  /**
   * Uloží obrázek do paměťové keše a nechá ho zapsat na disk.
   * A to i když tam už jsou.
   * Volající zodpovídá za to, že předaný image a dss
   * mají stejná data.
   * @param klic
   * @param img
   * @param dss Nemusí být, pak se neukládá.
   */
  public void putKachle(Ka0 klic, Image img, DiskSaveSpi dss) {
    if (img == null) return;
    cache.putKachle(klic, img);
    if (dss != null){
      DiskSaveRequest dsr = new DiskSaveRequest(klic, img, dss);
      try {
        naZapsaniNaDisk.put(dsr);
      } catch (InterruptedException e) {
        // Nevím, kdo by zabrejkoval, ale nic se nestane, když na disk nezapíšeme
        FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Tak na disk nezapíšeme, no");
      } // nebudeme blokovat, snad bude rychle zapisovat
      pocitVelikostZapisoveFronty.set(naZapsaniNaDisk.size());
    }

  }

  public void putKachle(Ka0 klic, Image img, byte[] data) {
    if (img == null) return; // v offline režimu
    putKachle(klic, img, data == null ? null : new DiskSaveByteArray(data));
  }

  //
  //
  //	public void scanDiskCache() {
  //		kfm.scanFolder(new KachloIdentListener() {
  //
  //			public void kachleExistsAsFile(KachloIdent ident) {
  //				putKachle(ident, null, null);
  //			}
  //		});
  //
  //	}

  public boolean isOnDiskOrMemory(Ka0 klic) {
    if (memoryCachedImage(klic) != null) return true;
    if (isOnDisk(klic)) return true;
    return false;
  }

  public boolean isOnDisk(Ka0 ki) {
    return kfm.exists(ki);
  }

  public Image memoryCachedImage(Ka0 klic) {
    Image img = cache.memoryCachedImage(klic);
    return img;
  }

  /**
   * Nahraje z disku kachli a strčí ji do paměťové keše.
   * Pokud však už byla v paměti, s diskem se nezatěžuje.
   * @param klic
   * @return
   */
  public Image diskCachedImage(Ka0 klic) {
    Image img = cache.memoryCachedImage(klic);
    if (img != null) return img;
    if (! kfm.exists(klic)) {
      pocitMinutiDiskoveKese.inc();
      return null;
    }
    img = kfm.load(klic);
    if (img != null) {
      pocitZasahDiskoveKese.inc();
      cache.putKachle(klic, img); // do paměťové keše vždy, když se vytáhne z disku
      //System.out.println("Loaded from disk: " + ki);
    } else {
      pocitMinutiDiskoveKese.inc();
      //System.err.println("KUKU diskCachedImage 3: " + ki);
      //				System.out.println("Nepovedlo se naloudovat: " + ki);
    }
    return img; // samozřejmě, že se

  }


  private class DiskovyZapisovac implements Runnable {

    @Override
    public void run() {
      for (;;) {
        try {
          DiskSaveRequest dsr = naZapsaniNaDisk.take();
          if (kachleModel.isUkladatMapyNaDisk()) {
            kfm.save(dsr.getKlic(), dsr.getUkladac());
          }
          pocitZapsanoNaDisk.inc();
          int size = naZapsaniNaDisk.size();
          pocitVelikostZapisoveFronty.set(size);
          if (size < LIMIT_POZADAVKU_VE_FRONTE_PRO_NECEKANI) {
            // čekáme, jen když toho máme málo, abychom zbytečně nebrzdili,
            // pokud fronta roste, musíme ukláadat.
            Thread.sleep(20);
          }
          //					System.out.println("Ukládám na disk: " + dsr);
        } catch (Throwable e) { // vlákno nesmí spadnout
          // toa by ani nemelo přijít, ale jinak jedeme dál
          FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Spadlo zapisování kachle do keše na disk");
          try {
            Thread.sleep(200);
          } catch (InterruptedException e1) {  // no, tak nás přerušili
            FExceptionDumper.dump(e1, EExceptionSeverity.WORKARROUND, "Přerušení k vláknu zapisující na disk");
          }
        }
      }

    }
  }

  {  // inicializace instance
    DiskovyZapisovac dz = new DiskovyZapisovac();
    Thread thread = new Thread(dz, "Diskovy zapisovac");
    thread.setPriority(Thread.NORM_PRIORITY - 1);
    thread.setDaemon(true);
    thread.start();
  }

  public void clearMemoryCache() {
    cache.clearMemoryCache();
  }



}
