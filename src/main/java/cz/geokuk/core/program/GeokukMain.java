package cz.geokuk.core.program;

import java.nio.charset.Charset;
import java.util.prefs.BackingStoreException;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.lookandfeel.LafSupport;
import cz.geokuk.core.profile.FPreferencesInNearFile;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.util.exception.*;

/**
 * @author veverka
 *
 */
public class GeokukMain {

	private static final Logger log = LogManager.getLogger(GeokukMain.class.getSimpleName());

	public void execute(final String[] args) {
		FConst.logInit();
		log.info("Default character encoding: {}", Charset.defaultCharset());
		nastavSkin();
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		promazPreferencePokudJeToPrikazano(args);
		FPreferencesInNearFile.loadNearToProgramIfNewer(); // Načíst ze souboru preferencový sobor, pokud došlo k jeho změně od minula

		SwingUtilities.invokeLater(() -> {
			final Inicializator inicializator = new Inicializator();
			inicializator.inicializace();
			final JMainFrame mainFrame = new JMainFrame();
			inicializator.setMainFrame(mainFrame);
			mainFrame.init();
			mainFrame.setVisible(true);
			inicializator.zkontrolovatAktualizace();
		});
	}

	private void nastavSkin() {
		try {
			LafSupport.updateLookAndFeel();
		} catch (final Throwable t) {
			FExceptionDumper.dump(t, EExceptionSeverity.WORKARROUND, "Nastavení skinu");
		}
	}

	private void promazPreferencePokudJeToPrikazano(final String[] args) {
		for (final String s : args) {
			if (s.trim().equalsIgnoreCase("--reset")) {
				try {
					MyPreferences.root().removeNode();
				} catch (final BackingStoreException e) {
					FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Problém s promazáváním preferencí");
				}
			}
		}
	}

	public static void main(final String[] args) {
		new GeokukMain().execute(args);
	}

}
