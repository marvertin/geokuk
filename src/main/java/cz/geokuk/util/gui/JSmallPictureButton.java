package cz.geokuk.util.gui;

import java.awt.Insets;

import javax.swing.*;

public class JSmallPictureButton extends JButton {

	private static final long serialVersionUID = 3409671407857656539L;

	public JSmallPictureButton() {
		nastav();
	}

	public JSmallPictureButton(final Action a) {
		super(a);
		nastav();
	}

	public JSmallPictureButton(final Action a, final Icon icon) {
		super(a);
		setIcon(icon);
		nastav();
	}

	public JSmallPictureButton(final Icon icon) {
		super(icon);
		nastav();
	}

	public JSmallPictureButton(final String string) {
		super(string);
		nastav();
	}

	private void nastav() {
		setMargin(new Insets(0, 0, 0, 0));

	}

}
