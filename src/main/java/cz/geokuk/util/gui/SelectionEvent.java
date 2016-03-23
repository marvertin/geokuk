package cz.geokuk.util.gui;

public class SelectionEvent<T> {

	private final SelectionModel<T>	model;
	final SelectionModel.Item<T>	item;

	public SelectionEvent(final SelectionModel<T> model, final SelectionModel.Item<T> item) {
		this.model = model;
		this.item = item;
	}

	public SelectionModel<T> getModel() {
		return model;
	}

	public T getSelected() {
		return item.value;
	}

}
