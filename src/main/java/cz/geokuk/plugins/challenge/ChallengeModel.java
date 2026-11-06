package cz.geokuk.plugins.challenge;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import cz.geokuk.framework.Model0;
import lombok.extern.slf4j.Slf4j;

/**
 * Stav plánovače challenge pro rozhraní: zapnuto, seznam definic (šablony ke čtení a vlastní), aktivní challenge (nejvýš {@link ChallengeSeznam#strop()}), výsledky
 * výpočtu, stav předpočtu, filtr „jen kandidáti“ a vybraný kandidát. Změny definic, aktivních a zapnutí se ukládají do nastavení. Metody měnící definice, aktivní
 * a zapnutí volat z vlákna Swingu; {@code nastav…} pro výsledky, stav předpočtu a kandidáta jsou bezpečné z kteréhokoli vlákna a změnu ohlásí ve vlákně Swingu.
 */
@Slf4j
public class ChallengeModel extends Model0 {

	private static final String UZEL = "challenge";

	private ChallengeSeznam seznam = new ChallengeSeznam();
	private boolean zapnuto;
	private final Map<String, VysledekSektoru> vysledky = new ConcurrentHashMap<>();
	private final Map<String, VysledekPolicek> vysledkyPolicek = new ConcurrentHashMap<>();
	private final Map<String, StavPredpoctu> stavy = new ConcurrentHashMap<>();
	private final Map<String, VybranyKandidat> kandidati = new ConcurrentHashMap<>();
	private volatile String jenKandidatiPro;
	/** Klíč challenge zobrazené v pruhu; neukládá se, bez ní nebo při neaktivní je to první aktivní. */
	private volatile String zobrazena;
	/** Uložené nastavení je novější nebo nečitelné; nepřepisuje se, aby o něj uživatel nepřišel. */
	private boolean ukladaniZakazano;
	private Supplier<ChallengeUloziste> uloziste = () -> new PreferenceUloziste(currPrefe().node(UZEL));

	/** Pro test: úložiště místo preferencí. */
	void setUloziste(final Supplier<ChallengeUloziste> uloziste) {
		this.uloziste = uloziste;
	}

	private static String klic(final Challenge c) {
		return c.nazev().trim().toLowerCase(Locale.ROOT);
	}

	@Override
	protected void initAndFire() {
		nactiNastaveni();
	}

	@Override
	protected void reloadPreferences() {
		nactiNastaveni();
	}

	private void nactiNastaveni() {
		try {
			final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(uloziste.get());
			seznam = n.seznam;
			zapnuto = n.zapnuto;
			ukladaniZakazano = n.neznamyFormat;
		} catch (final RuntimeException e) {
			log.warn("Nastavení challenge nejde načíst, použijí se výchozí hodnoty a ukládání se vypne: {}", e.toString());
			seznam = new ChallengeSeznam();
			zapnuto = false;
			ukladaniZakazano = true;
		}
		vysledky.clear();
		vysledkyPolicek.clear();
		zobrazena = null;
		stavy.clear();
		kandidati.clear();
		jenKandidatiPro = null;
		fire(new ChallengeZmenenyEvent(ChallengeZmenenyEvent.Druh.NACTENO, null));
	}

	private void uloz() {
		if (ukladaniZakazano) {
			return;
		}
		try {
			NastaveniChallenge.uloz(seznam, zapnuto, uloziste.get());
		} catch (final RuntimeException e) {
			log.warn("Nastavení challenge nejde uložit: {}", e.toString());
		}
	}

	private void ohlas(final ChallengeZmenenyEvent.Druh druh, final Challenge c) {
		fire(new ChallengeZmenenyEvent(druh, c));
	}

	private void naEdt(final Runnable akce) {
		if (SwingUtilities.isEventDispatchThread()) {
			akce.run();
		} else {
			SwingUtilities.invokeLater(akce);
		}
	}

	// --- Zapnutí

	public boolean jeZapnuto() {
		return zapnuto;
	}

	/** Pruh plánovače na mapě je vidět, když je plánovač zapnutý a je co ukazovat. */
	public boolean jePruhViditelny() {
		return zapnuto && !seznam.aktivni().isEmpty();
	}

	public void setZapnuto(final boolean zapnuto) {
		if (this.zapnuto == zapnuto) {
			return;
		}
		this.zapnuto = zapnuto;
		uloz();
		ohlas(ChallengeZmenenyEvent.Druh.ZAPNUTO, null);
	}

	// --- Definice

	public List<Challenge> sablony() {
		return seznam.sablony();
	}

	public List<Challenge> vlastni() {
		return seznam.vlastni();
	}

	/** Šablony a potom vlastní definice. */
	public List<Challenge> vsechny() {
		return seznam.vsechny();
	}

	public boolean jeSablona(final Challenge c) {
		return seznam.jeSablona(c.nazev());
	}

	/** Uloží novou nebo upravenou vlastní definici; šablonu přepsat nejde ({@link ChallengeChyba}), uloží se její kopie. */
	public Challenge ulozVlastni(final Challenge definice) {
		final Challenge c = seznam.ulozVlastni(definice);
		vysledky.remove(klic(c));
		vysledkyPolicek.remove(klic(c));
		stavy.remove(klic(c));
		kandidati.remove(klic(c));
		uloz();
		ohlas(ChallengeZmenenyEvent.Druh.DEFINICE, c);
		return c;
	}

	/** Vlastní kopie šablony k úpravě. */
	public Challenge kopieSablony(final Challenge sablona) {
		final Challenge c = seznam.kopieSablony(sablona.nazev());
		uloz();
		ohlas(ChallengeZmenenyEvent.Druh.DEFINICE, c);
		return c;
	}

	public Challenge prejmenuj(final Challenge c, final String novyNazev) {
		final String staryKlic = klic(c);
		final Challenge nova = seznam.prejmenuj(c.nazev(), novyNazev);
		prenes(vysledky, staryKlic, klic(nova));
		prenes(vysledkyPolicek, staryKlic, klic(nova));
		if (staryKlic.equals(zobrazena)) {
			zobrazena = klic(nova);
		}
		prenes(stavy, staryKlic, klic(nova));
		prenes(kandidati, staryKlic, klic(nova));
		if (staryKlic.equals(jenKandidatiPro)) {
			jenKandidatiPro = klic(nova);
		}
		uloz();
		ohlas(ChallengeZmenenyEvent.Druh.DEFINICE, nova);
		return nova;
	}

	private static <V> void prenes(final Map<String, V> m, final String z, final String na) {
		final V v = m.remove(z);
		if (v != null) {
			m.put(na, v);
		}
	}

	public void smazVlastni(final Challenge c) {
		final boolean bylaAktivni = seznam.jeAktivni(c.nazev());
		seznam.smazVlastni(c.nazev());
		odstranStav(c);
		uloz();
		ohlas(ChallengeZmenenyEvent.Druh.DEFINICE, c);
		if (bylaAktivni) {
			ohlas(ChallengeZmenenyEvent.Druh.AKTIVNI, c);
		}
	}

	private void odstranStav(final Challenge c) {
		vysledky.remove(klic(c));
		vysledkyPolicek.remove(klic(c));
		stavy.remove(klic(c));
		kandidati.remove(klic(c));
		if (klic(c).equals(jenKandidatiPro)) {
			jenKandidatiPro = null;
			ohlas(ChallengeZmenenyEvent.Druh.JEN_KANDIDATI, null);
		}
	}

	// --- Aktivní

	/** Aktivní challenge v pořadí zapnutí. */
	public List<Challenge> aktivni() {
		return seznam.aktivni();
	}

	public boolean jeAktivni(final Challenge c) {
		return seznam.jeAktivni(c.nazev());
	}

	/** Nejvíc aktivních challenge najednou. */
	public int strop() {
		return seznam.strop();
	}

	/** Zapne challenge; při překročení stropu {@link ChallengeChyba} se zprávou pro uživatele. */
	public void aktivuj(final Challenge c) {
		if (seznam.jeAktivni(c.nazev())) {
			return;
		}
		seznam.aktivuj(c.nazev());
		uloz();
		ohlas(ChallengeZmenenyEvent.Druh.AKTIVNI, c);
	}

	public void deaktivuj(final Challenge c) {
		if (!seznam.jeAktivni(c.nazev())) {
			return;
		}
		seznam.deaktivuj(c.nazev());
		odstranStav(c);
		uloz();
		ohlas(ChallengeZmenenyEvent.Druh.AKTIVNI, c);
	}

	// --- Výsledky (plánovač)

	/** Poslední výsledek výpočtu, nebo null, dokud není. */
	public VysledekSektoru vysledek(final Challenge c) {
		return vysledky.get(klic(c));
	}

	/** Počet chybějících sektorů, políček nebo dnů podle typu, nebo -1, dokud výsledek není. */
	public int pocetChybejicich(final Challenge c) {
		if (c.typ() == Challenge.Typ.SEKTORY_360) {
			final VysledekSektoru v = vysledek(c);
			return v == null ? -1 : 360 - v.pocetObsazenych();
		}
		final VysledekPolicek p = vysledekPolicek(c);
		return p == null ? -1 : p.pocetChybejicich();
	}

	/** Poslední výsledek D/T matice nebo kalendáře, nebo null, dokud není. */
	public VysledekPolicek vysledekPolicek(final Challenge c) {
		return vysledkyPolicek.get(klic(c));
	}

	/** Uloží výsledek D/T matice nebo kalendáře aktivní challenge a ohlásí ho; pro neaktivní nebo změněnou definici se zahodí. Z kteréhokoli vlákna. */
	public void nastavVysledekPolicek(final Challenge c, final VysledekPolicek vysledek) {
		naEdt(() -> {
			if (!seznam.jeAktivni(c.nazev()) || !c.equals(seznam.najdi(c.nazev()))) {
				return;
			}
			vysledkyPolicek.put(klic(c), vysledek);
			ohlas(ChallengeZmenenyEvent.Druh.VYSLEDEK, c);
		});
	}

	public StavPredpoctu stavPredpoctu(final Challenge c) {
		final StavPredpoctu s = stavy.get(klic(c));
		return s == null ? StavPredpoctu.NEBEZI : s;
	}

	/** Uloží výsledek výpočtu aktivní challenge a ohlásí ho; pro neaktivní challenge se zahodí. Z kteréhokoli vlákna. */
	public void nastavVysledek(final Challenge c, final VysledekSektoru vysledek) {
		naEdt(() -> {
			if (!seznam.jeAktivni(c.nazev()) || !c.equals(seznam.najdi(c.nazev()))) {
				return;
			}
			vysledky.put(klic(c), vysledek);
			ohlas(ChallengeZmenenyEvent.Druh.VYSLEDEK, c);
		});
	}

	/** Uloží stav předpočtu aktivní challenge a ohlásí ho. Z kteréhokoli vlákna. */
	public void nastavStavPredpoctu(final Challenge c, final StavPredpoctu stav) {
		naEdt(() -> {
			if (!seznam.jeAktivni(c.nazev())) {
				return;
			}
			stavy.put(klic(c), stav);
			ohlas(ChallengeZmenenyEvent.Druh.PREDPOCET, c);
		});
	}

	// --- Pruh na mapě

	/** Challenge zobrazená v pruhu: vybraná {@link #setZobrazena}, je-li aktivní, jinak první aktivní, nebo null. */
	public Challenge zobrazena() {
		final String k = zobrazena;
		if (k != null && seznam.jeAktivni(k)) {
			return seznam.najdi(k);
		}
		final List<Challenge> a = seznam.aktivni();
		return a.isEmpty() ? null : a.get(0);
	}

	/** Přepne pruh na jinou aktivní challenge. */
	public void setZobrazena(final Challenge c) {
		if (!seznam.jeAktivni(c.nazev())) {
			throw new ChallengeChyba("V pruhu jde zobrazit jen aktivní challenge.");
		}
		final Challenge pred = zobrazena();
		zobrazena = klic(c);
		if (pred == null || !klic(pred).equals(klic(c))) {
			ohlas(ChallengeZmenenyEvent.Druh.ZOBRAZENA, c);
		}
	}

	/** Co má pruh právě ukazovat; {@link StavPruhu#SKRYTY}, když je plánovač vypnutý nebo žádná challenge není aktivní. */
	public StavPruhu stavPruhu() {
		final Challenge c = zobrazena();
		if (!zapnuto || c == null) {
			return StavPruhu.SKRYTY;
		}
		final VybranyKandidat k = vybranyKandidat(c);
		final StavPredpoctu stav = stavPredpoctu(c);
		final String chyba = stav.faze() == StavPredpoctu.Faze.CHYBA ? stav.chyba() : null;
		final Challenge filtr = jenKandidatiPro();
		return new StavPruhu(true, c, seznam.aktivni().size(), pocetChybejicich(c), stitekKandidata(c, k), k, filtr != null && klic(filtr).equals(klic(c)), chyba);
	}

	private String stitekKandidata(final Challenge c, final VybranyKandidat k) {
		if (k == null) {
			return null;
		}
		if (c.typ() != Challenge.Typ.SEKTORY_360) {
			return k.stitek();
		}
		final VysledekSektoru v = vysledek(c);
		final VysledekSektoru.Kandidati kand = v == null ? null : v.kandidati(k.sektor());
		return StitkyPruhu.sektor(k.sektor(), kand == null ? 0 : kand.pocet(), k.vzdalenost());
	}

	// --- Kandidáti

	/** Vybraný kandidát („další chybějící“) pro challenge, nebo null. */
	public VybranyKandidat vybranyKandidat(final Challenge c) {
		return kandidati.get(klic(c));
	}

	/** Uloží vybraného kandidáta aktivní challenge (null = žádný) a ohlásí ho. Z kteréhokoli vlákna. */
	public void nastavVybranyKandidat(final Challenge c, final VybranyKandidat kandidat) {
		naEdt(() -> {
			if (!seznam.jeAktivni(c.nazev())) {
				return;
			}
			if (kandidat == null) {
				kandidati.remove(klic(c));
			} else {
				kandidati.put(klic(c), kandidat);
			}
			ohlas(ChallengeZmenenyEvent.Druh.KANDIDAT, c);
		});
	}

	/** Challenge, pro kterou je zapnutý filtr „jen kandidáti“ (zobrazit jen nenalezené keše, které zaplní chybějící sektor); null = filtr vypnutý. */
	public Challenge jenKandidatiPro() {
		final String k = jenKandidatiPro;
		if (k == null) {
			return null;
		}
		return seznam.najdi(k);
	}

	/** Zapne filtr „jen kandidáti“ pro aktivní challenge, nebo ho vypne (null). Filtr je jen jeden najednou a neukládá se. */
	public void setJenKandidatiPro(final Challenge c) {
		if (c != null && !seznam.jeAktivni(c.nazev())) {
			throw new ChallengeChyba("Filtr „jen kandidáti“ jde zapnout jen pro aktivní challenge.");
		}
		final String nove = c == null ? null : klic(c);
		if ((nove == null && jenKandidatiPro == null) || (nove != null && nove.equals(jenKandidatiPro))) {
			return;
		}
		jenKandidatiPro = nove;
		ohlas(ChallengeZmenenyEvent.Druh.JEN_KANDIDATI, c);
	}
}
