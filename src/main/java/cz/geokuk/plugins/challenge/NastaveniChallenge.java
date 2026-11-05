package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Uložení a načtení vlastních definic challenge, aktivních a zapnutí plánovače do {@link ChallengeUloziste}. Klíče: {@code format}, {@code zapnuto}, {@code aktivni}
 * (názvy po řádcích) a {@code definice.verze.N.klíč}, {@code verze} pro každou vlastní definici. Šablony se neukládají, jsou v kódu. Chybná nebo neznámá definice se při načtení přeskočí
 * s varováním, aby nastavení nikdy nezabránilo startu programu.
 */
@Slf4j
public final class NastaveniChallenge {

	private static final String FORMAT = "1";
	private static final String PREDPONA = "definice.";

	/** Načtený stav. */
	public static final class Nacteno {
		public final ChallengeSeznam seznam;
		public final boolean zapnuto;
		/** Kolik uložených definic se nepodařilo použít. */
		public final int preskoceno;
		/** Uložené nastavení je v novějším formátu; nesmí se přepisovat. */
		public final boolean neznamyFormat;

		Nacteno(final ChallengeSeznam seznam, final boolean zapnuto, final int preskoceno) {
			this(seznam, zapnuto, preskoceno, false);
		}

		Nacteno(final ChallengeSeznam seznam, final boolean zapnuto, final int preskoceno, final boolean neznamyFormat) {
			this.neznamyFormat = neznamyFormat;
			this.seznam = seznam;
			this.zapnuto = zapnuto;
			this.preskoceno = preskoceno;
		}
	}

	private NastaveniChallenge() {}

	/**
	 * Uloží stav. Nová sada definic se zapíše pod novou verzí a teprve potom se přepne klíč {@code verze}; při chybě zápisu zůstává původní sada beze změny a
	 * nedopsané klíče se smažou při příštím úspěšném uložení.
	 */
	public static void uloz(final ChallengeSeznam seznam, final boolean zapnuto, final ChallengeUloziste uloziste) {
		final String staraVerze = uloziste.get("verze");
		final String novaVerze = Integer.toString(nejvyssiVerze(uloziste, cisloVerze(staraVerze)) + 1);
		final Map<String, String> zapis = new java.util.LinkedHashMap<>();
		int i = 0;
		for (final Challenge c : seznam.vlastni()) {
			for (final Map.Entry<String, String> e : c.naMapu().entrySet()) {
				zapis.put(PREDPONA + novaVerze + "." + i + "." + e.getKey(), e.getValue());
			}
			i++;
		}
		for (final Map.Entry<String, String> e : zapis.entrySet()) {
			ChallengeSeznam.zkontrolujUlozitelne(e.getKey(), e.getValue());
		}
		final StringBuilder aktivni = new StringBuilder();
		for (final Challenge c : seznam.aktivni()) {
			if (aktivni.length() > 0) {
				aktivni.append('\n');
			}
			aktivni.append(c.nazev());
		}
		ChallengeSeznam.zkontrolujUlozitelne("aktivni", aktivni.toString());
		for (final Map.Entry<String, String> e : zapis.entrySet()) {
			uloziste.put(e.getKey(), e.getValue());
		}
		uloziste.put("verze", novaVerze);
		uloziste.put("format", FORMAT);
		uloziste.put("zapnuto", Boolean.toString(zapnuto));
		if (aktivni.length() > 0) {
			uloziste.put("aktivni", aktivni.toString());
		} else {
			uloziste.remove("aktivni");
		}
		final String zacatek = PREDPONA + novaVerze + ".";
		for (final String k : new ArrayList<>(uloziste.klice())) {
			if (k.startsWith(PREDPONA) && !k.startsWith(zacatek)) {
				uloziste.remove(k);
			}
		}
	}

	/** Nejvyšší verze, pod kterou už něco leží, ať dvě instance nezapisují do stejné sady. */
	private static int nejvyssiVerze(final ChallengeUloziste uloziste, final int vychozi) {
		int nejvyssi = vychozi;
		for (final String k : uloziste.klice()) {
			if (!k.startsWith(PREDPONA)) {
				continue;
			}
			final int tecka = k.indexOf('.', PREDPONA.length());
			if (tecka > PREDPONA.length()) {
				nejvyssi = Math.max(nejvyssi, cisloVerze(k.substring(PREDPONA.length(), tecka)));
			}
		}
		return nejvyssi;
	}

	private static int cisloVerze(final String verze) {
		try {
			return verze == null ? 0 : Math.max(0, Integer.parseInt(verze));
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	public static Nacteno nacti(final ChallengeUloziste uloziste) {
		final ChallengeSeznam seznam = new ChallengeSeznam();
		int preskoceno = 0;
		final String format = uloziste.get("format");
		if (format != null && !FORMAT.equals(format)) {
			log.warn("Nastavení challenge má neznámý formát {}, použijí se výchozí hodnoty", format);
			return new Nacteno(seznam, false, 0, true);
		}
		final Map<Integer, Map<String, String>> definice = new TreeMap<>();
		final String zacatek = PREDPONA + cisloVerze(uloziste.get("verze")) + ".";
		for (final String k : uloziste.klice()) {
			if (!k.startsWith(zacatek)) {
				continue;
			}
			final String zbytek = k.substring(zacatek.length());
			final int tecka = zbytek.indexOf('.');
			if (tecka <= 0) {
				preskoceno++;
				continue;
			}
			try {
				final int cislo = Integer.parseInt(zbytek.substring(0, tecka));
				final String hodnota = uloziste.get(k);
				if (hodnota != null) {
					definice.computeIfAbsent(cislo, x -> new TreeMap<>()).put(zbytek.substring(tecka + 1), hodnota);
				}
			} catch (final NumberFormatException e) {
				preskoceno++;
			}
		}
		for (final Map.Entry<Integer, Map<String, String>> e : definice.entrySet()) {
			try {
				final int pred = seznam.vlastni().size();
				seznam.ulozVlastni(Challenge.zMapy(e.getValue()));
				if (seznam.vlastni().size() == pred) {
					log.warn("Uložená definice challenge č. {} má stejný název jako dřívější, nahradila ji", e.getKey());
					preskoceno++;
				}
			} catch (final IllegalArgumentException | ChallengeChyba ex) {
				log.warn("Uložená definice challenge č. {} se přeskočí: {}", e.getKey(), ex.getMessage());
				preskoceno++;
			}
		}
		final String aktivni = uloziste.get("aktivni");
		final Set<String> videne = new HashSet<>();
		if (aktivni != null) {
			for (final String nazev : aktivni.split("\n")) {
				if (nazev.isEmpty() || !videne.add(nazev)) {
					continue;
				}
				try {
					seznam.aktivuj(nazev);
				} catch (final ChallengeChyba ex) {
					log.warn("Aktivní challenge „{}“ se přeskočí: {}", nazev, ex.getMessage());
					preskoceno++;
				}
			}
		}
		return new Nacteno(seznam, Boolean.parseBoolean(uloziste.get("zapnuto")), preskoceno);
	}
}
