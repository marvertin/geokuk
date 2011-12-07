package cz.geokuk.plugins.cesty.akce;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import cz.geokuk.framework.Dlg;


public class CestySmazAnoAction extends CestyAction0 {

  private static final long serialVersionUID = -7547868179813232769L;

  public CestySmazAnoAction() {
    super("Žádné nelovím");
    putValue(SHORT_DESCRIPTION, "Odstraní příznak lovení u všech keší.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_N);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (Dlg.anone("Opravdu odstranit z výletu " + cestyModel.getPocetWaypointuVeVyletu() + " waypointů, které jste chtěli navštívit?")) {
      cestyModel.removeAllFromCesta();
    }
  }

  @Override
  protected void vyletChanged() {
    super.vyletChanged();
    setEnabled(cestyModel.getPocetWaypointuVeVyletu() > 0);
  }


}
