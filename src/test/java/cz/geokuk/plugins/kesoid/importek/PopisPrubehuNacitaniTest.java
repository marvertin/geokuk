package cz.geokuk.plugins.kesoid.importek;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingUtilities;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.*;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.file.*;

/** Průběh načítání ve stavovém řádku ukazuje typ zdroje a cestu v datové složce, plnou cestu v bublině. */
public class PopisPrubehuNacitaniTest {

	private static final char S = File.separatorChar;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	/** Text průběhu → bublina. */
	private final Map<String, String> prubehy = new LinkedHashMap<>();

	@Test
	public void popisKazdehoTypuZdroje() throws Exception {
		final File geoget = tmp.newFolder("geoget");
		final File gsak = tmp.newFolder("gsak");
		final File opensak = tmp.newFolder("opensak");
		final File gpx = tmp.newFolder("gpx");

		Assert.assertEquals("GeoGet" + S + "geoget.db3", popis(geoget, new File(geoget, "geoget.db3"), MultiNacitac.FILE_NAME_REGEX_GEOGET_DIR, null));
		Assert.assertEquals("GSAK" + S + "Default" + S + "sqlite.db3", popis(gsak, new File(gsak, "Default/sqlite.db3"), MultiNacitac.GSAK_ROOTDIR_DEF, null));
		Assert.assertEquals("OpenSAK" + S + "kese.db", popis(opensak, new File(opensak, "kese.db"), MultiNacitac.OPENSAK_ROOTDIR_DEF, null));
		Assert.assertEquals("GPX" + S + "Kesky" + S + "a.gpx", popis(gpx, new File(gpx, "Kesky/a.gpx"), MultiNacitac.FILE_NAME_REGEX_GEOKUK_DIR, null));
		Assert.assertEquals("GPX" + S + "balik.zip" + S + "uvnitr" + S + "b.gpx",
				popis(gpx, new File(gpx, "balik.zip"), MultiNacitac.FILE_NAME_REGEX_GEOKUK_DIR, new ZipEntry("uvnitr/b.gpx")));
	}

	@Test(timeout = 60_000)
	public void nacitaniUkazeKratkyPopisAPlnouCestuVBubline() throws Exception {
		final File gpx = tmp.newFolder("gpx");
		final File gpxVPodslozce = new File(gpx, "Kesky/a.gpx");
		zapis(gpxVPodslozce, gpx("GC1A"));
		final File zip = new File(gpx, "balik.zip");
		try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(zip))) {
			z.putNextEntry(new ZipEntry("uvnitr/b.gpx"));
			z.write(gpx("GC1B").getBytes(StandardCharsets.UTF_8));
			z.closeEntry();
		}
		final File opensak = tmp.newFolder("opensak");
		final File db = new File(opensak, "kese.db");
		OpensakTestDb.zaloz(db, OpensakTestDb.verze());

		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, gpx, null, null, opensak, Collections.<File> emptySet());
		nacitac.nacti(null, new Genom());
		SwingUtilities.invokeAndWait(() -> {});

		Assert.assertEquals(prubehy.toString(), gpxVPodslozce.toString(), prubehy.get("GPX" + S + "Kesky" + S + "a.gpx"));
		Assert.assertEquals(prubehy.toString(), zip + "/uvnitr/b.gpx", prubehy.get("GPX" + S + "balik.zip" + S + "uvnitr" + S + "b.gpx"));
		Assert.assertEquals(prubehy.toString(), db.toString(), prubehy.get("OpenSAK" + S + "kese.db"));
	}

	private static String popis(final File koren, final File soubor, final Root.Def def, final ZipEntry entry) {
		return MultiNacitac.popisPrubehu(new KeFile(new FileAndTime(soubor, 0), new Root(koren, def)), entry);
	}

	private static String gpx(final String... kody) {
		return ImportKesiTest.gpx(Arrays.stream(kody).map(k -> ImportKesiTest.kes(k, "Geocache", "Traditional Cache", "Kačer", 7, true, false, "1.5", "")).toArray(String[]::new));
	}

	private static void zapis(final File f, final String obsah) throws IOException {
		f.getParentFile().mkdirs();
		Files.write(f.toPath(), obsah.getBytes(StandardCharsets.UTF_8));
	}

	private KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {
			if (udalost instanceof ProgressEvent) {
				final ProgressEvent e = (ProgressEvent) udalost;
				prubehy.put(e.getText(), e.getTooltip());
			}
		});
		final KesoidModel model = new KesoidModel() {
			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public KesBag getVsechnyKesoidy() {
				return null;
			}

			@Override
			public void fire(final Event0<?> udalost) {}

			@Override
			public void setNacitaneZdroje(final InformaceOZdrojich zdroje) {}

			@Override
			public void zaradGsakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public void zaradOpensakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
			@Override
			public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public boolean maSeNacist(final File zdroj) {
				return true;
			}

			@Override
			public boolean maSeNacist(final KeFile zdroj) {
				return true;
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}
}
