package cz.geokuk.plugins.kesoid.mapicon;

import java.util.*;

import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Gen;

/**
 * Definice ikony.
 *
 * @author Martin Veverka
 *
 */
public class IconDef {
	/** URL s definičním souborem, může to být obrázek, ale i properties s definicí */
	IkonDrawingProperties idp;
	public int priorita;

	// Jen jedna je nastavena. Před rozbalením je to alelyx, po rozbalení subdefs
	private Set<Alela> alelyx;
	private Set<IconSubDef> subdefs;

	// Alela symbolu. Je to redundantní informace, pomůže však pro zkrácení vykreslovací fronty.
	private Alela alelaSym;

	public Alela getAlelaSym() {
		return alelaSym;
	}

	public void setAlelaSym(final Alela alelaSym) {
		this.alelaSym = alelaSym;
	}

	@Override
	public String toString() {
		return idp.url.toString();
	}

	Set<Alela> getAlelyx() {
		return alelyx;
	}

	synchronized Set<IconSubDef> getSubdefs() {
		if (subdefs == null) {
			subdefs = new HashSet<>();
			rozmnoz(alelyx, subdefs);
			alelyx = null;
		}
		return subdefs;
	}

	void setAlelyx(final Set<Alela> alelyx) {
		this.alelyx = alelyx;
	}

	/**
	 * Rozmnoží set alel tak, aby ve výsledých setech byla pro každý gen jedna alela.
	 *
	 * @param set
	 * @return
	 */
	private void rozmnoz(final Set<Alela> set, final Set<IconSubDef> sese) {
		// Set<Set<Alela>> sese = new HashSet<Set<Alela>>();
		final Map<Gen, Alela> geny = new HashMap<>();
		for (final Alela alela : set) {
			if (!alela.hasGen()) {
				continue;
			}
			final Gen gen = alela.getGen();
			final Alela lastAlela = geny.get(gen);
			if (lastAlela != null) { // duplicita
				final Set<Alela> set1 = new HashSet<>(set);
				final Set<Alela> set2 = new HashSet<>(set);
				set1.remove(alela);
				set2.remove(lastAlela);
				rozmnoz(set1, sese);
				rozmnoz(set2, sese);
			}
			geny.put(gen, alela);
		}
		// Nebyly zjištěny žádné duplicity
		sese.add(new IconSubDef(set));

	}
}
