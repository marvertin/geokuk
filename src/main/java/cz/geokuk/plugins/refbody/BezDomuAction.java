/**
 *
 */
package cz.geokuk.plugins.refbody;

import java.awt.event.*;

import javax.swing.KeyStroke;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Action0;

/**
 *
 * @author Martin Veverka
 *
 */
public class BezDomuAction extends Action0 {

	private static final long serialVersionUID = -2882817111560336824L;
	private RefbodyModel refbodyModel;

	/**
	 * @param aBoard
	 */
	public BezDomuAction() {
		super("Přesunout domů");
		putValue(SHORT_DESCRIPTION, "Přesune mapu na domácí souřadnice.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_D);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {
		final Wgs hc = refbodyModel.getHc();
		poziceModel.setPozice(hc);
		vyrezModel.vystredovatNaPozici();
	}

	public void inject(final RefbodyModel refbodyModel) {
		this.refbodyModel = refbodyModel;
	}

}
