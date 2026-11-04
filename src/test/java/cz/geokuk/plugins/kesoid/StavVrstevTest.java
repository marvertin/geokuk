package cz.geokuk.plugins.kesoid;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
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
		Assert.assertFalse(t, t.contains("Jednotkové"));
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
		Assert.assertEquals("Zapnuté zvýrazňovací i jednotkové kruhy", ZpravyZkratek.jednotkoveKruhy(true, false, null));
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
		Assert.assertEquals(238, JToast.stredY(300));
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
		Assert.assertEquals("Kruhy skryté", oboji.text());
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
}
