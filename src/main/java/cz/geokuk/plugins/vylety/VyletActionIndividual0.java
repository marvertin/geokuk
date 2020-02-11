package cz.geokuk.plugins.vylety;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.plugins.kesoid.Wpt;

public abstract class VyletActionIndividual0 extends VyletAction0 implements AfterInjectInit {

	private static final long serialVersionUID = -649900052004328014L;

	private final Wpt wptpevny;
	private Wpt wptdocasny;

	public VyletActionIndividual0(final String string, final Wpt wpt) {
		super(string);
		wptpevny = wpt;
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
			wptdocasny = wpt;
			enablujPokudMaSmysl();
		} else {
			wptdocasny = null;
			setEnabled(false);
		}
	}

	protected abstract void enablujPokudMaSmysl();

	protected Wpt kesoid() {
		if (wptpevny != null) {
			return wptpevny;
		}
		return wptdocasny;
	}

	@Override
	protected void vyletChanged() {
		super.vyletChanged();
		if (kesoid() != null) {
			enablujPokudMaSmysl();
		}
	}
}
