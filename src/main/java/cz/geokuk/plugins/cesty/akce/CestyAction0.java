package cz.geokuk.plugins.cesty.akce;

import javax.swing.Icon;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.plugins.cesty.CestyChangedEvent;
import cz.geokuk.plugins.cesty.CestyModel;
import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.cesty.data.Doc;

public abstract class CestyAction0 extends Action0 implements AfterInjectInit {

	private static final long	serialVersionUID	= -2637836928166450446L;

	protected CestyModel		cestyModel;
	private String				puvodniJednoducheJmeno;

	public CestyAction0() {
		setEnabled(false);
	}

	public CestyAction0(final String string) {
		super(string);
		setEnabled(false);
	}

	/**
	 * @param aString
	 * @param aSeekResIcon
	 */
	public CestyAction0(final String aString, final Icon aIcon) {
		super(aString, aIcon);
		setEnabled(false);
	}

	@Override
	public final void initAfterInject() {
		puvodniJednoducheJmeno = (String) getValue(NAME);
	}

	public final void inject(final CestyModel cestyModel) {
		this.cestyModel = cestyModel;

	}

	public final void onEvent(final CestyChangedEvent aEvent) {
		// System.out.println("********* dorucen event na: " + System.identityHashCode(this) + ": " + getClass().getName());
		aEvent.getDoc().kontrolaKonzistence();
		vyletChanged();
	}

	@Override
	public void setEnabled(final boolean newValue) {
		super.setEnabled(newValue);
		if (!newValue) {
			if (puvodniJednoducheJmeno != null) {
				putValue(NAME, puvodniJednoducheJmeno);
			}
		}
	}

	protected final Doc curdoc() {
		return cestyModel.getDoc();
	}

	protected final Cesta curta() {
		return cestyModel.getCurta();
	}

	protected void vyletChanged() {
	}

}
