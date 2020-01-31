package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.KesBag;

public class JVybiracBestOf extends JVybiracCiselnyRuznychHodnoceni0 implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = -484273090975902036L;

	/**
	 *
	 */
	public JVybiracBestOf() {
		super("BestOf:");
	}

	@Override
	protected int getMaximum(final KesBag vsechny) {
		return vsechny.getMaximalniBestOf();
	}

	@Override
	protected int getPrah(final KesFilterDefinition filterDefinition) {
		return filterDefinition.getPrahBestOf();
	}

	@Override
	protected KesFilterDefinition withPrah(final KesFilterDefinition filterDefinition, final int prah) {
		return filterDefinition.withPrahBestOf(prah);

	}

}