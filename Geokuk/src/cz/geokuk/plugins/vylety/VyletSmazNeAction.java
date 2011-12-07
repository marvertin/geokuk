package cz.geokuk.plugins.lovim;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import cz.geokuk.framework.Dlg;


public class VyletSmazNeAction extends VyletAction0 {

  private static final long serialVersionUID = -7547868179813232769L;

  public VyletSmazNeAction() {
    super("Žádné neignoruji");
    putValue(SHORT_DESCRIPTION, "Odstraní příznak ignorace u všech keší.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_N);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (Dlg.anone("Opravdu odstranit z výletu " + vyletModel.get(EVylet.NE).size() + " keší, které chcete ignorovat?")) {
      vyletModel.removeAll(EVylet.NE);
    }
  }

  @Override
  protected void vyletChanged() {
    super.vyletChanged();
    setEnabled(vyletModel.get(EVylet.NE).size() > 0);
  }

}
