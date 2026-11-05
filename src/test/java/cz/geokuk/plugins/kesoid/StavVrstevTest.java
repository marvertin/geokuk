package cz.geokuk.plugins.kesoid;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import cz.geokuk.util.gui.BublinkaHodnoty;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;

/** Stav skrytých vrstev kruhů: texty důvodů, štítek, hlášení změn a texty zpráv po zkratkách. */
public class StavVrstevTest {

	private static final VrstvaNadMapou F8 = VrstvaNadMapou.ZVYRAZNOVACI_KRUHY;
	private static final VrstvaNadMapou ALT_F8 = VrstvaNadMapou.JEDNOTKOVE_KRUHY;
	private static final VrstvaNadMapou F9 = VrstvaNadMapou.OBSAZENOST;

	@Test
	public void prazdnyStavNemaSkryte() {
		Assert.assertFalse(StavVrstev.prazdny().maSkryte());
		Assert.assertNull(StavVrstev.prazdny().get(F8));
		Assert.assertNull(StavVrstev.prazdny().getSkrytyKruh());
	}

	@Test
	public void kresliNebNullVrstvuZeSkrytychVyradi() {
		final StavVrstev s = StavVrstev.prazdny().s(F8, DuvodNekresleni.PRILIS_HUSTE, 0);
		Assert.assertTrue(s.maSkryte());
		Assert.assertEquals(StavVrstev.prazdny(), s.s(F8, DuvodNekresleni.KRESLI, 0));
		Assert.assertEquals(StavVrstev.prazdny(), s.s(F8, null, 0));
		Assert.assertSame(StavVrstev.prazdny(), s.s(F8, null, 0));
	}

	@Test
	public void limitSeUkladaJenUDuvoduLimitPoctu() {
		Assert.assertEquals(90_000, StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 90_000).get(F8).getLimit());
		Assert.assertEquals(0, StavVrstev.prazdny().s(F8, DuvodNekresleni.PRILIS_HUSTE, 90_000).get(F8).getLimit());
		Assert.assertEquals("změna limitu je změna stavu", StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 1000),
				StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 1000));
		Assert.assertNotEquals(StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 1000), StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 2000));
	}

	@Test
	public void textyDuvoduPoVrstvach() {
		Assert.assertEquals(String.format(new java.util.Locale("cs"), "příliš mnoho bodů ve výřezu (limit %,d), přibližte mapu", 90_000), StavVrstev.textDuvodu(F8, DuvodNekresleni.LIMIT_POCTU, 90_000));
		Assert.assertTrue(StavVrstev.textDuvodu(F8, DuvodNekresleni.LIMIT_POCTU, 90_000).contains("(limit 90"));
		Assert.assertEquals("příliš husté, přibližte mapu", StavVrstev.textDuvodu(F8, DuvodNekresleni.PRILIS_HUSTE, 0));
		Assert.assertEquals("poloměr pod 3 px, přibližte mapu", StavVrstev.textDuvodu(ALT_F8, DuvodNekresleni.POLOMER_PRILIS_MALY, 0));
		Assert.assertEquals("nezobrazuje se v tak malém měřítku, přibližte mapu", StavVrstev.textDuvodu(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0));
		Assert.assertTrue(StavVrstev.textDuvodu(F9, DuvodNekresleni.LIMIT_POCTU, 20_000).contains("limit"));
		Assert.assertEquals("", StavVrstev.textDuvodu(F8, DuvodNekresleni.KRESLI, 0));
	}

	@Test
	public void stitekMaTextATooltipsRadkemProKazdouSkrytouVrstvu() {
		final StavVrstev s = StavVrstev.prazdny().s(F8, DuvodNekresleni.PRILIS_HUSTE, 0).s(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		Assert.assertEquals("Kruhy skryté", s.text());
		final String t = s.tooltip();
		Assert.assertTrue(t, t.startsWith("<html>") && t.endsWith("</html>"));
		Assert.assertEquals(2, t.split("<br/>").length);
		Assert.assertTrue(t, t.contains("Zvýrazňovací kruhy (F8): příliš husté, přibližte mapu"));
		Assert.assertTrue(t, t.contains("Obsazenost (F9): nezobrazuje se v tak malém měřítku, přibližte mapu"));
		Assert.assertFalse(t, t.contains("Kruhy v metrech"));
	}

	@Test
	public void skrytyKruhJeZvyraznovaciNeboJednotkovy() {
		Assert.assertEquals(DuvodNekresleni.PRILIS_HUSTE, StavVrstev.prazdny().s(F8, DuvodNekresleni.PRILIS_HUSTE, 0).getSkrytyKruh().getDuvod());
		Assert.assertEquals(DuvodNekresleni.POLOMER_PRILIS_MALY, StavVrstev.prazdny().s(ALT_F8, DuvodNekresleni.POLOMER_PRILIS_MALY, 0).getSkrytyKruh().getDuvod());
		Assert.assertNull("Obsazenost nejsou kruhy zvýraznění", StavVrstev.prazdny().s(F9, DuvodNekresleni.PRILIS_HUSTE, 0).getSkrytyKruh());
	}

	// --- model ---

	private static StavVrstevModel model(final List<Event0<?>> udalosti) {
		final StavVrstevModel m = new StavVrstevModel();
		final EventFirer firer = udalosti::add;
		m.inject(firer);
		return m;
	}

	@Test
	public void modelRozesilaJenZmenu() {
		final List<Event0<?>> udalosti = new ArrayList<>();
		final StavVrstevModel m = model(udalosti);
		m.nahlas(F8, DuvodNekresleni.KRESLI, 0);
		Assert.assertTrue("vrstva se kreslila a pořád kreslí", udalosti.isEmpty());
		m.nahlas(F8, DuvodNekresleni.PRILIS_HUSTE, 0);
		m.nahlas(F8, DuvodNekresleni.PRILIS_HUSTE, 0);
		Assert.assertEquals(1, udalosti.size());
		m.nahlas(F8, DuvodNekresleni.LIMIT_POCTU, 5);
		m.nahlas(F8, DuvodNekresleni.LIMIT_POCTU, 5);
		Assert.assertEquals(2, udalosti.size());
		m.nahlas(F8, DuvodNekresleni.KRESLI, 0);
		Assert.assertEquals(3, udalosti.size());
		Assert.assertFalse(((StavVrstevEvent) udalosti.get(2)).getStav().maSkryte());
		Assert.assertFalse(m.getStav().maSkryte());
	}

	@Test
	public void modelNeznaVrstvuPoVypnuti() {
		final List<Event0<?>> udalosti = new ArrayList<>();
		final StavVrstevModel m = model(udalosti);
		m.nahlas(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		m.nahlas(F9, null, 0);
		Assert.assertEquals(2, udalosti.size());
	}

	// --- zprávy po zkratkách ---

	private static StavVrstev.Skryta skryta(final VrstvaNadMapou v, final DuvodNekresleni d, final int limit) {
		return StavVrstev.prazdny().s(v, d, limit).get(v);
	}

	@Test
	public void f8JenKdyzZapnuteKruhyNejsouVidet() {
		Assert.assertNull("F8 viditelně přepíná kruhy", ZpravyZkratek.zvyraznovaciKruhy(true, null));
		Assert.assertNull("vypnutí nemá co vysvětlovat", ZpravyZkratek.zvyraznovaciKruhy(false, skryta(F8, DuvodNekresleni.PRILIS_HUSTE, 0)));
		Assert.assertEquals("Kruhy zapnuté, ale skryté: příliš husté, přibližte mapu", ZpravyZkratek.zvyraznovaciKruhy(true, skryta(F8, DuvodNekresleni.PRILIS_HUSTE, 0)));
		Assert.assertEquals("Kruhy zapnuté, ale skryté: poloměr pod 3 px, přibližte mapu", ZpravyZkratek.zvyraznovaciKruhy(true, skryta(F8, DuvodNekresleni.POLOMER_PRILIS_MALY, 0)));
	}

	@Test
	public void altF8ZapneObojeKdyzF8Nebylo() {
		Assert.assertEquals("Zapnuté zvýrazňovací kruhy s poloměrem v metrech", ZpravyZkratek.jednotkoveKruhy(true, false, null));
		Assert.assertNull("F8 už svítilo, zapnutí je vidět", ZpravyZkratek.jednotkoveKruhy(true, true, null));
		Assert.assertNull("vypnutí jednotkových", ZpravyZkratek.jednotkoveKruhy(false, false, null));
		Assert.assertNull(ZpravyZkratek.jednotkoveKruhy(false, true, skryta(ALT_F8, DuvodNekresleni.PRILIS_HUSTE, 0)));
	}

	@Test
	public void skrytiMaPrednostPredZpravouOZapnutiObou() {
		Assert.assertEquals("Kruhy zapnuté, ale skryté: poloměr pod 3 px, přibližte mapu",
				ZpravyZkratek.jednotkoveKruhy(true, false, skryta(ALT_F8, DuvodNekresleni.POLOMER_PRILIS_MALY, 0)));
	}

	@Test
	public void f5ZpravaPodleStavu() {
		Assert.assertEquals("Kešoidy skryté včetně kruhů, obsazenosti a popisků", ZpravyZkratek.kesoidy(false));
		Assert.assertEquals("Kešoidy zobrazené", ZpravyZkratek.kesoidy(true));
	}

	@Test
	public void f9ProZoomPodJedenactCiLimit() {
		Assert.assertNull(ZpravyZkratek.obsazenost(true, null));
		Assert.assertNull(ZpravyZkratek.obsazenost(false, skryta(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0)));
		Assert.assertEquals("Obsazenost se kreslí až po přiblížení mapy", ZpravyZkratek.obsazenost(true, skryta(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0)));
		final String limit = ZpravyZkratek.obsazenost(true, skryta(F9, DuvodNekresleni.LIMIT_POCTU, 20_000));
		Assert.assertTrue(limit, limit.startsWith("Obsazenost zapnutá, ale skrytá: příliš mnoho bodů"));
	}

	@Test
	public void zdrojeKesiPodleSkutecnehoStavu() {
		Assert.assertEquals("Zdroj keší: GSAK", ZpravyZkratek.zdroje(java.util.Arrays.asList("GSAK")));
		Assert.assertEquals("Zdroje keší: GeoGet, GSAK", ZpravyZkratek.zdroje(java.util.Arrays.asList("GeoGet", "GSAK")));
		Assert.assertEquals("Zdroje keší: žádný", ZpravyZkratek.zdroje(new ArrayList<>()));
	}

	@Test
	public void toastModelNeposilaPrazdnouZpravu() {
		final List<Event0<?>> udalosti = new ArrayList<>();
		final ToastModel t = new ToastModel();
		t.inject((EventFirer) udalosti::add);
		t.ukaz(null);
		t.ukaz("");
		Assert.assertTrue(udalosti.isEmpty());
		t.ukaz("a");
		t.ukaz("b");
		Assert.assertEquals(2, udalosti.size());
		Assert.assertEquals("b", ((ToastEvent) udalosti.get(1)).getText());
	}

	@Test
	public void toastNovaZpravaNahradiStarouAZrusiSeSNeviditelnosti() {
		final JToast toast = new JToast();
		toast.onEvent(new ToastEvent("první"));
		Assert.assertEquals("první", toast.text());
		toast.onEvent(new ToastEvent("druhá"));
		Assert.assertEquals("druhá", toast.text());
		Assert.assertTrue(toast.bezi());
		toast.removeNotify();
		Assert.assertFalse("po odebrání se časovač zastaví", toast.bezi());
		toast.onEvent(new ToastEvent("třetí"));
		toast.setVisible(false);
		Assert.assertFalse("po skrytí se časovač zastaví", toast.bezi());
		Assert.assertFalse("zpráva nechytá myš", toast.contains(1, 1));
		Assert.assertEquals(2500, JToast.DRZENI_MS);
	}

	@Test
	public void vynechaneKruhyDajiStitekOmezeneATooltip() {
		final StavVrstev s = StavVrstev.prazdny().sVynechanymi(5, 12);
		Assert.assertFalse(s.maSkryte());
		Assert.assertTrue(s.jeKVideni());
		Assert.assertEquals("Kruhy omezené", s.text());
		Assert.assertEquals(5, s.getVynechanychKruhu());
		Assert.assertTrue(s.tooltip(), s.tooltip().contains("kresleno jen 12 nejbližších"));
		Assert.assertSame("bez vynechaných je stav zase prázdný", StavVrstev.prazdny(), s.sVynechanymi(0, 12));
		final StavVrstev oboji = s.s(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		Assert.assertEquals("smíšený stav: něco skryté, něco omezené", "Kruhy skryté a omezené", oboji.text());
		Assert.assertEquals(2, oboji.tooltip().split("<br/>").length);
		Assert.assertNotEquals(s, StavVrstev.prazdny().sVynechanymi(6, 12));
	}

	@Test
	public void modelHlasiVynechaneJenPriZmene() {
		final List<Event0<?>> udalosti = new ArrayList<>();
		final StavVrstevModel m = model(udalosti);
		m.nahlasVynechane(0, 0);
		Assert.assertTrue(udalosti.isEmpty());
		m.nahlasVynechane(3, 12);
		m.nahlasVynechane(3, 12);
		Assert.assertEquals(1, udalosti.size());
		m.nahlasVynechane(0, 0);
		Assert.assertEquals(2, udalosti.size());
		Assert.assertFalse(m.getStav().jeKVideni());
	}

	@Test
	public void bezVynechanychSeKreslenyPocetNepamatuje() {
		final StavVrstev skryto = StavVrstev.prazdny().s(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		Assert.assertEquals(skryto, skryto.sVynechanymi(5, 12).sVynechanymi(0, 12));
	}

	@Test
	public void nazvyVrstevVTooltipuStitku() {
		Assert.assertEquals("Zvýrazňovací kruhy (F8)", F8.getNazev());
		Assert.assertEquals("Kruhy v metrech (Alt+F8)", ALT_F8.getNazev());
		Assert.assertEquals("Obsazenost (F9)", F9.getNazev());
		final String t = StavVrstev.prazdny().s(ALT_F8, DuvodNekresleni.POLOMER_PRILIS_MALY, 0).tooltip();
		Assert.assertTrue(t, t.contains("Kruhy v metrech (Alt+F8): poloměr pod 3 px, přibližte mapu"));
		Assert.assertFalse(t, t.contains("Jednotkové"));
		Assert.assertTrue(StavVrstev.prazdny().sVynechanymi(2, 7).tooltip().contains("Kruhy v metrech: kresleno jen 7 nejbližších"));
	}

	@Test
	public void textStitkuPodlePritomnostiSkrytychAOmezenych() {
		final StavVrstev skryto = StavVrstev.prazdny().s(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		Assert.assertEquals("jen skryté", "Kruhy skryté", skryto.text());
		Assert.assertEquals("jen omezené", "Kruhy omezené", StavVrstev.prazdny().sVynechanymi(3, 40).text());
		Assert.assertEquals("obojí v jednom pořadí", "Kruhy skryté a omezené", skryto.sVynechanymi(3, 40).text());
		Assert.assertEquals("obojí v druhém pořadí", "Kruhy skryté a omezené", StavVrstev.prazdny().sVynechanymi(3, 40).s(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0).text());
		Assert.assertEquals("po skončení omezení zůstane jen skryté", "Kruhy skryté", skryto.sVynechanymi(3, 40).sVynechanymi(0, 0).text());
		Assert.assertEquals("po zobrazení skryté vrstvy zůstane jen omezené", "Kruhy omezené", skryto.sVynechanymi(3, 40).s(F9, DuvodNekresleni.KRESLI, 0).text());
		final String tooltip = skryto.sVynechanymi(3, 2000).tooltip();
		Assert.assertTrue(tooltip, tooltip.contains("Obsazenost (F9): nezobrazuje se v tak malém měřítku, přibližte mapu"));
		Assert.assertTrue(tooltip, tooltip.contains("Kruhy v metrech: kresleno jen 2000 nejbližších ke středu"));
	}

	@Test
	public void zpravaJeUprostredMapyNadMeritkem() {
		final java.awt.Rectangle r = JToast.umisteni(1000, 700, 300, 24);
		Assert.assertEquals("vodorovně na středu", 500, r.x + r.width / 2);
		Assert.assertEquals(350, r.x);
		Assert.assertEquals("zpráva stojí 12 px nad oblastí měřítka", 700 - cz.geokuk.framework.RozmisteniPrekryvu.VYSKA_MERITKA - 12, r.y + r.height);
		Assert.assertEquals(300, r.width);
		Assert.assertEquals(24, r.height);
		final java.awt.Rectangle sudy = JToast.umisteni(1001, 700, 301, 24);
		Assert.assertEquals("střed i při lichých rozměrech v rámci pixelu", 500, sudy.x + sudy.width / 2, 1);
	}

	@Test
	public void zpravaSeVUzkemOknePosuneKLevemuOkrajiAZkratiSe() {
		final java.awt.Rectangle uzke = JToast.umisteni(300, 700, 340, 24);
		Assert.assertEquals("delší než mapa: začíná na okraji, nezkracuje se zleva", cz.geokuk.framework.RozmisteniPrekryvu.OKRAJ_ZPRAVY, uzke.x);
		Assert.assertEquals("a zkrátí se, aby nezasáhla do pásu legendy", cz.geokuk.framework.RozmisteniPrekryvu.nejvetsiSirkaZpravy(300), uzke.width);
		final java.awt.Rectangle volne = JToast.umisteni(500, 700, 340, 24);
		Assert.assertEquals("na pravé straně zůstane pás pro legendu", 500 - 90 - 12 - 340, volne.x);
		final java.awt.Rectangle stred = JToast.umisteni(800, 700, 340, 24);
		Assert.assertEquals("kde je místo, je vodorovně uprostřed", 230, stred.x);
	}

	@Test
	public void dlouhyTextSeZkratiTremiTeckami() {
		final java.awt.image.BufferedImage obraz = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g = obraz.createGraphics();
		final java.awt.FontMetrics fm = g.getFontMetrics(new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, 12));
		g.dispose();
		final String text = "Zapnuté zvýrazňovací kruhy s poloměrem v metrech";
		Assert.assertEquals("vejde se", text, JToast.zkrat(text, fm, fm.stringWidth(text)));
		final String kratky = JToast.zkrat(text, fm, fm.stringWidth(text) - 1);
		Assert.assertTrue(kratky, kratky.endsWith("…") && kratky.length() < text.length());
		Assert.assertTrue(fm.stringWidth(kratky) <= fm.stringWidth(text) - 1);
		Assert.assertEquals("…", JToast.zkrat(text, fm, 3).substring(JToast.zkrat(text, fm, 3).length() - 1));
		Assert.assertEquals("aspoň jeden znak zůstane i v nejužším místě", "Z…", JToast.zkrat(text, fm, 0));
		Assert.assertEquals("vejde se přesně", "Zap…", JToast.zkrat(text, fm, fm.stringWidth("Zap…")));
	}

	@Test
	public void zaokrouhleniPriLicemRozdiluSirky() {
		final java.awt.Rectangle r = JToast.umisteni(1000, 700, 301, 24);
		Assert.assertEquals(349, r.x);
		Assert.assertEquals(301, r.width);
		Assert.assertEquals("pravý okraj vychází na 650, střed o půl bodu vlevo od středu mapy", 650, r.x + r.width);
		Assert.assertEquals(350, JToast.umisteni(1000, 700, 300, 24).x);
	}

	@Test
	public void presnaHraniceOkrajeDvanactBodu() {
		// pás vpravo pro legendu: pravý okraj zprávy nejvýš 90 + 12 bodů od pravého okraje mapy
		Assert.assertEquals("právě na hranici", 12, JToast.umisteni(414, 700, 300, 24).x);
		Assert.assertEquals("o bod širší mapa: o bod dál od okraje", 13, JToast.umisteni(415, 700, 300, 24).x);
		Assert.assertEquals("o bod užší mapa: zpráva se zkrátí o bod a stojí na okraji", 12, JToast.umisteni(413, 700, 300, 24).x);
		Assert.assertEquals(299, JToast.umisteni(413, 700, 300, 24).width);
		Assert.assertEquals("delší než mapa", 12, JToast.umisteni(200, 700, 500, 24).x);
	}

	@Test
	public void zpravaSeOpravduNakresliDoSvehoObdelniku() throws Exception {
		final JToast toast = new JToast();
		toast.setSize(1000, 700);
		toast.onEvent(new ToastEvent("Kešoidy skryté včetně kruhů, obsazenosti a popisků"));
		Thread.sleep(BublinkaHodnoty.ZACATEK_MS + 100);
		final java.awt.image.BufferedImage obraz = new java.awt.image.BufferedImage(1000, 700, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g = obraz.createGraphics();
		toast.paintComponent(g);
		g.dispose();
		int x1 = Integer.MAX_VALUE;
		int y1 = Integer.MAX_VALUE;
		int x2 = -1;
		int y2 = -1;
		for (int y = 0; y < 700; y++) {
			for (int x = 0; x < 1000; x++) {
				if ((obraz.getRGB(x, y) >>> 24) != 0) {
					x1 = Math.min(x1, x);
					y1 = Math.min(y1, y);
					x2 = Math.max(x2, x);
					y2 = Math.max(y2, y);
				}
			}
		}
		final java.awt.FontMetrics fm = obraz.createGraphics().getFontMetrics(toast.font());
		final java.awt.Rectangle velikost = cz.geokuk.util.gui.PopisekNaMape.hranice(fm, toast.text(), 0, 0);
		final java.awt.Rectangle r = JToast.umisteni(1000, 700, velikost.width, velikost.height);
		Assert.assertTrue("něco se nakreslilo", x2 >= 0);
		Assert.assertEquals("levý okraj kresby je levý okraj obdélníku", r.x, x1, 1);
		Assert.assertEquals("horní okraj kresby je horní okraj obdélníku", r.y, y1, 1);
		Assert.assertEquals("pravý okraj", r.x + r.width, x2, 2);
		Assert.assertEquals("dolní okraj", r.y + r.height, y2, 2);
		toast.removeNotify();
	}

	@Test
	public void animacePrekresluJenOblastZpravyAZanikleZpravy() throws Exception {
		final java.util.List<java.awt.Rectangle> oblasti = new java.util.ArrayList<>();
		final JToast toast = new JToast() {
			private static final long serialVersionUID = 1L;

			@Override
			public void repaint(final int x, final int y, final int sirka, final int vyska) {
				oblasti.add(new java.awt.Rectangle(x, y, sirka, vyska));
			}

			@Override
			public void repaint() {
				oblasti.add(new java.awt.Rectangle(0, 0, getWidth(), getHeight()));
			}
		};
		toast.setSize(1000, 700);
		final java.awt.image.BufferedImage obraz = new java.awt.image.BufferedImage(1000, 700, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		toast.onEvent(new ToastEvent("Dlouhá první zpráva po zkratce, která je široká"));
		final java.awt.FontMetrics fm = obraz.createGraphics().getFontMetrics(toast.font());
		final java.awt.Rectangle v1 = cz.geokuk.util.gui.PopisekNaMape.hranice(fm, "Dlouhá první zpráva po zkratce, která je široká", 0, 0);
		final java.awt.Rectangle r1 = JToast.umisteni(1000, 700, v1.width, v1.height);
		Assert.assertFalse(oblasti.isEmpty());
		Assert.assertTrue("překreslí se oblast zprávy, ne celá vrstva: " + oblasti.get(oblasti.size() - 1), oblasti.get(oblasti.size() - 1).contains(r1));
		Assert.assertTrue(oblasti.get(oblasti.size() - 1).width < 1000);
		Thread.sleep(BublinkaHodnoty.ZACATEK_MS + 100);
		final java.awt.Graphics2D g = obraz.createGraphics();
		toast.paintComponent(g);
		g.dispose();
		oblasti.clear();
		toast.onEvent(new ToastEvent("Krátká"));
		final java.awt.Rectangle posledni = oblasti.get(oblasti.size() - 1);
		Assert.assertTrue("nahrazená zpráva se z mapy smaže: překreslí se i oblast staré", posledni.contains(r1));
		toast.removeNotify();
	}

	@Test
	public void rovnostAHashSeridiPodleSkrytychVrstevAPoctuKruhu() {
		final StavVrstev a = StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 5);
		final StavVrstev b = StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 5);
		Assert.assertEquals(a, b);
		Assert.assertEquals(a.hashCode(), b.hashCode());
		Assert.assertNotEquals(a, StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 6));
		Assert.assertNotEquals(a, StavVrstev.prazdny().s(F8, DuvodNekresleni.PRILIS_HUSTE, 5));
		Assert.assertNotEquals(a, StavVrstev.prazdny());
		Assert.assertNotEquals(a, "StavVrstev");
		Assert.assertNotEquals(StavVrstev.prazdny().hashCode(), a.hashCode());
		Assert.assertEquals(a.get(F8), b.get(F8));
		Assert.assertEquals(a.get(F8).hashCode(), b.get(F8).hashCode());
		Assert.assertNotEquals(a.get(F8), a);
		Assert.assertNotEquals(a.get(F8).hashCode(), StavVrstev.prazdny().s(F8, DuvodNekresleni.LIMIT_POCTU, 6).get(F8).hashCode());
	}

	@Test
	public void skryteVrstvyAPopisStavu() {
		final StavVrstev s = StavVrstev.prazdny().s(F8, DuvodNekresleni.PRILIS_HUSTE, 0).s(F9, DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		Assert.assertEquals(2, s.getSkryte().size());
		Assert.assertTrue(s.getSkryte().containsKey(F8));
		Assert.assertTrue(s.getSkryte().containsKey(F9));
		Assert.assertTrue(s.toString(), s.toString().startsWith("StavVrstev"));
		Assert.assertTrue(s.toString(), s.toString().contains(F8.toString()));
		Assert.assertTrue(StavVrstev.prazdny().getSkryte().isEmpty());
	}
}
