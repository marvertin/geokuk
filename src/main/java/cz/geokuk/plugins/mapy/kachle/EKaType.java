package cz.geokuk.plugins.mapy.kachle;


import java.util.EnumSet;

import cz.geokuk.plugins.mapy.EMapDekorace;
import cz.geokuk.plugins.mapy.EMapPodklad;



public enum EKaType {
  BASE_N(true, 4, 16, 16),
  TURIST(true, 4, 16, 16),
  OPHOTO(true, 4, 18, 16),
  ARMY2(true, 5, 13, 13),
  OPHOTO0203(true, 4, 16, 16),
  _BEZ_PODKLADU(true, 4, 18, 18),

  RELIEF_L(false),
  RELIEF_H(false),
  TCYKLO(false),
  TTUR(false),
  HYBRID(false),
  ;

  private final boolean podklad;
  private final int minMoumer;
  private final int maxMoumer;
  private final int maxAutoMoumer;

  public boolean isPodklad() {
    return podklad;
  }

  private EKaType(boolean podklad) {
    this.podklad = podklad;
    minMoumer = 3;
    maxMoumer = 18;
    maxAutoMoumer = 17;
  }

  private EKaType(boolean podklad, int minMoumer, int maxMoumer, int maxAutoMoumer) {
    this.podklad = podklad;
    this.minMoumer = minMoumer;
    this.maxMoumer = maxMoumer;
    this.maxAutoMoumer = maxAutoMoumer;
  }

  public void addToUrl(StringBuilder sb) {
    if (this == _BEZ_PODKLADU) {
      throw new RuntimeException("není možné downloadovat žádný podklad");
    }
    sb.append(name().toLowerCase().replace('_','-'));
  }

  public int getMinMoumer() {
    return minMoumer;
  }

  public int getMaxMoumer() {
    return maxMoumer;
  }

  public int fitMoumer(int moumer) {
    if (moumer < minMoumer) moumer = minMoumer;
    if (moumer > maxMoumer) moumer = maxMoumer;
    return moumer;
  }

  public static EnumSet<EKaType> compute(EMapPodklad p,  EnumSet<EMapDekorace> d) {
    //System.out.println("Tal co tu mame: " + p + " " + d);
    if (p == null) return null;
    EnumSet<EKaType> set = EnumSet.noneOf(EKaType.class);
    switch (p) {
    case BASE_N: set.add(EKaType.BASE_N); break;
    case TURIST: set.add(EKaType.TURIST); break;
    case OPHOTO: set.add(EKaType.OPHOTO); break;
    case ARMY2: set.add(EKaType.ARMY2); break;
    case OPHOTO0203: set.add(EKaType.OPHOTO0203); break;
    case ZADNE: set.add(EKaType._BEZ_PODKLADU); break;
    // ostatni jsou bez podkladu
    }
    for (EMapDekorace dek : d) {
      switch (dek) {
      case TTUR: set.add(EKaType.TTUR); break;
      case HYBRID: if (p.isJeMoznyHybrid()) set.add(EKaType.HYBRID);
      break;
      case TCYKLO: set.add(EKaType.TCYKLO); break;
      case RELIEF:
        if (p.isHRelief()) set.add(EKaType.RELIEF_H);
        if (p.isLRelief()) set.add(EKaType.RELIEF_L);
        break;
      }
    }
    return set;
  }

  public int getMaxAutoMoumer() {
    return maxAutoMoumer;
  }
}
