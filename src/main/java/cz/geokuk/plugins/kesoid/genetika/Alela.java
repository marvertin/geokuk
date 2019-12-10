package cz.geokuk.plugins.kesoid.genetika;

import java.util.HashSet;
import java.util.Set;

/**
 * Alela is a value of a property (i.e. a value of {@link Gen}. Alela reprezentuje alelu libovolného genu. Všechny alely jsou zřizovány centrálně v {@link Genom}. Nově vzniklá alela nemá gen, jakmile je alela přiřazena do genu má ho navždy, gen v alele už nelze změnit.
 *
 * Alela přidaná do genu jako první se stává alelou výchozí.
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

	// Indexuje alely, aby se dal rychleji realizovat čítač alel, stačí indexovat v poli
	private final int celkovePoradi;

	Alela(final String nazev, final int celkovePoradi) {
		this.nazev = nazev;
		this.celkovePoradi = celkovePoradi;
	}

	public Alela displayName(final String displayName) {
		this.displayName = displayName == null ? nazev : displayName;
		return this;
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

	/**
	 * Výchozí alela je alela přidána ke genu jako první. Pokud jedinci není přiřazena v nějakém genu alela, má alelu výchozí.
	 *
	 * @return
	 */
	public boolean isVychozi() {
		return gen.getVychoziAlela() == this;
	}

	/**
	 * Jméno alely.
	 *
	 * @return
	 */
	public String name() {
		return nazev;
	}

	void setGen(final Gen gen) {
		this.gen = gen;
	}

	@Override
	public String toString() {
		return nazev;
	}

	int getCelkovePoradi() {
		return celkovePoradi;
	}

}
