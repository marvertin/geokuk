/**
 * 
 */
package cz.geokuk.core.program;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.Action0;

public class FullScreenAction extends Action0 {

  private static final long serialVersionUID = 1948998108984785016L;

  /**
   * 
   */
  public FullScreenAction() {
    super("Celá obrazovka");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final GraphicsDevice gs = ge.getDefaultScreenDevice();
    setEnabled(gs.isFullScreenSupported());
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent aE) {

    //    System.out.println("Stisknouto F11");
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final GraphicsDevice gs = ge.getDefaultScreenDevice();
    boolean jsmeVeFulu = gs.getFullScreenWindow() != null;


    //TODO Tady předpokládáme JMainFrame a nevím, zda je to úplně správně
    JMainFrame mainFrame = (JMainFrame) getMainFrame();

    //		mainFrame.setVisible(false);
    mainFrame.dispose();
    if (jsmeVeFulu) { // ta kdeme pryc
      //      System.out.println("DO OKNA");
      gs.setFullScreenWindow(null);
      mainFrame.setFullScreen(false);
    } else {
      //      System.out.println("DO FULU");
      mainFrame.setFullScreen(true);
      gs.setFullScreenWindow(mainFrame);
    }
    mainFrame.setVisible(true);

  }

}