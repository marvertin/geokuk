package cz.geokuk.plugins.mapy.kachle;

import java.awt.Image;
import java.net.URL;
import java.util.EnumSet;

public class KachloStav {
  public final EnumSet<EKaType> types;
  public final Image img2;
  public final EFaze faze;
  public final URL url;
  public final Throwable thr;

  public KachloStav(EnumSet<EKaType> types, Image img2, EFaze faze) {
    this(types, img2, faze, null, null);
  }
  
  public KachloStav(EnumSet<EKaType> types, Image img2, EFaze faze, URL url, Throwable thr) {
    this.types = types;
    this.img2 = img2;
    this.faze = faze;
    this.url = url;
    this.thr = thr;
  }


  public static enum EFaze {
    ZACINAM_STAHOVAT,
    ZACINAM_NACITAT_Z_DISKU,
    STAZENA_PRUBEZNA_KACHLE,
    
    // tyto jsou konečné
    STAZENA_POSLEDNI_KACHLE,
    OFFLINE_MODE,
    CHYBA,
    
    
  }
}