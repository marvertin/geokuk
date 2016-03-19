/**
 *
 */
package cz.geokuk.plugins.mapy.kachle;


class KaOneReq extends Ka0Req  {

  public KaOneReq(final KaOne ka, final Priority priorita) {
    super(ka, priorita);
  }

  @Override
  public KaOne getKa() {
    return (KaOne)super.getKa();
  }



}