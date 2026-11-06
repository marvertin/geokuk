package cz.geokuk.plugins.challenge;

/** Uložená definice obsahuje hodnotu výčtu, kterou tento program nezná (zřejmě z novější verze). */
final class NeznamaHodnota extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	NeznamaHodnota(final String zprava) {
		super(zprava);
	}
}
