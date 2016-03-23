/**
 *
 */
package cz.geokuk.util.gui.fontchoser;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author Martin Veverka
 *
 */
public class Main extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 258357579389164969L;
	private final JLabel ukazovyText = new JLabel("tak toto fontujeme");

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		new Main().setVisible(true);
	}

	/**
	 *
	 */
	public Main() {
		final JFontChooser fc = new JFontChooser();
		fc.getSelectionModel().addChangeListener(aE -> ukazovyText.setFont(fc.getFont()));
		add(fc);
		add(ukazovyText, BorderLayout.SOUTH);
		pack();

	}

}
