package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import java.util.*;

public class Druh {

	static int NEEXISTUJICI_LOKUS = -1;

	private final Genom genom;
	private final String nazev;
	private String displayName;
	final int poradiVDruzich;
	/* Je to počet genů v druhu, tedy i lokus nad poslením lokusem */
	private Gen firstGen;

	/**
	 * Geny, které jsou zařazeny v druhu, délka je stejné jako počet genů v genomu. Indexy v poli odpovídají pořadí genu v genomu. Data jsou lokusy gentů vb dna příslušného druhu. Čísla jsou v pli vždy různá.
	 */
	private final List<Integer> lokusy = new ArrayList<>();

	/**
	 * Seznam genů v druhu. Indexy jsou lokusu genu v druhu. Použije se pro zjištění výchozích alel jedince.
	 */
	final List<Gen> geny = new ArrayList<>();

	Druh(final String nazev, final int poradiVDruzich, final Genom genom) {
		this.nazev = nazev;
		this.poradiVDruzich = poradiVDruzich;
		this.genom = genom;
	}

	/** zařadí gen do druhu, pokud už tam není */
	public int addGen(final Gen gen) {
		if (gen == null) {
			throw new NullPointerException("Gen nesmí být null");
		}
		// Naslepo nadimenzovat pole lokusů, zatím nepřidělujeme
		while (lokusy.size() <= gen.poradiVGenomu) {
			lokusy.add(Druh.NEEXISTUJICI_LOKUS);
		}
		int lokus = lokusy.get(gen.poradiVGenomu);
		if (lokus == Druh.NEEXISTUJICI_LOKUS) {
			lokus = geny.size();
			geny.add(gen); // a zároveň dát do genů
			lokusy.set(gen.poradiVGenomu, lokus); // a zároveň do lokusů
		}
		if (firstGen == null) {
			firstGen = gen; // první přidaný gen je první
		}
		return lokus;

	}

	public Druh displayName(final String displayName) {
		this.displayName = displayName;
		return this;
	}

	public String getDisplayName() {
		return displayName == null ? nazev : displayName;
	}

	public Genom getGenom() {
		return genom;
	}

	/**
	 * Lokus pro daná gen.
	 *
	 * @param gen
	 * @return null, pokud gen v druhu není.
	 */
	int getLokus(final Gen gen) {
		final int poradiVGenomu = gen.poradiVGenomu;
		if (poradiVGenomu >= lokusy.size()) {
			return Druh.NEEXISTUJICI_LOKUS;
		}
		return lokusy.get(poradiVGenomu); // podle pořadí v genomu získáme lokus genu
	}

	public String getNazev() {
		return nazev;
	}

	/**
	 * Zrodí se nový jedinec. Zrodí se s výchozími alelami.
	 */
	public Jedinec zrozeni() {
		return new Jedinec(this, geny.size()); // to je současná velikost DNA
	}

	/**
	 * Zrodí se nový jedinec. Zrodí se s výchozími alelami.
	 */
	public Jedinec zrozeni(final Set<Alela> alelas) {
		final Jedinec jedinec = zrozeni();
		jedinec.put(alelas);
		return jedinec;
	}

	/**
	 * Vrátí první gen
	 *
	 * @return
	 */
	public Gen getFirstGen() {
		if (firstGen == null) {
			throw new NullPointerException("Druhn " + this + " ještě nemá žádný gen");
		}
		return firstGen;
	}

	@Override
	public String toString() {
		return nazev;
	}
}
