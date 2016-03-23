package cz.geokuk.framework;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public final class Dlg {

	private static JFrame sFrame;

	public static boolean anone(final String otazka) {
		final int result = JOptionPane.showOptionDialog(parentFrame(), otazka, "Potvrzení", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		return result == JOptionPane.YES_OPTION;
	}

	public static void error(final String informace) {
		JOptionPane.showMessageDialog(parentFrame(), informace, "Geokuk: Chyba", JOptionPane.ERROR_MESSAGE);
	}

	public static void info(final String informace, final String titulek) {
		JOptionPane.showMessageDialog(parentFrame(), informace, titulek, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void natavMainFrameProMaleDialogy(final JFrame frame) {
		sFrame = frame;
	}

	public static JFrame parentFrame() {
		return sFrame == null ? new JFrame() : sFrame;
	}

	public static boolean prepsatSoubor(final File file) {
		return !file.exists() || anone("Soubor \"" + file.getAbsolutePath() + "\" existuje, má být přepsán?");
	}

	private Dlg() {
	}

}
