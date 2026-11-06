package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.sql.*;
import java.util.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.util.exception.*;
import cz.geokuk.util.file.*;

/** Načtení keší z databáze OpenSAKu. */
public class OpensakDbLoaderTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File db;

	@Before
	public void setUp() throws Exception {
		db = new File(tmp.getRoot(), "Default.db");
		OpensakTestDb.zaloz(db, OpensakTestDb.verze());
		naplnKese(db);
	}

	static void naplnKese(final File db) throws SQLException {
		OpensakTestDb.vlozKes(db, 1, "GC1111", "Unknown Cache", 50.1, 14.4, "difficulty", 3.5, "terrain", 2.0, "container", "Small", "placed_by", "Kačer", "owner_name", "Kačer",
				"owner_id", "7", "hidden_date", "2015-06-01 00:00:00.000000", "country", "Czech Republic", "state", "Praha", "encoded_hints", "Pod kamenem", "favorite_points", 12,
				"elevation", 250.0, "found", 1, "found_date", "2020-05-01 10:15:00.000000");
		OpensakTestDb.vlozKes(db, 2, "GC2222", "Traditional Cache", 49.2, 16.6, "archived", 1, "available", 0);
		OpensakTestDb.vlozKes(db, 3, "GC3333", "Multi-cache", 49.5, 16.0, "available", 0);
		OpensakTestDb.vloz(db, "user_notes", "id", 1, "cache_id", 1, "corrected_lat", 50.2, "corrected_lon", 14.5, "is_corrected", 1, "updated_at", "2020-01-01");
		OpensakTestDb.vloz(db, "user_notes", "id", 2, "cache_id", 2, "corrected_lat", 49.3, "corrected_lon", 16.7, "is_corrected", 0, "updated_at", "2020-01-01");
		OpensakTestDb.vloz(db, "waypoints", "id", 1, "cache_id", 2, "prefix", "PK", "wp_type", "Parking Area", "name", "Parkoviště", "latitude", 49.21, "longitude", 16.61);
		OpensakTestDb.vloz(db, "waypoints", "id", 2, "cache_id", 3, "wp_code", "S13333", "wp_type", "Stages of a Multicache", "description", "Stage 1", "latitude", 49.51,
				"longitude", 16.01);
		OpensakTestDb.vloz(db, "waypoints", "id", 3, "cache_id", 3, "prefix", "RP", "wp_type", "Reference Point", "name", "Bez souřadnic");
	}

	static Map<String, GpxWpt> nacti(final File db) throws Exception {
		final Map<String, GpxWpt> w = new LinkedHashMap<>();
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		new OpensakDbLoader().nacti(db, new Builder(w), null, progress);
		return w;
	}

	@Test
	public void rozpoznaDatabaziOpensaku() {
		Assert.assertTrue(new OpensakDbLoader().umiNacist(db));
	}

	@Test
	public void nerozpoznaJinouPriponu() throws Exception {
		final File jina = new File(tmp.getRoot(), "Default.db3");
		OpensakTestDb.zaloz(jina, 24);
		Assert.assertFalse(new OpensakDbLoader().umiNacist(jina));
	}

	@Test
	public void nerozpoznaDatabaziGsakuGeogetuAniCiziSqlite() throws Exception {
		final File gsak = new File(tmp.getRoot(), "gsak.db");
		sql(gsak, "CREATE TABLE Caches (Code TEXT, Name TEXT, Latitude REAL, Longitude REAL)", "CREATE TABLE CacheMemo (Code TEXT, Hints TEXT)", "CREATE TABLE Waypoints (cParent TEXT)");
		final File geoget = new File(tmp.getRoot(), "geoget.db");
		sql(geoget, "CREATE TABLE geocache (id TEXT, x REAL, y REAL)", "CREATE TABLE geolist (id TEXT)", "CREATE TABLE waypoint (id TEXT, x REAL, y REAL)");
		final File cizi = new File(tmp.getRoot(), "cizi.db");
		sql(cizi, "CREATE TABLE caches (id INTEGER, url TEXT)", "CREATE TABLE waypoints (id INTEGER)");
		final File bezWaypointu = new File(tmp.getRoot(), "bezwpt.db");
		sql(bezWaypointu, "CREATE TABLE caches (id INTEGER, gc_code TEXT)");
		for (final File f : Arrays.asList(gsak, geoget, cizi, bezWaypointu)) {
			Assert.assertFalse(f.getName(), new OpensakDbLoader().umiNacist(f));
		}
	}

	@Test
	public void kes() throws Exception {
		final GpxWpt kes = nacti(db).get("GC1111");
		Assert.assertEquals("Keš GC1111", kes.groundspeak.name);
		Assert.assertEquals("Unknown Cache", kes.groundspeak.type);
		Assert.assertEquals("3.5", kes.groundspeak.difficulty);
		Assert.assertEquals("2", kes.groundspeak.terrain);
		Assert.assertEquals("Small", kes.groundspeak.container);
		Assert.assertEquals("Kačer", kes.groundspeak.placedBy);
		Assert.assertEquals("Kačer", kes.groundspeak.owner);
		Assert.assertEquals(7, kes.groundspeak.ownerid);
		Assert.assertEquals("Czech Republic", kes.groundspeak.country);
		Assert.assertEquals("Praha", kes.groundspeak.state);
		Assert.assertEquals("2015-06-01T00:00:00", kes.time);
		Assert.assertNull("hint se při načítání nečte", kes.groundspeak.encodedHints);
		Assert.assertEquals("Pod kamenem", kes.groundspeak.hintZDatabaze.apply(kes.name));
		Assert.assertEquals(12, kes.gpxg.favorites);
		Assert.assertEquals(250, kes.gpxg.elevation);
		Assert.assertEquals("http://coord.info/GC1111", kes.link.href);
	}

	@Test
	public void stavy() throws Exception {
		final Map<String, GpxWpt> w = nacti(db);
		final GpxWpt nalezena = w.get("GC1111");
		Assert.assertEquals("Geocache Found", nalezena.sym);
		Assert.assertEquals("2020-05-01T10:15:00", nalezena.gpxg.found);
		Assert.assertFalse(nalezena.groundspeak.archived);
		Assert.assertTrue(nalezena.groundspeak.availaible);

		final GpxWpt archivovana = w.get("GC2222");
		Assert.assertEquals("Geocache", archivovana.sym);
		Assert.assertNull(archivovana.gpxg.found);
		Assert.assertTrue(archivovana.groundspeak.archived);

		final GpxWpt zakazana = w.get("GC3333");
		Assert.assertFalse(zakazana.groundspeak.archived);
		Assert.assertFalse(zakazana.groundspeak.availaible);
	}

	@Test
	public void typyKesiJakoVGpx() {
		Assert.assertEquals("Traditional Cache", OpensakDbLoader.typKese("Traditional Cache"));
		Assert.assertEquals("Traditional Cache", OpensakDbLoader.typKese("Traditional"));
		Assert.assertEquals("Multi-cache", OpensakDbLoader.typKese("multi"));
		Assert.assertEquals("Unknown Cache", OpensakDbLoader.typKese("Mystery"));
		Assert.assertEquals("Earthcache", OpensakDbLoader.typKese("EarthCache"));
		Assert.assertEquals("GPS Adventures Exhibit", OpensakDbLoader.typKese("GPS Adventures Maze"));
		Assert.assertEquals("Lost and Found Event Caches", OpensakDbLoader.typKese("Community Celebration Event"));
		Assert.assertEquals("Project APE Cache", OpensakDbLoader.typKese("Project A.P.E. Cache"));
		Assert.assertEquals("Groundspeak HQ Cache", OpensakDbLoader.typKese("Geocaching HQ Cache"));
		Assert.assertEquals("Hotel/POI", OpensakDbLoader.typKese("Hotel/POI"));
	}

	@Test
	public void opraveneSouradniceJakoFinal() throws Exception {
		final Map<String, GpxWpt> w = nacti(db);
		Assert.assertEquals("keš zůstává na původním místě", 50.1, w.get("GC1111").wgs.lat, 1e-9);
		final GpxWpt fin = w.get("##1111");
		Assert.assertNotNull(fin);
		Assert.assertEquals("Final Location", fin.sym);
		Assert.assertEquals(50.2, fin.wgs.lat, 1e-9);
		Assert.assertEquals(14.5, fin.wgs.lon, 1e-9);
		Assert.assertNull("souřadnice nejsou označené jako opravené", w.get("##2222"));
		Assert.assertNull("bez poznámky není final", w.get("##3333"));
	}

	@Test
	public void waypointy() throws Exception {
		final Map<String, GpxWpt> w = nacti(db);
		final GpxWpt parkoviste = w.get("PK2222");
		Assert.assertNotNull(w.keySet().toString(), parkoviste);
		Assert.assertEquals("Parking Area", parkoviste.sym);
		Assert.assertEquals("Parkoviště", parkoviste.desc);
		Assert.assertEquals(49.21, parkoviste.wgs.lat, 1e-9);
		final GpxWpt stage = w.get("S13333");
		Assert.assertNotNull(w.keySet().toString(), stage);
		Assert.assertEquals("Stage 1", stage.desc);
		Assert.assertNull("waypoint bez souřadnic není na 0, 0", w.get("RP3333"));
	}

	/** OpenSAK ukládá neznámou polohu waypointu i jako 0, 0. */
	@Test
	public void waypointNaNuleSeVynecha() throws Exception {
		OpensakTestDb.vloz(db, "waypoints", "id", 10, "cache_id", 2, "prefix", "TR", "latitude", 0.0, "longitude", 0.0);
		OpensakTestDb.vloz(db, "waypoints", "id", 11, "cache_id", 2, "prefix", "QA", "latitude", 0.0, "longitude", 16.0);
		final Map<String, GpxWpt> w = nacti(db);
		Assert.assertNull(w.get("TR2222"));
		Assert.assertNotNull("jen jedna souřadnice nulová", w.get("QA2222"));
	}

	/** Kódy waypointů se v keši nesmí opakovat, ani když jsou její waypointy v tabulce prokládané waypointy jiné keše. */
	@Test
	public void prokladaneWaypointyMajiJedinecneKody() throws Exception {
		OpensakTestDb.vloz(db, "waypoints", "id", 10, "cache_id", 2, "prefix", "ST", "latitude", 49.22, "longitude", 16.62);
		OpensakTestDb.vloz(db, "waypoints", "id", 11, "cache_id", 3, "prefix", "ST", "latitude", 49.52, "longitude", 16.02);
		OpensakTestDb.vloz(db, "waypoints", "id", 12, "cache_id", 2, "prefix", "ST", "latitude", 49.23, "longitude", 16.63);
		final Map<String, GpxWpt> w = nacti(db);
		Assert.assertNotNull(w.keySet().toString(), w.get("ST2222"));
		Assert.assertNotNull(w.keySet().toString(), w.get("S02222"));
		Assert.assertEquals(49.22, w.get("ST2222").wgs.lat, 1e-9);
		Assert.assertEquals(49.23, w.get("S02222").wgs.lat, 1e-9);
	}

	/** Každý waypoint s polohou patří ke své keši, i když OpenSAK uloží prefix jiné délky nebo stejný prefix víckrát. */
	@Test
	public void waypointyPatriKeKesim() throws Exception {
		final File d = new File(tmp.getRoot(), "Waypointy.db");
		OpensakTestDb.zaloz(d, OpensakTestDb.verze());
		OpensakTestDb.vlozKes(d, 1, "GC7A2JF", "Multi-cache", 50.0, 14.0);
		OpensakTestDb.vlozKes(d, 2, "GC4444", "Unknown Cache", 49.0, 15.0);
		int id = 0;
		for (final Object[] w : new Object[][] { { 1, "T", null }, { 1, "PK", null }, { 1, "PK", null }, { 1, "GC", null }, { 1, "STG", null }, { 1, null, null }, { 1, null, null },
				{ 2, null, "XY4444" }, { 2, "P", null }, { 2, "P0", null } }) {
			id++;
			OpensakTestDb.vloz(d, "waypoints", "id", id, "cache_id", w[0], "prefix", w[1], "wp_code", w[2], "wp_type", "Stages of a Multicache", "latitude", 49.0 + id / 100.0,
					"longitude", 15.0);
		}
		OpensakTestDb.vloz(d, "waypoints", "id", 20, "cache_id", 1, "prefix", "RP", "latitude", 0.0, "longitude", 0.0);
		OpensakTestDb.vloz(d, "waypoints", "id", 21, "cache_id", 99, "prefix", "PK", "latitude", 49.5, "longitude", 15.5);

		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidImportBuilder builder = new KesoidImportBuilder(new Genom(), new GccomNick("Ja", 42), progress, new KesoidPluginManager());
		builder.init();
		builder.setCurrentlyLoading(new KeFile(new FileAndTime(d, 0), new Root(tmp.getRoot(), new Root.Def(0, null, null))), true);
		new OpensakDbLoader().nacti(d, builder, null, progress);
		builder.done();

		final Map<String, Kesoid> kesoidy = new HashMap<>();
		for (final Kesoid k : builder.getKesBag().getKesoidy()) {
			kesoidy.put(k.getIdentifier(), k);
		}
		Assert.assertEquals(kesoidy.keySet().toString(), new HashSet<>(Arrays.asList("GC7A2JF", "GC4444")), kesoidy.keySet());
		Assert.assertEquals(1 + 7, kesoidy.get("GC7A2JF").getWptsCount());
		Assert.assertEquals(1 + 3, kesoidy.get("GC4444").getWptsCount());
		Assert.assertEquals(2 + 10, builder.getKesBag().getWpts().size());
	}

	/** Když nejde přečíst většina keší, uživatel se to dozví, a ty načtené zůstanou. */
	@Test
	public void vetsinaNectitelnychKesiSeOhlasi() throws Exception {
		final File d = new File(tmp.getRoot(), "Vadna.db");
		OpensakTestDb.zaloz(d, OpensakTestDb.verze());
		final int dobrych = 10;
		final int vadnych = Preskocene.MIN_KESI_PRO_HLASKU + 5;
		for (int i = 1; i <= dobrych + vadnych; i++) {
			OpensakTestDb.vlozKes(d, i, String.format("GC%04d", i), "Traditional Cache", 50.0, 14.0);
		}
		final List<String> hlasky = new ArrayList<>();
		FExceptionDumper.setExceptionDumper(new ExceptionDumper() {
			@Override
			public synchronized AExcId dump(final Throwable t, final EExceptionSeverity s, final String okolnost, final ExceptionDumperRepositorySpi r) {
				hlasky.add(s + ": " + okolnost + ": " + t.getMessage());
				return null;
			}
		});
		final Map<String, GpxWpt> w = new LinkedHashMap<>();
		try {
			final ProgressModel progress = new ProgressModel();
			progress.inject(udalost -> {});
			new OpensakDbLoader().nacti(d, new Builder(w) {
				@Override
				public void addGpxWpt(final GpxWpt g) {
					if (Integer.parseInt(g.name.substring(2)) > dobrych) {
						throw new IllegalStateException("vadná keš");
					}
					super.addGpxWpt(g);
				}
			}, null, progress);
		} finally {
			FExceptionDumper.setExceptionDumper(null);
		}
		Assert.assertEquals(dobrych, w.size());
		Assert.assertEquals(hlasky.toString(), 1, hlasky.size());
		Assert.assertTrue(hlasky.get(0), hlasky.get(0).startsWith("DISPLAY") && hlasky.get(0).contains(vadnych + " z " + (dobrych + vadnych)));
	}

	@Test
	public void parVadnychKesiSeNeohlasuje() throws Exception {
		final List<String> hlasky = new ArrayList<>();
		FExceptionDumper.setExceptionDumper(new ExceptionDumper() {
			@Override
			public synchronized AExcId dump(final Throwable t, final EExceptionSeverity s, final String okolnost, final ExceptionDumperRepositorySpi r) {
				hlasky.add(okolnost);
				return null;
			}
		});
		try {
			final Preskocene p = new Preskocene("keš");
			for (int i = 0; i < Preskocene.MIN_KESI_PRO_HLASKU - 1; i++) {
				p.preskoc("GC" + i, new IllegalStateException());
			}
			p.ohlasVetsinuKesi(0, db, "OpenSAKu");
			final Preskocene mensina = new Preskocene("keš");
			for (int i = 0; i < Preskocene.MIN_KESI_PRO_HLASKU; i++) {
				mensina.preskoc("GC" + i, new IllegalStateException());
			}
			mensina.ohlasVetsinuKesi(Preskocene.MIN_KESI_PRO_HLASKU, db, "OpenSAKu");
		} finally {
			FExceptionDumper.setExceptionDumper(null);
		}
		Assert.assertEquals(hlasky.toString(), 0, hlasky.size());
	}

	/** Starší OpenSAK nemá některé sloupce ani tabulku poznámek, keše se přesto načtou. */
	@Test
	public void bezVolitelnychSloupcu() throws Exception {
		final File starsi = new File(tmp.getRoot(), "Starsi.db");
		OpensakTestDb.zaloz(starsi, 15, "caches.encoded_hints", "caches.favorite_points", "caches.container", "caches.found_date", "caches.owner_id", "waypoints.prefix", "user_notes");
		OpensakTestDb.vlozKes(starsi, 1, "GC1111", "Traditional Cache", 50.1, 14.4);
		OpensakTestDb.vloz(starsi, "waypoints", "id", 1, "cache_id", 1, "wp_code", "PK1111", "latitude", 50.11, "longitude", 14.41);
		final Map<String, GpxWpt> w = nacti(starsi);
		final GpxWpt kes = w.get("GC1111");
		Assert.assertNotNull(kes);
		Assert.assertNull(kes.groundspeak.hintZDatabaze);
		Assert.assertEquals(Gpxg.NEUVEDENO, kes.gpxg.favorites);
		Assert.assertNotNull(w.get("PK1111"));
	}

	@Test
	public void bezPovinnehoSloupceSePreskoci() throws Exception {
		final File jina = new File(tmp.getRoot(), "Jina.db");
		OpensakTestDb.zaloz(jina, 24, "caches.latitude");
		OpensakTestDb.vloz(jina, "caches", "id", 1, "gc_code", "GC1111", "name", "x", "cache_type", "Traditional Cache", "longitude", 14.4);
		try {
			nacti(jina);
			Assert.fail();
		} catch (final DatabazeJinehoProgramu.JineSchema e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("OpenSAKu") && e.getMessage().contains("caches.latitude"));
		}
	}

	@Test
	public void novejsiSchemaSeNacte() throws Exception {
		final File novejsi = new File(tmp.getRoot(), "Novejsi.db");
		OpensakTestDb.zaloz(novejsi, OpensakTestDb.verze() + 1);
		naplnKese(novejsi);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + novejsi); Statement s = c.createStatement()) {
			s.execute("ALTER TABLE caches ADD COLUMN novy_sloupec TEXT");
		}
		Assert.assertTrue(new OpensakDbLoader().umiNacist(novejsi));
		Assert.assertEquals("schéma v testu je ověřená verze", OpensakDbLoader.OVERENA_VERZE_SCHEMATU, OpensakTestDb.verze());
		Assert.assertTrue(nacti(novejsi).containsKey("GC1111"));
	}

	private File dbSNalezy(final String nazev, final String typSloupce, final Object... datumy) throws Exception {
		final File f = new File(tmp.getRoot(), nazev);
		OpensakTestDb.zaloz(f, OpensakTestDb.verze(), "caches.found_date");
		sql(f, "ALTER TABLE caches ADD COLUMN found_date " + typSloupce);
		for (int i = 0; i < datumy.length; i++) {
			OpensakTestDb.vlozKes(f, i + 1, "GC" + (1000 + i), "Traditional Cache", 50.0 + i * 0.01, 14.0, "found", 1, "found_date", datumy[i]);
		}
		return f;
	}

	@Test
	public void datumNalezuVeZnamemZapisuSeNacte() throws Exception {
		final Map<String, GpxWpt> w = nacti(dbSNalezy("Zapisy.db", "TEXT", "2020-05-01 10:15:00.000000", "2020-05-02", "2020-05-03T11:12", "2020-05-04 11:12:13", "", " 2020-05-05 "));
		Assert.assertEquals("2020-05-01T10:15:00", w.get("GC1000").gpxg.found);
		Assert.assertEquals("2020-05-02", w.get("GC1001").gpxg.found);
		Assert.assertEquals("2020-05-03T11:12", w.get("GC1002").gpxg.found);
		Assert.assertEquals("2020-05-04T11:12:13", w.get("GC1003").gpxg.found);
		Assert.assertNull("prázdné datum", w.get("GC1004").gpxg.found);
		Assert.assertNotNull("mezery okolo se ořežou", w.get("GC1005").gpxg.found);
	}

	@Test
	public void datumNalezuVJinemTvaruSeNepouzijeAleNalezZustane() throws Exception {
		for (final String spatne : new String[] { "1.5.2020", "20200501", "2020-5-1", "05/01/2020", "datum", "2020-05-01 10:15:00 +01:00", "2020-05-01x" }) {
			final Map<String, GpxWpt> w = nacti(dbSNalezy("Spatny" + spatne.hashCode() + ".db", "TEXT", "2020-05-01", spatne));
			Assert.assertNull(spatne, w.get("GC1000").gpxg.found);
			Assert.assertNull(spatne, w.get("GC1001").gpxg.found);
			Assert.assertEquals("keš zůstává nalezená", "Geocache Found", w.get("GC1000").sym);
		}
	}

	@Test
	public void datumNalezuVNetextovemSloupciSeNepouzije() throws Exception {
		for (final String typ : new String[] { "INTEGER", "REAL", "NUMERIC", "BLOB" }) {
			final Map<String, GpxWpt> w = nacti(dbSNalezy("Typ" + typ + ".db", typ, 20200501, 20200502));
			Assert.assertNull(typ, w.get("GC1000").gpxg.found);
			Assert.assertEquals(typ, "Geocache Found", w.get("GC1000").sym);
		}
	}

	@Test
	public void datumNalezuSeNepouzijeVeCiselnemSloupciIKdyzMaZnamyZapis() throws Exception {
		for (final String typ : new String[] { "INTEGER", "REAL", "BLOB" }) {
			final Map<String, GpxWpt> w = nacti(dbSNalezy("ZapisVTypu" + typ + ".db", typ, "2020-05-01 10:15:00"));
			Assert.assertNull(typ, w.get("GC1000").gpxg.found);
		}
	}

	@Test
	public void datumNalezuVeVariantachTextovehoTypuSeNacte() throws Exception {
		for (final String typ : new String[] { "text", "VARCHAR(30)", "NVARCHAR", "CLOB" }) {
			final Map<String, GpxWpt> w = nacti(dbSNalezy("Typ" + typ.hashCode() + ".db", typ, "2020-05-01"));
			Assert.assertEquals(typ, "2020-05-01", w.get("GC1000").gpxg.found);
		}
	}

	@Test
	public void zapisDataSeOverujeNaPrvnichDvaceti() throws Exception {
		final Object[] datumy = new Object[21];
		Arrays.fill(datumy, "2020-05-01");
		datumy[19] = "špatně";
		Assert.assertNull("dvacátá hodnota je ještě v ověření", nacti(dbSNalezy("Dvacata.db", "TEXT", datumy)).get("GC1000").gpxg.found);
		datumy[19] = "2020-05-01";
		datumy[20] = "špatně";
		Assert.assertNotNull("jedenadvacátá už ne", nacti(dbSNalezy("Jedenadvacata.db", "TEXT", datumy)).get("GC1000").gpxg.found);
	}

	@Test
	public void bezSloupceDatumNalezuSeNacteBezNeho() throws Exception {
		final File f = new File(tmp.getRoot(), "BezData.db");
		OpensakTestDb.zaloz(f, OpensakTestDb.verze(), "caches.found_date");
		OpensakTestDb.vlozKes(f, 1, "GC1000", "Traditional Cache", 50.0, 14.0, "found", 1);
		Assert.assertNull(nacti(f).get("GC1000").gpxg.found);
	}

	private static void sql(final File db, final String... prikazy) throws SQLException {
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			for (final String p : prikazy) {
				s.execute(p);
			}
		}
	}

	static class Builder implements IImportBuilder {
		private final Map<String, GpxWpt> w;

		Builder(final Map<String, GpxWpt> w) {
			this.w = w;
		}

		@Override
		public void addGpxWpt(final GpxWpt g) {
			w.put(g.name, g);
		}

		@Override
		public void init() {}

		@Override
		public void done() {}

		@Override
		public void addTrackWpt(final GpxWpt wpt) {}

		@Override
		public void begTrack() {}

		@Override
		public void begTrackSegment() {}

		@Override
		public void endTrack() {}

		@Override
		public void endTrackSegment() {}

		@Override
		public void setTrackName(final String nazev) {}
	}

	/** Kódy zjištěné předem pokrývají klíče všech načtených waypointů, jinak by se překryv zdrojů nepoznal včas. */
	@Test
	public void jmenaPredemPokryvajiKliceVsechWaypointu() throws Exception {
		final KliceZdroje.Sberac s = new KliceZdroje.Sberac();
		for (final String jmeno : new OpensakDbLoader().jmenaPredem(db)) {
			s.pridej(KliceZdroje.klicJmena(jmeno));
		}
		final KliceZdroje predem = s.hotovo();
		final java.util.Map<String, GpxWpt> w = nacti(db);
		Assert.assertTrue(w.size() > 1);
		for (final String jmeno : w.keySet()) {
			final KliceZdroje.Sberac jeden = new KliceZdroje.Sberac();
			jeden.pridej(KliceZdroje.klicJmena(jmeno));
			Assert.assertEquals(jmeno, 1, predem.spolecnych(jeden.hotovo()));
		}
	}
}
