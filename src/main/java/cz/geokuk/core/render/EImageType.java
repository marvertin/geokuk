/**
 * 
 */
package cz.geokuk.core.render;

/**
 * @author veverka
 *
 */
public enum EImageType {
  bmp(false),
  png(true),
  jpg(false),
  ;

  private final boolean umoznujePruhlednost;

  /**
   * @return the umoznujePruhlednost
   */
  public boolean isUmoznujePruhlednost() {
    return umoznujePruhlednost;
  }

  /**
   * 
   */
  private EImageType(boolean umoznujePruhlednost) {
    this.umoznujePruhlednost = umoznujePruhlednost;
  }

  public String getType() {
    return name().toLowerCase();

  }
}
