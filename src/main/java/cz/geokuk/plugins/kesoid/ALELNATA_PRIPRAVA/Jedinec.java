/**
 *
 */
package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

/**
 * Jedinec nějakého druhu.
 */
public class Jedinec {

	private final Druh druh;
	private Alela[] dna;

	Jedinec(final Druh druh, final int soucasnaVelikostDna) {
		this.druh = druh;
		dna = new Alela[soucasnaVelikostDna];
	}

	/**
	 * Nechť má od teď jedinec tuto alelu. Je možné dávat jen alely, které mají gen, jinak to slítne. Pokud daný druh nemá daný gen, je přidán.
	 *
	 * @param alela
	 */
	public void add(final Alela alela) {
		final Gen gen = alela.getGen();
		if (gen == null) {
			throw new RuntimeException("Není možné přidávat alely bez genu!" + alela);
		}
		int lokus = druh.getLokus(gen);
		if (lokus == Druh.NEEXISTUJICI_LOKUS) {
			lokus = druh.addGen(gen); // zařadíme ho a dostaneme lokus toho genu
		}
		setAlela(lokus, alela);
	}

	/**
	 * Vrátí alelu pro daný gen. null, pokud gen není v druhu, ke kterému je jedinec
	 */
	public Alela getAlela(final Gen gen) {
		if (gen == null) { // pro negen není alela
			return null;
		}
		final int lokus = druh.getLokus(gen);
		if (lokus < 0) {
			return null;
		} else if (lokus >= dna.length) { // pole je kratš, ale to je z implementačních důvodů, znamená to, že máme co činit s výchozí alelou
			return gen.getVychoziAlela();
		} else {
			final Alela alela = dna[lokus];
			return alela == null ? gen.getVychoziAlela() : alela; // null je také výchozí alela.
		}
	}

	/**
	 * Vrací duhh, ke kterému je jedie přifařen.
	 *
	 * @return Nevrací null a druh se nikdy nezmění.
	 */
	public Druh getDruh() {
		return druh;
	}

	public Genom getGenom() {
		return getDruh().getGenom();
	}

	/**
	 * Zjistí, zda má jedinec danou alelu.
	 *
	 * @param alela
	 * @return
	 */
	public boolean has(final Alela alela) {
		if (alela == null) { // nemůže mít neexistující alelu
			return false;
		}
		return alela.hasGen() && getAlela(alela.getGen()) == alela;
	}

	private void setAlela(final int lokus, final Alela alela) {
		if (lokus >= dna.length) {
			final Alela[] newDna = new Alela[lokus + 1];
			System.arraycopy(dna, 0, newDna, 0, dna.length);
			dna = newDna;
		}
		dna[lokus] = alela;
	}
}
