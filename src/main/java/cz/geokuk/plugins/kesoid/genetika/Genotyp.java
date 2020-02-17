/**
 *
 */
package cz.geokuk.plugins.kesoid.genetika;

import java.util.*;
import java.util.stream.Collectors;

import cz.geokuk.plugins.kesoid.genetika.Genom.CitacAlel;
import lombok.AllArgsConstructor;

/**
 * Genotyp jako množina alel. Typ je immutable, různé instance mají různou množinu alel.
 */
public class Genotyp implements Indexable {

	private final Druh druh;
	private final Alela[] dna;
	private final int index;

	/** prechody na jiny genotyp */
	private final IndexMap<Alela, Genotyp> prechod = new IndexMap<>();

	Genotyp(final Druh druh, final Set<Alela> alely, final int index) {
		this.druh = druh;
		this.index = index;
		dna = new Alela[druh.geny.size()];
		alely.forEach(alela -> {
			dna[druh.getLokus(alela.getGen())] = alela;
		});
	}

	/**
	 * Vrátí genotyp s novou alelou.
	 *
	 * @param alela
	 *            Alela, která má nahradit stávající alelu v genu.
	 * @return Nový genetyp s vyměněou alelou.
	 */
	public Genotyp with(final Alela alela) {
		return prechod.computeIfAbsent(alela, a -> druh.genotyp(vymenAlelu(a)));
	}

	/**
	 * Vrátí genotyp s vyměněnými alelami.
	 *
	 * @param alelas
	 *            Různých genů alely, nedefinováno, pokud je zde více alel stejného genu.
	 * @return
	 */
	public Genotyp with(final Iterable<Alela> alelas) {
		Genotyp g = this;
		for (final Alela alela : alelas) {
			g = g.with(alela);
		}
		return g;
	}

	/**
	 * Vrátí genotyp s vyměněnými alelami.
	 *
	 * @param alelas
	 *            Různých genů alely, nedefinováno, pokud je zde více alel stejného genu.
	 * @return
	 */
	public Genotyp with(final Alela... alely) {
		return with(Arrays.asList(alely));
	}

	/**
	 * Odstraní alelu, což znamená, že ji nahradí default alelou u daného genu. ale jen v případě, kdy jedinec měl tuto alelu, jinak ne.
	 *
	 * @param alela
	 *            Alela bez které to má být. Myslí se tím, že je nahrazena výchozí alelou genu. Když je sama výchozí, vrátí se objekt nezměněný.
	 */
	public Genotyp without(final Alela alela) {
		final Gen gen = alela.getGen();
		if (getAlela(gen) == alela) {
			return with(gen.getVychoziAlela()); // jen když tuto alelu máme, můžeme být bez ní
		} else {
			return this;
		}
	}

	/**
	 * Vrátí genotyp s vyměněnými alelami.
	 *
	 * @param alelas
	 *            Různých genů alely, nedefinováno, pokud je zde více alel stejného genu.
	 * @return
	 */
	public Genotyp without(final Iterable<Alela> alelas) {
		Genotyp g = this;
		for (final Alela alela : alelas) {
			g = g.without(alela);
		}
		return g;
	}

	/**
	 * Odstraní alelu, což znamená, že ji nahradí default alelou u daného genu. ale jen v případě, kdy jedinec měl tuto alelu, jinak ne.
	 *
	 * @param alela
	 *            Alela bez které to má být. Myslí se tím, že je nahrazena výchozí alelou genu. Když je sama výchozí, vrátí se objekt nezměněný.
	 */
	public Genotyp without(final Alela... alely) {
		return without(Arrays.asList(alely));
	}

	/**
	 * Vrátí alelu pro daný gen. Gen musí být v druhu, ve kterém je jedinec.
	 * Výsledky metody se mohou lišit pro stejný jedinec, pokud byl přidán gen do druhu později. liší se, ale pouze v tom,
	 * že přestanou vyhazovat výjimku.
	 */
	public Alela getAlela(final Gen gen) {
		if (gen == null) { // pro negen není alela
			throw new NullPointerException("Gen musí být dán");
		}
		final int lokus = druh.getLokus(gen);
		if (lokus < 0) {
			throw new NullPointerException("Druh " + druh + " nemá gen " + gen);
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
		return getAlela(alela.getGen()) == alela;
	}

	/**
	 * @return Seznam alel jako množinu, ale jen alely, které nejsou výchozí.
	 */
	private Set<Alela> getNevychoziAlely() {
		final HashSet<Alela> set = new HashSet<Alela>(Arrays.asList(dna));
		set.remove(null); // pokud v poli bylo null, bude to v setu jednou a smažeme to
		return set;
	}



	/**
	 * Vrátí množinu alel s tím, že jedna je vyměněná. Vyhodí alelu stejného genu, která tam byla místo toho se přidá zadaná alela. Pokud je to výchozí alela, tak se npřidává.
	 *
	 * @param alela
	 *            Alela, která má být v množině.
	 * @return Seznam alel genu, ale nebude tam nikdy výchozí alela.
	 */
	private Set<Alela> vymenAlelu(final Alela alela) {
		if (!druh.hasGen(alela.getGen())) {
			druh.addGen(alela.getGen());
			System.err.println("Pri vymene alely " + alela.qualName() + " v genotypu " + this + " bylo zjisteno, ze v hjeho druhu neni gen " + alela.getGen());
		}
		final Set<Alela> alely = getNevychoziAlely(); // stávající alely
		alely.remove(getAlela(alela.getGen())); // pokud tam byla alela tohoto genu, zahubíme ji
		if (!alela.isVychozi()) {
			alely.add(alela); // pokud alela není výchozí přidáme ji
		}
		return alely;
	}

	@Override
	public int getIndex() {
		return index;
	}


	private Set<Alela> alely;
	private int minulyPocetGenu;

	/**
	 * @return Sezanm všech alel aktuálně přiřazených ke genu všetně defaultních alel.
	 * Pokud je přidán do druhu existujícího genotuypu gen, má to vliv na výsledek této funkce, bude vracena jeho defaultní alela.
	 * Používejme jen tam, kde opravdu alely potřebujeme, existují funkce na testování, zda mám potřebné alely, jenž jso rychlejší.
	 * @return
	 */
	public Set<Alela> getAlely() {
		if (alely == null || minulyPocetGenu != druh.geny.size()) {
			final int genySize = druh.geny.size();
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
			minulyPocetGenu = druh.geny.size();
		}
		return alely;
	}


	/**
	 * Pokud je přidán do druhu existujícího genotuypu gen, má to vliv na výsledek této funkce, bude vracena jeho defaultní alela.
	 * @return Vrátí kvalifikovaná jména všech alel všetně výchozích.
	 */
	public QualAlelaNames getQualAlelaNames() {
		return Alela.alelyToQualNames(getAlely());
	}


	@Override
	public String toString() {
		final String alelyNamesStr = Arrays.stream(dna)
				.filter(x -> x != null)
				.map(Alela::qualName)
				.collect(Collectors.joining(", "));

		return "Genotyp [druh=" + druh + ", index=" + index + ", dna=[" + alelyNamesStr + "]]";
	}

	/**
	 * Zjistí, zda má aspoň jednu alelu.
	 * @param nechteneAlely
	 * @return
	 */
	public boolean hasAny(final Set<Alela> alely) {
		return alely.stream().anyMatch(this::has);
	}

	/**
	 * Zjistí, zda má všechny požadované alely.
	 * @param nechteneAlely
	 * @return
	 */
	public boolean hasAll(final Set<Alela> alely) {
		return alely.stream().allMatch(this::has);
	}

	public void countTo(final CitacAlel citacAlel) {
		for (final Alela alela : getAlely()) {
			assert alela != null;
			citacAlel.add(alela);
		}

	}

	/**
	 *
	 * @param alely
	 * @return
	 */
	public Genotyp zuzNaObrazkove(final Set<Alela> alely) {
		// TODO [veverka] Prozkoumat co to dělá. -- 13. 12. 2019 14:12:14 veverka
		Genotyp g = this;
		for (final Alela a : getAlely()) {
			if (! alely.contains(a)) {
				if (! alely.contains(a.getGen().getVychoziAlela())) {
					g = g.without(a);
				}
			}
		}
		return g;
	}

	public <T> T ifuj(final Iface<T> iface) {
		return iface.ifuj(new Ifer<T>(), getGenom());
	}

	@FunctionalInterface
	public interface Iface<T> {
		T ifuj(Ifer<T> maper, Genom genom);
	}

	/** Ifování podle alel a vyrábění objektů */
	public class Ifer<T> {
		T vybrany;

		/** IF Alela then this object */
		public Ifer<T> ifa (final Alela alela, final T obj) {
			if (Genotyp.this.has(alela)) {
				vybrany = obj;
			}
			return this;
		}

		/** else this value */
		public T els(final T obj) {
			return vybrany == null ? obj : vybrany;
		}
	}

	@FunctionalInterface
	public interface Uface<T> {
		Uface<T> ufuj(Updator<T> maper, Genom genom);
	}

	/**
	 * Aktualiuje alelu porovnáním nějaké hodnoty s jinou hodnotou.
	 * @param <T>
	 * @param obj
	 * @param updator
	 * @return
	 */
	public <T> Genotyp update(final T obj, final Uface<T> updator) {
		final Updator<T> upd = new Updator<T>(obj, this);
		updator.ufuj(upd,  getGenom());
		return upd.genotyp;
	}


	@AllArgsConstructor
	public class Updator<T> {
		private final T obj;
		Genotyp genotyp;

		public Updator<T> iff(final T o, final Alela alela) {
			if (o.equals(obj)) {
				genotyp = genotyp.with(alela);
			}
			return this;
		}
	}



}
