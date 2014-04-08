package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;


abstract class Ka0Req  {

  private static Pocitadlo pocitadloInstanci = new PocitadloMalo("Počet instancí požadavků na dlaždice.",
      "Počítá, kolik existuje instancí " + Ka0Req.class.getName() + ".");

  private final Ka0 ka;
  private int vzdalenostOdStredu;

  private final Priorita priorita;


  protected Ka0Req(Ka0 ka, Priorita priorita) {
    super();
    this.ka = ka;
    this.priorita = priorita;
    pocitadloInstanci.inc();
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    pocitadloInstanci.dec();
  }


  public Ka0 getKa() {
    return ka;
  }

  public abstract boolean jesteToChceme();

  public int getVzdalenostOdStredu() {
    return vzdalenostOdStredu;
  }

  public void setVzdalenostOdStredu(int vzdalenostOdStredu) {
    this.vzdalenostOdStredu = vzdalenostOdStredu;
  }

  public Priorita getPriorita() {
    return priorita;
  }

}
