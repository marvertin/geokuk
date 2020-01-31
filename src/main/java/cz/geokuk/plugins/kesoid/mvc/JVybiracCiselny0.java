package cz.geokuk.plugins.kesoid.mvc;

import java.awt.Dimension;

import javax.swing.*;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;

public abstract class JVybiracCiselny0 extends JPanel implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = -484273090975902036L;

	public final SpinnerNumberModel iModel = new SpinnerNumberModel(0, 0, 100, 1);

	private final JSpinner jSpinner;
	private final JLabel jLabel;

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

	@Override
	public void initAfterEventReceiverRegistration() {
		iModel.addChangeListener(e -> {
			final Integer prah = (Integer) iModel.getNumber();
			setPrah(prah);
		});
	}

	protected abstract int getPrah();

	protected abstract void setPrah(int prah);

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

}