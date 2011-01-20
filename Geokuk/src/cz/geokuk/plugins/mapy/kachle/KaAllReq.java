/**
 * 
 */
package cz.geokuk.plugins.mapy.kachle;



public class KaAllReq extends Ka0Req {
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */

  private ImageReceiver imageReceiver;

  public KaAllReq(KaAll ka, ImageReceiver imageReceiver, Priorita priorita) {
    super(ka, priorita);
    this.imageReceiver = imageReceiver;
  }


  @Override
  public KaAll getKa() {
    return (KaAll) super.getKa();
  }

  /**
   * Zahodíme tento požadavek a už nebudeme dotahovat
   */
  public void uzToZahod() {
    imageReceiver = null;
  }


  public ImageReceiver getImageReceiver() {
    return imageReceiver;
  }


  @Override
  public boolean jesteToChceme() {
    return imageReceiver != null;
  }



}