package cz.geokuk.plugins.kesoid;

import java.awt.Component;

/**
 * Viditelnost vrstvy, která patří ke kešoidům (kruhy, popisky): vrstva je vidět, jen když jsou zapnuté kešoidy (F5) i její vlastní
 * vypínač. Vypnutí kešoidů vlastní vypínač nemění, takže se po zapnutí kešoidů vrátí předchozí stav.
 */
public final class ViditelnostSKesoidy {

	private final Component vrstva;
	private boolean kesoidy = true;
	private boolean vlastni;

	/** Vrstva je skrytá, dokud nepřijde vlastní vypínač. */
	public ViditelnostSKesoidy(final Component vrstva) {
		this.vrstva = vrstva;
		uplatni();
	}

	/** Z {@code KesoidOnoffEvent}. */
	public void setKesoidy(final boolean zapnute) {
		kesoidy = zapnute;
		uplatni();
	}

	/** Vlastní vypínač vrstvy (F8, F9, popisky…). */
	public void setVlastni(final boolean zapnuto) {
		vlastni = zapnuto;
		uplatni();
	}

	public boolean isViditelna() {
		return kesoidy && vlastni;
	}

	private void uplatni() {
		vrstva.setVisible(isViditelna());
	}
}
