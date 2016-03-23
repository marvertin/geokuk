package cz.geokuk.framework;


import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public final class Dlg {

	private Dlg() {}

	private static JFrame sFrame;

	public static void natavMainFrameProMaleDialogy(JFrame frame) {
		sFrame = frame;
	}

	public static boolean anone(String otazka) {
		int result = JOptionPane.showOptionDialog(parentFrame(), otazka,
				"Potvrzení", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		return result == JOptionPane.YES_OPTION;
	}

	public static boolean prepsatSoubor(File file) {
		return !file.exists() || anone("Soubor \"" + file.getAbsolutePath() + "\" existuje, má být přepsán?");
	}

	public static  void info(String informace, String titulek) {
		JOptionPane.showMessageDialog(parentFrame(), informace, titulek, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void error(String informace) {
		JOptionPane.showMessageDialog(parentFrame(), informace, "Geokuk: Chyba", JOptionPane.ERROR_MESSAGE);
	}

	public static JFrame parentFrame() {
		return sFrame == null ? new JFrame() : sFrame;
	}

}
