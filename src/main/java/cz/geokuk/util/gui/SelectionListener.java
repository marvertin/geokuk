package cz.geokuk.util.gui;

public interface SelectionListener<T> {

	/**
	 * Došle ke změně listenera.
	 * @param event
	 */
	void selectionChanged(SelectionEvent<T> event);
}
