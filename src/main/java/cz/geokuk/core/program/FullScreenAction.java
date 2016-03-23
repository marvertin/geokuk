/**
 *
 */
package cz.geokuk.core.program;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.framework.Action0;

public class FullScreenAction extends Action0 {

	private static final Logger	log					= LogManager.getLogger(FullScreenAction.class.getSimpleName());

	private static final long	serialVersionUID	= 1948998108984785016L;

	/**
	 *
	 */
	public FullScreenAction() {
		super("Celá obrazovka");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice gs = ge.getDefaultScreenDevice();
		setEnabled(gs.isFullScreenSupported());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {

		// System.out.println("Stisknouto F11");
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice gs = ge.getDefaultScreenDevice();
		final boolean jsmeVeFulu = gs.getFullScreenWindow() != null;

		// LATER Tady předpokládáme JMainFrame a nevím, zda je to úplně správně
		final JMainFrame mainFrame = (JMainFrame) getMainFrame();

		// mainFrame.setVisible(false);
		// mainFrame.dispose();
		if (jsmeVeFulu) { // ta kdeme pryc
			log.trace("DO OKNA");
			gs.setFullScreenWindow(null);
			mainFrame.setFullScreen(false);
		} else {
			log.trace("DO FULU 1");
			mainFrame.setFullScreen(true);
			log.trace("DO FULU 2");
			gs.setFullScreenWindow(mainFrame);
			log.trace("DO FULU 3");
		}
		log.trace("DO FULU 4");
		// mainFrame.setVisible(true);
		log.trace("DO FULU 5");

	}

}