package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;
import cz.geokuk.plugins.kesoid.mvc.*;

public abstract class JVybiracCiselnyRuznychHodnoceni0 extends JVybiracCiselny0 implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = -484273090975902036L;

	private KesoidModel kesoidModel;

	/**
	 *
	 */
	public JVybiracCiselnyRuznychHodnoceni0(final String label) {
		super(label);
	}

	@Override
	public void initAfterEventReceiverRegistration() {
		iModel.addChangeListener(e -> {
			final FilterDefinition definition = kesoidModel.getDefinition();
			final Integer prah = (Integer) iModel.getNumber();
			setPrah(definition, prah);
			kesoidModel.setDefinition(definition);
		});
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void onEvent(final FilterDefinitionChangedEvent event) {
		final FilterDefinition filterDefinition = event.getFilterDefinition();
		iModel.setValue(getPrah(filterDefinition));
	}

	@Override
	public void onEvent(final KeskyNactenyEvent aEvent) {
		final KesBag vsechny = aEvent.getVsechny();
		final int maximalniFavorit = getMaximum(vsechny);
		iModel.setMinimum(0);
		iModel.setMaximum(maximalniFavorit);
		iModel.setValue(Math.min((Integer) iModel.getNumber(), maximalniFavorit));
		setVisible(!iModel.getMinimum().equals(iModel.getMaximum()));
	}

	@Override
	protected abstract int getMaximum(KesBag vsechny);

	protected abstract int getPrah(FilterDefinition filterDefinition);

	protected abstract void setPrah(FilterDefinition filterDefinition, int prah);


	@Override
	protected int getPrah() {
		return getPrah(kesoidModel.getDefinition());
	}

	@Override
	protected void setPrah(final int prah) {
		final FilterDefinition definition = kesoidModel.getDefinition();
		setPrah(definition, prah);
		kesoidModel.setDefinition(definition);
	}


}