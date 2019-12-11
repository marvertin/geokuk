package cz.geokuk.plugins.kesoid.genetika;

import java.util.*;
import java.util.stream.Collectors;

import cz.geokuk.util.lang.FString;

/**
 * Gen je součástí genomu. Gen může být v některých druzích, ale v některých ne. Gen musí mít nejméně jednu alelu a to alelu výchozí. Po svém zřízení je však bez alely a očekává se, že alelu brzy získá. Geny se zřizují centálně z genomu,
 *
 *
 * Determines a property of a {@link cz.geokuk.plugins.kesoid.Kesoid}. *
 * <p>
 * For example, gen can be {@code size of the cache} or {@code type of the waypoint}.
 */
public class Gen implements Grupa {

	private final String nazev;
	private final Set<Alela> alely = new LinkedHashSet<>();
	private final Map<String, Alela> alelyMap = new LinkedHashMap<>();
	private final Alela vychoziAlela;
	private boolean locked;
	private final Genom genom;
	boolean vypsatelnyVeZhasinaci;
	final int poradiVGenomu;
	private String displayName;

	Gen(final String nazev, final int poradiVGenomu, final Genom genom) {
		this.nazev = nazev;
		this.poradiVGenomu = poradiVGenomu;
		this.genom = genom;
		vychoziAlela = alela(Alela.VYCHOZI_ALELA_NAME);
	}

	/**
	 * Přidá exisutjící alelu do genu. Alela nesmí být svázána s jiným genem, jinak to spadne. Od tohoto okamžiku bude navždy alela s tímto genem svázána.
	 *
	 * První alela, která bude přidána do genu se stává výchozí alelou. Nově zrození jedinci mají alely všech genů jako výchozí.
	 *
	 * @param alela
	 *            Přidávaná alela.
	 */
	synchronized void addy(final Alela alela) {
		if (locked) {
			throw new RuntimeException("Nemozne pridavat alely " + alela + " k zamcenemu genu " + this);
		}

		alelyMap.put(alela.simpleName(), alela);
		alely.add(alela);
	}

	/**
	 * Získá nebo zřídí novou alelu a přidá ji do genu.
	 *
	 * Alela už nesmí být v jiném genu, jinak spadne.
	 *
	 * První alela přidaná do genu se stává alelou výchozí.
	 *
	 * @param nazev
	 *            Název přidávané alely.
	 * @return Přidaná alela.
	 */
	public Alela alela(final String nazev) {
		final Alela alela = genom.getOrCreateAlela(nazev, this);
		return alela;
	}

	public Gen displayName(final String displayName) {
		this.displayName = displayName;
		return this;
	}

	/**
	 * Vrací seznam všech alel genu.
	 */
	@Override
	public Set<Alela> getAlely() {
		return alely;
	}

	@Override
	public String getDisplayName() {
		return displayName == null ? nazev : displayName;
	}

	public Genom getGenom() {
		return genom;
	}

	public Alela getVychoziAlela() {
		assert vychoziAlela != null : "Na genu: " + nazev;
		return vychoziAlela;
	}

	public boolean isVypsatelnyVeZhasinaci() {
		return vypsatelnyVeZhasinaci;
	}

	/**
	 *
	 */
	public void lock() {
		locked = true;
	}

	@Override
	public String toString() {
		final String alelyNamesStr = alely.stream()
				.filter(x -> x != null)
				.map(Alela::simpleName)
				.collect(Collectors.joining(", "));

		return "Gen [nazev=" + nazev + ", alely=[" + alelyNamesStr + "], vychoziAlela=\"" + vychoziAlela.simpleName() + "\"]";
	}

	/**
	 * @deprecated Žádní grupy
	 */
	@Deprecated
	private Map<String, GrupaSym> grupy;

	/**
	 * @deprecated Žádní grupy
	 */
	@Deprecated
	synchronized GrupaSym grupa(final String grupaName) {
		if (FString.isEmpty(grupaName)) {
			//throw new IllegalArgumentException("Chybi jmeno grupy");
			return null;
		}
		if (!isSymGen()) {
			throw new IllegalStateException("Jen symgen smí mít grupy, ne " + nazev + " - " + displayName);
		}
		if (grupy == null) {
			grupy = new HashMap<>();
		}
		GrupaSym grupa = grupy.get(grupaName);
		if (grupa == null) {
			grupa = new GrupaSym(grupaName);
			if ("gcawp!".equals(grupaName)) {
				genom.GRUPA_gcawp = grupa;
			}
			if ("gc!".equals(grupaName)) {
				genom.GRUPA_gc = grupa;
			}
			grupy.put(grupaName, grupa);
		}
		return grupa;
	}

	/**
	 * @deprecated Žádní grupy
	 */
	@Deprecated
	public Map<String, ? extends Grupa> getGrupy() {
		if (isSymGen()) {
			return grupy;
		} else {
			return Collections.singletonMap(GrupaSym.IMPLICITNI_GRUPA_NAME, this);
		}

	}

	private boolean isSymGen() {
		final boolean is = genom.symGen == this;
		if (is && poradiVGenomu != 0) {
			throw new IllegalStateException("Symgen musí mít pořadí v genomu 0");
		}
		if (!is && grupy != null) {
			throw new IllegalStateException("Jen symgen smí mít grupy, ne " + nazev + " - " + displayName);
		}
		return is;
	}

	/**
	 * Vyhledá alelu podle jednoduchéh jména v genu. Je to jméno vracené v simpleName()
	 * @param alelaName
	 * @return
	 */
	public Optional<Alela> locateAlela(final String alelaName) {
		return Optional.ofNullable(alelyMap.get(alelaName));
	}

	String getNazev() {
		return nazev;
	}

}
