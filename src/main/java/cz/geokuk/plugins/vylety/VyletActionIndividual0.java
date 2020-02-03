package cz.geokuk.plugins.vylety;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;

public abstract class VyletActionIndividual0 extends VyletAction0 implements AfterInjectInit {

	private static final long serialVersionUID = -649900052004328014L;

	private final Kesoid kespevna;
	private Kesoid kesdocasna;

	public VyletActionIndividual0(final String string, final Wpt wpt) {
		super(string);
		kespevna = wpt == null ? null : wpt.getKesoid();
		setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.AfterInjectInit#initAfterInject()
	 */
	@Override
	public void initAfterInject() {
		if (kesoid() != null) {
			enablujPokudMaSmysl();
		}
	}

	public void onEvent(final PoziceChangedEvent aEvent) {
		final Wpt wpt = aEvent.poziceq.getWpt();
		if (wpt != null) {
			kesdocasna = wpt.getKesoid();
			enablujPokudMaSmysl();
		} else {
			kesdocasna = null;
			setEnabled(false);
		}
	}

	protected abstract void enablujPokudMaSmysl();

	protected Kesoid kesoid() {
		if (kespevna != null) {
			return kespevna;
		}
		return kesdocasna;
	}

	@Override
	protected void vyletChanged() {
		super.vyletChanged();
		if (kesoid() != null) {
			enablujPokudMaSmysl();
		}
	}
}
