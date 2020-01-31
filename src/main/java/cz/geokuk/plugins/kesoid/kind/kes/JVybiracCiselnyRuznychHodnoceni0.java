package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mvc.JVybiracCiselny0;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;

public abstract class JVybiracCiselnyRuznychHodnoceni0 extends JVybiracCiselny0 implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = -484273090975902036L;

	private KesFilterModel kesFilterModel;

	/**
	 *
	 */
	public JVybiracCiselnyRuznychHodnoceni0(final String label) {
		super(label);
	}

	@Override
	public void initAfterEventReceiverRegistration() {
		iModel.addChangeListener(e -> {
			setPrah((Integer) iModel.getNumber());
		});
	}

	public void inject(final KesFilterModel kesFilterModel) {
		this.kesFilterModel = kesFilterModel;
	}

	public void onEvent(final KesFilterDefinitionChangedEvent event) {
		final KesFilterDefinition filterDefinition = event.getKesFilterDefinition();
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

	protected abstract int getPrah(KesFilterDefinition filterDefinition);

	protected abstract KesFilterDefinition withPrah(KesFilterDefinition filterDefinition, int prah);


	@Override
	protected int getPrah() {
		return getPrah(kesFilterModel.getKesFilterDefinition());
	}

	@Override
	protected void setPrah(final int prah) {
		kesFilterModel.modify(definition -> withPrah(definition, prah));
	}


}