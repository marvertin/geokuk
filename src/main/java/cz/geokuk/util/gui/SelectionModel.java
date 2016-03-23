package cz.geokuk.util.gui;

import java.util.ArrayList;
import java.util.List;

import cz.geokuk.util.lang.FObject;

/**
 * GUI model pro výáběr nějakých objektů.
 *
 * @author Martin Veverka
 *
 * @param <T>
 */
public class SelectionModel<T> {

	static class Item<T> {
		T value;
		String displayText;
		int poradi;

		public Item(final T value, final String displayText, final int poradi) {
			this.value = value;
			this.displayText = displayText;
			this.poradi = poradi;
		}

		@Override
		public String toString() {
			return displayText;
		}

	}

	final List<Item<T>> items = new ArrayList<>();

	private Item<T> selectedItem;

	// public void add(Map<? extends T, String> map) {
	// this.map.putAll(map);
	// }

	private final List<SelectionListener<T>> listeners = new ArrayList<>();

	public void add(final T item, final String displayText) {
		items.add(new Item<>(item, displayText, items.size()));
	}

	public void addListener(final SelectionListener<T> listener) {
		listeners.add(listener);
	}

	public void setSelected(final T value) {
		for (final Item<T> item : items) {
			if (FObject.isEqual(value, item.value)) {
				setSelectedItem(item);
				return;
			}
		}
		setSelectedFirst();
	}

	public void setSelectedFirst() {
		if (!items.isEmpty()) {
			setSelectedItem(items.get(0));
		}
	}

	void setSelectedItem(final Item<T> item) {
		if (selectedItem != item) { // mohu přímo porovnávat, protože je to jeden z našeho
			selectedItem = item;
			fire(new SelectionEvent<>(this, selectedItem));
		}
	}

	private void fire(final SelectionEvent<T> event) {
		for (final SelectionListener<T> listener : listeners) {
			listener.selectionChanged(event);
		}

	}
}
