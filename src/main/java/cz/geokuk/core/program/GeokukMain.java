/**
 * 
 */
package cz.geokuk.core.program;


import java.nio.charset.Charset;
import java.util.prefs.BackingStoreException;

import javax.swing.SwingUtilities;

import cz.geokuk.core.lookandfeel.LafSupport;
import cz.geokuk.core.profile.FPreferencesInNearFile;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.exception.MyExceptionHandler;



/**
 * @author veverka
 *
 */
public class GeokukMain  {

  public void execute(String[] args) {
    FConst.logInit();
    System.out.println("Default character encoding: " + Charset.defaultCharset());
    nastavSkin();
    Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
    promazPreferencePokudJeToPrikazano(args);
    FPreferencesInNearFile.loadNearToProgramIfNewer(); // Načíst ze souboru preferencový sobor, pokud došlo k jeho změně od minula

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Inicializator inicializator = new Inicializator();
        inicializator.inicializace();
        JMainFrame mainFrame = new JMainFrame();
        inicializator.setMainFrame(mainFrame);
        mainFrame.init();
        mainFrame.setVisible(true);
        inicializator.zkontrolovatAktualizace();
      }
    });

  }

  private void nastavSkin() {
    try {
      LafSupport.updateLookAndFeel();
    } catch (Throwable t) {
      FExceptionDumper.dump(t, EExceptionSeverity.WORKARROUND, "Nastavení skinu");
    }
  }

  private void promazPreferencePokudJeToPrikazano(String[] args) {
    for (String s : args) {
      if (s.trim().equalsIgnoreCase("--reset")) {
        try {
          MyPreferences.root().removeNode();
        } catch (BackingStoreException e) {
          FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Problém s promazáváním preferencí");
        }
      }
    }
  }


  public static void main(String[] args)  {
    new GeokukMain().execute(args);
  }

}
