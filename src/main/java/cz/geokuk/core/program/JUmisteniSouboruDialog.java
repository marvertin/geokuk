package cz.geokuk.core.program;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;


public class JUmisteniSouboruDialog extends JMyDialog0 implements AfterInjectInit {

  private static final long serialVersionUID = 7180968190465321695L;
  private JPrehledSouboru jPrehledSoubor;

  public JUmisteniSouboruDialog() {
    setTitle("Přehled souborů a složek");
    init();
  }

  @Override
  protected void initComponents() {
    jPrehledSoubor = new JPrehledSouboru(null);
    //add(new JScrollPane(jPrehledSoubor));
    add(jPrehledSoubor);
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterInjectInit#initAfterInject()
   */
  @Override
  public void initAfterInject() {
    factory.init(jPrehledSoubor);
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return "UmisteniSouboru";
  }

  public void fokusni(ESouborPanelName panelName) {
    jPrehledSoubor.fokusni(panelName);
  }

}
