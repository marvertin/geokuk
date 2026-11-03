package cz.geokuk.plugins.mapy.kachle.podklady;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.mapy.kachle.data.KaLoc;

/**
 * Překryv tras ze stažené vrstvy tras (layers/…/*.tourist.map) i bez základní mapy: syntetická vrstva {@code vrstva.tourist.map} s červenou a modrou turistickou
 * trasou a cyklotrasou (tagy fmrel* jako z geokuk-maps), bez silnic.
 */
public class PrekryvVrstvyTrasTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private File slozka;
	private File vrstvy;
	private OfflineMapy trasy;

	@Before
	public void setUp() throws IOException {
		slozka = tmp.newFolder("offline-mapy");
		vrstvy = new File(slozka, OfflineMapy.SLOZKA_VRSTEV + "/europe");
		Assert.assertTrue(vrstvy.mkdirs());
		trasy = new OfflineMapy(() -> {}, true);
		trasy.kontrolaSlozkyNs = 0;
		trasy.nastav(slozka, TemaOfflineMapy.TRASY);
	}

	@After
	public void tearDown() {
		trasy.zavri();
	}

	private static void zkopiruj(final String zdroj, final File cil) throws IOException {
		try (InputStream in = PrekryvVrstvyTrasTest.class.getResourceAsStream(zdroj)) {
			Files.copy(in, cil.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static int pocet(final BufferedImage img, final int r, final int g, final int b) {
		int n = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				final int p = img.getRGB(x, y);
				if (p >>> 24 >= 200 && Math.abs((p >> 16 & 0xff) - r) < 40 && Math.abs((p >> 8 & 0xff) - g) < 40 && Math.abs((p & 0xff) - b) < 40) {
					n++;
				}
			}
		}
		return n;
	}

	private static int pruhlednych(final BufferedImage img) {
		int n = 0;
		for (final int p : img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth())) {
			if (p >>> 24 == 0) {
				n++;
			}
		}
		return n;
	}

	@Test(timeout = 30000)
	public void trasyZeVrstvyBezZakladniMapy() throws IOException {
		zkopiruj("/offline-mapy-trasy/vrstva.tourist.map", new File(vrstvy, "kukov.tourist.map"));
		Assert.assertEquals("základní mapa ve složce není", Collections.emptyList(), OfflineMapy.mapyVeSlozce(slozka));
		final OfflineRenderer r = trasy.pouzij();
		try {
			Assert.assertTrue("pokrytí podle hlavičky vrstvy", r.pokryva(PrekryvTrasTest.PESINA_Z16));
			Assert.assertFalse(r.pokryva(KaLoc.ofJZ(new Wgs(49.0, 16.0).toMou(), 16)));
			final BufferedImage img = r.vyrendruj(PrekryvTrasTest.PESINA_Z16);
			Assert.assertTrue("průhledné pozadí", pruhlednych(img) > 7L * img.getWidth() * img.getHeight() / 10);
			Assert.assertTrue("červená trasa", pocet(img, 0xE0, 0x00, 0x00) > 50);
			Assert.assertTrue("modrá trasa vedle červené", pocet(img, 0x0A, 0x50, 0xE0) > 50);
		} finally {
			r.skonci();
		}
		Assert.assertNull(trasy.getChyba());
	}

	@Test
	public void bezMapIBezTrasChyba() {
		try {
			trasy.pouzij();
			Assert.fail("bez map a tras se nekreslí");
		} catch (final IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("nejsou offline mapy ani stažené trasy"));
		}
	}

	/** Základní mapa, ke které je stažená vrstva tras, se do překryvu nebere: stejné trasy by se kreslily dvakrát. Ostatní základní mapy zůstanou. */
	@Test
	public void zakladniMapaSVrstvouTrasSeVynecha() throws IOException {
		for (final String f : Arrays.asList("czech-republic.map", "austria.map", "moje.map")) {
			Files.write(new File(slozka, f).toPath(), new byte[1]);
		}
		Files.write(new File(vrstvy, "czech-republic.tourist.map").toPath(), new byte[1]);
		Files.write(new File(vrstvy, "czech-republic.contours.map").toPath(), new byte[1]);
		Assert.assertTrue(new File(vrstvy, "germany").mkdir());
		Files.write(new File(vrstvy, "germany/bayern.tourist.map").toPath(), new byte[1]);
		Assert.assertEquals(Arrays.asList(new File(vrstvy, "czech-republic.tourist.map"), new File(vrstvy, "germany/bayern.tourist.map"),
				new File(slozka, "austria.map"), new File(slozka, "moje.map")), OfflineMapy.mapyPrekryvu(slozka, false));
		Assert.assertEquals(Collections.emptyList(), OfflineMapy.mapyPrekryvu(null, false));
		Assert.assertEquals("vrstvy tras kreslí podklad, překryv bere jen základní mapy bez vrstvy tras",
				Arrays.asList(new File(slozka, "austria.map"), new File(slozka, "moje.map")), OfflineMapy.mapyPrekryvu(slozka, true));
	}

	/** Starší verze vrstvy tras (*.routes.map) slouží překryvu jako vrstva tras, dokud vedle ní není vrstva tourist. */
	@Test
	public void starsiVerzeVrstvyTras() throws IOException {
		for (final String f : Arrays.asList("czech-republic.map", "austria.map")) {
			Files.write(new File(slozka, f).toPath(), new byte[1]);
		}
		Files.write(new File(vrstvy, "czech-republic.routes.map").toPath(), new byte[1]);
		Files.write(new File(vrstvy, "austria.routes.map").toPath(), new byte[1]);
		Files.write(new File(vrstvy, "austria.tourist.map").toPath(), new byte[1]);
		Assert.assertEquals(Arrays.asList(new File(vrstvy, "austria.tourist.map"), new File(vrstvy, "czech-republic.routes.map")),
				OfflineMapy.mapyPrekryvu(slozka, false));
	}

	/** Se základní mapou i vrstvou tras jiného území se kreslí obojí; po stažení vrstvy se renderer otevře znovu. */
	@Test(timeout = 30000)
	public void vrstvaTrasPribudePoStazeni() throws IOException {
		PrekryvTrasTest.zkopirujMapu(slozka);
		final OfflineRenderer r0 = trasy.pouzij();
		r0.skonci();
		Assert.assertEquals(1, r0.getPocetMap());
		zkopiruj("/offline-mapy-trasy/vrstva.tourist.map", new File(vrstvy, "kukov.tourist.map"));
		trasy.zkontrolujSlozku();
		trasy.zkontrolujSlozku();
		final OfflineRenderer r1 = trasy.pouzij();
		r1.skonci();
		Assert.assertEquals(2, r1.getPocetMap());
		Assert.assertNotEquals(r0.getKlicDlazdice(PrekryvTrasTest.PESINA_Z16), r1.getKlicDlazdice(PrekryvTrasTest.PESINA_Z16));
	}

	/** Když vrstvy tras kreslí podklad (offline mapa s tématem GeoKuk), překryv je vynechá a otevře se znovu jen se základní mapou. */
	@Test(timeout = 30000)
	public void vrstvuTrasKresliPodklad() throws IOException {
		PrekryvTrasTest.zkopirujMapu(slozka);
		zkopiruj("/offline-mapy-trasy/vrstva.tourist.map", new File(vrstvy, "kukov.tourist.map"));
		final OfflineRenderer r0 = trasy.pouzij();
		r0.skonci();
		Assert.assertEquals(2, r0.getPocetMap());
		trasy.setTrasyKresliPodklad(true);
		final OfflineRenderer r1 = trasy.pouzij();
		r1.skonci();
		Assert.assertEquals(1, r1.getPocetMap());
		Assert.assertNotEquals(r0.getKlicDlazdice(PrekryvTrasTest.PESINA_Z16), r1.getKlicDlazdice(PrekryvTrasTest.PESINA_Z16));
	}
}
