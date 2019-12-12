package cz.geokuk.plugins.kesoid.mapicon;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.geokuk.plugins.kesoid.genetika.*;
import lombok.RequiredArgsConstructor;

public class Vrstva {

	@RequiredArgsConstructor
	private class Seznamec {
		final IconDef iconDef;
		final Seznamec next; // další položka. "Seznam má mnoho vrcholů v mapě dle symbolové alely a nakonec vždy jede dolů do seznamu spolčných icondefů.

	}

	/** Počátky definic ikon pro selektivní alelu. Je to z výkonnostích důvodů, zkracuje to počet icondefů, které musíme pro každý Genotyp porovnávat */
	private final Map<Alela, Seznamec> pocatkyIcondefovychDefinic = new HashMap<>();
	private Seznamec hlavickaObecnychIkondefu = null;

	private final Set<Alela> pouziteAlely = new HashSet<>();

	private int pocet;

	/** Selektivní geny jso geny, které mají hodně alel a alela může rychle rozhodnout, které ikondefy
	 *  prohledávat a které ne. Typicky jsou seklektivní geny geny základního symbolu, jako je třeba kategorie waymarku,
	 */
	private final Set<Gen> selektivniGeny = new LinkedHashSet<>();

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

		for (final Seznamec pocatek: najdiPocatky(genotyp)) {
			for (Seznamec seznamec = pocatek; seznamec != null; seznamec = seznamec.next) {
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
		}
		//System.out.println("Procházeny seznamce: " + citac + " / " + xcitac);
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
			final Alela selektivniAlela = iconDef.getSelektivniAlela();
			if (selektivniAlela == null) {
				hlavickaObecnychIkondefu = new Seznamec(iconDef, hlavickaObecnychIkondefu);
				pocet++;
			} else {
				selektivniGeny.add(selektivniAlela.getGen());
				pocatkyIcondefovychDefinic.compute(selektivniAlela, (__, seznamec) -> new Seznamec(iconDef, seznamec));
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

	private List<Seznamec> najdiPocatky(final Genotyp genotyp) {
		return Stream.concat(
				selektivniGeny.stream()
				.filter(genotyp.getDruh()::hasGen) // jen ty selektivní geny, které má druh zkoumaného genotypu
				.map(genotyp::getAlela)  // a cheme jeho alely
				.map(pocatkyIcondefovychDefinic::get) // chceme počáteční seznamec
				.filter(s -> s != null) // ale pokud seznamec nebyl, tak nic
				,
				Stream.of(hlavickaObecnychIkondefu) // v každém případě procházíme obecné
				).collect(Collectors.toList());

	}

}
