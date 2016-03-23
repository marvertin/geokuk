/**
 *
 */
package cz.geokuk.core.program;


import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;



/**
 * @author veverka
 *
 */
public class ZobrazServisniOknoAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	/**
	 * @param aBoard
	 */
	public ZobrazServisniOknoAction() {
		super("Servis...");
		putValue(SHORT_DESCRIPTION, "Zobrazí okno se servisními informacemi.");
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public JMyDialog0 createDialog() {
		return new JServiceFrame();
	}

}
