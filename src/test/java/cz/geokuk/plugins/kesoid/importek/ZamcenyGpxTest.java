package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.exception.*;

/** GPX, který zrovna drží jiný program (Windows: sharing violation), se nehlásí jako chyba a načte se, až ho program pustí. */
public class ZamcenyGpxTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final Genom genom = new Genom();
	private File gpx;
	private File slozkaGeogetu;
	private MultiNacitac nacitac;
	private KesBag zobrazene;
	private final List<String> hlaseni = new ArrayList<>();
	/** Kolikrát ještě otevření souboru selže jako na Windows, když ho drží jiný program. */
	private int zamceno;

	/** Čte GPX jako NacitacGpx, ale dokud je soubor „zamčený“, otevření selže stejnou výjimkou jako ve Windows. */
	private class ZamykanyGpx extends Nacitac0 {
		private final NacitacGpx gpx = new NacitacGpx();

		@Override
		protected void nacti(final File file, final IImportBuilder builder, final Future<?> future, final ProgressModel p) throws IOException {
			if (zamceno > 0) {
				zamceno--;
				throw new java.io.FileNotFoundException(file + " (The process cannot access the file because it is being used by another process)");
			}
			gpx.nacti(file, builder, future, p);
		}

		@Override
		protected void nacti(final ZipFile zipFile, final ZipEntry zipEntry, final IImportBuilder builder, final Future<?> future, final ProgressModel p) throws IOException {
			gpx.nacti(zipFile, zipEntry, builder, future, p);
		}

		@Override
		boolean umiNacist(final File file) {
			return gpx.umiNacist(file);
		}

		@Override
		boolean umiNacist(final ZipEntry zipEntry) {
			return gpx.umiNacist(zipEntry);
		}
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		slozkaGeogetu = tmp.newFolder("geoget");
		nacitac = new MultiNacitac(model());
		final Field f = MultiNacitac.class.getDeclaredField("nacitace");
		f.setAccessible(true);
		final List<Nacitac0> nacitace = (List<Nacitac0>) f.get(nacitac);
		nacitace.replaceAll(n -> n instanceof NacitacGpx ? new ZamykanyGpx() : n);
		FExceptionDumper.setExceptionDumper(new ExceptionDumper() {
			@Override
			public synchronized AExcId dump(final Throwable t, final EExceptionSeverity s, final String okolnost, final ExceptionDumperRepositorySpi r) {
				hlaseni.add(s + ": " + okolnost + ": " + t.getMessage());
				return null;
			}
		});
	}

	@After
	public void obnovDumper() {
		FExceptionDumper.setExceptionDumper(null);
	}

	/** Jako pravidelné načítání v programu: složky se nemění, nic se nevynucuje. */
	private void nactiZmeny() throws Exception {
		nacitac.setRootDirs(false, gpx, slozkaGeogetu, null, Collections.emptySet());
		final KesBag bag = nacitac.nacti(null, genom);
		if (bag != null) {
			zobrazene = bag;
		}
	}

	private Set<String> kody() {
		final Set<String> kody = new HashSet<>();
		if (zobrazene != null) {
			for (final Kesoid k : zobrazene.getKesoidy()) {
				kody.add(k.getIdentifier());
			}
		}
		return kody;
	}

	@Test
	public void zamcenyGpxSeNacteAzHoProgramPusti() throws Exception {
		final String[] wpts = { ImportKesiTest.kes("GC0001", "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "") };
		final File f = new File(gpx, "kesky.gpx");
		Files.write(f.toPath(), ImportKesiTest.gpx(wpts).getBytes(StandardCharsets.UTF_8));
		Assert.assertTrue(f.setLastModified(System.currentTimeMillis() - 120_000));

		zamceno = 1;
		nactiZmeny();
		Assert.assertEquals("zámek jiného programu není chyba souboru", Collections.emptyList(), hlaseni);

		// Program soubor pustil, obsah ani čas se nezměnily; při dalším pravidelném načtení se keše objeví.
		nactiZmeny();
		Assert.assertEquals(Collections.singleton("GC0001"), kody());
	}

	@Test
	public void drzenySeHlasiAzPoLimitu() throws Exception {
		final File f = tmp.newFile("drzeny.gpx");
		final Exception drzeni = new java.nio.file.FileSystemException(f.getPath(), null, "The process cannot access the file because it is being used by another process");
		Assert.assertTrue(Nacitac0.jeDrzenyJinymProgramem(drzeni, f, 1_000));
		Assert.assertTrue(Nacitac0.jeDrzenyJinymProgramem(drzeni, f, 1_000 + Nacitac0.LIMIT_DRZENI_MS - 1));
		Assert.assertFalse("po limitu se hlásí jako chyba", Nacitac0.jeDrzenyJinymProgramem(drzeni, f, 1_000 + Nacitac0.LIMIT_DRZENI_MS));
		Assert.assertTrue("nový zámek se počítá znovu", Nacitac0.jeDrzenyJinymProgramem(drzeni, f, 2_000_000));
		Nacitac0.prectenCely(f);
	}

	@Test
	public void nepristupnyNeboChybejiciNeniDrzeny() throws Exception {
		final File f = tmp.newFile("bezprav.gpx");
		Assert.assertFalse(Nacitac0.jeDrzenyJinymProgramem(new java.nio.file.AccessDeniedException(f.getPath()), f));
		Assert.assertFalse(Nacitac0.jeDrzenyJinymProgramem(new java.io.FileNotFoundException("neni"), new File(tmp.getRoot(), "neni.gpx")));
		Assume.assumeTrue("práva jdou odebrat (ne root)", f.setReadable(false) && !f.canRead());
		Assert.assertFalse(Nacitac0.jeDrzenyJinymProgramem(new java.io.FileNotFoundException(f + " (Permission denied)"), f));
	}

	private KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel model = new KesoidModel() {
			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public KesBag getVsechnyKesoidy() {
				return zobrazene;
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
			public boolean maSeNacist(final cz.geokuk.util.file.KeFile zdroj) {
				return true;
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}
}
