package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

/**
 * @author veverka
 *
 */
public class RefreshIkonAction extends Action0 {

	private static final long	serialVersionUID	= -8054017274338240706L;
	private KesoidModel			kesoidModel;

	/**
	 *
	 */
	public RefreshIkonAction() {
		super("Přenačíst ikony");
		putValue(SHORT_DESCRIPTION, "Přenačte všechny ikony ze souborů a obnoví menu ze sadama.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_R);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	public void actionPerformed(ActionEvent e) {
		setEnabled(false);
		kesoidModel.startIkonLoad(true);
	}

	public void onEvent(IkonyNactenyEvent event) {
		setEnabled(true);
	}

	public void inject(KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

}
