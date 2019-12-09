/**
 *
 */
package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import java.util.HashSet;
import java.util.Set;

/**
 * Jedinec nějakého druhu.
 */
public class Jedinec {

	private final Druh druh;
	private Alela[] dna;
	private Set<Alela> alely;

	Jedinec(final Druh druh, final int soucasnaVelikostDna) {
		this.druh = druh;
		dna = new Alela[soucasnaVelikostDna];
	}

	/**
	 * Nechť má od teď jedinec tuto alelu. Je možné dávat jen alely, které mají gen, jinak to slítne. Pokud daný druh nemá daný gen, je přidán.
	 *
	 * @param alela
	 */
	public void put(final Alela alela) {
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

	public void put(final Iterable<Alela> alelas) {
		alelas.forEach(this::put);
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
			zmenDelkuDna(lokus + 1);
		}
		dna[lokus] = alela;
		alely = null;
	}

	private void zmenDelkuDna(final int novaDelka) {
		final Alela[] newDna = new Alela[novaDelka];
		System.arraycopy(dna, 0, newDna, 0, dna.length);
		dna = newDna;
	}

	/**
	 * @deprecated Určitě neceme dělat seznam alel, ale cheme něco výkonnějšího nejlépe přímo v jedinci.
	 * @return
	 */
	@Deprecated
	public Set<Alela> getAlely() {
		final int genySize = druh.geny.size();
		if (dna.length != genySize) {
			alely = null;
			zmenDelkuDna(genySize);
		}
		if (alely == null) {
			final HashSet<Alela> set = new HashSet<Alela>();
			for (int i = 0; i < genySize; i++) {
				if (i >= dna.length) { // nemáme takovou explicitní DNA, musí to být výchozky
					final Gen gen = druh.geny.get(i);
					set.add(gen.getVychoziAlela());
				} else {
					final Alela alela = dna[i];
					if (alela == null) { // pokud nemám specifickou alelu, tak použiji výchozí svého genu.
						final Gen gen = druh.geny.get(i);
						set.add(gen.getVychoziAlela());
					} else {
						set.add(alela);
					}
				}
			}
			alely = set;
		}
		return alely;
	}

	/**
	 * Odstraní alelu, což znamená, že ji nahradí default alelou u daného genu. ale jen v případě, kdy jedinec měl tuto alelu, jinak ne.
	 *
	 * @param alela
	 */
	public void remove(final Alela alela) {
		if (alela == null || !alela.hasGen()) { // žádná alela nebo alela bez genu nelze odstranit, neboť tam určitě není.
			return;
		}
		final Gen gen = alela.getGen();
		final int lokus = druh.getLokus(gen);
		if (dna[lokus] == alela) {
			dna[lokus] = gen.getVychoziAlela();
			alely = null;
		}
	}

	/**
	 * @deprecated Mělo by se jít přes druhy.
	 * @return
	 */
	@Deprecated
	public Alela getAlelaSym() {
		return getAlela(druh.getGenom().getSymGen());
	}

	/**
	 * FIXME genetika: zrevidovat, zda opravdu takto. Přejmenovat na remove
	 *
	 * @param fenotypoveZakazaneAlely
	 */
	public void removeAll(final Iterable<Alela> alely) {
		alely.forEach(this::remove);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getAlely() == null ? 0 : getAlely().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Jedinec other = (Jedinec) obj;
		if (getAlely() == null) {
			if (other.getAlely() != null) {
				return false;
			}
		} else if (!getAlely().equals(other.getAlely())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Jedinec [druh=" + druh + ", alely=" + getAlely() + "]";
	}
}
