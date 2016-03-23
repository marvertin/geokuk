package cz.geokuk.plugins.cesty.akce;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.Poziceq;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.plugins.cesty.data.Bod;
import cz.geokuk.plugins.kesoid.Wpt;

public abstract class CestyActionIndividual0 extends CestyAction0 {

	private static final long	serialVersionUID	= -649900052004328014L;

	private final Mouable		kontextoveMouable;
	private Poziceq				poziceq;

	public CestyActionIndividual0(final Mouable kontextoveMouable) {
		this.kontextoveMouable = kontextoveMouable;
		setEnabled(false);
	}

	public void onEvent(final PoziceChangedEvent aEvent) {
		poziceq = aEvent.poziceq;
		enablujPokudMaSmysl();
	}

	@Override
	protected final void vyletChanged() {
		super.vyletChanged();
		enablujPokudMaSmysl();
	}

	protected abstract void enablujPokudMaSmysl();

	/**
	 * Do vyýletu se dá přidat jen WPT, ale ne Bod, ale také volná pozice.
	 *
	 * @param mouable
	 * @return
	 */
	private Mouable proOdstraneniZVyletu(final Mouable mouable) {
		if (mouable instanceof Wpt)
			return mouable;
		if (mouable instanceof Bod)
			return mouable; // připojovat na bod se nebudeme
		return null; // a nic jiného nelze odstranit
	}

	protected Mouable effectiveMouable() {
		if (kontextoveMouable != null)
			return proOdstraneniZVyletu(kontextoveMouable);
		// jinak se musíme spolehnout na pozici
		if (!poziceq.isNoPosition())
			return proOdstraneniZVyletu(poziceq.getPoziceMouable());
		return null;
	}

	// protected Mouable effectiveMouable() {
	// if (uchopenec != null) return uchopenec.getProOdsraneniZVyletu();
	// return pozice == null ? null : pozice.getProOdsraneniZVyletu();
	// }

}
