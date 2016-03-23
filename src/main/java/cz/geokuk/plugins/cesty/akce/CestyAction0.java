package cz.geokuk.plugins.cesty.akce;


import javax.swing.Icon;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.plugins.cesty.CestyChangedEvent;
import cz.geokuk.plugins.cesty.CestyModel;
import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.cesty.data.Doc;



public abstract class CestyAction0 extends Action0 implements AfterInjectInit {

	private static final long serialVersionUID = -2637836928166450446L;

	protected CestyModel cestyModel;
	private String puvodniJednoducheJmeno;

	public CestyAction0() {
		setEnabled(false);
	}


	@Override
	public final void initAfterInject() {
		puvodniJednoducheJmeno = (String) getValue(NAME);
	}

	@Override
	public void setEnabled(boolean newValue) {
		super.setEnabled(newValue);
		if (! newValue) {
			if (puvodniJednoducheJmeno != null) {
				putValue(NAME, puvodniJednoducheJmeno);
			}
		}
	}

	public CestyAction0(String string) {
		super(string);
		setEnabled(false);
	}

	/**
	 * @param aString
	 * @param aSeekResIcon
	 */
	public CestyAction0(String aString, Icon aIcon) {
		super(aString, aIcon);
		setEnabled(false);
	}

	public final void onEvent(CestyChangedEvent aEvent) {
		//System.out.println("********* dorucen event na: " + System.identityHashCode(this) + ": " + getClass().getName());
		aEvent.getDoc().kontrolaKonzistence();
		vyletChanged();
	}


	protected void vyletChanged() {
	}


	public final void inject(CestyModel cestyModel) {
		this.cestyModel = cestyModel;

	}

	protected final Cesta curta() {
		return cestyModel.getCurta();
	}

	protected final Doc curdoc() {
		return cestyModel.getDoc();
	}

}


