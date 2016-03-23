/**
 *
 */
package cz.geokuk.framework;

import java.awt.event.*;

/**
 * @author veverka
 *
 */
public abstract class DialogOpeningAction0 extends Action0 {

	private static final long	serialVersionUID	= 1736854705007384645L;

	private JMyDialog0			mydialog;

	/**
	 * @param string
	 */
	public DialogOpeningAction0(final String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public final void actionPerformed(final ActionEvent e) {
		if (mydialog == null) {
			final JMyDialog0 dialog = createDialog();
			if (factory == null) {
				throw new RuntimeException("Neinicializovana akce: " + this.getClass());
			}
			factory.init(dialog);
			mydialog = dialog;
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(final WindowEvent e) {
					mydialog = null;
				}

				@Override
				public void windowClosing(final WindowEvent e) {
					mydialog = null;
				}
			});
			dialog.setVisible(true);
		} else {
			mydialog.requestFocus();
		}
	}

	public abstract JMyDialog0 createDialog();

}
