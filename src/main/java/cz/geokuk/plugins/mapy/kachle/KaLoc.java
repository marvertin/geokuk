package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.core.coordinates.Mou;


public class KaLoc {

  public static final int MOU_BITS = 32;
  public static final int KACHLE_BITS = 8;
  public static final int MAX_MOUMER = MOU_BITS - KACHLE_BITS;  // 24
  public static final int KACHLE_PIXELS = 1 << KACHLE_BITS; // 256
  public static final int KACHLE_MASKA  = KACHLE_PIXELS - 1; // 255

  private final int moumer;
  private final int ksx;
  private final int ksy;

  /**
   * Konstruuje lokaci kachle na základě snalosti souřadnic severozápadního rohu.
   * @param mouSZ
   * @param moumer
   * @return
   */
  public static KaLoc ofSZ(Mou mouSZ, int moumer) {
    if (moumer == 0) return new KaLoc(0, 0, moumer);
    return ofJZ(new Mou(mouSZ.xx, mouSZ.yy  - (1 << (MOU_BITS - moumer)) ), moumer);
  }

  /**
   * Konstruuje lokaci kachle na základě snalosti souřadnic jihozápadního rohu.
   * @param mouJZ
   * @param moumer
   * @return
   */
  public static KaLoc ofJZ(Mou mouJZ, int moumer) {
    if (moumer == 0) return new KaLoc(0, 0, moumer);
    int ksx = mouJZ.xx >> (MOU_BITS - moumer);
    int ksy = mouJZ.yy >> (MOU_BITS - moumer);
    return new KaLoc(ksx, ksy, moumer);
  }



  /**
   * Vrátí souřadnice JZ rohu.
   * 
   * Je to potřeba jen kvůli prioritnímu stahování od středu.
   * @return
   */
  public Mou getMouJZ() {
    return new Mou(ksx << (MOU_BITS - moumer), ksy << (MOU_BITS - moumer));
  }  

  /**
   * Vrátí souřadnice JZ rohu.
   * 
   * Je to potřeba jen kvůli prioritnímu stahování od středu.
   * @return
   */
  public Mou getMouSZ() {
    return getMouJZ().add(0, (1 << (MOU_BITS - moumer)));
  }  
  
  
  /**
   * @param mou Souřadnice levého horního (SZ) rohu kachle.
   * @param moumer měřítko
   */
  private KaLoc(int ksx, int ksy, int moumer) {
    this.ksx = ksx;
    this.ksy = ksy;
    this.moumer = moumer;
  }

  
  /**
   * Počet kachlí vodorovně nebo svisle pro aktuální měřítko.
   * Je t ovždy mocnina dvou
   * @return
   */
  public int getSize() {
    return 1 << moumer;
  }
  /**
   * Xsová souřadnice kachle. U vedlejších kachlí se souřadnice liší o jednu.
   * Jsou se zanménkkem, například pro moumer=3 jdou souřadnice -4 až 3.
   * Nula je kachle ležící svým JZ bodem na WGS=[0,0]
   * 
   * Kachle jdou zleva doprava a zespodu nahoru jako normální souřadnice.
   * Kachle ze [signedMapX, signedMapY] leží v africe
   * @return
   */
  public int getSignedX() {
    return ksx;
  }
  
  public int getSignedY() {
    return ksy;
  }
  
  /**
   * Konvertuje merkátorovu souřadnici na mapovou souřadnici
   * @param mersou
   * @return
   */
  public int getFromSzUnsignedX() {
    return getSize() / 2 + getSignedX();
  }

  /**
   * Konvertuje merkátorovu souřadnici na mapovou souřadnici
   * @param mersou
   * @return
   */
  public int getFromSzUnsignedY() {
    if (moumer == 0) return 0;
    return getSize() / 2 - 1 - getSignedY();
  }





  public int getMoumer() {
    return moumer;
  }

  private static int maskuj(int a, int bitu) {
    int m = (1 << bitu) - 1;
    return (a & (1 << (bitu - 1))) != 0 ? a | ~ m : a & m;  
    
  }
  
  public static void main(String[] args) {
    
    for (int i=-20; i< 20; i++) {
      
      System.out.println(i + " " + maskuj(i, 3));
      
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ksx;
    result = prime * result + ksy;
    result = prime * result + moumer;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    KaLoc other = (KaLoc) obj;
    if (ksx != other.ksx)
      return false;
    if (ksy != other.ksy)
      return false;
    if (moumer != other.moumer)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "KaLoc [moumer=" + moumer + ", ksx=" + ksx + ", ksy=" + ksy + "]";
  }
}
