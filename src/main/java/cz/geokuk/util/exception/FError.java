/**
 *
 */
package cz.geokuk.util.exception;

import javax.swing.SwingUtilities;

/**
 * @author Martin Veverka
 *
 */
public class FError {

	private static JErrorDialog jErrorDialog;

	public static void report(final String text) {
		report(text, null);
	}

	public static void report(final String text, final AExcId excid) {
		SwingUtilities.invokeLater(() -> {
			if (jErrorDialog == null) {
				jErrorDialog = new JErrorDialog();
			}
			jErrorDialog.setVisible(true);
			jErrorDialog.addProblem(text, excid);
		});

	}

}
