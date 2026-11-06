package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.framework.Progressor;
import cz.geokuk.util.lang.StringUtils;
import cz.geokuk.plugins.kesoid.kind.kes.DotahovacZDatabaze;
import lombok.extern.slf4j.Slf4j;

/** Načítá keše z databáze OpenSAKu (soubor .db v jeho datové složce). */
@Slf4j
public class OpensakDbLoader extends Nacitac0 {

	/** Nejvyšší verze schématu ({@code PRAGMA user_version}), se kterou je načítání ověřené. */
	static final int OVERENA_VERZE_SCHEMATU = 28;

	private static final int PROGRESS_VAHA_CACHES = 1;
	private static final int PROGRESS_VAHA_WAYPOINTS = 1;

	/** Bez nich by keše skončily bez kódu, typu nebo na souřadnicích 0, 0. */
	private static final Map<String, List<String>> POVINNE_SLOUPCE = ImmutableMap.of( //
			"caches", Arrays.asList("id", "gc_code", "name", "cache_type", "latitude", "longitude"), //
			"waypoints", Arrays.asList("cache_id", "latitude", "longitude"));

	private static final String[] SLOUPCE_CACHES = { "id", "gc_code", "name", "cache_type", "latitude", "longitude", "container", "difficulty", "terrain", "placed_by", "owner_name",
			"owner_id", "hidden_date", "available", "archived", "found", "found_date", "country", "state", "favorite_points", "elevation" };

	private static final String[] SLOUPCE_USER_NOTES = { "corrected_lat", "corrected_lon", "is_corrected" };

	private static final String[] SLOUPCE_WAYPOINTS = { "prefix", "wp_type", "name", "wp_code", "description", "latitude", "longitude" };

	/** Texty čtené jedním voláním JDBC ({@link SpojeneTexty}). */
	private static final String[] TEXTY_CACHES = { "gc_code", "name", "cache_type", "container", "placed_by", "owner_name", "owner_id", "hidden_date", "found_date", "country", "state" };
	private static final String[] TEXTY_WAYPOINTS = { "prefix", "wp_type", "name", "wp_code", "description" };

	/** Sloupce, které načítání čte, podle tabulek; z nich vychází týdenní kontrola schématu OpenSAKu. */
	static Map<String, SortedSet<String>> pouziteSloupce() {
		final Map<String, SortedSet<String>> vysledek = new TreeMap<>();
		POVINNE_SLOUPCE.forEach((tabulka, sloupce) -> vysledek.computeIfAbsent(tabulka, t -> new TreeSet<>()).addAll(sloupce));
		vysledek.computeIfAbsent("caches", t -> new TreeSet<>()).addAll(Arrays.asList(SLOUPCE_CACHES));
		vysledek.get("caches").addAll(Arrays.asList(TEXTY_CACHES));
		vysledek.get("caches").add("encoded_hints"); // HintZDatabaze.OPENSAK
		vysledek.computeIfAbsent("waypoints", t -> new TreeSet<>()).addAll(Arrays.asList(SLOUPCE_WAYPOINTS));
		vysledek.get("waypoints").addAll(Arrays.asList(TEXTY_WAYPOINTS));
		vysledek.get("waypoints").add("id"); // ORDER BY ve čtení waypointů
		vysledek.computeIfAbsent("user_notes", t -> new TreeSet<>()).addAll(Arrays.asList(SLOUPCE_USER_NOTES));
		vysledek.get("user_notes").add("cache_id");
		return vysledek;
	}

	/** Typy, které OpenSAK pojmenovává jinak než groundspeak:type v GPX, a zkrácené názvy. */
	private static final Map<String, String> TYPY_KESI = typyKesi();

	@Override
	protected void nacti(final File file, final IImportBuilder builder, final Future<?> future, final ProgressModel aProgressModel) throws IOException {
		try (Connection c = DatabazeJinehoProgramu.otevri(file); Statement statement = c.createStatement()) {
			if (!jeDatabazeOpensaku(statement)) {
				throw new IllegalArgumentException("Cannot load from file " + file);
			}
			DatabazeJinehoProgramu.zkontrolujSloupce(statement, file, "OpenSAKu", POVINNE_SLOUPCE, Collections.emptySet());
			final int verze = verzeSchematu(statement);
			if (verze > OVERENA_VERZE_SCHEMATU) {
				log.info("Databáze OpenSAKu {} má schéma {}, GeoKuk je ověřený do verze {}.", file, verze, OVERENA_VERZE_SCHEMATU);
			}
			final int pocet = count(statement, "SELECT count(*) FROM caches") * PROGRESS_VAHA_CACHES + count(statement, "SELECT count(*) FROM waypoints") * PROGRESS_VAHA_WAYPOINTS;
			final Progressor progressor = zahajPrubeh(aProgressModel, pocet, file.toString());
			loadCaches(file, statement, builder, future, progressor, datumNalezuPouzitelny(statement, file));
			loadWaypoints(statement, builder, future, progressor);
			progressor.finish();
		} catch (final SQLException e) {
			throw new IOException("Unable to load from " + file, e);
		}
	}

	@Override
	protected void nacti(final ZipFile zipFile, final ZipEntry zipEntry, final IImportBuilder builder, final Future<?> f, final ProgressModel aProgressModel) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	boolean umiNacist(final File file) {
		if (!"db".equals(Files.getFileExtension(file.getName().toLowerCase(Locale.ROOT)))) {
			return false;
		}
		try (Connection c = DatabazeJinehoProgramu.otevri(file, DatabazeJinehoProgramu.CEKANI_PRI_ZJISTOVANI_MS); Statement statement = c.createStatement()) {
			return jeDatabazeOpensaku(statement);
		} catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	boolean umiNacist(final ZipEntry zipEntry) {
		return false;
	}

	/** Jiné SQLite databáze s příponou .db mají jiné tabulky; kód keše je jen v OpenSAKu. */
	private static boolean jeDatabazeOpensaku(final Statement statement) throws SQLException {
		return DatabazeJinehoProgramu.sloupce(statement, "caches").contains("gc_code") && !DatabazeJinehoProgramu.sloupce(statement, "waypoints").isEmpty();
	}

	private static int verzeSchematu(final Statement statement) throws SQLException {
		try (ResultSet rs = statement.executeQuery("PRAGMA user_version")) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}

	private static int count(final Statement statement, final String dotaz) throws SQLException {
		try (ResultSet rs = statement.executeQuery(dotaz)) {
			return rs.getInt(1);
		}
	}

	/** Zápis data nálezu, který umíme číst: RRRR-MM-DD, případně s časem a zlomkem sekundy. */
	private static final Pattern ZAPIS_DATA = Pattern.compile("\\d{4}-\\d{2}-\\d{2}([ T]\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?)?");

	/** Kolik hodnot data nálezu se při ověření zápisu přečte. */
	private static final int VZORKU_DATA = 20;

	/**
	 * Datum nálezu se čte jen z textového sloupce {@code caches.found_date} se zápisem {@link #ZAPIS_DATA}. Jiný typ sloupce nebo zápis (změna formátu v novější verzi
	 * OpenSAKu) by se četl špatně, proto se pak datum nálezu nepoužije a ohlásí se to.
	 */
	private static boolean datumNalezuPouzitelny(final Statement statement, final File file) throws SQLException {
		final Set<String> sloupce = DatabazeJinehoProgramu.sloupce(statement, "caches");
		if (!sloupce.contains("found_date") || !sloupce.contains("found")) {
			return true;
		}
		String typ = "";
		try (ResultSet rs = statement.executeQuery("PRAGMA table_info(caches)")) {
			while (rs.next()) {
				if ("found_date".equalsIgnoreCase(rs.getString("name"))) {
					typ = String.valueOf(rs.getString("type")).toUpperCase(Locale.ROOT);
				}
			}
		}
		if (!typ.contains("TEXT") && !typ.contains("CHAR") && !typ.contains("CLOB")) {
			log.warn("Databáze OpenSAKu {}: sloupec caches.found_date má typ „{}“, datum nálezu se nepoužije.", file, typ);
			return false;
		}
		try (ResultSet rs = statement.executeQuery("SELECT found_date FROM caches WHERE found = 1 AND found_date IS NOT NULL LIMIT " + VZORKU_DATA)) {
			while (rs.next()) {
				final String hodnota = rs.getString(1).trim();
				if (!hodnota.isEmpty() && !ZAPIS_DATA.matcher(hodnota).matches()) {
					log.warn("Databáze OpenSAKu {}: datum nálezu „{}“ má neznámý tvar, datum nálezu se nepoužije.", file, hodnota);
					return false;
				}
			}
		}
		return true;
	}

	private void loadCaches(final File file, final Statement statement, final IImportBuilder builder, final Future<?> future, final Progressor progressor, final boolean datumNalezu)
			throws SQLException {
		final DotahovacZDatabaze hint = HintZDatabaze.dotahovac(file, HintZDatabaze.OPENSAK);
		final Preskocene preskocene = new Preskocene("keš");
		final boolean hinty = DatabazeJinehoProgramu.sloupce(statement, "caches").contains("encoded_hints");
		final Set<String> poznamky = DatabazeJinehoProgramu.sloupce(statement, "user_notes");
		final boolean opravy = poznamky.contains("cache_id");
		final String dotaz = "SELECT " + DatabazeJinehoProgramu.vyber(statement, "caches", SLOUPCE_CACHES) + ", "
				+ (opravy ? DatabazeJinehoProgramu.vyber(statement, "user_notes", SLOUPCE_USER_NOTES) : "NULL as corrected_lat, NULL as corrected_lon, NULL as is_corrected")
				+ ", " + SpojeneTexty.vyraz(DatabazeJinehoProgramu.sloupceNeboNull(statement, "caches", TEXTY_CACHES)) + " FROM caches"
				+ (opravy ? " LEFT JOIN user_notes ON user_notes.cache_id = caches.id" : "");
		int citac = 0;
		try (ResultSet rs = statement.executeQuery(dotaz)) {
			final SpojeneTexty texty = SpojeneTexty.posledniSloupec(rs, TEXTY_CACHES);
			while (rs.next()) {
				if (future != null && future.isCancelled()) {
					return;
				}
				progressor.addProgress(PROGRESS_VAHA_CACHES);
				texty.nacti(rs);
				final String kod = texty.get("gc_code");
				try {
					final GpxWpt cache = new GpxWpt();
					cache.name = kod;
					cache.sym = rs.getBoolean("found") ? "Geocache Found" : "Geocache";
					cache.wgs = new Wgs(rs.getDouble("latitude"), rs.getDouble("longitude"));
					cache.time = datum(texty.get("hidden_date"));

					final Groundspeak groundspeak = new Groundspeak();
					groundspeak.name = texty.get("name");
					groundspeak.type = intern(typKese(texty.get("cache_type")));
					groundspeak.container = intern(StringUtils.isBlank(texty.get("container")) ? "Unknown" : texty.get("container"));
					groundspeak.difficulty = intern(hodnoceni(rs, "difficulty"));
					groundspeak.terrain = intern(hodnoceni(rs, "terrain"));
					groundspeak.placedBy = intern(texty.get("placed_by"));
					groundspeak.owner = intern(texty.get("owner_name"));
					groundspeak.ownerid = cislo(texty.get("owner_id"));
					groundspeak.country = intern(texty.get("country"));
					groundspeak.state = intern(texty.get("state"));
					groundspeak.archived = rs.getBoolean("archived");
					groundspeak.availaible = rs.getObject("available") == null || rs.getBoolean("available");
					if (hinty) {
						groundspeak.hintZDatabaze = hint;
					}
					cache.groundspeak = groundspeak;
					cache.desc = String.format("%s by %s (%s / %s)", groundspeak.name, groundspeak.placedBy, groundspeak.difficulty, groundspeak.terrain);
					cache.link.href = "http://coord.info/" + kod;
					cache.link.text = String.format("%s by %s", groundspeak.name, groundspeak.placedBy);

					if (rs.getBoolean("found") && datumNalezu) {
						cache.gpxg.found = datum(texty.get("found_date"));
					}
					if (rs.getObject("favorite_points") != null) {
						cache.gpxg.favorites = rs.getInt("favorite_points");
					}
					cache.gpxg.elevation = (int) Math.round(rs.getDouble("elevation"));
					cache.gpxg.czkraj = groundspeak.state;
					builder.addGpxWpt(cache);

					// Opravené souřadnice jako Final, keš zůstane na místě z listingu (stejně jako u GSAKu).
					if (rs.getBoolean("is_corrected") && rs.getObject("corrected_lat") != null && rs.getObject("corrected_lon") != null) {
						final Wgs opravene = new Wgs(rs.getDouble("corrected_lat"), rs.getDouble("corrected_lon"));
						if (!opravene.equals(cache.wgs)) {
							final GpxWpt fin = new GpxWpt();
							fin.wgs = opravene;
							fin.name = "##" + kod.substring(2);
							fin.sym = "Final Location";
							fin.desc = "Final (" + groundspeak.name + ")";
							builder.addGpxWpt(fin);
						}
					}
					citac++;
				} catch (final RuntimeException e) {
					preskocene.preskoc(kod, e);
				}
			}
		} finally {
			preskocene.ohlas();
			log.info("{} keší z OpenSAKu načteno", citac);
		}
		preskocene.ohlasVetsinuKesi(citac, file, "OpenSAKu");
	}

	private void loadWaypoints(final Statement statement, final IImportBuilder builder, final Future<?> future, final Progressor progressor) throws SQLException {
		final Preskocene preskocene = new Preskocene("waypoint");
		final String sPolohou = "waypoints.latitude IS NOT NULL AND waypoints.longitude IS NOT NULL AND NOT (waypoints.latitude = 0 AND waypoints.longitude = 0)";
		final List<String> vyrazy = new ArrayList<>();
		vyrazy.add("caches.gc_code");
		vyrazy.addAll(DatabazeJinehoProgramu.sloupceNeboNull(statement, "waypoints", TEXTY_WAYPOINTS));
		final String[] jmenaTextu = { "parent", "prefix", "wp_type", "name", "wp_code", "description" };
		final String dotaz = "SELECT caches.gc_code as parent, " + DatabazeJinehoProgramu.vyber(statement, "waypoints", SLOUPCE_WAYPOINTS) + ", " + SpojeneTexty.vyraz(vyrazy)
				+ " FROM waypoints JOIN caches ON caches.id = waypoints.cache_id WHERE " + sPolohou + " ORDER BY waypoints.cache_id, waypoints.id";
		final int bezPolohy = count(statement, "SELECT count(*) FROM waypoints WHERE NOT (" + sPolohou + ")");
		final int bezKese = count(statement, "SELECT count(*) FROM waypoints WHERE " + sPolohou + " AND cache_id NOT IN (SELECT id FROM caches)");
		if (bezPolohy + bezKese > 0) {
			log.info("Z OpenSAKu vynecháno {} waypointů bez polohy (nebo na 0, 0) a {} waypointů bez keše", bezPolohy, bezKese);
		}
		final Set<String> kodyVKesi = new HashSet<>();
		String predchoziParent = null;
		int citac = 0;
		try (ResultSet rs = statement.executeQuery(dotaz)) {
			final SpojeneTexty texty = SpojeneTexty.posledniSloupec(rs, jmenaTextu);
			while (rs.next()) {
				if (future != null && future.isCancelled()) {
					return;
				}
				progressor.addProgress(PROGRESS_VAHA_WAYPOINTS);
				texty.nacti(rs);
				final String parent = texty.get("parent");
				if (!parent.equals(predchoziParent)) {
					kodyVKesi.clear();
					kodyVKesi.add(parent);
					predchoziParent = parent;
				}
				try {
					final GpxWpt wpt = new GpxWpt();
					wpt.wgs = new Wgs(rs.getDouble("latitude"), rs.getDouble("longitude"));
					wpt.name = kodWaypointu(parent, texty.get("prefix"), texty.get("wp_code"), kodyVKesi);
					wpt.sym = texty.get("wp_type");
					wpt.desc = StringUtils.isBlank(texty.get("name")) ? texty.get("description") : texty.get("name");
					builder.addGpxWpt(wpt);
					citac++;
				} catch (final RuntimeException e) {
					preskocene.preskoc(parent, e);
				}
			}
		} finally {
			preskocene.ohlas();
			log.info("{} waypointů z OpenSAKu načteno", citac);
		}
	}

	/**
	 * Keš si waypoint přiřadí podle kódu bez prvních dvou znaků. OpenSAK z GPX ukládá jen prefix, který může mít jinou délku než dva znaky
	 * a v jedné keši se může opakovat, proto se kód doplní na dva znaky a případně změní na volný.
	 */
	private static String kodWaypointu(final String parent, final String prefix, final String wpCode, final Set<String> kodyVKesi) {
		final String pripona = parent.substring(Math.min(2, parent.length()));
		final String zaklad;
		if (!StringUtils.isBlank(prefix)) {
			zaklad = prefix.trim();
		} else if (!StringUtils.isBlank(wpCode)) {
			final String kod = wpCode.trim();
			zaklad = kod.endsWith(pripona) && kod.length() > pripona.length() ? kod.substring(0, kod.length() - pripona.length()) : kod;
		} else {
			zaklad = "WP";
		}
		final String prvni = zaklad.length() >= 2 ? zaklad.substring(0, 2) : (zaklad + "0").substring(0, 2);
		if (kodyVKesi.add(prvni + pripona)) {
			return prvni + pripona;
		}
		final String znaky = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i = -1; i < znaky.length(); i++) {
			final char a = i < 0 ? prvni.charAt(0) : znaky.charAt(i);
			for (int j = 0; j < znaky.length(); j++) {
				final String kod = "" + a + znaky.charAt(j) + pripona;
				if (kodyVKesi.add(kod)) {
					return kod;
				}
			}
		}
		throw new IllegalStateException("Keš " + parent + " má příliš mnoho waypointů");
	}

	static String typKese(final String typ) {
		if (typ == null) {
			return null;
		}
		final String gpx = TYPY_KESI.get(typ.trim().toLowerCase(Locale.ROOT));
		return gpx != null ? gpx : typ.trim();
	}

	/** OpenSAK ukládá datum jako „2020-05-01 00:00:00.000000“, GPX má „2020-05-01T00:00:00“. */
	private static String datum(final String s) {
		if (StringUtils.isBlank(s)) {
			return null;
		}
		final String d = s.trim().replace(' ', 'T');
		final int tecka = d.indexOf('.');
		return tecka > 0 ? d.substring(0, tecka) : d;
	}

	private static String hodnoceni(final ResultSet rs, final String sloupec) throws SQLException {
		final Object o = rs.getObject(sloupec);
		if (o == null) {
			return null;
		}
		try {
			return new BigDecimal(o.toString().trim()).stripTrailingZeros().toPlainString();
		} catch (final NumberFormatException e) {
			return o.toString();
		}
	}

	private static int cislo(final String s) {
		if (s == null) {
			return 0;
		}
		try {
			return Integer.parseInt(s.trim());
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	private static Map<String, String> typyKesi() {
		final Map<String, String> m = new HashMap<>();
		for (final String[] t : new String[][] { //
				{ "Traditional Cache", "traditional", "tradi" }, //
				{ "Multi-cache", "multi", "multicache", "multi cache" }, //
				{ "Unknown Cache", "unknown", "mystery", "mystery cache", "puzzle" }, //
				{ "Letterbox Hybrid", "letterbox", "letterbox cache" }, //
				{ "Wherigo Cache", "wherigo" }, //
				{ "Earthcache", "earth", "earth cache" }, //
				{ "Virtual Cache", "virtual" }, //
				{ "Webcam Cache", "webcam" }, //
				{ "Event Cache", "event" }, //
				{ "Cache In Trash Out Event", "cito" }, //
				{ "Mega-Event Cache", "mega", "mega-event", "mega event" }, //
				{ "Giga-Event Cache", "giga", "giga-event", "giga event" }, //
				{ "Lost and Found Event Caches", "community celebration event" }, //
				{ "Groundspeak Lost and Found Celebration", "geocaching hq celebration" }, //
				{ "Groundspeak Block Party", "geocaching hq block party" }, //
				{ "Groundspeak HQ Cache", "geocaching hq cache" }, //
				{ "GPS Adventures Exhibit", "gps adventures maze" }, //
				{ "Lab Cache", "lab" }, //
				{ "Project APE Cache", "project a.p.e. cache" }, //
				{ "Locationless (Reverse) Cache", "locationless" }, //
		}) {
			for (final String alias : t) {
				m.put(alias.toLowerCase(Locale.ROOT), t[0]);
			}
		}
		return Collections.unmodifiableMap(m);
	}

	/** Waypointy mají příponu kódu své keše. */
	@Override
	Collection<String> jmenaPredem(final File file) throws SQLException {
		return DatabazeJinehoProgramu.jmena(file, "SELECT gc_code FROM caches");
	}
}
