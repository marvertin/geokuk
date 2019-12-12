package cz.geokuk.plugins.kesoid.genetika;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alela is a value of a property (i.e. a value of {@link Gen}.
 * Alela reprezentuje alelu libovolného genu. Všechny alely jsou evidovány centrálně v {@link Genom},
 * ale vznikají na genu voláním alela. Alela má gen od svého počátku a nemůže ho změnit.
 *
 * Alela přidaná do genu jako první se stává alelou výchozí.
 *
 * <p>
 * For example, Gen {@code size of cache} has alelas {@code micro}, {@code small}, {@code regular}...
 */
public class Alela {

	public static String VYCHOZI_ALELA_NAME = "~~";

	private final String nazev;
	private final Gen gen;
	private String displayName;

	// Indexuje alely, aby se dal rychleji realizovat čítač alel, stačí indexovat v poli
	private final int celkovePoradi;

	Alela(final String nazev, final Gen gen, final int celkovePoradi) {
		this.nazev = nazev;
		this.gen = gen;
		this.celkovePoradi = celkovePoradi;
		gen.addy(this);
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

	/**
	 * Výchozí alela je alela přidána ke genu jako první. Pokud jedinci není přiřazena v nějakém genu alela, má alelu výchozí.
	 *
	 * @return
	 */
	public boolean isVychozi() {
		return gen.getVychoziAlela() == this;
	}

	public String simpleName() {
		return nazev;
	}

	/**
	 * Vrací kvalifikované jméno alely, je kvalifikované jménem genu, takže je unikátní v celém genomu.
	 * Je úplně jedno, jak se kvalifikace provede, důležité je větět, že takto získané jméno vstupuje do
	 * matod, které mají v názvech parametru qualName.
	 *
	 * @return Kvalifikované jméno. Jen ta mezi námi: je to "alela:gen".
	 */
	public String qualName() {
		return simpleName() + getGenom().ODDELOVAC_KVALIFOVANY + gen.getNazev();
	}

	@Override
	public String toString() {
		//throw new RuntimeException("Volan toString na Alele a to může být špatný, moc špatný");
		return qualName();
	}

	int getCelkovePoradi() {
		return celkovePoradi;
	}

	/**
	 *
	 * @param alely
	 * @return
	 */
	public static QualAlelaNames alelyToQualNames(final Set<Alela> alely) {
		return new QualAlelaNames(alely.stream()
				.map(Alela::qualName)
				.collect(Collectors.toSet()));
	}

}
