package cz.geokuk.core.profile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.SouborovePreferences;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.Soubory;
import lombok.extern.slf4j.Slf4j;

/**
 * Nastavení programu v souboru {@link FConst#NASTAVENI_FILE} v datové složce. Při prvním spuštění se jednou převezme nastavení starší verze: ze
 * souboru vedle programu, nebo z Java Preferences (ve Windows registr). Cesty ke složkám a souborům se nepřebírají, platí výchozí složky
 * u programu. Výjimkou jsou datové složky GeoGetu a GSAKu, které patří jiným programům, a obsah výletů (lovim.ggt a tedne.ggt), který se
 * zkopíruje do složky výletů v datové složce.
 */
@Slf4j
public final class Nastaveni {

	private static final long DOCASNY_MAX_STARI_MS = 60_000;
	private static final String UZEL = "geokuk";
	/** Uzly a klíče s cestami starší verze; mířily do původní složky dat, tak se nepřebírají. */
	private static final String UMISTENI_SOUBORU = "umisteniSouboru";
	private static final String VYLET = "vylet";
	private static final String AKTUALNI_SOUBOR = "aktualniSoubor";
	/** Složky jiných programů; nemíří do původní složky dat, tak se převezmou. */
	private static final List<String> SLOZKY_JINYCH_PROGRAMU = Arrays.asList("geogetDataDir", "gsakDataDir");
	private static final String PRIPONA_AKTIVNI = "_active";
	private static final String PRIPONA_RELATIVNE = "_relativeToProgram";
	/** Klíče výletů ve starším nastavení a jména jejich souborů; výchozí umístění starší verze bylo ve složce geokuk v domovské složce. */
	private static final String[][] VYLETY = { { "anoGgtFile", "lovim.ggt" }, { "neGgtFile", "tedne.ggt" } };
	private static final String STARA_SLOZKA_DAT = "geokuk";
	/** Složka výletů v datové složce, stejná jako KesoidUmisteniSouboru.VYLETY_DIR. */
	private static final String SLOZKA_VYLETU = "vylety";
	/** Výchozí profil nastavení, viz MyPreferences.current(). */
	private static final String PROFIL = "current";

	private static SouborovePreferences koren;
	private static String varovani;

	public static synchronized SouborovePreferences koren() {
		if (koren == null) {
			koren = otevri(FConst.NASTAVENI_FILE, FConst.STARE_NASTAVENI_FILE, true, FConst.HOME_DIR);
			koren.ulozitPriUkonceni();
		}
		return koren;
	}

	/** Varování pro uživatele, jednou po zobrazení hlavního okna. */
	public static synchronized String prevzitVarovani() {
		final String v = varovani;
		varovani = null;
		return v;
	}

	/**
	 * Otevře nastavení v souboru. Poškozený soubor nesmí bránit spuštění, proto se odloží stranou a program pokračuje s výchozím nastavením.
	 */
	static SouborovePreferences otevri(final File soubor, final File stary, final boolean zRegistru) {
		return otevri(soubor, stary, zRegistru, null);
	}

	/**
	 * @param domov
	 *            domovská složka, ve které měla starší verze výchozí složku dat s výlety; null, když se výlety z výchozího umístění nepřebírají
	 */
	static SouborovePreferences otevri(final File soubor, final File stary, final boolean zRegistru, final File domov) {
		uklidDocasne(soubor);
		if (soubor.isFile() && soubor.length() == 0) {
			// Prázdný soubor (výpadek proudu, antivir) nemá co zachraňovat, platí jako chybějící.
			smaz(soubor);
		}
		if (soubor.isFile()) {
			try {
				return SouborovePreferences.nacti(soubor);
			} catch (final IOException e) {
				final File vadne = new File(soubor.getPath() + ".vadne");
				final boolean odlozeno = odloz(soubor, vadne);
				varovani = "Nastavení ze souboru " + soubor + " nelze načíst, program pokračuje s výchozím nastavením."
						+ (odlozeno ? "\nPůvodní soubor je uložený jako " + vadne + "."
								: "\nPůvodní soubor zůstal beze změny, změny nastavení se při tomto spuštění neuloží.");
				FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, varovani);
				// Původní soubor, který nejde odložit, se nepřepíše; zkusí se znovu při příštím spuštění.
				return SouborovePreferences.prazdne(odlozeno ? soubor : new File(soubor.getPath() + ".nove"));
			}
		}
		final SouborovePreferences nove = prevezmiStare(soubor, stary, zRegistru, domov);
		try {
			nove.ulozHned();
		} catch (final IOException e) {
			// ohlásí kontrola zapisovatelnosti datové složky
		}
		return nove;
	}

	/** Smaže dočasné soubory zápisu, které po pádu zůstaly vedle souboru nastavení; čerstvé nechá, mohl by je právě zapisovat jiný běh. */
	private static void uklidDocasne(final File soubor) {
		final File slozka = soubor.getAbsoluteFile().getParentFile();
		final String predpona = soubor.getName() + ".";
		final File[] soubory = slozka == null ? null : slozka.listFiles();
		if (soubory == null) {
			return;
		}
		final long hranice = System.currentTimeMillis() - DOCASNY_MAX_STARI_MS;
		for (final File f : soubory) {
			final String jmeno = f.getName();
			if (f.isFile() && jmeno.startsWith(predpona) && jmeno.endsWith(".tmp") && f.lastModified() < hranice) {
				smaz(f);
			}
		}
	}

	private static void smaz(final File soubor) {
		try {
			Files.deleteIfExists(soubor.toPath());
		} catch (final IOException e) {
			log.warn("Soubor {} nelze smazat", soubor, e);
		}
	}

	/** Starší odložený soubor se smaže, až když jde soubor odsunout (antivirus ho může držet). */
	private static boolean odloz(final File soubor, final File vadne) {
		final File docasne = new File(soubor.getPath() + ".odkladani");
		Soubory.smaz(docasne);
		if (!soubor.renameTo(docasne)) {
			return false;
		}
		Soubory.smaz(vadne);
		if (docasne.renameTo(vadne)) {
			return true;
		}
		if (!docasne.renameTo(soubor)) {
			log.warn("Soubor {} nejde vrátit z {}", soubor, docasne);
		}
		return false;
	}

	private static SouborovePreferences prevezmiStare(final File soubor, final File stary, final boolean zRegistru, final File domov) {
		final Prevod prevod = new Prevod(new File(soubor.getAbsoluteFile().getParentFile(), SLOZKA_VYLETU), domov);
		if (stary != null && stary.isFile()) {
			try {
				final SouborovePreferences nacteno = SouborovePreferences.nacti(stary);
				final SouborovePreferences nove = SouborovePreferences.prazdne(soubor);
				prevezmi(nove, nacteno.node(UZEL), prevod);
				log.info("Převzato nastavení ze souboru {}", stary);
				return nove;
			} catch (final IOException | BackingStoreException | RuntimeException e) {
				log.warn("Nastavení ze souboru {} nelze převzít", stary, e);
			}
		}
		final SouborovePreferences nove = SouborovePreferences.prazdne(soubor);
		if (zRegistru) {
			try {
				final Preferences registr = Preferences.userRoot();
				if (registr.nodeExists(UZEL)) {
					prevezmi(nove, registr.node(UZEL), prevod);
					log.info("Převzato nastavení z Java Preferences");
				}
			} catch (final BackingStoreException | RuntimeException e) {
				log.warn("Nastavení z Java Preferences nelze převzít", e);
			}
		}
		return nove;
	}

	/** Kam se při převodu kopírují výlety a kde byla výchozí složka dat starší verze. */
	static final class Prevod {
		final File slozkaVyletu;
		final File domov;

		Prevod(final File slozkaVyletu, final File domov) {
			this.slozkaVyletu = slozkaVyletu;
			this.domov = domov;
		}
	}

	static void prevezmi(final SouborovePreferences nove, final Preferences zdroj, final Prevod prevod) throws BackingStoreException {
		final SouborovePreferences uzel = (SouborovePreferences) nove.node(UZEL);
		uzel.zkopirujZ(zdroj);
		// Výlety se berou z profilu, se kterým starší verze běžela; uzel se jen čte, aby v registru nevznikl prázdný.
		final String umisteniProfilu = PROFIL + "/" + UMISTENI_SOUBORU;
		zkopirujVylety(zdroj.nodeExists(umisteniProfilu) ? zdroj.node(umisteniProfilu) : null, prevod);
		vynechCesty(uzel);
	}

	private static void vynechCesty(final Preferences uzel) throws BackingStoreException {
		if (VYLET.equals(uzel.name())) {
			uzel.remove(AKTUALNI_SOUBOR);
		}
		for (final String dite : uzel.childrenNames()) {
			if (UMISTENI_SOUBORU.equals(dite)) {
				ponechJenSlozkyJinychProgramu(uzel.node(dite));
			} else {
				vynechCesty(uzel.node(dite));
			}
		}
	}

	/**
	 * Ponechá jen absolutní cesty ke složkám jiných programů. Relativní cesta se ve starší verzi počítala od jiné složky programu, tak se
	 * nepřebírá. Složka, která neexistuje nebo je prázdná, se převezme vypnutá; starší verze měla zapnutou výchozí složku GSAKu pro Windows
	 * a sama ji vytvořila i na počítačích, kde GSAK není. Uzel bez ponechaných klíčů se smaže.
	 */
	private static void ponechJenSlozkyJinychProgramu(final Preferences umisteni) throws BackingStoreException {
		for (final String klic : umisteni.keys()) {
			if (!jePrebiranaSlozka(umisteni, klic)) {
				umisteni.remove(klic);
			}
		}
		for (final String slozka : SLOZKY_JINYCH_PROGRAMU) {
			final String cesta = umisteni.get(slozka, null);
			if (cesta != null && jePrazdnaNeboNeni(new File(cesta))) {
				umisteni.putBoolean(slozka + PRIPONA_AKTIVNI, false);
			}
		}
		if (umisteni.keys().length == 0 && umisteni.childrenNames().length == 0) {
			umisteni.removeNode();
		}
	}

	private static boolean jePrazdnaNeboNeni(final File slozka) {
		final String[] obsah = slozka.list();
		return obsah == null || obsah.length == 0;
	}

	private static boolean jePrebiranaSlozka(final Preferences umisteni, final String klic) {
		for (final String slozka : SLOZKY_JINYCH_PROGRAMU) {
			if (klic.equals(slozka) || klic.equals(slozka + PRIPONA_AKTIVNI) || klic.equals(slozka + PRIPONA_RELATIVNE)) {
				return jeAbsolutniCesta(umisteni, slozka);
			}
		}
		return false;
	}

	private static boolean jeAbsolutniCesta(final Preferences umisteni, final String klic) {
		final String cesta = umisteni.get(klic, null);
		return cesta != null && new File(cesta).isAbsolute() && !umisteni.getBoolean(klic + PRIPONA_RELATIVNE, false);
	}

	/**
	 * Zkopíruje soubory výletů starší verze do složky výletů. Soubor, který už ve složce výletů je, se nepřepíše; původní soubor zůstane,
	 * může ho dál používat starší verze nebo GeoGet.
	 */
	private static void zkopirujVylety(final Preferences umisteni, final Prevod prevod) {
		for (final String[] vylet : VYLETY) {
			final File zdroj = staryVylet(umisteni, vylet[0], vylet[1], prevod.domov);
			if (zdroj == null || !zdroj.isFile()) {
				continue;
			}
			final File cil = new File(prevod.slozkaVyletu, vylet[1]);
			if (cil.exists()) {
				continue;
			}
			try {
				Files.createDirectories(prevod.slozkaVyletu.toPath());
				Files.copy(zdroj.toPath(), cil.toPath());
				log.info("Převzat výlet {} do {}", zdroj, cil);
			} catch (final IOException | RuntimeException e) {
				log.warn("Výlet {} nelze převzít do {}", zdroj, cil, e);
			}
		}
	}

	/** Soubor výletu podle staršího nastavení; bez klíče platí výchozí umístění starší verze, vypnutý nebo relativní se nepřebírá. */
	private static File staryVylet(final Preferences umisteni, final String klic, final String jmeno, final File domov) {
		if (umisteni != null && !umisteni.getBoolean(klic + PRIPONA_AKTIVNI, true)) {
			return null;
		}
		final String cesta = umisteni == null ? null : umisteni.get(klic, null);
		if (cesta == null) {
			return domov == null ? null : new File(new File(domov, STARA_SLOZKA_DAT), jmeno);
		}
		return jeAbsolutniCesta(umisteni, klic) ? new File(cesta) : null;
	}

	private Nastaveni() {}
}
