/**
 *
 */
package cz.geokuk.util.exception;

import javax.swing.SwingUtilities;

/**
 * @author veverka
 *
 */
public class FError {

	private static JErrorDialog jErrorDialog;

	public static void report(final String text) {
		report(text, null);
	}

	public static void report(final String text, final AExcId excid) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (jErrorDialog == null)
					jErrorDialog = new JErrorDialog();
				jErrorDialog.setVisible(true);
				jErrorDialog.addProblem(text, excid);
			}
		});

	}

}
