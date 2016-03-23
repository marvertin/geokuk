package cz.geokuk.plugins.kesoid;

import java.awt.Insets;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;

/**
 * Ovjekt držící, co se bude repaintovat
 *
 * @author tatinek
 *
 */
public class Repaintanger {
	private Insets insets = new Insets(0, 0, 0, 0);

	/**
	 * @return the insets
	 */
	public Insets getInsets() {
		return insets;
	}

	public void include(final Imagant imagant) {
		if (imagant == null) {
			return;
		}
		final Insets insets = new Insets(-imagant.getYpos(), -imagant.getXpos(), imagant.getYpos() + imagant.getImage().getHeight(), imagant.getXpos() + imagant.getImage().getWidth());
		include(insets);
	}

	public void include(final Insets in) {
		insets = new Insets(Math.max(insets.top, in.top), Math.max(insets.left, in.left), Math.max(insets.bottom, in.bottom), Math.max(insets.right, in.right));

	}

	public void include(final Sklivec sklivec) {
		if (sklivec == null) {
			return;
		}
		for (final Imagant imagant : sklivec.imaganti) {
			if (imagant != null) {
				include(imagant);
			}
		}
	}

	@Override
	public String toString() {
		return "Repaintanger [insets=" + insets + "]";
	}

}
