/**
 * 
 */
package cz.geokuk.plugins.kesoid.hledani;

import cz.geokuk.core.hledani.HledaciPodminka0;

/**
 * @author veverka
 *
 */
public class HledaciPodminka extends HledaciPodminka0 {

  private boolean regularniVyraz;
  private boolean jenVZobrazenych;
  /**
   * @return the regularniVyraz
   */
  public boolean isRegularniVyraz() {
    return regularniVyraz;
  }
  /**
   * @param aRegularniVyraz the regularniVyraz to set
   */
  public void setRegularniVyraz(boolean aRegularniVyraz) {
    regularniVyraz = aRegularniVyraz;
  }
	public void setJenVZobrazenych(boolean selected) {
		jenVZobrazenych = selected;
  }
	
  public boolean isJenVZobrazenych() {
  	return jenVZobrazenych;
  }

}
