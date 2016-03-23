package cz.geokuk.plugins.kesoid.mapicon;

import java.util.*;

public class Vrstva {

	private class Seznamec {
		Seznamec	next;	// další položka. "Seznam má mnoho vrcholů v mapě dle symbolové alely a nakonec vždy jede dolů do seznamu spolčných icondefů.
		IconDef		iconDef;

	}

	private final Map<Alela, Seznamec>	icondefsProSymbol	= new HashMap<>();
	private final Seznamec				hlavickaObecnych	= new Seznamec();

	private final Set<Alela>			pouziteAlely		= new HashSet<>();

	private int							pocet;

	public Set<Alela> getPouziteAlely() {
		return pouziteAlely;
	}

	/**
	 * Nalezne jediný icondef vyhovující danému genotypu, nebo vrátí null, pokud nic nevyhovuje
	 *
	 * @param genotyp
	 * @return
	 */
	public IconDef locate(final Genotyp genotyp) {

		final Set<Alela> hledaneAlely = genotyp.getAlely();
		final List<IconDef> vybrane = new ArrayList<>(pocet);
		int maxPriorita = -1;

		for (Seznamec seznamec = najdiPocatek(genotyp); seznamec != null; seznamec = seznamec.next) {
			final IconDef iconDef = seznamec.iconDef;
			if (iconDef == null) {
				continue; // to bude určtitě v hlavičce obecných
			}
			for (final IconSubDef subDef : iconDef.getSubdefs()) {
				if (hledaneAlely.containsAll(subDef.alely)) { // je to kandidát
					if (iconDef.priorita > maxPriorita) { // vysoka priorita, přebíjí všechny jiné
						vybrane.clear();
						vybrane.add(iconDef);
						maxPriorita = iconDef.priorita;
					} else if (iconDef.priorita < maxPriorita) {
						// ignorujeme toto, protože už máme s vyšší prioritou
					} else { // máme nejvyšší prioritu, musíme tedy zjistit, které jsou méně obecné
						boolean pridat = true;
						for (final ListIterator<IconDef> it = vybrane.listIterator(); it.hasNext();) {
							final IconDef icondef2 = it.next();
							for (final IconSubDef subDef2 : icondef2.getSubdefs()) {
								if (subDef.alely.containsAll(subDef2.alely)) { // tento je konkretnejsi než seznamový nebo stejný, vymažme ze seznamu
									it.remove();
								} else if (subDef2.alely.containsAll(subDef.alely)) { // ten v seznamu je konkrétnější ignorujeme
									pridat = false;
								}
							}
						}
						if (pridat) {
							vybrane.add(iconDef);
						}
					}
				}
			}
		}
		// nyní máme vybrané podle jednoduchého kritéria, pokud je jich zde moc, nelze jednoduše určit
		// vezmeme tedy jeden z nich
		if (vybrane.isEmpty()) {
			return null; // nic jsme nenašli
		}
		if (vybrane.size() > 1) {
			error("Naslo se toho moc", genotyp, vybrane);
		}

		return vybrane.get(0); // a ten první vátit
	}

	void add(final IconDef iconDef) {
		if (iconDef != null) {
			final Alela alelaSym = iconDef.getAlelaSym();
			final Seznamec seznamecPredNeho = new Seznamec();
			seznamecPredNeho.iconDef = iconDef;
			if (alelaSym == null) {
				seznamecPredNeho.next = hlavickaObecnych.next;
				hlavickaObecnych.next = seznamecPredNeho;
				pocet++;
			} else {
				Seznamec seznamec = icondefsProSymbol.get(alelaSym);
				if (seznamec == null) {
					seznamec = hlavickaObecnych;
				}
				seznamecPredNeho.next = seznamec;
				icondefsProSymbol.put(alelaSym, seznamecPredNeho);
				pocet++;
			}
			// A Ještě schovat použité alely
			for (final IconSubDef subDef : iconDef.getSubdefs()) {
				pouziteAlely.addAll(subDef.alely);
			}
		}

	}

	/**
	 * @param aString
	 * @param genotyp
	 * @param vybrane
	 */
	private void error(final String aString, final Genotyp genotyp, final List<IconDef> vybrane) {
		System.err.println("Našlo se toho moc na zobrazení pro: " + genotyp);
		for (final IconDef iconDef : vybrane) {
			System.err.println("    " + iconDef.getSubdefs() + "  -  " + iconDef.idp.url);
		}
	}

	private Seznamec najdiPocatek(final Genotyp genotyp) {
		final Alela alelaSym = genotyp.getAlelaSym();
		Seznamec pocatek;
		if (alelaSym == null) {
			return hlavickaObecnych;
		}
		pocatek = icondefsProSymbol.get(alelaSym);
		if (pocatek != null) {
			return pocatek;
		}
		return hlavickaObecnych;
	}

}
