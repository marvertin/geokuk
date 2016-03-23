package cz.geokuk.plugins.kesoid.mvc;

import java.awt.Dimension;

import javax.swing.*;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;

public abstract class JVybiracCiselny0 extends JPanel implements AfterEventReceiverRegistrationInit {

	private static final long		serialVersionUID	= -484273090975902036L;

	public final SpinnerNumberModel	iModel				= new SpinnerNumberModel(0, 0, 100, 1);

	private final JSpinner			jSpinner;
	private final JLabel			jLabel;

	private KesoidModel				kesoidModel;

	/**
	 *
	 */
	public JVybiracCiselny0(final String label) {
		jLabel = new JLabel(label);
		jSpinner = new JSpinner();
		jSpinner.setModel(iModel);
		jSpinner.setMaximumSize(new Dimension(35, 22));
		jLabel.setLabelFor(jSpinner);

		add(jLabel);
		add(jSpinner);
		setVisible(false);
		lejaut();
	}

	private void lejaut() {
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		layout.setHorizontalGroup(layout.createSequentialGroup() // hroup
				.addGap(3).addComponent(jLabel).addGap(3).addComponent(jSpinner).addGap(3));
		layout.setVerticalGroup(layout.createParallelGroup() // hroup
				.addComponent(jLabel, GroupLayout.Alignment.CENTER).addComponent(jSpinner, GroupLayout.Alignment.CENTER));

	}

	public void onEvent(final KeskyNactenyEvent aEvent) {
		final KesBag vsechny = aEvent.getVsechny();
		final int maximalniFavorit = getMaximum(vsechny);
		iModel.setMinimum(0);
		iModel.setMaximum(maximalniFavorit);
		iModel.setValue(Math.min((Integer) iModel.getNumber(), maximalniFavorit));
		setVisible(!iModel.getMinimum().equals(iModel.getMaximum()));
	}

	public void onEvent(final FilterDefinitionChangedEvent event) {
		final FilterDefinition filterDefinition = event.getFilterDefinition();
		iModel.setValue(getPrah(filterDefinition));
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

	protected abstract void setPrah(FilterDefinition filterDefinition, int prah);

	protected abstract int getPrah(FilterDefinition filterDefinition);

	protected abstract int getMaximum(KesBag vsechny);

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

}