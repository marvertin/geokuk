package cz.geokuk.plugins.mapy.kachle;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import cz.geokuk.plugins.mapy.kachle.KachloStav.EFaze;

class DlazebniPosilac {

  private DlazebniKombiner kombiner;
  private final ImageReceiver imgrcv;


  public DlazebniPosilac(DlazebniKombiner kombiner, ImageReceiver imgrcv) {
    super();
    this.kombiner = kombiner;
    this.imgrcv = imgrcv;
  }


  public void add(EKaType type, Image img) {
    if (img == null || kombiner == null) {
        return;
    }
    kombiner.add(type, img);
    BufferedImage kombinedImage = kombiner.getKombinedImage();
    boolean hotovo = kombiner.isHotovo();
    if (kombinedImage != null) {
      // Jen pokud už se něco podařilo nakombinovat budu někam posílat.
      // NULL je zde tehdy, když přišly nejdříve dekorace jako turistické trasy či stíny, ale ještě není podklad
      imgrcv.setImage(new KachloStav(kombiner.getCoMam(), kombinedImage, hotovo ? EFaze.STAZENA_POSLEDNI_KACHLE : EFaze.STAZENA_PRUBEZNA_KACHLE));
    }
    if (hotovo) {
      this.kombiner.uzTeNepotrebuju();
      this.kombiner = null; // uvolnit paměť
    }
  }


  public void zacinamStahovat(URL url) {
    imgrcv.setImage(new KachloStav(null, null, EFaze.ZACINAM_STAHOVAT, url, null));
  }


  public void zacinamNacitatZDisku() {
    imgrcv.setImage(new KachloStav(null, null, EFaze.ZACINAM_NACITAT_Z_DISKU));
  }


  public void neziskano(EKaType type, EFaze faze, Throwable e) {
    imgrcv.setImage(new KachloStav(null, null, faze, null, e));
    
  }

}
