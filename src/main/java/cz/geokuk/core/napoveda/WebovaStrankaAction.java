/**
 * 
 */
package cz.geokuk.core.napoveda;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;


import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.Action0;
import cz.geokuk.util.process.BrowserOpener;


/**
 * @author veverka
 *
 */
public class WebovaStrankaAction extends Action0 {

  private static final long serialVersionUID = -2882817111560336824L;
  /**
   * @param aBoard
   */
  public WebovaStrankaAction() {
    super("Webová stránka");
    putValue(SHORT_DESCRIPTION, "Zobrazí webovou stránku programu s nápovědou a jinými infroamcemi.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_W);
  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent aE) {
    try {
      BrowserOpener.displayURL(new URL(FConst.WEB_PAGE_URL));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

}
