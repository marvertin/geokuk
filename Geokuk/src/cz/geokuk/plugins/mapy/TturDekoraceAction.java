package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


public class TturDekoraceAction extends DekoraceAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public TturDekoraceAction() {
    super("Turistické trasy");
    putValue(SHORT_DESCRIPTION, "Turistické trasy, červená, modrá, zelená, žlutá.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_U);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('u'));
  }

  

  @Override
  protected EMapDekorace getDekorace() {
    return EMapDekorace.TTUR;
  }







}
