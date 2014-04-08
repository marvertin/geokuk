/**
 * 
 */
package cz.geokuk.core.render;

import cz.geokuk.framework.Event0;

/**
 * @author veverka
 *
 */
public class RenderUmisteniSouboruChangedEvent extends Event0<RenderModel> {

  private final RenderUmisteniSouboru umisteniSouboru;

  /**
   * @param umisteniSouboru
   */
  public RenderUmisteniSouboruChangedEvent(RenderUmisteniSouboru umisteniSouboru) {
    super();
    this.umisteniSouboru = umisteniSouboru;
  }

  /**
   * @return the umisteniSouboru
   */
  public RenderUmisteniSouboru getUmisteniSouboru() {
    return umisteniSouboru;
  }


}
