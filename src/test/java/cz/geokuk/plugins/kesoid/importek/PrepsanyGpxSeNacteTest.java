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

/** Uživatel přepíše GPX ve složce dat (nový export, dokončená kopie); při dalším pravidelném načtení se ukážou nové keše bez restartu. */
public class PrepsanyGpxSeNacteTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final Genom genom = new Genom();
	private File gpx;
	private File slozkaGeogetu;
	private MultiNacitac nacitac;
	private KesBag zobrazene;

	@Before
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		slozkaGeogetu = tmp.newFolder("geoget");
		nacitac = new MultiNacitac(model());
	}

	private void zapis(final long zmenen, final String... kody) throws Exception {
		final String[] wpts = new String[kody.length];
		for (int i = 0; i < kody.length; i++) {
			wpts[i] = ImportKesiTest.kes(kody[i], "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "");
		}
		final File f = new File(gpx, "kesky.gpx");
		Files.write(f.toPath(), ImportKesiTest.gpx(wpts).getBytes(StandardCharsets.UTF_8));
		Assert.assertTrue(f.setLastModified(zmenen));
	}

	/** Jako pravidelné načítání v programu: složky se nemění, nic se nevynucuje. */
	private KesBag nactiZmeny() throws Exception {
		nacitac.setRootDirs(false, gpx, slozkaGeogetu, null, Collections.emptySet());
		final KesBag bag = nacitac.nacti(null, genom);
		if (bag != null) {
			zobrazene = bag;
		}
		return bag;
	}

	private Set<String> kody() {
		final Set<String> kody = new HashSet<>();
		for (final Kesoid k : zobrazene.getKesoidy()) {
			kody.add(k.getIdentifier());
		}
		return kody;
	}

	@Test
	public void prepsanyGpxUkazeNoveKese() throws Exception {
		final long pred = System.currentTimeMillis() - 120_000;
		zapis(pred, "GC0001", "GC0002");
		Assert.assertNotNull(nactiZmeny());
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC0001", "GC0002")), kody());
		Assert.assertNull("beze změny se znovu nenačítá", nactiZmeny());

		zapis(pred + 30_000, "GC0003");
		Assert.assertNotNull("přepsaný soubor se má načíst znovu", nactiZmeny());
		Assert.assertEquals(Collections.singleton("GC0003"), kody());
	}

	/** Kopie do složky: nejdřív useknutý soubor, pak tentýž dopsaný. */
	@Test
	public void dokoncenaKopieSeNacte() throws Exception {
		final long ted = System.currentTimeMillis();
		final File f = new File(gpx, "kesky.gpx");
		zapis(ted, "GC0001", "GC0002", "GC0003");
		final byte[] vse = Files.readAllBytes(f.toPath());
		Files.write(f.toPath(), Arrays.copyOf(vse, vse.length / 2));
		Assert.assertTrue(f.setLastModified(ted));
		nactiZmeny();

		Files.write(f.toPath(), vse);
		Assert.assertTrue(f.setLastModified(ted + 2_000));
		Assert.assertNotNull(nactiZmeny());
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC0001", "GC0002", "GC0003")), kody());
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
