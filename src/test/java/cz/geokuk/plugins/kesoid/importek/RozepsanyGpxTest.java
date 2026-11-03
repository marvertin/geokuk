package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

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

/** GPX, který se právě kopíruje, nesmí hlásit chybu; načte se, až je celý. */
public class RozepsanyGpxTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final Genom genom = new Genom();
	private File gpx;
	private File slozkaGeogetu;
	private MultiNacitac nacitac;
	private KesBag zobrazene;
	private final List<String> hlaseni = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		slozkaGeogetu = tmp.newFolder("geoget");
		nacitac = new MultiNacitac(model());
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

	private static byte[] cely(final String... kody) {
		final String[] wpts = new String[kody.length];
		for (int i = 0; i < kody.length; i++) {
			wpts[i] = ImportKesiTest.kes(kody[i], "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "");
		}
		return ImportKesiTest.gpx(wpts).getBytes(StandardCharsets.UTF_8);
	}

	private void zapis(final byte[] obsah, final long zmenen) throws Exception {
		final File f = new File(gpx, "kesky.gpx");
		Files.write(f.toPath(), obsah);
		f.setLastModified(zmenen);
	}

	private KesBag nacti() throws Exception {
		nacitac.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.emptySet());
		zobrazene = nacitac.nacti(null, genom);
		return zobrazene;
	}

	private static Set<String> kody(final KesBag bag) {
		Assert.assertNotNull("má se načíst", bag);
		final Set<String> kody = new HashSet<>();
		for (final Kesoid k : bag.getKesoidy()) {
			kody.add(k.getIdentifier());
		}
		return kody;
	}

	private StavZdroje stavSouboru() {
		return stavSouboru("kesky.gpx");
	}

	private StavZdroje stavSouboru(final String jmeno) {
		return nacitac.getRegistr().getSnimek().getPolozky().stream().filter(p -> p.getSoubor().getName().equals(jmeno)).findFirst().get().getStav();
	}

	private void zapisZip(final byte[] obsah, final long zmenen) throws Exception {
		final File f = new File(gpx, "kesky.zip");
		Files.write(f.toPath(), obsah);
		Assert.assertTrue(f.setLastModified(zmenen));
	}

	private static byte[] zip(final byte[] gpxObsah) throws Exception {
		final java.io.ByteArrayOutputStream bajty = new java.io.ByteArrayOutputStream();
		try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(bajty)) {
			zos.putNextEntry(new java.util.zip.ZipEntry("kesky.gpx"));
			zos.write(gpxObsah);
			zos.closeEntry();
		}
		return bajty.toByteArray();
	}

	private static byte[] pulka(final byte[] obsah) {
		return Arrays.copyOf(obsah, obsah.length * 2 / 3);
	}

	@Test
	public void useknutyCerstvyGpxNehlasiChybuAPrectesePoDokonceni() throws Exception {
		final byte[] vse = cely("GC0001", "GC0002", "GC0003");
		zapis(pulka(vse), System.currentTimeMillis());
		nacti();
		Assert.assertEquals(Collections.emptyList(), hlaseni);
		Assert.assertEquals("čeká na dokončení zápisu, ne načteno", StavZdroje.CEKA_NA_ZAPIS, stavSouboru());

		zapis(vse, System.currentTimeMillis() + 5000);
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC0001", "GC0002", "GC0003")), kody(nacti()));
		Assert.assertEquals(Collections.emptyList(), hlaseni);
		Assert.assertEquals(StavZdroje.NACTENO, stavSouboru());
	}

	@Test
	public void gpxSNulamiZPredemRezervovanehoMistaNehlasiChybu() throws Exception {
		final byte[] vse = cely("GC0001", "GC0002", "GC0003");
		final byte[] sNulami = Arrays.copyOf(vse, vse.length);
		Arrays.fill(sNulami, sNulami.length / 2, sNulami.length, (byte) 0);
		zapis(sNulami, System.currentTimeMillis());
		nacti();
		Assert.assertEquals(Collections.emptyList(), hlaseni);

		zapis(vse, System.currentTimeMillis() + 5000);
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC0001", "GC0002", "GC0003")), kody(nacti()));
	}

	@Test
	public void useknutyStarySouborSeHlasiJakoChyba() throws Exception {
		zapis(pulka(cely("GC0001", "GC0002", "GC0003")), System.currentTimeMillis() - 60_000);
		nacti();
		Assert.assertEquals(hlaseni.toString(), 1, hlaseni.size());
		Assert.assertTrue(hlaseni.get(0), hlaseni.get(0).startsWith("DISPLAY"));
	}

	@Test
	public void useknutyCerstvySouborKterySeNezmeniSeNakonecNahlasi() throws Exception {
		final byte[] obsah = pulka(cely("GC0001", "GC0002", "GC0003"));
		zapis(obsah, System.currentTimeMillis());
		nacti();
		Assert.assertEquals(Collections.emptyList(), hlaseni);
		Assert.assertEquals(StavZdroje.CEKA_NA_ZAPIS, stavSouboru());

		new File(gpx, "kesky.gpx").setLastModified(System.currentTimeMillis() - 60_000);
		nacti();
		Assert.assertEquals(hlaseni.toString(), 1, hlaseni.size());
		Assert.assertTrue(hlaseni.get(0), hlaseni.get(0).startsWith("DISPLAY"));
	}

	@Test
	public void useknutyCerstvyZipNehlasiChybuAPrectesePoDokonceni() throws Exception {
		final byte[] vse = zip(cely("GC0001", "GC0002", "GC0003"));
		zapisZip(pulka(vse), System.currentTimeMillis());
		nacti();
		Assert.assertEquals(Collections.emptyList(), hlaseni);
		Assert.assertEquals(StavZdroje.CEKA_NA_ZAPIS, stavSouboru("kesky.zip"));

		zapisZip(vse, System.currentTimeMillis() + 5000);
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC0001", "GC0002", "GC0003")), kody(nacti()));
		Assert.assertEquals(Collections.emptyList(), hlaseni);
	}

	/** Kopírování s předem vyhrazeným místem: zip je zatím jen nuly, nemá ani hlavičku. */
	@Test
	public void zipSNulamiZPredemRezervovanehoMistaNehlasiChybu() throws Exception {
		final byte[] vse = zip(cely("GC0001", "GC0002"));
		zapisZip(new byte[vse.length], System.currentTimeMillis());
		nacti();
		Assert.assertEquals(Collections.emptyList(), hlaseni);

		zapisZip(vse, System.currentTimeMillis() + 5000);
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC0001", "GC0002")), kody(nacti()));
		Assert.assertEquals(Collections.emptyList(), hlaseni);
	}

	@Test
	public void useknutyStaryZipSeHlasiJakoChyba() throws Exception {
		zapisZip(pulka(zip(cely("GC0001"))), System.currentTimeMillis() - 60_000);
		nacti();
		Assert.assertEquals(hlaseni.toString(), 1, hlaseni.size());
	}

	@Test
	public void zipSeJmenyVCp852SeNacte() throws Exception {
		final java.io.ByteArrayOutputStream bajty = new java.io.ByteArrayOutputStream();
		try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(bajty, java.nio.charset.Charset.forName("IBM852"))) {
			zos.putNextEntry(new java.util.zip.ZipEntry("Kešky Jiří.gpx"));
			zos.write(cely("GC0001"));
			zos.closeEntry();
		}
		zapisZip(bajty.toByteArray(), System.currentTimeMillis() - 60_000);
		Assert.assertEquals(Collections.singleton("GC0001"), kody(nacti()));
		Assert.assertEquals(Collections.emptyList(), hlaseni);
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
