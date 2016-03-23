package cz.geokuk.plugins.kesoid.mapicon;

import java.util.*;

/**
 * Definice ikony.
 * 
 * @author tatinek
 *
 */
public class IconDef {
	/** URL s definičním souborem, může to být obrázek, ale i properties s definicí */
	IkonDrawingProperties	idp;
	public int				priorita;

	// Jen jedna je nastavena. Před rozbalením je to alelyx, po rozbalení subdefs
	private Set<Alela>		alelyx;
	private Set<IconSubDef>	subdefs;

	// Alela symbolu. Je to redundantní informace, pomůže však pro zkrácení vykreslovací fronty.
	private Alela			alelaSym;

	@Override
	public String toString() {
		return idp.url.toString();
	}

	synchronized Set<IconSubDef> getSubdefs() {
		if (subdefs == null) {
			subdefs = new HashSet<>();
			rozmnoz(alelyx, subdefs);
			alelyx = null;
		}
		return subdefs;
	}

	/**
	 * Rozmnoží set alel tak, aby ve výsledých setech byla pro každý gen jedna alela.
	 * 
	 * @param set
	 * @return
	 */
	private void rozmnoz(Set<Alela> set, Set<IconSubDef> sese) {
		// Set<Set<Alela>> sese = new HashSet<Set<Alela>>();
		Map<Gen, Alela> geny = new HashMap<>();
		for (Alela alela : set) {
			if (!alela.hasGen()) {
				continue;
			}
			Gen gen = alela.getGen();
			Alela lastAlela = geny.get(gen);
			if (lastAlela != null) { // duplicita
				Set<Alela> set1 = new HashSet<>(set);
				Set<Alela> set2 = new HashSet<>(set);
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

	void setAlelyx(Set<Alela> alelyx) {
		this.alelyx = alelyx;
	}

	Set<Alela> getAlelyx() {
		return alelyx;
	}

	public void setAlelaSym(Alela alelaSym) {
		this.alelaSym = alelaSym;
	}

	public Alela getAlelaSym() {
		return alelaSym;
	}
}
