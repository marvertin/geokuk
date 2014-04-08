/**
 * 
 */
package cz.geokuk.plugins.mapy.kachle;




class KaOneReq extends Ka0Req implements Comparable<KaOneReq> {
  private final KaAllReq pricina;
  private final DlazebniPosilac dlazebniPosilac;


  public KaOneReq(KaOne ka, KaAllReq pricina, DlazebniPosilac dlazebniPosilac, Priorita priorita) {
    super(ka, priorita);
    this.pricina = pricina;
    this.dlazebniPosilac = dlazebniPosilac;
  }

  public int compareTo(KaOneReq kaReq) {
    KaOne ka1 = getKa();
    KaOne ka2 = kaReq.getKa();

    int prioritaDiff = getPriorita().ordinal() - getPriorita().ordinal();
    if (prioritaDiff != 0) return prioritaDiff;

    if (getPriorita() == Priorita.KACHLE) { // prioritní se dotahují dle podkladů a pak od středu
      int ordinalDiff = ka1.getType().ordinal() - ka2.getType().ordinal();
      if (ordinalDiff != 0) return ordinalDiff;

      int vzdalenostOdStreduDiff = getVzdalenostOdStredu() - kaReq.getVzdalenostOdStredu();
      if (vzdalenostOdStreduDiff != 0) return vzdalenostOdStreduDiff;
    } else { // neprioritní se dotahují napřed od středu a pak až dle podkladů
      int vzdalenostOdStreduDiff = getVzdalenostOdStredu() - kaReq.getVzdalenostOdStredu();
      if (vzdalenostOdStreduDiff != 0) return vzdalenostOdStreduDiff;

      int ordinalDiff = ka1.getType().ordinal() - ka2.getType().ordinal();
      if (ordinalDiff != 0) return ordinalDiff;
    }

    return 0;
  }

  @Override
  public KaOne getKa() {
    return (KaOne)super.getKa();
  }


  public KaAllReq getPricina() {
    return pricina;
  }

  public DlazebniPosilac getPosilac() {
    return dlazebniPosilac;
  }

  @Override
  public boolean jesteToChceme() {
    return pricina.jesteToChceme();
  }



}