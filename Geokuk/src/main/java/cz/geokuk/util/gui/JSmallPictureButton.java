package cz.geokuk.util.gui;

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class JSmallPictureButton extends JButton {

  private static final long serialVersionUID = 3409671407857656539L;

  public JSmallPictureButton() {
  	nastav();
  }
  
  public JSmallPictureButton(Action a) {
	  super(a);
	  nastav();
  }

	public JSmallPictureButton(Icon icon) {
	  super(icon);
	  nastav();
  }

	public JSmallPictureButton(Action a, Icon icon) {
	  super(a);
	  setIcon(icon);
	  nastav();
  }


	public JSmallPictureButton(String string) {
		super(string);
		nastav();
  }

	private void nastav() {
		setMargin(new Insets(0, 0, 0, 0));
		
	}

	
}
