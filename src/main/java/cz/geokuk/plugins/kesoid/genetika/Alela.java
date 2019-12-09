package cz.geokuk.plugins.kesoid.genetika;

import java.util.HashSet;
import java.util.Set;

/**
 * Alela is a value of a property (i.e. a value of {@link Gen}.
 *
 * <p>
 * For example, Gen {@code size of cache} has alelas {@code micro}, {@code small}, {@code regular}...
 */
public class Alela {

	private final String alelaName;
	private String displayName;
	private Gen gen;
	private GrupaImpl grupa;
	private final int celkovePoradi;

	public static Set<String> alelyToNames(final Set<Alela> alely) {
		final Set<String> jmenaAlel = new HashSet<>(alely.size());
		for (final Alela alela : alely) {
			jmenaAlel.add(alela.name());
		}
		return jmenaAlel;
	}

	public Alela(final String alelaName, final int celkovePoradi) {
		this.alelaName = alelaName;
		displayName = alelaName;
		this.celkovePoradi = celkovePoradi;
	}

	public int getCelkovePoradi() {
		return celkovePoradi;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Gen getGen() {
		if (gen == null) {
			throw new RuntimeException("Alela " + alelaName + " nemá gen!");
		}
		return gen;
	}

	public Genom getGenom() {
		return getGen().getGenom();
	}

	/**
	 * @return the grupa
	 */
	GrupaImpl getGrupa() {
		return grupa;
	}

	public boolean hasGen() {
		return gen != null;
	}

	public boolean isSym() {
		return getGenom().isAlelaSym(this);
	}

	public boolean isVychozi() {
		return gen.getVychoziAlela() == this;
	}

	public String name() {
		return alelaName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName == null ? alelaName : displayName;
	}

	public void setGen(final Gen gen) {
		this.gen = gen;
	}

	/**
	 * @param aGrupa
	 *            the grupa to set
	 */
	void setGrupa(final GrupaImpl aGrupa) {
		grupa = aGrupa;
	}

	@Override
	public String toString() {
		return alelaName;
	}
}
