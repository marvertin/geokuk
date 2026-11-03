package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.util.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.file.KeFile;

/** Špatně zadané datové složky GeoGetu, GSAKu a OpenSAKu se ohlásí jednou zprávou s důvodem u každé složky, ne výpisem chyby. */
public class SpatneDatoveSlozkyTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final List<String> hlaseni = new ArrayList<>();

	@Test(timeout = 60_000)
	public void kazdaSpatnaSlozkaJednouVJedneHlasce() throws Exception {
		final File gpx = tmp.newFolder("gpx");
		final File geoget = new File(tmp.getRoot(), "geoget/data");
		final File gsak = tmp.newFolder("gsak");
		final File opensak = tmp.newFile("opensak");
		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.ohlasovac = hlaseni::add;

		nacitac.setRootDirs(true, gpx, geoget, gsak, opensak, Collections.<File> emptySet());
		nacitac.nacti(null, new Genom());

		Assert.assertEquals(hlaseni.toString(), 1, hlaseni.size());
		final String[] radky = hlaseni.get(0).split("\n");
		Assert.assertEquals(hlaseni.get(0), 4, radky.length);
		Assert.assertEquals("Datová složka GeoGetu \"" + geoget + "\" neexistuje nebo není dostupná.", radky[0]);
		Assert.assertEquals("V datové složce GSAKu \"" + gsak + "\" nejsou žádné databáze (.db3).", radky[1]);
		Assert.assertEquals("Datová složka OpenSAKu \"" + opensak + "\" není čitelná složka.", radky[2]);
		Assert.assertEquals("Zkontrolujte složky v Soubor > Umístění souborů.", radky[3]);

		nacitac.setRootDirs(true, gpx, geoget, gsak, opensak, Collections.<File> emptySet());
		nacitac.nacti(null, new Genom());
		Assert.assertEquals("stejné složky se podruhé neohlašují", 1, hlaseni.size());
	}

	private static KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
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
