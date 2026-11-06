package cz.geokuk.plugins.mapy.kachle.data;

import java.awt.event.InputEvent;
import java.io.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import javax.swing.KeyStroke;

import cz.geokuk.core.program.FConst;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapy, které si uživatel zadá ve složce mapy v datové složce, každou ve vlastním
 * souboru. Zobrazují se vedle vestavěných pod vlastními jmény, vestavěné nemění.
 */
@Slf4j
public final class UzivatelskeMapy {

	public static final String SLOZKA = "mapy";
	static final int SIRKA_DIALOGU = 100;

	public static final String PRIPONA = ".mapa";
	static final String STARY_SOUBOR = "uzivatelske-mapy.properties";
	static final String PREFIX = "user-";
	private static final List<String> VLASTNOSTI = Arrays.asList("nazev", "url", "popis", "min", "max", "maxauto", "klavesa", "zkratka", "atribuce", "hromadne");
	private static final String HLAVICKA = "hlavicka.";
	private static final int MAX_MERITKO = 22;

	private static Map<KeyStroke, String> zkratkyProgramu = Collections.emptyMap();

	/** Zkratky akcí programu, které uživatelská mapa nesmí převzít. */
	public static void setZkratkyProgramu(final Map<KeyStroke, String> zkratky) {
		zkratkyProgramu = zkratky;
	}

	/**
	 * Písmeno jako stisk klávesy, aby zkratku nespustilo písmeno napsané do otevřeného menu; velké písmeno se Shiftem. Číslice a ostatní znaky jako
	 * napsaný znak, na české klávesnici se číslice píšou se Shiftem.
	 */
	static KeyStroke jednaKlavesa(final char c) {
		if (c < 128 && Character.isLetter(c)) {
			return KeyStroke.getKeyStroke(Character.toUpperCase(c), Character.isUpperCase(c) ? InputEvent.SHIFT_DOWN_MASK : 0);
		}
		return KeyStroke.getKeyStroke(c);
	}

	/** Složka s uživatelskými mapami v datové složce. */
	public static File slozka() {
		return new File(FConst.DATA_DIR, SLOZKA);
	}

	static final int MAX_VELIKOST = 64 * 1024;
	private static final int MAX_DELKA_KLICE = 30;

	/** Neznámý klíč bývá celý řádek cizího textu; do hlášky jen jeho začátek bez řídicích znaků. */
	static String zkrat(final String klic) {
		final String bezRidicich = klic.replaceAll("\\p{Cntrl}", "?");
		return bezRidicich.length() <= MAX_DELKA_KLICE ? bezRidicich : bezRidicich.substring(0, MAX_DELKA_KLICE) + "…";
	}

	/** Načte mapy z datové složky a vrátí text pro uživatele s chybami v nich, nebo null. */
	public static String nacti() {
		return nacti(FConst.DATA_DIR);
	}

	static String nacti(final File dataDir) {
		final File slozka = new File(dataDir, SLOZKA);
		final List<String> chyby = nactiSlozku(slozka);
		final List<String> zpravy = new ArrayList<>();
		if (!chyby.isEmpty()) {
			final List<String> nezobrazene = nezobrazene(slozka);
			zpravy.add("Chyby v uživatelských mapách ve složce " + slozka + ":\n" + String.join("\n", chyby)
					+ (nezobrazene.isEmpty() ? "" : "\nNezobrazí se: " + String.join(", ", nezobrazene)));
		}
		final File stary = new File(dataDir, STARY_SOUBOR);
		final String oznaceni = oznaceniVeStaremSouboru(stary);
		if (oznaceni != null) {
			zpravy.add("Mapy ze souboru " + stary + " přesuňte do složky " + slozka + ". Každou mapu dejte do vlastního souboru pojmenovaného podle dosavadního označení, třeba "
					+ oznaceni + PRIPONA + " pro řádky " + oznaceni + ".…, aby zůstaly uložené dlaždice i vybraná mapa. Vlastnosti v něm pište bez označení: url=… místo " + oznaceni
					+ ".url=…. Potom soubor " + stary + " smažte.");
		}
		return zpravy.isEmpty() ? null : String.join("\n\n", zpravy);
	}

	/** Soubory map, ze kterých se mapa nenačetla. */
	private static List<String> nezobrazene(final File slozka) {
		final List<String> vysledek = new ArrayList<>();
		for (final File soubor : mapy(slozka)) {
			final String jmeno = soubor.getName();
			if (EKaType.podleJmena(PREFIX + jmeno.substring(0, jmeno.length() - PRIPONA.length()).toLowerCase(Locale.ROOT)) == null) {
				vysledek.add(jmeno);
			}
		}
		return vysledek;
	}

	/** Soubory s příponou .mapa seřazené podle jména; bez průvodních souborů ._*, které vytváří macOS. */
	private static List<File> mapy(final File slozka) {
		final File[] soubory = slozka.listFiles(f -> f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(PRIPONA) && !f.getName().startsWith("._"));
		if (soubory == null) {
			return Collections.emptyList();
		}
		Arrays.sort(soubory);
		return Arrays.asList(soubory);
	}

	/** Hlášku o chybách map zalomenou do šířky dialogu. */
	public static String textDialogu(final String chyby) {
		return zalom(chyby, SIRKA_DIALOGU);
	}

	/** Zalomí řádky delší než {@code sirka} na mezerách, dlouhé slovo (cestu) za lomítkem nebo natvrdo, ať dialog nepřeteče obrazovku. */
	public static String zalom(final String text, final int sirka) {
		final StringBuilder vysledek = new StringBuilder();
		for (final String radek : text.split("\n", -1)) {
			if (vysledek.length() > 0) {
				vysledek.append('\n');
			}
			int delka = 0;
			for (final String slovo : radek.split(" ", -1)) {
				final List<String> kusy = rozdelSlovo(slovo, sirka);
				for (int i = 0; i < kusy.size(); i++) {
					final String kus = kusy.get(i);
					if (i > 0 || delka > 0 && delka + 1 + kus.length() > sirka) {
						vysledek.append('\n');
						delka = 0;
					} else if (delka > 0) {
						vysledek.append(' ');
						delka++;
					}
					vysledek.append(kus);
					delka += kus.length();
				}
			}
		}
		return vysledek.toString();
	}

	private static List<String> rozdelSlovo(final String slovo, final int sirka) {
		final List<String> kusy = new ArrayList<>();
		String zbytek = slovo;
		while (zbytek.length() > sirka) {
			int konec = Math.max(zbytek.lastIndexOf('/', sirka - 1), zbytek.lastIndexOf('\\', sirka - 1)) + 1;
			if (konec <= 0) {
				konec = sirka;
			}
			kusy.add(zbytek.substring(0, konec));
			zbytek = zbytek.substring(konec);
		}
		kusy.add(zbytek);
		return kusy;
	}

	/** První označení mapy ve starém souboru, null když soubor není nebo v něm žádná mapa nezůstala. */
	private static String oznaceniVeStaremSouboru(final File stary) {
		if (!stary.isFile()) {
			return null;
		}
		final Properties p = new Properties();
		try (Reader reader = Files.newBufferedReader(stary.toPath(), StandardCharsets.UTF_8)) {
			p.load(reader);
		} catch (final IOException | IllegalArgumentException e) {
			return "mapa";
		}
		return p.stringPropertyNames().stream().sorted().map(klic -> klic.contains(".") ? klic.substring(0, klic.indexOf('.')) : klic).findFirst().orElse(null);
	}

	static List<String> nactiSlozku(final File slozka) {
		EKaType.setUzivatelske(Collections.emptyList());
		final File[] soubory = slozka.listFiles(File::isFile);
		if (soubory == null) {
			return Collections.emptyList();
		}
		Arrays.sort(soubory);
		final List<String> chyby = new ArrayList<>();
		final SortedMap<String, Properties> obsah = new TreeMap<>();
		for (final File soubor : soubory) {
			final String jmeno = soubor.getName().toLowerCase(Locale.ROOT);
			if (!jmeno.startsWith("._") && jmeno.endsWith(PRIPONA + ".txt")) {
				chyby.add(soubor.getName() + ": soubor má příponu .txt, přejmenujte ho na " + soubor.getName().substring(0, soubor.getName().length() - 4));
			}
			if (!jmeno.endsWith(PRIPONA) || jmeno.startsWith("._")) {
				continue;
			}
			// Obsah souboru jde do hlášky, Diagnostiky a hlášení chyby; cizí soubor (odkaz, velký soubor) se nečte.
			if (Files.isSymbolicLink(soubor.toPath())) {
				chyby.add(soubor.getName() + ": soubor je odkaz na jiný soubor, zkopírujte do složky samotný soubor s mapou");
				continue;
			}
			if (soubor.length() > MAX_VELIKOST) {
				chyby.add(soubor.getName() + ": soubor je větší než " + MAX_VELIKOST / 1024 + " kB, mapa to není");
				continue;
			}
			try (BufferedReader reader = Files.newBufferedReader(soubor.toPath(), StandardCharsets.UTF_8)) {
				// Poznámkový blok a PowerShell 5 ukládají UTF-8 se značkou BOM.
				reader.mark(1);
				if (reader.read() != '\uFEFF') {
					reader.reset();
				}
				final Properties p = new Properties();
				p.load(reader);
				obsah.put(soubor.getName(), p);
			} catch (final CharacterCodingException e) {
				chyby.add(soubor.getName() + ": soubor není v kódování UTF-8, uložte ho znovu s kódováním UTF-8");
			} catch (final IOException | IllegalArgumentException e) {
				chyby.add(soubor.getName() + ": soubor nelze přečíst: " + e.getMessage());
			}
		}
		EKaType.setUzivatelske(zpracuj(obsah, chyby));
		if (!chyby.isEmpty()) {
			log.warn("Chyby v {}: {}", slozka, chyby);
		}
		return chyby;
	}

	/** Ze souborů (jméno souboru → obsah) udělá mapy seřazené podle označení. Mapy, které spolu kolidují, vyřadí obě. */
	static List<EKaType> zpracuj(final SortedMap<String, Properties> soubory, final List<String> chyby) {
		final SortedMap<String, List<String>> podleOznaceni = new TreeMap<>();
		for (final String jmeno : soubory.keySet()) {
			final String id = jmeno.substring(0, jmeno.length() - PRIPONA.length()).toLowerCase(Locale.ROOT);
			if (id.matches("[a-z0-9][a-z0-9-]*")) {
				podleOznaceni.computeIfAbsent(id, k -> new ArrayList<>()).add(jmeno);
			} else {
				chyby.add(jmeno + ": název souboru smí obsahovat jen písmena bez diakritiky, číslice a pomlčky a nesmí začínat pomlčkou");
			}
		}
		final List<EKaType> mapy = new ArrayList<>();
		final Map<EKaType, String> souborMapy = new HashMap<>();
		for (final Map.Entry<String, List<String>> e : podleOznaceni.entrySet()) {
			if (e.getValue().size() > 1) {
				chyby.add(String.join(", ", e.getValue()) + ": názvy souborů se liší jen velikostí písmen");
				continue;
			}
			final String jmeno = e.getValue().get(0);
			final Properties p = soubory.get(jmeno);
			final Map<String, String> vlastnosti = new HashMap<>();
			for (final String klic : p.stringPropertyNames()) {
				if (VLASTNOSTI.contains(klic) || klic.matches(HLAVICKA.replace(".", "\\.") + "[A-Za-z0-9-]+")) {
					vlastnosti.put(klic, p.getProperty(klic).trim());
				} else {
					chyby.add(jmeno + ": " + zkrat(klic) + " je neznámá vlastnost, povolené jsou " + String.join(", ", VLASTNOSTI) + " a " + HLAVICKA + "<jméno hlavičky>");
				}
			}
			final EKaType mapa = vytvor(e.getKey(), jmeno, vlastnosti, chyby);
			if (mapa != null) {
				mapy.add(mapa);
				souborMapy.put(mapa, jmeno);
			}
		}
		final Set<EKaType> kolize = new HashSet<>();
		for (int i = 0; i < mapy.size(); i++) {
			for (int j = i + 1; j < mapy.size(); j++) {
				final EKaType a = mapy.get(i);
				final EKaType b = mapy.get(j);
				final String obe = souborMapy.get(a) + ", " + souborMapy.get(b);
				if (a.getNazev().equalsIgnoreCase(b.getNazev())) {
					chyby.add(obe + ": stejný název v menu „" + a.getNazev() + "“");
					kolize.add(a);
					kolize.add(b);
				}
				if (a.getKeyStroke() != null && a.getKeyStroke().equals(b.getKeyStroke())) {
					chyby.add(obe + ": stejná klávesová zkratka");
					kolize.add(a);
					kolize.add(b);
				}
			}
		}
		mapy.removeAll(kolize);
		return mapy;
	}

	private static EKaType vytvor(final String id, final String jmeno, final Map<String, String> v, final List<String> chyby) {
		final String nazev = v.getOrDefault("nazev", "");
		final String url = v.getOrDefault("url", "");
		if (nazev.isEmpty()) {
			chyby.add(jmeno + ": nazev chybí");
			return null;
		}
		if (!url.matches("https?://\\S+") || !url.contains("{z}") || !url.contains("{x}") || !url.contains("{y}")) {
			chyby.add(jmeno + ": url musí začínat http:// nebo https:// a obsahovat {z}, {x} a {y}");
			return null;
		}
		if (url.replace("{z}", "").replace("{x}", "").replace("{y}", "").matches(".*[{}].*")) {
			chyby.add(jmeno + ": url smí z proměnných ve složených závorkách obsahovat jen {z}, {x} a {y}; místo {s} napište jednu subdoménu, třeba a");
			return null;
		}
		final int min;
		final int max;
		final int maxauto;
		try {
			min = Integer.parseInt(v.getOrDefault("min", "0"));
			max = Integer.parseInt(v.getOrDefault("max", "18"));
			maxauto = Integer.parseInt(v.getOrDefault("maxauto", String.valueOf(max)));
		} catch (final NumberFormatException e) {
			chyby.add(jmeno + ": min, max a maxauto musí být celá čísla");
			return null;
		}
		if (min < 0 || max > MAX_MERITKO || min > max || maxauto < min || maxauto > max) {
			chyby.add(jmeno + ": měřítka musí splňovat 0 ≤ min ≤ maxauto ≤ max ≤ " + MAX_MERITKO);
			return null;
		}
		final String klavesa = v.getOrDefault("klavesa", "");
		if (!klavesa.isEmpty() && !klavesa.matches("[A-Za-z0-9]")) {
			chyby.add(jmeno + ": klavesa musí být jedno písmeno nebo číslice");
			return null;
		}
		final String zkratka = v.getOrDefault("zkratka", "");
		final KeyStroke keyStroke = zkratka.isEmpty() ? null : zkratka.length() == 1 ? jednaKlavesa(zkratka.charAt(0)) : KeyStroke.getKeyStroke(zkratka);
		if (!zkratka.isEmpty() && keyStroke == null) {
			chyby.add(jmeno + ": zkratka není platná klávesová zkratka (např. u, alt U, ctrl U)");
			return null;
		}
		final String akce = keyStroke == null ? null : zkratkyProgramu.get(keyStroke);
		if (akce != null) {
			chyby.add(jmeno + ": zkratka " + zkratka + " už používá " + akce);
			return null;
		}
		for (final EKaType jina : EKaType.vestavene()) {
			if (keyStroke != null && keyStroke.equals(jina.getKeyStroke())) {
				chyby.add(jmeno + ": zkratka " + zkratka + " už používá mapa " + jina.getNazev());
				return null;
			}
		}
		final String hromadne = v.getOrDefault("hromadne", "ne");
		if (!hromadne.equals("ano") && !hromadne.equals("ne")) {
			chyby.add(jmeno + ": hromadne musí být ano, nebo ne");
			return null;
		}
		final Map<String, String> hlavicky = new TreeMap<>();
		for (final Map.Entry<String, String> e : v.entrySet()) {
			if (e.getKey().startsWith(HLAVICKA)) {
				if (e.getValue().matches("(?s).*\\p{Cntrl}.*")) {
					chyby.add(jmeno + ": " + e.getKey() + " nesmí obsahovat řídicí znaky (\\r, \\n, \\t)");
					return null;
				}
				hlavicky.put(e.getKey().substring(HLAVICKA.length()), e.getValue().replace("{verze}", FConst.VERSION));
			}
		}
		return EKaType.uzivatelska(id, nazev, v.getOrDefault("popis", nazev), min, max, maxauto, klavesa.isEmpty() ? 0 : Character.toUpperCase(klavesa.charAt(0)), keyStroke, hlavicky,
				v.getOrDefault("atribuce", ""), hromadne.equals("ano"),
				new UzivatelskyUrlBuilder(url));
	}

	private UzivatelskeMapy() {}
}
