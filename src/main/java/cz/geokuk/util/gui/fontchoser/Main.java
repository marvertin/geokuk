/**
 *
 */
package cz.geokuk.util.gui.fontchoser;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author veverka
 *
 */
public class Main extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 258357579389164969L;
	private JLabel ukazovyText = new JLabel("tak toto fontujeme");
	/**
	 *
	 */
	public Main() {
		final JFontChooser fc = new JFontChooser();
		fc.getSelectionModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent aE) {
				ukazovyText.setFont(fc.getFont());
			}
		});
		add(fc);
		add(ukazovyText, BorderLayout.SOUTH);
		pack();

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main().setVisible(true);
	}

}
