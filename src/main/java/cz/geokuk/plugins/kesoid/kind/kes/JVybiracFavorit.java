package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.KesBag;

public class JVybiracFavorit extends JVybiracCiselnyRuznychHodnoceni0 {

	private static final long serialVersionUID = -484273090975902036L;

	public JVybiracFavorit() {
		super("Favorit:");
	}

	@Override
	protected int getMaximum(final KesBag vsechny) {
		return vsechny.getMaximalniFavorit();
	}

	@Override
	protected int getPrah(final FilterDefinition filterDefinition) {
		return filterDefinition.getPrahFavorit();
	}

	@Override
	protected void setPrah(final FilterDefinition filterDefinition, final int prah) {
		filterDefinition.setPrahFavorit(prah);
	}

}