package cz.geokuk.plugins.mapy.kachle;


import java.util.EnumSet;

import cz.geokuk.plugins.mapy.EMapDekorace;
import cz.geokuk.plugins.mapy.EMapPodklad;


public enum EKaType {
  BASE_M(true, 0, 16, 16, "base-m"),
  TURIST_M(true, 0, 16, 16, "turist-m"),
  WTURIST_WINTER_M(true, 0, 16, 16, "wturist_winter-m"),
  TURIST_AQUATIC_M(true, 0, 16, 16, "wturist_aquatic-m"),
  OPHOTO_M(true, 0, 18, 16, "ophoto-m"),
  ARMY2_M(true, 0, 13, 13, "army2-m"),
  OPHOTO0203_M(true, 0, 16, 16, "ophoto0203-m"),
  OPHOTO0406_M(true, 0, 18, 16, "ophoto0406-m"),
  OPHOTO1012_M(true, 0, 18, 16, "ophoto1012-m"),
  ZEMEPIS_M(true, 0, 16, 16, "zemepis-m"),
  _BEZ_PODKLADU(true, 0, 18, 18, null),

  RELIEF_M(false, "hybrid-border-m"),
  TCYKLO_M(false, "hybrid-cyklo-m"),
  TTUR_M(false, "hybrid-tz-m"),
  HYBRID_M(false, "hybrid-m"),
  ;

  private final boolean podklad;
  private final int minMoumer;
  private final int maxMoumer;
  private final int maxAutoMoumer;
  private final String url_type;

  public boolean isPodklad() {
    return podklad;
  }

  private EKaType(final boolean podklad, final String url_type) {
    this.podklad = podklad;
    minMoumer = 3;
    maxMoumer = 18;
    maxAutoMoumer = 17;
    this.url_type = url_type;
  }

  private EKaType(final boolean podklad, final int minMoumer, final int maxMoumer, final int maxAutoMoumer, final String url_type) {
    this.podklad = podklad;
    this.minMoumer = minMoumer;
    this.maxMoumer = maxMoumer;
    this.maxAutoMoumer = maxAutoMoumer;
    this.url_type = url_type;
  }

  public void addToUrl(final StringBuilder sb) {
    if (this == _BEZ_PODKLADU) {
      throw new RuntimeException("není možné downloadovat žádný podklad");
    }
    sb.append(url_type);
  }

  public int getMinMoumer() {
    return minMoumer;
  }

  public int getMaxMoumer() {
    return maxMoumer;
  }

  public int fitMoumer(int moumer) {
    if (moumer < minMoumer) {
      moumer = minMoumer;
    }
    if (moumer > maxMoumer) {
      moumer = maxMoumer;
    }
    return moumer;
  }

  public static EnumSet<EKaType> compute(final EMapPodklad p, final EnumSet<EMapDekorace> d) {
    if (p == null) {
      return null;
    }
    final EnumSet<EKaType> set = EnumSet.noneOf(EKaType.class);
    switch (p) {
    case BASE_N:
      set.add(EKaType.BASE_M);
      break;
    case TURIST:
      set.add(EKaType.TURIST_M);
      break;
    case TURIST_WINTER:
      set.add(EKaType.WTURIST_WINTER_M);
      break;
    case TURIST_AQUATIC:
      set.add(EKaType.TURIST_AQUATIC_M);
      break;
    case OPHOTO:
      set.add(EKaType.OPHOTO_M);
      break;
    case ARMY2:
      set.add(EKaType.ARMY2_M);
      break;
    case OPHOTO0203:
      set.add(EKaType.OPHOTO0203_M);
      break;
    case OPHOTO0406:
      set.add(EKaType.OPHOTO0406_M);
      break;
    case OPHOTO1012:
      set.add(EKaType.OPHOTO1012_M);
      break;
    case ZEMEPIS:
      set.add(EKaType.ZEMEPIS_M);
      break;
    case ZADNE:
      set.add(EKaType._BEZ_PODKLADU);
      break;
      // ostatni jsou bez podkladu
    }
    for (final EMapDekorace dek : d) {
      switch (dek) {
      case TTUR:
        set.add(EKaType.TTUR_M);
        break;
      case HYBRID:
        if (p.isJeMoznyHybrid()) {
          set.add(EKaType.HYBRID_M);
        }
        break;
      case TCYKLO:
        set.add(EKaType.TCYKLO_M);
        break;
      case RELIEF:
        set.add(EKaType.RELIEF_M);
        break;
      }
    }
    return set;
  }

  public int getMaxAutoMoumer() {
    return maxAutoMoumer;
  }
}
