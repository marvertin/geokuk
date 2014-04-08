package cz.geokuk.plugins.mapy;


public enum EMapPodklad {
  BASE_N(4, 16, false, false),
  TURIST(4, 13, false, false),
  OPHOTO(4, 18, true, true),
  ARMY2(5, 13, false, true),
  OPHOTO0203(4, 16, true, true),
  ZADNE(4, 18, true, true),

  ;

  private final int minMoumer;
  private final int maxMoumer;
  private final boolean reliefType;
  private final boolean jeMoznyHybrid;

  //  public EnumSet<EMapDekorace> getMozneDekorace() {
  //    EnumSet<EMapDekorace> dek = EnumSet.of(EMapDekorace.TCYKLO, EMapDekorace.TTUR, EMapDekorace.RELIEF);
  //    if (this == OPHOTO || this == OPHOTO0203) {
  //      dek.add(EMapDekorace.HYBRID);
  //    }
  //    return dek;
  //  }

  private EMapPodklad(int minMoumer, int maxMoumer, boolean hRelief, boolean jeMoznyHybrid) {
    this.minMoumer = minMoumer;
    this.maxMoumer = maxMoumer;
    reliefType = hRelief;
    this.jeMoznyHybrid = jeMoznyHybrid;
  }

  public boolean isHRelief() {
    return reliefType;
  }

  public boolean isLRelief() {
    return ! reliefType;
  }

  /**
   * @return the jeMoznyHybrid
   */
  public boolean isJeMoznyHybrid() {
    return jeMoznyHybrid;
  }


  public int getMinMoumer() {
    return minMoumer;
  }

  public int getMaxMoumer() {
    return maxMoumer;
  }


}
