package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.KesBag;

public class JVybiracFavorit extends JVybiracCiselny0 {

	public JVybiracFavorit() {
		super("Favorit:");
	}

	private static final long serialVersionUID = -484273090975902036L;

	@Override
	protected void setPrah(final FilterDefinition filterDefinition, final int prah) {
		filterDefinition.setPrahFavorit(prah);
	}

	@Override
	protected int getPrah(final FilterDefinition filterDefinition) {
		return filterDefinition.getPrahFavorit();
	}

	@Override
	protected int getMaximum(final KesBag vsechny) {
		return vsechny.getMaximalniFavorit();
	}

}