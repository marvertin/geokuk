/**
 *
 */
package cz.geokuk.core.napoveda;

import java.awt.event.KeyEvent;
import java.util.List;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;

/**
 * @author Martin Veverka
 *
 */
public class ZpravyUzivatelumAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -2882817111560336824L;
	private List<ZpravaUzivateli> zpravyUzivatelum;

	/**
	 * @param aBoard
	 */
	public ZpravyUzivatelumAction() {
		super("Zprávy uživatelům");
		putValue(SHORT_DESCRIPTION, "Zobrazí dialog s ještě nepřečtenými zprávami uživatelům.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_U);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.DialogOpeningAction0#createDialog()
	 */
	@Override
	public JMyDialog0 createDialog() {
		final JZpravyUzivatelumDialog dlg = new JZpravyUzivatelumDialog(zpravyUzivatelum);
		return dlg;
	}

	public void onEvent(final NapovedaModelChangedEvent event) {
		zpravyUzivatelum = event.getModel().getZpravyUzivatelum();
		setEnabled(zpravyUzivatelum != null && zpravyUzivatelum.size() > 0);
	}
}
