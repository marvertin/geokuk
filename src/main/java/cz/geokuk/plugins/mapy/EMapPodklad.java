package cz.geokuk.plugins.mapy;


public enum EMapPodklad {
  BASE_N(false),
  TURIST(false),
  TURIST_WINTER(false),
  TURIST_AQUATIC(false),
  OPHOTO(true),
  ARMY2(true),
  OPHOTO0203(true),
  OPHOTO0406(true),
  OPHOTO1012(true),
  ZEMEPIS(false),
  ZADNE(true);

  private final boolean jeMoznyHybrid;

  private EMapPodklad(final boolean jeMoznyHybrid) {
    this.jeMoznyHybrid = jeMoznyHybrid;
  }

  /**
   * @return the jeMoznyHybrid
   */
  public boolean isJeMoznyHybrid() {
    return jeMoznyHybrid;
  }



}
