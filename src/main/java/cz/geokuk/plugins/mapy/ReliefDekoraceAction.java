package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


public class ReliefDekoraceAction extends DekoraceAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public ReliefDekoraceAction() {
    super("Reliéf");
    putValue(SHORT_DESCRIPTION, "Prostorové zobrazení mapy.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_R);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('r'));
  }

  @Override
  protected EMapDekorace getDekorace() {
    return EMapDekorace.RELIEF;
  }







}
