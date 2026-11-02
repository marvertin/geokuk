package cz.geokuk.plugins.mapy.kachle.data;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.KeyStroke;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.program.FConst;

/** Formát a ověřování souborů uživatelských map. */
public class UzivatelskeMapyTest {

	private static final String URL = "https://tile.example.org/{z}/{x}/{y}.png";

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final List<String> chyby = new ArrayList<>();

	@After
	public void uklid() {
		EKaType.setUzivatelske(Collections.emptyList());
	}

	/** Řádek {@code m.nazev=M} je vlastnost {@code nazev=M} v souboru {@code m.mapa}. */
	private List<EKaType> zpracuj(final String... radky) throws Exception {
		final SortedMap<String, Properties> soubory = new TreeMap<>();
		for (final String radek : radky) {
			if (!radek.isEmpty() && !radek.startsWith("#")) {
				final int tecka = radek.indexOf('.');
				soubory.computeIfAbsent(radek.substring(0, tecka) + UzivatelskeMapy.PRIPONA, k -> new Properties()).load(new StringReader(radek.substring(tecka + 1)));
			}
		}
		return UzivatelskeMapy.zpracuj(soubory, chyby);
	}

	private File slozka(final String... souboryAObsah) throws Exception {
		final File slozka = new File(tmp.getRoot(), UzivatelskeMapy.SLOZKA);
		slozka.mkdirs();
		for (int i = 0; i + 1 < souboryAObsah.length; i += 2) {
			Files.write(new File(slozka, souboryAObsah[i]).toPath(), souboryAObsah[i + 1].getBytes(StandardCharsets.UTF_8));
		}
		return slozka;
	}

	private static List<Path> priklady() throws Exception {
		try (Stream<Path> s = Files.list(Paths.get("priklady", "mapy"))) {
			return s.sorted().collect(Collectors.toList());
		}
	}

	private EKaType jedna(final String... radky) throws Exception {
		final List<EKaType> mapy = zpracuj(radky);
		Assert.assertEquals(Collections.emptyList(), chyby);
		Assert.assertEquals(1, mapy.size());
		return mapy.get(0);
	}

	private void chyba(final String castTextu, final String... radky) throws Exception {
		final List<EKaType> mapy = zpracuj(radky);
		Assert.assertTrue("mapa se měla vynechat", mapy.isEmpty());
		Assert.assertEquals(chyby.toString(), 1, chyby.size());
		Assert.assertTrue(chyby.get(0), chyby.get(0).contains(castTextu));
	}

	private static String url(final EKaType mapa, final int x, final int y, final int z) throws Exception {
		final KaLoc loc = KaLoc.ofJZ(new Mou(x, y), z);
		final String url = new Ka(loc, mapa).getUrl().toString();
		return url.replace(String.valueOf(loc.getFromSzUnsignedX()), "X").replace(String.valueOf(loc.getFromSzUnsignedY()), "Y");
	}

	// Výchozí hodnoty

	@Test
	public void minimalniMapaMaVychoziHodnoty() throws Exception {
		final EKaType m = jedna("topo.nazev=Topo", "topo.url=" + URL);
		Assert.assertEquals("user-topo", m.name());
		Assert.assertEquals("user-topo", m.toString());
		Assert.assertTrue(m.isUzivatelska());
		Assert.assertEquals("Topo", m.getNazev());
		Assert.assertEquals("Topo", m.getPopis());
		Assert.assertEquals(0, m.getMinMoumer());
		Assert.assertEquals(18, m.getMaxMoumer());
		Assert.assertEquals(18, m.getMaxAutoMoumer());
		Assert.assertEquals(0, m.getKlavesa());
		Assert.assertNull(m.getKeyStroke());
		Assert.assertTrue(m.getHlavicky().isEmpty());
		Assert.assertEquals("", m.getAtribuce());
		Assert.assertFalse(m.isHromadneStahovaniPovoleno());
	}

	@Test
	public void vsechnyVlastnosti() throws Exception {
		final EKaType m = jedna("m.nazev=Moje mapa", "m.url=" + URL, "m.popis=Popis v nápovědě", "m.min=3", "m.max=19", "m.maxauto=17",
				"m.klavesa=u", "m.zkratka=ctrl U", "m.atribuce=© Autor", "m.hromadne=ano", "m.hlavicka.Referer=https://mapy.com/", "m.hlavicka.User-Agent=Geokuk/{verze} (test)");
		Assert.assertEquals("Moje mapa", m.getNazev());
		Assert.assertEquals("Popis v nápovědě", m.getPopis());
		Assert.assertEquals(3, m.getMinMoumer());
		Assert.assertEquals(19, m.getMaxMoumer());
		Assert.assertEquals(17, m.getMaxAutoMoumer());
		Assert.assertEquals('U', m.getKlavesa());
		Assert.assertEquals(KeyStroke.getKeyStroke("ctrl U"), m.getKeyStroke());
		Assert.assertEquals("© Autor", m.getAtribuce());
		Assert.assertEquals("https://mapy.com/", m.getHlavicky().get("Referer"));
		Assert.assertEquals("Geokuk/" + FConst.VERSION + " (test)", m.getHlavicky().get("User-Agent"));
		Assert.assertEquals(2, m.getHlavicky().size());
		Assert.assertTrue(m.isHromadneStahovaniPovoleno());
	}

	@Test
	public void mezeryKolemHodnotSeOrezou() throws Exception {
		final EKaType m = jedna("m.nazev =  Topo  ", "m.url = " + URL + "  ", "m.max = 15 ");
		Assert.assertEquals("Topo", m.getNazev());
		Assert.assertEquals(15, m.getMaxMoumer());
	}

	@Test
	public void maxautoVychoziJeMax() throws Exception {
		Assert.assertEquals(12, jedna("m.nazev=M", "m.url=" + URL, "m.max=12").getMaxAutoMoumer());
	}

	@Test
	public void krajniMeritka() throws Exception {
		final EKaType m = jedna("m.nazev=M", "m.url=" + URL, "m.min=0", "m.max=22", "m.maxauto=0");
		Assert.assertEquals(0, m.getMinMoumer());
		Assert.assertEquals(22, m.getMaxMoumer());
		Assert.assertEquals(0, m.getMaxAutoMoumer());
		Assert.assertEquals(5, jedna("n.nazev=N", "n.url=" + URL, "n.min=5", "n.max=5").getMaxMoumer());
	}

	@Test
	public void fitMoumerDrziRozsah() throws Exception {
		final EKaType m = jedna("m.nazev=M", "m.url=" + URL, "m.min=4", "m.max=10");
		Assert.assertEquals(4, m.fitMoumer(1));
		Assert.assertEquals(7, m.fitMoumer(7));
		Assert.assertEquals(10, m.fitMoumer(20));
	}

	// Adresy dlaždic

	@Test
	public void adresaSeSklada() throws Exception {
		Assert.assertEquals("https://tile.example.org/13/X/Y.png", url(jedna("m.nazev=M", "m.url=" + URL), 0x40000000, 0x20000000, 13));
	}

	@Test
	public void adresaVeTvaruMapyCz() throws Exception {
		Assert.assertEquals("http://mapserver.mapy.cz/turist-m/13-X-Y", url(jedna("m.nazev=M", "m.url=http://mapserver.mapy.cz/turist-m/{z}-{x}-{y}"), 0x40000000, 0x20000000, 13));
	}

	@Test
	public void zastupneZnakyVLibovolnemPoradiIVDotazu() throws Exception {
		Assert.assertEquals("https://t.example.org/tile?y=Y&x=X&z=13&key=abc", url(jedna("m.nazev=M", "m.url=https://t.example.org/tile?y={y}&x={x}&z={z}&key=abc"), 0x40000000, 0x20000000, 13));
	}

	@Test
	public void adresaVMalemMeritku() throws Exception {
		final EKaType m = jedna("m.nazev=M", "m.url=" + URL);
		final String url = new Ka(KaLoc.ofJZ(new Mou(0x40000000, 0x20000000), 2), m).getUrl().toString();
		Assert.assertTrue(url, url.matches("https://tile\\.example\\.org/2/[0-3]/[0-3]\\.png"));
	}

	@Test
	public void httpIHttpsSeSmi() throws Exception {
		jedna("m.nazev=M", "m.url=http://t.example.org/{z}/{x}/{y}");
		chyby.clear();
		jedna("n.nazev=N", "n.url=https://t.example.org/{z}/{x}/{y}");
	}

	// Chyby

	@Test
	public void subdomenaSeOdmitne() throws Exception {
		chyba("{s}", "m.nazev=M", "m.url=https://{s}.tile.example.org/{z}/{x}/{y}.png");
	}

	@Test
	public void chybiNazev() throws Exception {
		chyba("m.mapa: nazev", "m.url=" + URL);
	}

	@Test
	public void prazdnyNazev() throws Exception {
		chyba("m.mapa: nazev", "m.nazev= ", "m.url=" + URL);
	}

	@Test
	public void chybiUrl() throws Exception {
		chyba("m.mapa: url", "m.nazev=M");
	}

	@Test
	public void urlBezZastupnychZnaku() throws Exception {
		chyba("m.mapa: url", "m.nazev=M", "m.url=https://t.example.org/{x}/{y}.png");
		chyby.clear();
		chyba("m.mapa: url", "m.nazev=M", "m.url=https://t.example.org/{z}/{y}.png");
		chyby.clear();
		chyba("m.mapa: url", "m.nazev=M", "m.url=https://t.example.org/{z}/{x}.png");
	}

	@Test
	public void urlJinehoProtokolu() throws Exception {
		chyba("m.mapa: url", "m.nazev=M", "m.url=ftp://t.example.org/{z}/{x}/{y}");
		chyby.clear();
		chyba("m.mapa: url", "m.nazev=M", "m.url=file:///{z}/{x}/{y}");
	}

	@Test
	public void urlSMezerou() throws Exception {
		chyba("m.mapa: url", "m.nazev=M", "m.url=https://t.example.org/{z} /{x}/{y}");
	}

	@Test
	public void neplatnyNazevSouboru() throws Exception {
		for (final String id : new String[] { "mapa_1", "-topo", "mapička", "a b" }) {
			chyby.clear();
			Assert.assertTrue(zpracuj(id + ".nazev=M", id + ".url=" + URL).isEmpty());
			Assert.assertEquals(id, 1, chyby.size());
			Assert.assertTrue(chyby.get(0), chyby.get(0).startsWith(id + ".mapa: název souboru"));
		}
	}

	@Test
	public void oznaceniJeNazevSouboruMalymiPismeny() throws Exception {
		Assert.assertEquals("user-topo-25", jedna("Topo-25.nazev=Topo", "Topo-25.url=" + URL).name());
	}

	@Test
	public void neznamaVlastnost() throws Exception {
		final List<EKaType> mapy = zpracuj("m.nazev=M", "m.url=" + URL, "m.barva=modrá", "m.m.nazev=X");
		Assert.assertEquals("platné vlastnosti mapu nezruší", 1, mapy.size());
		Assert.assertEquals(chyby.toString(), 2, chyby.size());
		Assert.assertTrue(chyby.toString(), chyby.contains("m.mapa: barva je neznámá vlastnost, povolené jsou nazev, url, popis, min, max, maxauto, klavesa, zkratka, atribuce, hromadne a hlavicka.<jméno hlavičky>"));
		Assert.assertTrue(chyby.toString(), chyby.stream().anyMatch(ch -> ch.startsWith("m.mapa: m.nazev je neznámá vlastnost")));
	}

	@Test
	public void neplatneJmenoHlavicky() throws Exception {
		zpracuj("m.nazev=M", "m.url=" + URL, "m.hlavicka.Špatné=x", "m.hlavicka.=x");
		Assert.assertEquals(chyby.toString(), 2, chyby.size());
	}

	@Test
	public void hodnotaHlavickySRidicimZnakem() throws Exception {
		for (final String hodnota : new String[] { "a\\rX-Jina: b", "a\\nb", "a\\tb" }) {
			chyby.clear();
			chyba("m.mapa: hlavicka.X-Test nesmí obsahovat řídicí znaky", "m.nazev=M", "m.url=" + URL, "m.hlavicka.X-Test=" + hodnota);
		}
	}

	@Test
	public void meritkaMimoRozsah() throws Exception {
		for (final String[] meritka : new String[][] { { "min", "-1" }, { "max", "23" }, { "max", "30" } }) {
			chyby.clear();
			chyba("měřítka", "m.nazev=M", "m.url=" + URL, "m." + meritka[0] + "=" + meritka[1]);
		}
	}

	@Test
	public void minVetsiNezMax() throws Exception {
		chyba("měřítka", "m.nazev=M", "m.url=" + URL, "m.min=10", "m.max=5");
	}

	@Test
	public void maxautoMimoMinMax() throws Exception {
		chyba("měřítka", "m.nazev=M", "m.url=" + URL, "m.max=10", "m.maxauto=11");
		chyby.clear();
		chyba("měřítka", "m.nazev=M", "m.url=" + URL, "m.min=5", "m.maxauto=4");
	}

	@Test
	public void meritkoNeniCislo() throws Exception {
		chyba("celá čísla", "m.nazev=M", "m.url=" + URL, "m.max=deset");
		chyby.clear();
		chyba("celá čísla", "m.nazev=M", "m.url=" + URL, "m.min=1.5");
	}

	@Test
	public void neplatnaKlavesa() throws Exception {
		for (final String klavesa : new String[] { "ab", "č", "+" }) {
			chyby.clear();
			chyba("klavesa", "m.nazev=M", "m.url=" + URL, "m.klavesa=" + klavesa);
		}
	}

	@Test
	public void serveryBezHromadnehoStahovaniIUzivatelskychMap() throws Exception {
		Assert.assertFalse(jedna("m.nazev=M", "m.url=https://mapserver.mapy.cz/turist-m/{z}-{x}-{y}", "m.hromadne=ano").isHromadneStahovaniPovoleno());
		Assert.assertFalse(jedna("m.nazev=M", "m.url=https://api.MAPY.com/v1/maptiles/outdoor/256/{z}/{x}/{y}", "m.hromadne=ano").isHromadneStahovaniPovoleno());
		Assert.assertFalse(jedna("m.nazev=M", "m.url=https://a.tile.openstreetmap.org/{z}/{x}/{y}.png", "m.hromadne=ano").isHromadneStahovaniPovoleno());
		Assert.assertFalse(jedna("m.nazev=M", "m.url=https://tile.waymarkedtrails.org/hiking/{z}/{x}/{y}.png", "m.hromadne=ano").isHromadneStahovaniPovoleno());
		Assert.assertFalse(jedna("m.nazev=M", "m.url=https://tile.openstreetmap.org./{z}/{x}/{y}.png", "m.hromadne=ano").isHromadneStahovaniPovoleno());
		Assert.assertTrue(jedna("m.nazev=M", "m.url=https://outdoor.tiles.freemap.sk/{z}/{x}/{y}.png", "m.hromadne=ano").isHromadneStahovaniPovoleno());
	}

	@Test
	public void hostitelSubdomenyBezOhleduNaVelikost() {
		Assert.assertTrue(EKaType.jeServerBezHromadnehoStahovani("mapy.cz"));
		Assert.assertTrue(EKaType.jeServerBezHromadnehoStahovani("Mapserver.Mapy.CZ"));
		Assert.assertTrue(EKaType.jeServerBezHromadnehoStahovani("{s}.tile.openstreetmap.org"));
		Assert.assertTrue(EKaType.jeServerBezHromadnehoStahovani("tile.openstreetmap.org."));
		Assert.assertTrue(EKaType.jeServerBezHromadnehoStahovani("mapserver.mapy.cz.."));
		Assert.assertFalse(EKaType.jeServerBezHromadnehoStahovani("nemapy.cz"));
		Assert.assertFalse(EKaType.jeServerBezHromadnehoStahovani("openstreetmap.org"));
		Assert.assertFalse(EKaType.jeServerBezHromadnehoStahovani("ags.cuzk.gov.cz"));
	}

	@Test
	public void hromadneJenAnoNeboNe() throws Exception {
		Assert.assertFalse(jedna("m.nazev=M", "m.url=" + URL, "m.hromadne=ne").isHromadneStahovaniPovoleno());
		chyba("hromadne", "n.nazev=N", "n.url=" + URL, "n.hromadne=true");
	}

	@Test
	public void platneZkratky() throws Exception {
		Assert.assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), jedna("m.nazev=M", "m.url=" + URL, "m.zkratka=u").getKeyStroke());
		Assert.assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_U, java.awt.event.InputEvent.SHIFT_DOWN_MASK), jedna("p.nazev=P", "p.url=" + URL, "p.zkratka=U").getKeyStroke());
		Assert.assertEquals(KeyStroke.getKeyStroke('2'), jedna("q.nazev=Q", "q.url=" + URL, "q.zkratka=2").getKeyStroke());
		Assert.assertEquals(KeyStroke.getKeyStroke("F5"), jedna("n.nazev=N", "n.url=" + URL, "n.zkratka=F5").getKeyStroke());
		Assert.assertEquals(KeyStroke.getKeyStroke("alt shift U"), jedna("o.nazev=O", "o.url=" + URL, "o.zkratka=alt shift U").getKeyStroke());
	}

	@Test
	public void neplatnaZkratka() throws Exception {
		chyba("zkratka", "m.nazev=M", "m.url=" + URL, "m.zkratka=ctrl nesmysl");
	}

	@Test
	public void zkratkaKazdeVestaveneMapySeOdmitne() throws Exception {
		for (final EKaType vestavena : EKaType.vestavene()) {
			if (vestavena.getKeyStroke() != null) {
				chyby.clear();
				chyba(vestavena.getNazev(), "m.nazev=M", "m.url=" + URL, "m.zkratka=" + KeyEvent.getKeyText(vestavena.getKeyStroke().getKeyCode()).toLowerCase());
			}
		}
	}

	/** Příklady zkratek v chybové hlášce nesmí být obsazené akcí programu ani vestavěnou mapou, jinak by hláška radila zkratku, kterou program odmítne. */
	@Test
	public void prikladyZkratekVHlasceJsouVolne() throws Exception {
		zpracuj("m.nazev=M", "m.url=" + URL, "m.zkratka=nesmysl nesmysl");
		Assert.assertEquals(chyby.toString(), 1, chyby.size());
		final String hlaska = chyby.get(0);
		final java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(např\\. ([^)]*)\\)").matcher(hlaska);
		Assert.assertTrue(hlaska, m.find());
		final java.util.Map<KeyStroke, String> obsazene = new cz.geokuk.core.program.Akce().zkratky();
		for (final String priklad : m.group(1).split(", ")) {
			final KeyStroke zkratka = priklad.length() == 1 ? KeyStroke.getKeyStroke(Character.toUpperCase(priklad.charAt(0)), 0) : KeyStroke.getKeyStroke(priklad);
			Assert.assertNotNull(priklad, zkratka);
			Assert.assertFalse(priklad + " je obsazená: " + obsazene.get(zkratka), obsazene.containsKey(zkratka));
			for (final EKaType mapa : EKaType.vestavene()) {
				Assert.assertNotEquals(priklad + " je obsazená mapou " + mapa.getNazev(), zkratka, mapa.getKeyStroke());
			}
		}
	}

	@Test
	public void zkratkaAkceProgramuSeOdmitne() throws Exception {
		UzivatelskeMapy.setZkratkyProgramu(Collections.singletonMap(KeyStroke.getKeyStroke("F3"), "akce Listing do Geogetu"));
		try {
			chyba("Listing do Geogetu", "m.nazev=M", "m.url=" + URL, "m.zkratka=F3");
		} finally {
			UzivatelskeMapy.setZkratkyProgramu(Collections.emptyMap());
		}
	}

	// Kolize

	@Test
	public void stejnaZkratkaVyradiObeMapy() throws Exception {
		final List<EKaType> mapy = zpracuj("a.nazev=A", "a.url=" + URL, "a.zkratka=F5", "b.nazev=B", "b.url=" + URL, "b.zkratka=F5", "c.nazev=C", "c.url=" + URL, "c.zkratka=F6");
		Assert.assertEquals(Collections.singletonList("user-c"), Arrays.asList(mapy.stream().map(EKaType::name).toArray()));
		Assert.assertEquals(Collections.singletonList("a.mapa, b.mapa: stejná klávesová zkratka"), chyby);
	}

	@Test
	public void stejnaZkratkaZapsanaJinakVyradiObeMapy() throws Exception {
		Assert.assertTrue(zpracuj("a.nazev=A", "a.url=" + URL, "a.zkratka=U", "b.nazev=B", "b.url=" + URL, "b.zkratka=shift U").isEmpty());
		Assert.assertEquals(1, chyby.size());
	}

	@Test
	public void stejnyNazevVMenuVyradiObeMapy() throws Exception {
		final List<EKaType> mapy = zpracuj("a.nazev=Topo", "a.url=" + URL, "b.nazev=TOPO", "b.url=" + URL, "c.nazev=Jiná", "c.url=" + URL);
		Assert.assertEquals(1, mapy.size());
		Assert.assertEquals("user-c", mapy.get(0).name());
		Assert.assertEquals(Collections.singletonList("a.mapa, b.mapa: stejný název v menu „Topo“"), chyby);
	}

	@Test
	public void triMapySeStejnymNazvemVyradiVsechny() throws Exception {
		Assert.assertTrue(zpracuj("a.nazev=T", "a.url=" + URL, "b.nazev=T", "b.url=" + URL, "c.nazev=T", "c.url=" + URL).isEmpty());
	}

	@Test
	public void nazvySouboruLisiciSeVelikostiPismenVyradiObe() throws Exception {
		final List<EKaType> mapy = zpracuj("Topo.nazev=A", "Topo.url=" + URL, "topo.nazev=B", "topo.url=" + URL, "jina.nazev=J", "jina.url=" + URL);
		Assert.assertEquals(1, mapy.size());
		Assert.assertEquals("user-jina", mapy.get(0).name());
		Assert.assertEquals(Collections.singletonList("Topo.mapa, topo.mapa: názvy souborů se liší jen velikostí písmen"), chyby);
	}

	@Test
	public void nazevShodnySVestavenouNekoliduje() throws Exception {
		Assert.assertEquals("Turistická", jedna("t.nazev=Turistická", "t.url=" + URL).getNazev());
	}

	// Více map

	@Test
	public void viceMapSeradenychPodleOznaceni() throws Exception {
		final List<EKaType> mapy = zpracuj("zeta.nazev=Z", "zeta.url=" + URL, "alfa.nazev=A", "alfa.url=" + URL, "m-2.nazev=M", "m-2.url=" + URL);
		Assert.assertEquals(Collections.emptyList(), chyby);
		Assert.assertEquals(Arrays.asList("user-alfa", "user-m-2", "user-zeta"), Arrays.asList(mapy.get(0).name(), mapy.get(1).name(), mapy.get(2).name()));
	}

	@Test
	public void chybnaMapaNerusiOstatni() throws Exception {
		final List<EKaType> mapy = zpracuj("dobra.nazev=D", "dobra.url=" + URL, "spatna.nazev=S", "spatna.url=ftp://x", "druha.nazev=D2", "druha.url=" + URL);
		Assert.assertEquals(2, mapy.size());
		Assert.assertEquals(1, chyby.size());
	}

	@Test
	public void oznaceniShodneSVestavenouNekoliduje() throws Exception {
		final EKaType m = jedna("turist-m.nazev=Turistická vlastní", "turist-m.url=" + URL);
		Assert.assertEquals("user-turist-m", m.name());
		Assert.assertNotSame(EKaType.TURIST_M, m);
	}

	@Test
	public void prazdnySouborNicNevytvori() throws Exception {
		Assert.assertTrue(zpracuj("# jen komentář", "").isEmpty());
		Assert.assertTrue(chyby.isEmpty());
	}

	// Registr podkladů

	@Test
	public void uzivatelskeJsouZaVestavenymi() throws Exception {
		final List<EKaType> mapy = zpracuj("a.nazev=A", "a.url=" + URL, "b.nazev=B", "b.url=" + URL);
		EKaType.setUzivatelske(mapy);
		final List<EKaType> vse = Arrays.asList(EKaType.values());
		Assert.assertEquals(EKaType.vestavene(), vse.subList(0, EKaType.vestavene().size()));
		Assert.assertEquals(mapy, vse.subList(EKaType.vestavene().size(), vse.size()));
		Assert.assertSame(mapy.get(1), EKaType.podleJmena("user-b"));
		Assert.assertNull(EKaType.podleJmena("b"));
	}

	@Test
	public void vestaveneMapyNemajiVAdreseKlic() throws Exception {
		for (final EKaType ka : EKaType.vestavene()) {
			if (ka.isOffline()) {
				continue;
			}
			final String url = new Ka(KaLoc.ofJZ(new Mou(0x40000000, 0x20000000), 10), ka).getUrl().toString();
			Assert.assertFalse(url, url.toLowerCase().contains("key"));
		}
	}

	@Test
	public void vestaveneNejsouUzivatelske() {
		for (final EKaType ka : EKaType.vestavene()) {
			Assert.assertFalse(ka.name(), ka.isUzivatelska());
			Assert.assertTrue(ka.getHlavicky().isEmpty());
			Assert.assertSame(ka, EKaType.podleJmena(ka.name()));
		}
		Assert.assertNull(EKaType.podleJmena("neexistuje"));
	}

	// Složka

	@Test
	public void nacteSouborVUtf8() throws Exception {
		final File slozka = slozka("mapa.mapa", "nazev=Žluťoučká mapa\nurl=" + URL + "\natribuce=© Kůň\n");
		Assert.assertEquals(Collections.emptyList(), UzivatelskeMapy.nactiSlozku(slozka));
		final EKaType m = EKaType.podleJmena("user-mapa");
		Assert.assertEquals("Žluťoučká mapa", m.getNazev());
		Assert.assertEquals("© Kůň", m.getAtribuce());
	}

	@Test
	public void nactouSeJenSouborySPriponouMapa() throws Exception {
		final File slozka = slozka("a.mapa", "nazev=A\nurl=" + URL, "B.MAPA", "nazev=B\nurl=" + URL, "c.mapa.priklad", "nazev=C\nurl=" + URL, "d.properties", "nazev=D\nurl=" + URL);
		new File(slozka, "e.mapa").mkdir();
		Assert.assertEquals(Collections.emptyList(), UzivatelskeMapy.nactiSlozku(slozka));
		Assert.assertEquals(Arrays.asList("user-a", "user-b"), Arrays.asList(EKaType.values()).subList(EKaType.vestavene().size(), EKaType.values().length).stream().map(EKaType::name).collect(Collectors.toList()));
	}

	@Test
	public void souborSeZnackouBomSeNacte() throws Exception {
		final File slozka = slozka();
		Files.write(new File(slozka, "bom.mapa").toPath(), ("\uFEFFnazev=Mapa s BOM\nurl=" + URL + "\n").getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals(Collections.emptyList(), UzivatelskeMapy.nactiSlozku(slozka));
		Assert.assertEquals("Mapa s BOM", EKaType.podleJmena("user-bom").getNazev());
	}

	@Test
	public void priponaTxtSeOhlasi() throws Exception {
		final File slozka = slozka("topo.mapa.txt", "nazev=Topo\nurl=" + URL, "Jina.MAPA.TXT", "nazev=J\nurl=" + URL, "poznamky.txt", "x");
		Assert.assertEquals(Arrays.asList("Jina.MAPA.TXT: soubor má příponu .txt, přejmenujte ho na Jina.MAPA", "topo.mapa.txt: soubor má příponu .txt, přejmenujte ho na topo.mapa"),
				UzivatelskeMapy.nactiSlozku(slozka).stream().sorted().collect(Collectors.toList()));
		Assert.assertEquals(EKaType.vestavene().size(), EKaType.values().length);
	}

	@Test
	public void pruvodniSouborMacOsSPriponouTxtSeNehlasi() throws Exception {
		final File slozka = slozka("._a.mapa.txt", "x");
		Assert.assertEquals(Collections.emptyList(), UzivatelskeMapy.nactiSlozku(slozka));
	}

	@Test
	public void chybejiciSlozkaNicNezmeni() throws Exception {
		Assert.assertTrue(UzivatelskeMapy.nactiSlozku(new File(tmp.getRoot(), "neni")).isEmpty());
		Assert.assertEquals(EKaType.vestavene().size(), EKaType.values().length);
	}

	@Test
	public void nactenimSeNahradiPredchoziUzivatelske() throws Exception {
		final File slozka = slozka("a.mapa", "nazev=A\nurl=" + URL);
		UzivatelskeMapy.nactiSlozku(slozka);
		Files.delete(new File(slozka, "a.mapa").toPath());
		slozka("b.mapa", "nazev=B\nurl=" + URL);
		UzivatelskeMapy.nactiSlozku(slozka);
		Assert.assertNull(EKaType.podleJmena("user-a"));
		Assert.assertNotNull(EKaType.podleJmena("user-b"));
	}

	@Test
	public void poskozenySouborNerusiOstatni() throws Exception {
		final File slozka = slozka("a.mapa", "nazev=\\uZZZZ\n", "b.mapa", "nazev=B\nurl=" + URL);
		final List<String> chybySouboru = UzivatelskeMapy.nactiSlozku(slozka);
		Assert.assertEquals(1, chybySouboru.size());
		Assert.assertTrue(chybySouboru.get(0), chybySouboru.get(0).startsWith("a.mapa: soubor nelze přečíst"));
		Assert.assertNotNull(EKaType.podleJmena("user-b"));
	}

	// Zpráva při startu

	@Test
	public void bezChybBezZpravy() throws Exception {
		slozka("a.mapa", "nazev=A\nurl=" + URL);
		Assert.assertNull(UzivatelskeMapy.nacti(tmp.getRoot()));
		Assert.assertNotNull(EKaType.podleJmena("user-a"));
		Assert.assertNull(UzivatelskeMapy.nacti(new File(tmp.getRoot(), "neni")));
	}

	@Test
	public void chybyVeZprave() throws Exception {
		final File slozka = slozka("a.mapa", "nazev=A", "b.mapa", "nazev=B\nurl=" + URL);
		Assert.assertEquals("Chyby v uživatelských mapách ve složce " + slozka + ":\na.mapa: url musí začínat http:// nebo https:// a obsahovat {z}, {x} a {y}\nNezobrazí se: a.mapa",
				UzivatelskeMapy.nacti(tmp.getRoot()));
		Assert.assertNotNull(EKaType.podleJmena("user-b"));
	}

	@Test
	public void mapaSNeznamouVlastnostiSeVeZpraveNeuvadiJakoNezobrazena() throws Exception {
		final File slozka = slozka("a.mapa", "nazev=A\nurl=" + URL + "\nbarva=modrá");
		Assert.assertEquals("Chyby v uživatelských mapách ve složce " + slozka + ":\na.mapa: barva je neznámá vlastnost, povolené jsou nazev, url, popis, min, max, maxauto, klavesa, zkratka, atribuce, hromadne a hlavicka.<jméno hlavičky>",
				UzivatelskeMapy.nacti(tmp.getRoot()));
		Assert.assertNotNull(EKaType.podleJmena("user-a"));
	}

	@Test
	public void souborVJinemKodovaniSeOhlasiSrozumitelne() throws Exception {
		final File slozka = slozka();
		Files.write(new File(slozka, "ansi.mapa").toPath(), ("nazev=Turistická\nurl=" + URL + "\n").getBytes("windows-1250"));
		Assert.assertEquals(Collections.singletonList("ansi.mapa: soubor není v kódování UTF-8, uložte ho znovu s kódováním UTF-8"), UzivatelskeMapy.nactiSlozku(slozka));
	}

	@Test
	public void pruvodniSouboryMacuSeIgnoruji() throws Exception {
		final File slozka = slozka("a.mapa", "nazev=A\nurl=" + URL);
		Files.write(new File(slozka, "._a.mapa").toPath(), new byte[] { 0, 5, 22, 7, (byte) 0xff, (byte) 0xfe });
		Assert.assertEquals(Collections.emptyList(), UzivatelskeMapy.nactiSlozku(slozka));
		Assert.assertNotNull(EKaType.podleJmena("user-a"));
	}

	@Test
	public void staryFormatSeNenacteAleOhlasi() throws Exception {
		final File stary = tmp.newFile(UzivatelskeMapy.STARY_SOUBOR);
		Files.write(stary.toPath(), ("# komentář\nzimni.nazev=Z\nturisticka.url=" + URL + "\nturisticka.nazev=T\n").getBytes(StandardCharsets.UTF_8));
		final File slozka = new File(tmp.getRoot(), UzivatelskeMapy.SLOZKA);
		Assert.assertEquals("Mapy ze souboru " + stary + " přesuňte do složky " + slozka + ". Každou mapu dejte do vlastního souboru pojmenovaného podle dosavadního označení, "
				+ "třeba turisticka.mapa pro řádky turisticka.…, aby zůstaly uložené dlaždice i vybraná mapa. Vlastnosti v něm pište bez označení: url=… místo turisticka.url=…. "
				+ "Potom soubor " + stary + " smažte.", UzivatelskeMapy.nacti(tmp.getRoot()));
		Assert.assertEquals(EKaType.vestavene().size(), EKaType.values().length);
		Assert.assertFalse("starý soubor se nepřevádí", slozka.exists());
	}

	@Test
	public void staryFormatIChybyVJedneZprave() throws Exception {
		Files.write(tmp.newFile(UzivatelskeMapy.STARY_SOUBOR).toPath(), "b.nazev=B\n".getBytes(StandardCharsets.UTF_8));
		slozka("a.mapa", "nazev=A");
		final String zprava = UzivatelskeMapy.nacti(tmp.getRoot());
		Assert.assertTrue(zprava, zprava.startsWith("Chyby v uživatelských mapách"));
		Assert.assertTrue(zprava, zprava.contains("\n\nMapy ze souboru "));
	}

	@Test
	public void staryFormatJenSKomentariSeNehlasi() throws Exception {
		Files.write(tmp.newFile(UzivatelskeMapy.STARY_SOUBOR).toPath(), "# Uživatelské mapy\n#osm.nazev=OSM\n\n".getBytes(StandardCharsets.UTF_8));
		Assert.assertNull(UzivatelskeMapy.nacti(tmp.getRoot()));
	}

	@Test
	public void prazdnyStaryFormatSeNehlasi() throws Exception {
		tmp.newFile(UzivatelskeMapy.STARY_SOUBOR);
		Assert.assertNull(UzivatelskeMapy.nacti(tmp.getRoot()));
	}

	@Test
	public void necitelnyStaryFormatSeOhlasi() throws Exception {
		Files.write(tmp.newFile(UzivatelskeMapy.STARY_SOUBOR).toPath(), "a.nazev=\\uZZZZ\n".getBytes(StandardCharsets.UTF_8));
		Assert.assertTrue(UzivatelskeMapy.nacti(tmp.getRoot()).startsWith("Mapy ze souboru "));
	}

	// Zalamování hlášky

	@Test
	public void kratkyTextSeNezmeni() {
		Assert.assertEquals("a b\n\nc", UzivatelskeMapy.zalom("a b\n\nc", 10));
	}

	@Test
	public void radekPresnePoSirkuSeNezalomiOJednaDelsiAno() {
		final String presne = "aaaa bbbb";
		Assert.assertEquals(presne, UzivatelskeMapy.zalom(presne, 9));
		Assert.assertEquals("aaaa\nbbbb", UzivatelskeMapy.zalom(presne, 8));
	}

	@Test
	public void dialogZalamujeDoDefinovaneSirky() {
		final StringBuilder dlouhy = new StringBuilder("Chyba");
		for (int i = 0; i < 40; i++) {
			dlouhy.append(" slovo").append(i);
		}
		for (final String radek : UzivatelskeMapy.textDialogu(dlouhy.toString()).split("\n")) {
			Assert.assertTrue(radek, radek.length() <= UzivatelskeMapy.SIRKA_DIALOGU);
		}
	}

	@Test
	public void dlouhyRadekSeZalomiNaMezerach() {
		Assert.assertEquals("aaa bbb\nccc ddd\ne", UzivatelskeMapy.zalom("aaa bbb ccc ddd e", 7));
		Assert.assertEquals("aaa\nbbbb", UzivatelskeMapy.zalom("aaa bbbb", 7));
	}

	@Test
	public void dlouhaCestaSeZalomiZaLomitkem() {
		Assert.assertEquals("Soubor\n/home/kacer/\nGeoKuk/data/\nmapy smažte.", UzivatelskeMapy.zalom("Soubor /home/kacer/GeoKuk/data/mapy smažte.", 12));
		Assert.assertEquals("C:\\Program\\\nGeoKuk\\data", UzivatelskeMapy.zalom("C:\\Program\\GeoKuk\\data", 12));
	}

	@Test
	public void slovoBezLomitkaSeRozdeliNatvrdo() {
		Assert.assertEquals("abcd\nefgh\nij", UzivatelskeMapy.zalom("abcdefghij", 4));
	}

	@Test
	public void zadnyRadekHlaskyNepresahneSirku() throws Exception {
		final File stary = tmp.newFile(UzivatelskeMapy.STARY_SOUBOR);
		Files.write(stary.toPath(), "turisticka.url=x\n".getBytes(StandardCharsets.UTF_8));
		slozka("a.mapa", "nazev=A\nbarva=modrá");
		final String zprava = UzivatelskeMapy.nacti(tmp.getRoot());
		final String zalomena = UzivatelskeMapy.zalom(zprava, 100);
		for (final String radek : zalomena.split("\n")) {
			Assert.assertTrue(radek, radek.length() <= 100);
		}
		Assert.assertEquals("zalomení jen nahrazuje mezery koncem řádku", zprava.replaceAll("\\s+", ""), zalomena.replaceAll("\\s+", ""));
	}

	// Příklady

	@Test
	public void prikladyJsouPlatne() throws Exception {
		final SortedMap<String, Properties> soubory = new TreeMap<>();
		for (final Path priklad : priklady()) {
			Assert.assertTrue(priklad.toString(), priklad.getFileName().toString().endsWith(UzivatelskeMapy.PRIPONA));
			final Properties p = new Properties();
			try (Reader r = Files.newBufferedReader(priklad, StandardCharsets.UTF_8)) {
				p.load(r);
			}
			soubory.put(priklad.getFileName().toString(), p);
		}
		final List<EKaType> mapy = UzivatelskeMapy.zpracuj(soubory, chyby);
		Assert.assertEquals(Collections.emptyList(), chyby);
		Assert.assertEquals(7, mapy.size());
		for (final EKaType m : mapy) {
			Assert.assertFalse(m.name(), m.getAtribuce().isEmpty());
		}
	}

	@Test
	public void prikladyPopisujiVsechnyVlastnosti() throws Exception {
		for (final Path priklad : priklady()) {
			final String text = new String(Files.readAllBytes(priklad), StandardCharsets.UTF_8);
			for (final String vlastnost : new String[] { "nazev", "url", "popis", "min", "max", "maxauto", "klavesa", "zkratka", "atribuce", "hromadne", "hlavicka" }) {
				Assert.assertTrue(priklad + " " + vlastnost, text.contains("\n#   " + vlastnost));
			}
		}
	}

	/** Odkaz na cizí soubor (třeba s klíči) se nečte, jeho řádky by skončily v hlášce a v hlášení chyby. */
	@Test
	public void odkazNaCiziSouborSeNecte() throws Exception {
		final File cizi = tmp.newFile("id_rsa");
		Files.write(cizi.toPath(), "ghp_TAJNYTOKEN0123456789abcdef\n".getBytes(StandardCharsets.UTF_8));
		final File slozka = slozka();
		try {
			Files.createSymbolicLink(new File(slozka, "hezka.mapa").toPath(), cizi.toPath());
		} catch (final UnsupportedOperationException | IOException e) {
			Assume.assumeNoException("symbolický odkaz nejde vytvořit", e);
		}
		final List<String> chyby = UzivatelskeMapy.nactiSlozku(slozka);
		Assert.assertFalse(chyby.toString(), chyby.toString().contains("TAJNY"));
		Assert.assertTrue(chyby.toString(), chyby.get(0).startsWith("hezka.mapa: soubor je odkaz"));
	}

	@Test
	public void velkySouborSeNecte() throws Exception {
		final StringBuilder sb = new StringBuilder("nazev=Velká\nurl=https://a/{z}/{x}/{y}.png\n#");
		while (sb.length() <= UzivatelskeMapy.MAX_VELIKOST) {
			sb.append("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		}
		final List<String> chyby = UzivatelskeMapy.nactiSlozku(slozka("velka.mapa", sb.toString()));
		Assert.assertTrue(chyby.toString(), chyby.get(0).startsWith("velka.mapa: soubor je větší než 64 kB"));
	}

	@Test
	public void neznamyKlicZkracenyBezRidicichZnaku() throws Exception {
		zpracuj("m.nazev=M", "m.url=https://a/{z}/{x}/{y}.png", "m.b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAABlwAAAAdzc2gtcn");
		Assert.assertTrue(chyby.toString(), chyby.contains("m.mapa: b3BlbnNzaC1rZXktdjEAAAAABG5vbm… je neznámá vlastnost, povolené jsou nazev, url, popis, min, max, maxauto, klavesa, zkratka, atribuce, hromadne a hlavicka.<jméno hlavičky>"));
		Assert.assertEquals("a?b", UzivatelskeMapy.zkrat("a\u0007b"));
	}
}
