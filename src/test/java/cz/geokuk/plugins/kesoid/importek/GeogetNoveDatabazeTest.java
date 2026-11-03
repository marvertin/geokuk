package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.util.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.util.file.FileAndTime;
import cz.geokuk.util.file.KeFile;
import cz.geokuk.util.file.Root;

/** Prohledání složky GeoGetu zařazuje její databáze (.db3) podle volby „Nové databáze načíst až po vybrání“, odděleně od GSAKu. */
public class GeogetNoveDatabazeTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final MyPreferences pref = MyPreferences.current().node("test-geoget-nove-databaze");
	private final GsakParametryNacitani parametry = new GsakParametryNacitani();
	private final List<Set<File>> zarazeno = new ArrayList<>();
	private final List<Set<File>> nedostupneVZarazeni = new ArrayList<>();
	private boolean zaznamenat;

	@After
	public void uklid() throws Exception {
		pref.removeNode();
	}

	@Test
	public void novaDatabazeVeSlozceGeogetuSeZablokuje() throws Exception {
		parametry.setNoveDatabazeGeogetuHned(false);
		final File geoget = tmp.newFolder("geoget");
		final File cesko = db(geoget, "Cesko.db3");
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, geoget, null, Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertTrue("první prohledání nic nezablokuje", nacte(cesko));

		final File slovensko = db(geoget, "Slovensko.db3");
		nacitac.nacti(null, new Genom());
		Assert.assertTrue(nacte(cesko));
		Assert.assertFalse(nacte(slovensko));
	}

	@Test
	public void novaDatabazeVeSlozceGeogetuSeNacteHnedPriVychozimNastaveni() throws Exception {
		final File geoget = tmp.newFolder("geoget");
		db(geoget, "Cesko.db3");
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, geoget, null, Collections.emptySet());
		nacitac.nacti(null, new Genom());
		final File slovensko = db(geoget, "Slovensko.db3");
		nacitac.nacti(null, new Genom());
		Assert.assertTrue(nacte(slovensko));
	}

	/** Seznamy známých databází GeoGetu a GSAKu jsou oddělené a každý zdroj se řídí svou volbou. */
	@Test
	public void geogetAGsakMajiOddeleneSeznamyAVolby() throws Exception {
		parametry.setNoveDatabazeGeogetuHned(false);
		parametry.setNoveDatabazeHned(true);
		final File geoget = tmp.newFolder("geoget");
		final File gsak = tmp.newFolder("gsak");
		final File cesko = db(geoget, "Cesko.db3");
		final File gsakA = gsakDb(gsak, "A");
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, geoget, gsak, Collections.emptySet());
		nacitac.nacti(null, new Genom());

		final File slovensko = db(geoget, "Slovensko.db3");
		final File gsakB = gsakDb(gsak, "B");
		nacitac.nacti(null, new Genom());

		final MyPreferences kesoid = pref.node(FPref.KESOID_node);
		Assert.assertEquals(new HashSet<>(Arrays.asList(cesko, slovensko)), new HashSet<>(kesoid.getFileCollection(FPref.ZNAME_GEOGET_DATABAZE_value, null)));
		Assert.assertEquals(new HashSet<>(Arrays.asList(gsakA, gsakB)), new HashSet<>(kesoid.getFileCollection(FPref.ZNAME_GSAK_DATABAZE_value, null)));
		Assert.assertFalse("GeoGet čeká na výběr", nacte(slovensko));
		Assert.assertTrue("GSAK se řídí svou volbou", nacte(gsakB));
	}

	@Test
	public void nedostupnaSlozkaGeogetuNicNezarazuje() throws Exception {
		zaznamenat = true;
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, new File(tmp.getRoot(), "odpojeny-disk"), null, Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals(Collections.emptyList(), zarazeno);
	}

	@Test
	public void dostupnaSlozkaGeogetuZarazujeSvouDatabaze() throws Exception {
		zaznamenat = true;
		final File geoget = tmp.newFolder("geoget");
		final File cesko = db(geoget, "Cesko.db3");
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, geoget, null, Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals(Collections.singletonList(Collections.singleton(cesko)), zarazeno);
		Assert.assertEquals(Collections.singletonList(Collections.emptySet()), nedostupneVZarazeni);
	}

	@Test
	public void vypnutyGeogetZarazujePrazdnouMnozinu() throws Exception {
		zaznamenat = true;
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, null, null, null, Collections.emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals(Collections.singletonList(Collections.emptySet()), zarazeno);
	}

	private static File db(final File slozka, final String jmeno) throws Exception {
		final File f = new File(slozka, jmeno);
		Assert.assertTrue(f.createNewFile());
		return f;
	}

	private static File gsakDb(final File gsak, final String jmeno) throws Exception {
		final File slozka = new File(gsak, jmeno);
		Assert.assertTrue(slozka.mkdirs());
		return db(slozka, "sqlite.db3");
	}

	private boolean nacte(final File f) {
		return pomocny.maSeNacist(new KeFile(new FileAndTime(f, 0), new Root(f.getParentFile(), new Root.Def(1, null, null))));
	}

	private KesoidModel pomocny;

	private KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel model = new KesoidModel() {
			@Override
			protected MyPreferences currPrefe() {
				return pref;
			}

			@Override
			public GsakParametryNacitani getGsakParametryNacitani() {
				return parametry;
			}

			@Override
			public void fire(final cz.geokuk.framework.Event0<?> udalost) {}

			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {
				if (zaznamenat) {
					zarazeno.add(databaze);
					nedostupneVZarazeni.add(nedostupne);
				} else {
					super.zaradGeogetDatabaze(databaze, nedostupne);
				}
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		pomocny = model;
		return model;
	}
}
