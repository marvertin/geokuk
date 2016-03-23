package cz.geokuk.plugins.geocoding;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.onoffline.OnofflineModelChangeEvent;
import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;

public class GeocodingAdrAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -2637836928166450446L;

	public GeocodingAdrAction() {
		super("Adresa ...");
		putValue(SHORT_DESCRIPTION, "Zobrazí dialog s možností vyhledat dle adresy.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_A);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('M', InputEvent.CTRL_DOWN_MASK));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.DialogOpeningAction0#createDialog()
	 */
	@Override
	public JMyDialog0 createDialog() {
		return new JAdrDialog();
	}

	public void onEvent(final OnofflineModelChangeEvent event) {
		setEnabled(event.isOnlineMOde());
	}

}
