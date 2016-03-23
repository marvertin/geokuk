package cz.geokuk.core.render;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class JTwoColumnsPanel extends JPanel {

	private static final long serialVersionUID = 1201157823732225635L;

	GridBagConstraints gbc;
	int radek;

	public JTwoColumnsPanel(final String titleText) {
		final TitledBorder border = BorderFactory.createTitledBorder(titleText);
		if (border.getTitleFont() != null) {
			border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD | Font.ITALIC));
		}
		setBorder(border);
		setLayout(new GridBagLayout());
		createNewConstraint();
	}

	public void addx(final String label, final JComponent jComp) {
		final JLabel jLabel = new JLabel(label);
		jLabel.setLabelFor(jComp);
		final GridBagConstraints gbcLabel = new GridBagConstraints();
		gbcLabel.gridx = 0;
		gbcLabel.gridy = radek;
		gbcLabel.anchor = GridBagConstraints.WEST;
		gbcLabel.insets = new Insets(6, 5, 0, 0);
		if (radek == 0) {
			gbcLabel.insets.top = 0;
		}
		add(jLabel, gbcLabel);

		// gbc.insets.bottom = 3;
		add(jComp, gbc);
		radek++;
		createNewConstraint();

	}

	private void createNewConstraint() {
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = radek;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 10, 0, 10);
		gbc.weightx = 1;
		if (radek == 0) {
			gbc.insets.top = 0;
		}
	}
}
