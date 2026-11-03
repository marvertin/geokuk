package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.util.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.file.DirScanner;
import cz.geokuk.util.file.KeFile;

/** Dočasně nedostupná složka GSAKu nemění seznam známých databází; vypnutý GSAK ano. */
public class GsakNedostupnaSlozkaTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final List<Set<File>> zarazeno = new ArrayList<>();
	private final List<Set<File>> nedostupneVZarazeni = new ArrayList<>();
	private final List<Set<File>> nedostupnePriNacitani = new ArrayList<>();

	@Test
	public void nedostupnaSlozkaNicNemeni() throws Exception {
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, null, new File(tmp.getRoot(), "odpojeny-disk"), Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals(Collections.emptyList(), zarazeno);
	}

	@Test
	public void vypnutyGsakJePrazdny() throws Exception {
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, null, null, Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals(Collections.singletonList(Collections.emptySet()), zarazeno);
	}

	@Test
	public void dostupnaPrazdnaSlozkaJePrazdna() throws Exception {
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, null, tmp.newFolder("gsak"), Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals(Collections.singletonList(Collections.emptySet()), zarazeno);
	}

	/** Složka se vrátí po skenu, ale před zařazením databází; rozhoduje stav při skenu. */
	@Test
	public void slozkaVracenaPoSkenuNicNemeni() throws Exception {
		final File slozka = new File(tmp.getRoot(), "odpojeny-disk");
		final DirScanner ds = new DirScanner() {
			@Override
			public synchronized List<KeFile> coMamNacist() {
				final List<KeFile> list = super.coMamNacist();
				Assert.assertTrue(slozka.mkdirs());
				return list;
			}
		};
		final MultiNacitac nacitac = new MultiNacitac(model(), ds);
		nacitac.setRootDirs(true, null, null, slozka, Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals(Collections.emptyList(), zarazeno);
	}

	/** Nedostupná podsložka zjištěná při skenu dojde do modelu, aby se její databáze nezablokovaly. */
	@Test
	public void nedostupnaPodslozkaZeSkenuDojdeDoModelu() throws Exception {
		final File gsak = tmp.newFolder("gsak");
		final Set<File> zeSkenu = Collections.singleton(new File(gsak, "nedostupna"));
		final DirScanner ds = new DirScanner() {
			@Override
			public Set<File> getNedostupne() {
				return zeSkenu;
			}
		};
		final MultiNacitac nacitac = new MultiNacitac(model(), ds);
		nacitac.setRootDirs(true, null, null, gsak, Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals(Collections.singletonList(zeSkenu), nedostupneVZarazeni);
		Assert.assertEquals(Collections.singletonList(zeSkenu), nedostupnePriNacitani);
	}

	private KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel model = new KesoidModel() {
			// Bez EventFireru by události z vlákna načítání spadly na EDT.
			@Override
			public void fire(final cz.geokuk.framework.Event0<?> udalost) {}

			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public void zaradGsakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {
				zarazeno.add(databaze);
				nedostupneVZarazeni.add(nedostupne);
			}

			@Override
			public void setNedostupnePriNacitani(final Set<File> nedostupne) {
				nedostupnePriNacitani.add(nedostupne);
				super.setNedostupnePriNacitani(nedostupne);
			}

			@Override
			public void zaradOpensakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
			@Override
			public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}
}
