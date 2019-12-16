package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mvc.JVybiracCiselny0;

public class JVybiracBestOf extends JVybiracCiselny0 implements AfterEventReceiverRegistrationInit {

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
	protected int getPrah(final FilterDefinition filterDefinition) {
		return filterDefinition.getPrahBestOf();
	}

	@Override
	protected void setPrah(final FilterDefinition filterDefinition, final int prah) {
		filterDefinition.setPrahBestOf(prah);

	}

}