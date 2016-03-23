package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Genom {

	private static final Logger log = LogManager.getLogger(Genom.class.getSimpleName());

	private final Map<String, Alela> alely = new LinkedHashMap<>();

	// Geny jsou v mapš ui v seznamu pro snadný přístup
	private final Map<String, Gen> geny = new LinkedHashMap<>();
	private final List<Gen> genyList = new ArrayList<>();

	private final Map<String, Druh> druhy = new LinkedHashMap<>();

	/**
	 * Zde jsou zveřejněné alely a geny, které se používají v kódu. Řetězce uvedené pro jejich definici se nikde jinde v programu nesmějí vykytnout.
	 */
	public final Gen GEN_postavenikMysi = gen("Postavení k myši");

	public final Alela ALELA_nomouse = GEN_postavenikMysi.alela("nomouse").displayName("Žádná myš");
	public final Alela ALELA_mousean = GEN_postavenikMysi.alela("mousean").displayName("Myš nad jiným wpt kešoidu");
	public final Alela ALELA_mouseon = GEN_postavenikMysi.alela("mouseon").displayName("Myš nad tímto wpt");

	/** Vrátí nebo zřídí alelu. Pokud je nová, není přiřazena ke genu */
	public synchronized Alela alela(final String alelaName) {
		Alela alela = alely.get(alelaName);
		if (alela == null) {
			alela = new Alela(alelaName, alely.size());
			alely.put(alelaName, alela);
		}
		return alela;
	}

	/** Vrátí nebo zřídí druh */
	public synchronized final Druh druh(final String nazev) {
		Druh druh = druhy.get(nazev);
		if (druh == null) {
			druh = new Druh(nazev, druhy.size(), this);
			druhy.put(nazev, druh);
		}
		return druh;
	}

	boolean existsGen(final String nazev) {
		return geny.containsKey(nazev);
	}

	/**
	 * Vrátí nebo zřídí gen.
	 *
	 * @param nazev
	 * @return
	 */
	public synchronized Gen gen(final String nazev) {
		Gen gen = geny.get(nazev);
		if (gen == null) {
			gen = new Gen(nazev, geny.size(), this);
			genyList.add(gen);
			geny.put(nazev, gen);
		}
		return gen;
	}

	public Collection<Alela> getAlely() {
		return Collections.unmodifiableCollection(alely.values());
	}

	public Collection<Druh> getDruhy() {
		return Collections.unmodifiableCollection(druhy.values());
	}

	/**
	 * Seznam všech registrovaných genů.
	 *
	 * @return
	 */
	public List<Gen> getGeny() {
		return Collections.unmodifiableList(genyList);
	}

	Alela locateAlela(final String alelaName) {
		return alely.get(alelaName);
	}

	Set<Alela> namesToAlely(final Set<String> jmenaAlel) {
		return jmenaAlel.stream().filter(jmeno -> jmeno != null && jmeno.length() > 0).map(jmeno -> {
			return seekAlela(jmeno);
		}).collect(Collectors.toSet());
	}

	Set<Alela> namesToAlelyIgnorujNeexistujici(final Set<String> jmenaAlel) {
		return jmenaAlel.stream().filter(jmeno -> jmeno != null && jmeno.length() > 0).map(jmeno -> {
			return seekAlela(jmeno);
		}).filter(alela -> alela != null).collect(Collectors.toSet());
	}

	Alela seekAlela(final String alelaName) {
		Alela alela = alely.get(alelaName);
		if (alela == null) {
			Genom.log.warn("Alela [{}] neni definovana!", alelaName);
			alela = alela(alelaName);
		}
		return alela;
	}
}
