package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

/** Databáze GeoGetu s neznámou strukturou se přeskočí a ostatní zdroje se načtou. */
public class JineSchemaTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void geogetBezSouradnicSePreskoci() throws Exception {
		final File slozkaGeogetu = tmp.newFolder("geoget");
		final File db = new File(slozkaGeogetu, "geoget.db3");
		zalozGeoget(db, "lat REAL, lon REAL");
		try {
			new GeogetLoader().nacti(db, null, null, progress());
			Assert.fail();
		} catch (final DatabazeJinehoProgramu.JineSchema e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("GeoGetu") && e.getMessage().contains("geocache.x") && e.getMessage().contains("geocache.y"));
		}

		final File gpx = tmp.newFolder("gpx");
		Files.write(new File(gpx, "a.gpx").toPath(), ImportKesiTest.gpx(ImportKesiTest.kes("GC1111", "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "")).getBytes(StandardCharsets.UTF_8));
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.emptySet());
		final KesBag bag = nacitac.nacti(null, new Genom());
		final Set<String> kody = new HashSet<>();
		for (final Kesoid k : bag.getKesoidy()) {
			kody.add(k.getIdentifier());
		}
		Assert.assertEquals(Collections.singleton("GC1111"), kody);
	}

	@Test
	public void geogetSeSpravnymiSloupciProjde() throws Exception {
		final File db = new File(tmp.getRoot(), "geoget.db3");
		zalozGeoget(db, "x REAL, y REAL");
		final List<String> nactene = new ArrayList<>();
		new GeogetLoader().nacti(db, new Builder(nactene), null, progress());
		Assert.assertEquals(Collections.singletonList("GC00001"), nactene);
	}

	private static void zalozGeoget(final File db, final String souradnice) throws SQLException {
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute("CREATE TABLE geocache (id TEXT, " + souradnice + ", name TEXT, author TEXT, cachetype TEXT, cachesize TEXT, difficulty TEXT, terrain TEXT,"
					+ " cachestatus INTEGER, gs_ownerid INTEGER, dthidden INTEGER, country TEXT, state TEXT, dtfound INTEGER)");
			s.execute("CREATE TABLE geolist (id TEXT, shortdesc BLOB, hint TEXT)");
			s.execute("CREATE TABLE waypoint (id TEXT, x REAL, y REAL, prefixid TEXT, wpttype TEXT, name TEXT)");
			s.execute("CREATE TABLE geotag (id TEXT, ptrkat INTEGER, ptrvalue INTEGER)");
			s.execute("CREATE TABLE geotagcategory (key INTEGER, value TEXT)");
			s.execute("CREATE TABLE geotagvalue (key INTEGER, value TEXT)");
			s.execute("INSERT INTO geocache VALUES ('GC00001', 50.1, 14.4, 'První', 'autor', 'Traditional Cache', 'Regular', '2', '3', 0, 1, 20200101, 'CZ', 'Praha', 0)");
		}
	}

	private static ProgressModel progress() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		return progress;
	}

	private static KesoidModel model() {
		final KesoidModel model = new KesoidModel() {
			// Bez EventFireru by události z vlákna načítání spadly na EDT.
			@Override
			public void fire(final cz.geokuk.framework.Event0<?> udalost) {}

			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public void zaradGsakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public void zaradOpensakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
			@Override
			public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
		};
		model.inject(progress());
		model.inject(new KesoidPluginManager());
		return model;
	}

	private static class Builder implements IImportBuilder {
		private final List<String> nactene;

		Builder(final List<String> nactene) {
			this.nactene = nactene;
		}

		@Override
		public void addGpxWpt(final GpxWpt w) {
			nactene.add(w.name);
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
		public void setTrackName(final String aTrackName) {}
	}
}
