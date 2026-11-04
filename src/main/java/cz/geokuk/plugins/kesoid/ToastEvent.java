package cz.geokuk.plugins.kesoid;

import cz.geokuk.framework.Event0;

/** Krátká zpráva po uživatelově akci, která se ukáže nad mapou. */
public class ToastEvent extends Event0<ToastModel> {

	private final String text;

	ToastEvent(final String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return text;
	}
}
