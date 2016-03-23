package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import java.util.HashSet;
import java.util.Set;

/**
 * Alela is a value of a property (i.e. a value of {@link Gen}.
 *
 * <p>
 * For example, Gen {@code size of cache} has alelas {@code micro}, {@code small}, {@code regular}...
 */
public class Alela {

	public static Set<String> alelyToNames(final Set<Alela> alely) {
		final Set<String> jmenaAlel = new HashSet<>(alely.size());
		for (final Alela alela : alely) {
			jmenaAlel.add(alela.name());
		}
		return jmenaAlel;
	}

	private final String nazev;
	private String displayName;
	private Gen gen;

	private final int poradiVGenomu;

	Alela(final String nazev, final int poradiVGenomu) {
		this.nazev = nazev;
		this.poradiVGenomu = poradiVGenomu;
	}

	public Alela displayName(final String displayName) {
		this.displayName = displayName;
		return this;
	}

	public int getCelkovePoradi() {
		return poradiVGenomu;
	}

	public String getDisplayName() {
		return displayName == null ? nazev : displayName;
	}

	/**
	 * Vrací gen k alele. Null pokud nemá
	 *
	 * @return
	 */
	public Gen getGen() {
		if (gen == null) {
			throw new NullPointerException("Alela " + nazev + " nemá gen!");
		}
		return gen;
	}

	public Genom getGenom() {
		return getGen().getGenom();
	}

	public boolean hasGen() {
		return gen != null;
	}

	public boolean isVychozi() {
		return gen.getVychoziAlela() == this;
	}

	public String name() {
		return nazev;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName == null ? nazev : displayName;
	}

	public void setGen(final Gen gen) {
		this.gen = gen;
	}

	@Override
	public String toString() {
		return nazev;
	}

}
