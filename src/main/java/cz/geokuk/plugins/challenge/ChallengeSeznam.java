package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Seznam definic challenge: šablony jen ke čtení, vlastní definice (včetně kopií šablon) a aktivní challenge. Názvy jsou jednoznačné bez ohledu na velikost písmen.
 * Aktivních je nejvýš {@link #NEJVIC_AKTIVNICH}. Třída je bez vazby na model programu, hlídá jen pravidla; změny nehlásí a z vláken ji nepoužívat souběžně.
 */
public final class ChallengeSeznam {

	/** Výchozí strop aktivních challenge najednou: filtry a štítky na mapě patří jedné challenge. */
	public static final int NEJVIC_AKTIVNICH = 1;

	public static final int NEJDELSI_NAZEV = 80;

	private final List<Challenge> sablony;
	private final Map<String, Challenge> vlastni = new LinkedHashMap<>();
	private final List<String> aktivni = new ArrayList<>();
	private final int strop;

	public ChallengeSeznam() {
		this(Sablony.vse(), NEJVIC_AKTIVNICH);
	}

	public ChallengeSeznam(final List<Challenge> sablony, final int strop) {
		if (strop < 1) {
			throw new IllegalArgumentException("Strop aktivních challenge musí být aspoň 1");
		}
		this.sablony = Collections.unmodifiableList(new ArrayList<>(sablony));
		this.strop = strop;
	}

	private static String klic(final String nazev) {
		return nazev.trim().toLowerCase(Locale.ROOT);
	}

	public int strop() {
		return strop;
	}

	public List<Challenge> sablony() {
		return sablony;
	}

	public List<Challenge> vlastni() {
		return Collections.unmodifiableList(new ArrayList<>(vlastni.values()));
	}

	/** Šablony a potom vlastní definice. */
	public List<Challenge> vsechny() {
		final List<Challenge> v = new ArrayList<>(sablony);
		v.addAll(vlastni.values());
		return Collections.unmodifiableList(v);
	}

	public boolean jeSablona(final String nazev) {
		return najdiSablonu(nazev) != null;
	}

	private Challenge najdiSablonu(final String nazev) {
		for (final Challenge s : sablony) {
			if (klic(s.nazev()).equals(klic(nazev))) {
				return s;
			}
		}
		return null;
	}

	/** Definice podle názvu (šablona nebo vlastní), nebo null. */
	public Challenge najdi(final String nazev) {
		final Challenge s = najdiSablonu(nazev);
		return s != null ? s : vlastni.get(klic(nazev));
	}

	/** Aktivní challenge v pořadí, v jakém byly zapnuty. */
	public List<Challenge> aktivni() {
		final List<Challenge> v = new ArrayList<>();
		for (final String k : aktivni) {
			v.add(najdi(k));
		}
		return Collections.unmodifiableList(v);
	}

	public boolean jeAktivni(final String nazev) {
		return aktivni.contains(klic(nazev));
	}

	private static String platnyNazev(final String nazev) {
		final String n = nazev == null ? "" : nazev.trim();
		if (n.isEmpty()) {
			throw new ChallengeChyba("Challenge musí mít název.");
		}
		if (n.length() > NEJDELSI_NAZEV) {
			throw new ChallengeChyba("Název challenge může mít nejvýš " + NEJDELSI_NAZEV + " znaků.");
		}
		for (int i = 0; i < n.length(); i++) {
			if (Character.isISOControl(n.charAt(i))) {
				throw new ChallengeChyba("Název challenge nesmí obsahovat zalomení řádku ani jiné řídicí znaky.");
			}
		}
		return n;
	}

	/** Nejdelší uložená hodnota; preference snesou 8192 znaků. */
	static final int NEJDELSI_HODNOTA = 4000;

	/** Ověří, že jde klíč s hodnotou uložit do preferencí. */
	static void zkontrolujUlozitelne(final String klic, final String hodnota) {
		if (klic.length() > 80) {
			throw new ChallengeChyba("Definice je příliš složitá k uložení.");
		}
		if (hodnota.length() > NEJDELSI_HODNOTA) {
			throw new ChallengeChyba("Definice je příliš rozsáhlá k uložení (nejvýš " + NEJDELSI_HODNOTA + " znaků v jedné položce).");
		}
		if (hodnota.indexOf('\0') >= 0) {
			throw new ChallengeChyba("Definice obsahuje nepovolený znak.");
		}
	}

	/**
	 * Uloží vlastní definici: nová se přidá, definice stejného názvu se nahradí (zůstane-li aktivní, zůstává). Šablonu přepsat nejde, uloží se její kopie pod jiným
	 * názvem, viz {@link #kopieSablony(String)}.
	 */
	public Challenge ulozVlastni(final Challenge definice) {
		final String nazev = platnyNazev(definice.nazev());
		if (jeSablona(nazev)) {
			throw new ChallengeChyba("Šablonu „" + najdiSablonu(nazev).nazev() + "“ nejde přepsat, uložte vlastní kopii pod jiným názvem.");
		}
		final Challenge ulozena = nazev.equals(definice.nazev()) ? definice : definice.sNazvem(nazev);
		for (final Map.Entry<String, String> e : ulozena.naMapu().entrySet()) {
			zkontrolujUlozitelne("definice.999999.99." + e.getKey(), e.getValue());
		}
		vlastni.put(klic(nazev), ulozena);
		return ulozena;
	}

	/** Vlastní kopie šablony k úpravě, pojmenovaná „název (vlastní)“, popř. s číslem; uloží se do vlastních definic. */
	public Challenge kopieSablony(final String nazevSablony) {
		final Challenge s = najdiSablonu(nazevSablony);
		if (s == null) {
			throw new ChallengeChyba("Šablona „" + nazevSablony + "“ neexistuje.");
		}
		String nazev = s.nazev() + " (vlastní)";
		for (int i = 2; najdi(nazev) != null; i++) {
			nazev = s.nazev() + " (vlastní " + i + ")";
		}
		return ulozVlastni(s.sNazvem(nazev));
	}

	/** Kopie vlastní definice pojmenovaná „název (kopie)“, popř. „(kopie 2)“…; nikdy neaktivní, název nejvýš {@link #NEJDELSI_NAZEV} znaků. */
	public Challenge kopieVlastni(final String nazevVlastni) {
		final Challenge v = vlastni.get(klic(nazevVlastni));
		if (v == null) {
			throw new ChallengeChyba("Challenge „" + nazevVlastni + "“ neexistuje.");
		}
		final String zaklad = v.nazev().replaceFirst(" \\(kopie( \\d+)?\\)$", "");
		String nazev = nazevKopie(zaklad, " (kopie)");
		for (int i = 2; najdi(nazev) != null; i++) {
			nazev = nazevKopie(zaklad, " (kopie " + i + ")");
		}
		return ulozVlastni(v.sNazvem(nazev));
	}

	private static String nazevKopie(final String zaklad, final String koncovka) {
		final int misto = NEJDELSI_NAZEV - koncovka.length();
		return (zaklad.length() > misto ? zaklad.substring(0, misto).trim() : zaklad) + koncovka;
	}

	/** Přejmenuje vlastní definici; aktivita se zachová. */
	public Challenge prejmenuj(final String stary, final String novy) {
		final Challenge d = vlastni.get(klic(stary));
		if (d == null) {
			throw new ChallengeChyba(jeSablona(stary) ? "Šablonu nejde přejmenovat." : "Challenge „" + stary + "“ neexistuje.");
		}
		final String nazev = platnyNazev(novy);
		if (!klic(nazev).equals(klic(stary)) && najdi(nazev) != null) {
			throw new ChallengeChyba("Challenge s názvem „" + nazev + "“ už existuje.");
		}
		final boolean bylAktivni = aktivni.remove(klic(stary));
		final Map<String, Challenge> poradi = new LinkedHashMap<>();
		for (final Map.Entry<String, Challenge> e : vlastni.entrySet()) {
			if (e.getKey().equals(klic(stary))) {
				poradi.put(klic(nazev), d.sNazvem(nazev));
			} else {
				poradi.put(e.getKey(), e.getValue());
			}
		}
		vlastni.clear();
		vlastni.putAll(poradi);
		if (bylAktivni) {
			aktivni.add(klic(nazev));
		}
		return vlastni.get(klic(nazev));
	}

	/** Smaže vlastní definici (a vypne ji, byla-li aktivní); šablonu smazat nejde. */
	public void smazVlastni(final String nazev) {
		if (jeSablona(nazev)) {
			throw new ChallengeChyba("Šablonu nejde smazat.");
		}
		if (vlastni.remove(klic(nazev)) == null) {
			throw new ChallengeChyba("Challenge „" + nazev + "“ neexistuje.");
		}
		aktivni.remove(klic(nazev));
	}

	/**
	 * Zapne challenge; zapnutá už zůstane. Je-li aktivních už {@link #strop()}, vypne se nejdéle zapnutá a vrátí se; jinak se vrací null.
	 */
	public Challenge aktivuj(final String nazev) {
		final Challenge d = najdi(nazev);
		if (d == null) {
			throw new ChallengeChyba("Challenge „" + nazev + "“ neexistuje.");
		}
		final String k = klic(d.nazev());
		if (aktivni.contains(k)) {
			return null;
		}
		Challenge vypnuta = null;
		if (aktivni.size() >= strop) {
			vypnuta = najdi(aktivni.remove(0));
		}
		aktivni.add(k);
		return vypnuta;
	}

	public void deaktivuj(final String nazev) {
		aktivni.remove(klic(nazev));
	}
}
