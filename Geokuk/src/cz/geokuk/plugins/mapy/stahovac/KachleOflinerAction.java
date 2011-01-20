package cz.geokuk.plugins.mapy.stahovac;

import java.awt.event.ActionEvent;

import cz.geokuk.core.napoveda.NapovedaModelChangedEvent;
import cz.geokuk.framework.Action0;


public class KachleOflinerAction extends Action0 {

  private static final long serialVersionUID = -5465641756515262340L;

  public KachleOflinerAction() {
    super("Postahovat dlaždice...");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JKachleOflinerDialog jKachleOflinerDialog = factory.init(new JKachleOflinerDialog());
    jKachleOflinerDialog.setVisible(true);
  }

  public void onEvent(NapovedaModelChangedEvent event) {
    setEnabled(event.getModel().isOnlineMode());
  }
}
