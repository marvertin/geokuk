package cz.geokuk.plugins.mapy;


public enum EMapPodklad {
  BASE_N(4, 16, false),
  TURIST(4, 13, false),
  OPHOTO(4, 18, true),
  ARMY2(5, 13, true),
  OPHOTO0203(4, 16, true),
  ZADNE(4, 18, true);

  private final int minMoumer;
  private final int maxMoumer;
  private final boolean jeMoznyHybrid;

  private EMapPodklad(int minMoumer, int maxMoumer, boolean jeMoznyHybrid) {
    this.minMoumer = minMoumer;
    this.maxMoumer = maxMoumer;
    this.jeMoznyHybrid = jeMoznyHybrid;
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
