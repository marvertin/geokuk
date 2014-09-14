package cz.geokuk.plugins.mapy.kachle;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Synchronizátor ma za úkol zajistit, že se více věcí nedownloaduje vícekrát.
 * @author tatinek
 *
 * @param <T>
 */
class Synchronizator<T> {

  public static final Image NEZISKAVEJ = new BufferedImage(1, 1, 1);

  private final Map<Ka0, List<T>> ziskavanci = new HashMap<>();

  private final CacheNaKachleDisk cache;

  public Synchronizator(CacheNaKachleDisk cache) {
    this.cache = cache;
  }

  /**
   * Chce získat obrázek pro danou kachli.
   * @param ki
   * @param proCo
   * @return null - nikdo nezískáá a není k dispozici, zapsal jsem tedy, že získávám já
   *         NEZISKAVEJ - už někdo jiný získává a ten se nakonec postará o to, aby se získanes spojil s tím proCo
   *         jiný -- není nutné tískávat, byl vrácen.
   *    Pokud je vráceno null, klient musí získat s nakonec zavolat ziskaljsem.
   */
  public synchronized Image chciZiskat(Ka0 ki, T proCo) {
    Image img = cache.memoryCachedImage(ki);
    if (img != null) return img; // máme ho v paměti, není co řešit
    List<T> list = ziskavanci.get(ki);
    if (list == null)  { // nikdo neziskava, budu ziskavat ja¨
      list = new ArrayList<>();
      ziskavanci.put(ki, list);
      img = null;
    } else {
      img = NEZISKAVEJ; // nekdo jiny ziskava, tak ja nebudu
    }
    list.add(proCo);
    //System.out.println("Ziskavanci chciZiskat: " + ziskavanci.size());
    return img;
  }


  public synchronized void nepovedloSe(Ka0 ki) {
    ziskavanci.remove(ki);
    System.out.println("Ziskavanci nepovedleSe: " + ziskavanci.size());
  }

  /**
   * Vrátí seznam těch, pro které se dlaždice získávala, aby bylo možné výsledek zpracovat.
   * @param ki
   * @param img
   * @param data
   * @return
   */
  public synchronized List<T> ziskalJsem(Ka0 ki, Image img, byte[] data) {
    cache.putKachle(ki, img, data);
    List<T> result = ziskavanci.remove(ki); // a jdu to zpracovavat
    //System.out.println("Ziskavanci ziskalJsem: " + ziskavanci.size());
    return result;
  }

  /**
   * @return
   */
  public synchronized int pocetZiskavancu() {
    return ziskavanci.size();
  }

}
