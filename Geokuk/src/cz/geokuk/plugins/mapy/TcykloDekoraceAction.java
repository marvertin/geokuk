package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


public class TcykloDekoraceAction extends DekoraceAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public TcykloDekoraceAction() {
    super("Cyklotrasy");
    putValue(SHORT_DESCRIPTION, "Cyklistické trasy fialově.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_C);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('c'));
  }

  @Override
  protected EMapDekorace getDekorace() {
    return EMapDekorace.TCYKLO;
  }







}
