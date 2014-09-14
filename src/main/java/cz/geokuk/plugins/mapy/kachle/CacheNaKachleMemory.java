package cz.geokuk.plugins.mapy.kachle;



import java.awt.Image;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;
import cz.geokuk.util.pocitadla.PocitadloRoste;



/**
 * To je keš na kachlice
 * @author tatinek
 */
class CacheNaKachleMemory {

  private final Pocitadlo pocitMinutiPametoveKese = new PocitadloRoste("Počet minutí dlaždic v memcache: ",
      "Kolikrát se nepodařilo hledanou dlaždici v paměťové keši nalézt a co se dělo dál není tímto atributem určeno..");

  private final Pocitadlo pocitZasahPametoveKese = new PocitadloRoste("Počet zásahů dlaždic v memcache: ",
      "Kolikrát se podařilo hledanou dlaždici zasáhnout v paměti. Číslo stále roste a mělo by být ve srovnání s ostatními zásahy co největší.");

  private final Pocitadlo pocitVelikostPametoveKese = new PocitadloMalo("Počet dlaždic v memcache: ",
      "Počet závisí na velikosti pro Javu dostupné paměti (-Xmx) a měl by se ustálit na určité hodnotě, občas možná snížit, nikdy však nemůže být menší než počet dlaždic na mapě.");

  private final Pocitadlo pocitZametenePametoveKese = new PocitadloRoste("Počet zametených dlaždic v memcache: ",
      "Počet obrázků, které garbage collector zametrl pryč a my díky tomu odstranili referenci z keše.");


  private final ReferenceQueue<Image> referenceQueue = new ReferenceQueue<>();
  private final Map<Ka0, Item> cache = new HashMap<>();


  public CacheNaKachleMemory() {
  }


  public synchronized void putKachle(Ka0 klic, Image img) {
    Item item = new Item(img, referenceQueue);
    item.klic = klic;
    cache.put(klic, item);
    pocitVelikostPametoveKese.set(cache.size());
  }

  public synchronized Image memoryCachedImage(Ka0 klic) {
    Item item = cache.get(klic);
    if (item != null) {
      Image image = item.get();
      if (image != null) {
        pocitZasahPametoveKese.inc();
      } else { // nemá cenu to v keši nechávat
        cache.remove(klic);
        pocitVelikostPametoveKese.set(cache.size());
      }
      return image;
    }
    pocitMinutiPametoveKese.inc();
    return null;
  }


  private class Item extends WeakReference<Image> {

    private Ka0 klic;

    Item(Image referent, ReferenceQueue<? super Image> q) {
      super(referent, q);
    }

    Item(Image referent) {
      super(referent);
    }
  }


  { // Instanční incializátor. Pro každou instanci keše je likvidační vlákno.
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        for (;;) {
          try {
            Reference<? extends Image> ref = referenceQueue.remove();
            Item item = (Item)ref;
            synchronized (CacheNaKachleMemory.this) {
              Image image = memoryCachedImage(item.klic);
              if (image == null) {
                cache.remove(item.klic);
                pocitZametenePametoveKese.inc();
              }
            }
            pocitVelikostPametoveKese.set(cache.size());
          } catch (InterruptedException e) { // to se snad nestane
            FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Přerušení metodou interrupt(), taková výjimka asi nikdy nenastane.");
          }
        }

      }
    }, "Čistiš keše");
    thread.setDaemon(true);
    thread.start();
  }


  public synchronized void clearMemoryCache() {
    cache.clear();
  }

}
