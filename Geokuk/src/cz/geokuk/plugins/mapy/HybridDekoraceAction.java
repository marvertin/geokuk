package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


public class HybridDekoraceAction extends DekoraceAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public HybridDekoraceAction() {
    super("Popisy");
    putValue(SHORT_DESCRIPTION, "Cesty a názvy měst na fotomapách.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('p'));
  }

  @Override
  protected EMapDekorace getDekorace() {
    return EMapDekorace.HYBRID;
  }

  @Override
  public void onEvent(ZmenaMapNastalaEvent event) {
    super.onEvent(event);
    setEnabled(mapyModel.getPodklad().isJeMoznyHybrid());
  }






}
