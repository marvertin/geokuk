package cz.geokuk.plugins.cesty.akce.bod;

import java.awt.event.ActionEvent;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.Poziceq;
import cz.geokuk.plugins.cesty.akce.BousekAction0;
import cz.geokuk.plugins.cesty.data.Bod;

public abstract class BodAction0 extends BousekAction0 {

  private static final long serialVersionUID = -7263554527636792222L;
  protected Poziceq poziceq;

  private final Bod kontextovyBod;

  public BodAction0(Bod kontextovyBod) {
    this.kontextovyBod = kontextovyBod;
  }

  @Override
  protected final void vyletChanged() {
    if (poziceq == null) return; // ještě je čas
    vyhodnotitPovolenost();
  }

  public final void onEvent(PoziceChangedEvent aEvent) {
    poziceq = aEvent.poziceq;
    vyhodnotitPovolenost();
  }

  private void vyhodnotitPovolenost() {
    Bod bod = getBod();
    if (bod == null) {
      setEnabled(false);
    } else {
      if (mamPovolitProBod(bod)) {
        setEnabled(true);
        nastavJmenoAkce(bod, kontextovyBod != null);
      } else {
        setEnabled(false);
      }
    }
  }

  @Override
  public final void actionPerformed(ActionEvent aE) {
    Bod bod = getBod();
    if (bod != null) {
      provedProBod(bod);
    }
  }

  /**
   * Pokud je daný bod, tak se nechá natavit jméno. Natavovací metoda také
   * ví, zda je akce z kontexotvého menu nebo z hlavního
   * @param bod
   * @param aZKontextovehoMenu
   */
  protected abstract void nastavJmenoAkce(Bod bod, boolean aZKontextovehoMenu);

  /**
   * Zda se pro dany bod ma akce povolit.
   * @param bod Nikdy nebude predano null
   * @return
   */
  protected abstract boolean mamPovolitProBod(Bod bod);

  /**
   * Provedení akce pro daný bod.
   * @param bod
   */
  protected abstract void provedProBod(Bod bod);

  protected Bod getBod() {
    if (kontextovyBod != null)
      return kontextovyBod;
    else
      return poziceq.getBod();
  }


}
