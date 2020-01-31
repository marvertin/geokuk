package cz.geokuk.plugins.kesoid.kind.kes;

public class JVybiracFavorit extends JVybiracCiselnyRuznychHodnoceni0 {

	private static final long serialVersionUID = -484273090975902036L;

	public JVybiracFavorit() {
		super("Favorit:");
	}

	@Override
	protected int getMaximum(final KesWptSumarizer sumarizer) {
		return sumarizer.getMaximalniFavorit();
	}

	@Override
	protected int getPrah(final KesFilterDefinition filterDefinition) {
		return filterDefinition.getPrahFavorit();
	}

	@Override
	protected KesFilterDefinition withPrah(final KesFilterDefinition filterDefinition, final int prah) {
		return filterDefinition.withPrahFavorit(prah);
	}

}