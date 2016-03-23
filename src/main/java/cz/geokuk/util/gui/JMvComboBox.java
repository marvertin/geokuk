package cz.geokuk.util.gui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import cz.geokuk.util.gui.SelectionModel.Item;

public class JMvComboBox<T> extends JComboBox<Item<T>> {

	private static final long	serialVersionUID	= 3831515220850009660L;

	private SelectionModel<T>	model				= new SelectionModel<>();

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
	}

	public SelectionModel<T> getSelectionModel() {
		return model;
	}

	public void setSelectionModel(final SelectionModel<T> model) {
		this.model = model;
		final DefaultComboBoxModel<Item<T>> defmod = new DefaultComboBoxModel<>();
		setModel(defmod);
		final List<Item<T>> items = model.items;
		for (final Item<T> item : items) {
			defmod.addElement(item);
		}
		defmod.addListDataListener(new ListDataListener() {

			@Override
			public void contentsChanged(final ListDataEvent e) {
				nastavto();
			}

			@Override
			public void intervalAdded(final ListDataEvent e) {
				nastavto();
			}

			@Override
			public void intervalRemoved(final ListDataEvent e) {
				nastavto();
			}

			private void nastavto() {
				@SuppressWarnings("unchecked")
				final Item<T> item = (Item<T>) defmod.getSelectedItem();
				model.setSelectedItem(item);
			}
		});
		model.addListener(event -> defmod.setSelectedItem(event.item));

	}
}
