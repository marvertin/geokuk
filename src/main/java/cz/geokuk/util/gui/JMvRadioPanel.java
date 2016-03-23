package cz.geokuk.util.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import cz.geokuk.util.gui.SelectionModel.Item;

public class JMvRadioPanel<T> extends JPanel {

	private static final long serialVersionUID = -4181105935856103641L;

	private SelectionModel<T> model = new SelectionModel<>();
	private final List<JRadioButton> buttons = new ArrayList<>();

	public JMvRadioPanel(final String title) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		// setBorder(BorderFactory.createRaisedBevelBorder());
		setBorder(BorderFactory.createTitledBorder(title));
	}

	public SelectionModel<T> getSelectionModel() {
		return model;
	}

	public void setSelectionModel(final SelectionModel<T> model) {
		this.model = model;
		final List<Item<T>> items = model.items;
		final ButtonGroup bg = new ButtonGroup();
		for (final Item<T> item : items) {
			final JRadioButton rb = new JRadioButton(item.displayText);
			buttons.add(rb);
			bg.add(rb);
			add(rb);
			rb.addChangeListener(e -> {
				if (rb.isSelected()) {
					model.setSelectedItem(item);
				}
			});
		}
		model.addListener(event -> buttons.get(event.item.poradi).setSelected(true));

	}

}
