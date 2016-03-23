package cz.geokuk.plugins.vylety;

import javax.swing.Icon;

import cz.geokuk.framework.Action0;

public abstract class VyletAction0 extends Action0 {

	private static final long	serialVersionUID	= -2637836928166450446L;

	protected VyletModel		vyletModel;

	public VyletAction0(final String string) {
		super(string);
		setEnabled(false);
	}

	/**
	 * @param aString
	 * @param aSeekResIcon
	 */
	public VyletAction0(final String aString, final Icon aIcon) {
		super(aString, aIcon);
		setEnabled(false);
	}

	public final void onEvent(final VyletChangeEvent aEvent) {
		vyletChanged();
	}

	protected void vyletChanged() {
	}

	public void inject(final VyletModel vyletModel) {
		this.vyletModel = vyletModel;

	}
}
