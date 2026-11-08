package cz.geokuk.plugins.challenge;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;
import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.kes.EKesDiffTerRating;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Klik na keš: klín sektoru a štítky keší sektoru nebo políčka D/T na mapě; potřebuje displej. */
public class JPlanovacVybranaKesTest {

	private static final int W = 1280;
	private static final int H = 720;
	private static final double STRED_LAT = 50.0;
	private static final double STRED_LON = 14.0;

	private ChallengeModel model;
	private JPlanovacPodKeskami pod;
	private JPlanovacNadKeskami nad;

	@Before
	public void setUp() {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		TvarCache.vymaz();
		model = new ChallengeModel();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
		pod = new JPlanovacPodKeskami();
		pod.inject(model);
		nad = new JPlanovacNadKeskami();
		nad.inject(model);
	}

	private static Kes kes(final String id, final double lat, final double lon, final int d, final int t) {
		final Kes k = new Kes();
		k.setIdentifier(id);
		k.setVztahx(EKesVztah.NORMAL);
		k.setDifficulty(EKesDiffTerRating.values()[d]);
		k.setTerrain(EKesDiffTerRating.values()[t]);
		final Wpt w = new Wpt();
		w.setWgs(new Wgs(lat, lon));
		w.setSym("Geocache");
		k.addWpt(w);
		return k;
	}

	/** Keš ve vzdálenosti metrů od středu v daném azimutu. */
	private static Kes kesVAzimutu(final String id, final double azimut, final double metru, final int d, final int t) {
		final double a = Math.toRadians(azimut);
		return kes(id, STRED_LAT + metru * Math.cos(a) / 111_000.0, STRED_LON + metru * Math.sin(a) / (111_000.0 * Math.cos(Math.toRadians(STRED_LAT))), d, t);
	}

	private static Indexator<Wpt> index(final Kes... kese) {
		final List<Wpt> wpts = new ArrayList<>();
		for (final Kes k : kese) {
			wpts.add(k.getMainWpt());
		}
		return Indexator.postav(BoundingRect.ALL, wpts, w -> w.getMou().xx, w -> w.getMou().yy);
	}

	private static VybranaKes vybrana(final Kes k) {
		return SledovaniVybraneKese.zWpt(k.getMainWpt());
	}

	private static BitSet obsazene() {
		final BitSet b = new BitSet(360);
		b.set(0, 360);
		return b;
	}

	/** Výsledek 360°: všechny sektory splněné kromě zadaných chybějících, každý s jedním kandidátem. */
	private Challenge zobraz360(final int... chybejici) throws Exception {
		final BitSet ob = obsazene();
		final List<VysledekSektoru.KandidatKes> kandidati = new ArrayList<>();
		final VysledekSektoru.Kandidati[] poSektorech = new VysledekSektoru.Kandidati[360];
		for (final int s : chybejici) {
			ob.clear(s);
			poSektorech[s] = new VysledekSektoru.Kandidati(s);
			poSektorech[s].pridej("GC" + s, 3000);
			final double a = Math.toRadians(s + 0.5);
			kandidati.add(new VysledekSektoru.KandidatKes("GC" + s, STRED_LAT + 3000 * Math.cos(a) / 111_000.0, STRED_LON + 3000 * Math.sin(a) / 71_300.0, s, 3000, false));
		}
		final Challenge c = model.ulozVlastni(new Challenge("A", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(c);
		model.setZapnuto(true);
		model.nastavVysledek(c, new VysledekSektoru(ob, new BitSet(360), poSektorech, 360 - chybejici.length, kandidati), STRED_LAT, STRED_LON);
		SwingUtilities.invokeAndWait(() -> {});
		return c;
	}

	private BufferedImage nakresli(final JVrstvaPlanovace slide, final Coord coord) throws Exception {
		final BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
		SwingUtilities.invokeAndWait(() -> {
			slide.setSoord(coord);
			slide.setSize(W, H);
			final Graphics2D g = img.createGraphics();
			slide.paintComponent(g);
			g.dispose();
		});
		return img;
	}

	private static Coord mapa(final int moumer) {
		return new Coord(moumer, new Wgs(STRED_LAT, STRED_LON).toMou(), new Dimension(W, H), 0);
	}

	private void vyber(final Kes k) throws Exception {
		SwingUtilities.invokeAndWait(() -> model.setVybranaKes(vybrana(k)));
	}

	/** Je v obraze v okolí 3 px kolem bodu neprůhledný pixel? */
	private static boolean neprazdneKolem(final BufferedImage img, final int x, final int y) {
		for (int dy = -3; dy <= 3; dy++) {
			for (int dx = -3; dx <= 3; dx++) {
				if (x + dx >= 0 && y + dy >= 0 && x + dx < W && y + dy < H && (img.getRGB(x + dx, y + dy) >>> 24) != 0) {
					return true;
				}
			}
		}
		return false;
	}

	private static Point naPaprsku(final Coord c, final double azimut, final double pixlu) {
		final Point s = PixelyMapy.transform(c, PixelyMapy.mou(STRED_LAT, STRED_LON), new Point());
		final double a = Math.toRadians(azimut);
		return new Point(s.x + (int) Math.round(pixlu * Math.sin(a)), s.y - (int) Math.round(pixlu * Math.cos(a)));
	}

	@Test
	public void klinSektoruVybraneKeseSeKresliIKdyzKesNeniKandidat() throws Exception {
		zobraz360(250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		nad.setIndexatorKesi(index(vybrana));
		vyber(vybrana);
		final Coord c = mapa(14);
		final BufferedImage img = nakresli(pod, c);
		final Point v = naPaprsku(c, 100.5, 150);
		final Point jinde = naPaprsku(c, 160.5, 150);
		Assert.assertTrue("klín sektoru 100 je vidět", neprazdneKolem(img, v.x, v.y));
		Assert.assertFalse("jiný sektor ne", neprazdneKolem(img, jinde.x, jinde.y));
	}

	@Test
	public void clenoveVyberuSeStaviMimoEdtAAzPoHotovemSeKresli() throws Exception {
		zobraz360(250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		final Kes stejny = kesVAzimutu("GC2", 100.3, 1200, 3, 3);
		final java.util.Queue<Runnable> fronta = new java.util.ArrayDeque<>();
		nad.setStavba(fronta::add);
		nad.setIndexatorKesi(index(vybrana, stejny));
		vyber(vybrana);
		final Coord c = mapa(14);
		nakresli(nad, c);
		while (!fronta.isEmpty()) {
			fronta.poll().run();
		}
		nakresli(nad, c);
		Assert.assertTrue("dokud se členové staví, nekreslí se nic", nad.obdelnikyStitkuZvyrazneni().isEmpty());
		Assert.assertEquals("stavba čeká na vlákně", 1, fronta.size());
		fronta.poll().run();
		nakresli(nad, c);
		Assert.assertEquals("po dokončení se štítky kreslí", 2, nad.obdelnikyStitkuZvyrazneni().size());
		Assert.assertTrue("a znovu se nestaví", fronta.isEmpty());
	}

	@Test
	public void zmenaVyberuZrusiRozpracovanouStavbuClenu() throws Exception {
		zobraz360(250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		final Kes jina = kesVAzimutu("GC3", 190.5, 1500, 2, 2);
		final java.util.Queue<Runnable> fronta = new java.util.ArrayDeque<>();
		nad.setStavba(fronta::add);
		nad.setIndexatorKesi(index(vybrana, jina));
		vyber(vybrana);
		final Coord c = mapa(14);
		nakresli(nad, c);
		while (!fronta.isEmpty()) {
			fronta.poll().run();
		}
		nakresli(nad, c);
		Assert.assertEquals("členové prvního výběru čekají", 1, fronta.size());
		final CleniVyberu prvni = nad.cleniVyberu();
		vyber(jina);
		nakresli(nad, c);
		Assert.assertEquals("a nová stavba se zařadí za ni", 2, fronta.size());
		Assert.assertNotSame(prvni, nad.cleniVyberu());
		Assert.assertTrue("stará stavba je zrušená", prvni.jeZrusena());
		fronta.poll().run();
		fronta.poll().run();
		nakresli(nad, c);
		Assert.assertEquals("kreslí se jen členové aktuálního výběru", 1, nad.obdelnikyStitkuZvyrazneni().size());
	}

	@Test
	public void zrusenaStavbaClenuNeniDokoncenaAPoHotovoSeNevola() throws Exception {
		zobraz360(250);
		final List<Wpt> wpts = new ArrayList<>();
		for (int i = 0; i < 9000; i++) {
			wpts.add(kesVAzimutu("GC" + i, 100.5, 1000 + i, 2, 2).getMainWpt());
		}
		final Indexator<Wpt> ix = Indexator.postav(BoundingRect.ALL, wpts, w -> w.getMou().xx, w -> w.getMou().yy);
		final java.util.Queue<Runnable> fronta = new java.util.ArrayDeque<>();
		final int[] hotovo = new int[1];
		final VyberZvyrazneni v = new VyberZvyrazneni(100, null, null);
		final CleniVyberu zrusena = CleniVyberu.spust(model.prehled(), ix, v, fronta::add, () -> hotovo[0]++);
		zrusena.zrus();
		fronta.poll().run();
		Assert.assertNull(zrusena.clenove());
		Assert.assertEquals(0, hotovo[0]);
		final CleniVyberu cela = CleniVyberu.spust(model.prehled(), ix, v, fronta::add, () -> hotovo[0]++);
		fronta.poll().run();
		Assert.assertEquals(9000, cela.clenove().length);
		Assert.assertEquals(1, hotovo[0]);
	}

	@Test
	public void klinMaVidetelnyPasIKdyzJeSektorUzky() throws Exception {
		zobraz360(250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		nad.setIndexatorKesi(index(vybrana));
		vyber(vybrana);
		final Coord c = mapa(14);
		final BufferedImage img = nakresli(pod, c);
		final Point osa = naPaprsku(c, 100.5, 150);
		final Point mimoOsu = new Point(osa.x, osa.y + 3);
		Assert.assertTrue("pás klínu je široký aspoň několik pixelů, ne jen čára: " + (img.getRGB(mimoOsu.x, mimoOsu.y) >>> 24), (img.getRGB(mimoOsu.x, mimoOsu.y) >>> 24) > 40);
		Assert.assertTrue("na obě strany", (img.getRGB(osa.x, osa.y - 3) >>> 24) > 40);
		Assert.assertEquals("a nepokračuje daleko od osy", 0, img.getRGB(osa.x, osa.y + 12) >>> 24);
	}

	@Test
	public void klinPokracujeZaKruznicIAzZaOkno() throws Exception {
		zobraz360(250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		nad.setIndexatorKesi(index(vybrana));
		vyber(vybrana);
		final Coord blizko = mapa(14);
		final BufferedImage img = nakresli(pod, blizko);
		final Point daleko = naPaprsku(blizko, 100.5, 600);
		Assert.assertTrue("klín dosahuje k okraji okna i za prstenec (600 px od středu): " + daleko, neprazdneKolem(img, daleko.x, daleko.y));
		// okno 40 km od středu na ose sektoru: klín prochází oknem, střed ani prstenec v něm nejsou
		final Wgs okno = new Wgs(STRED_LAT + 40_000 * Math.cos(Math.toRadians(100.5)) / 111_000.0, STRED_LON + 40_000 * Math.sin(Math.toRadians(100.5)) / (111_000.0 * Math.cos(Math.toRadians(STRED_LAT))));
		final Coord vzdalene = new Coord(14, okno.toMou(), new Dimension(W, H), 0);
		final BufferedImage img2 = nakresli(pod, vzdalene);
		Assert.assertTrue("vzdálený klín je v okně vidět na ose", neprazdneKolem(img2, W / 2, H / 2));
		Assert.assertFalse("a mimo osu ne", neprazdneKolem(img2, W / 2, H / 2 - 100));
	}

	/** Klín vybraného sektoru 30 km od středu na jeho ose: okno je v něm a střed ani prstenec v okně nejsou; v Mercatorově mapě u pólu je klín široký, tam se jeho šířka nekontroluje. */
	private void klinSeVidiVOkneNaOse(final double lat, final double lon, final int sektor, final boolean uzky) throws Exception {
		final GeometrieSektoru.Bod kandidat = GeometrieSektoru.bod(lat, lon, sektor + 0.5, 3000, GeometrieSektoru.Model.KOULE);
		final BitSet ob = obsazene();
		ob.clear(sektor);
		final VysledekSektoru.Kandidati[] poSektorech = new VysledekSektoru.Kandidati[360];
		poSektorech[sektor] = new VysledekSektoru.Kandidati(sektor);
		poSektorech[sektor].pridej("GC1", 3000);
		final List<VysledekSektoru.KandidatKes> kandidati = new ArrayList<>();
		kandidati.add(new VysledekSektoru.KandidatKes("GC1", kandidat.lat, kandidat.lon, sektor, 3000, false));
		final Challenge c = model.ulozVlastni(new Challenge("A", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(c);
		model.setZapnuto(true);
		model.nastavVysledek(c, new VysledekSektoru(ob, new BitSet(360), poSektorech, 359, kandidati), lat, lon);
		SwingUtilities.invokeAndWait(() -> model.setVybranySektor(c, sektor));
		final GeometrieSektoru.Bod okno = GeometrieSektoru.bod(lat, lon, sektor + 0.5, 30_000, GeometrieSektoru.Model.KOULE);
		final Coord coord = new Coord(14, new Wgs(okno.lat, okno.lon).toMou(), new Dimension(W, H), 0);
		final BufferedImage img = nakresli(pod, coord);
		Assert.assertTrue("klín je v okně na ose: sektor " + sektor + " u " + lat + "," + lon, neprazdneKolem(img, W / 2, H / 2));
		if (uzky) {
			Assert.assertFalse("a mimo osu ne", neprazdneKolem(img, W / 2, H / 2 - 150) && neprazdneKolem(img, W / 2, H / 2 + 150));
		}
	}

	@Test
	public void klinPrechazejiciPresAntimeridianSeVidi() throws Exception {
		klinSeVidiVOkneNaOse(50.0, 179.95, 90, true);
	}

	@Test
	public void klinPrechazejiciPresZapadniAntimeridianSeVidi() throws Exception {
		klinSeVidiVOkneNaOse(50.0, -179.95, 270, true);
	}

	@Test
	public void klinBlizkoSeverniemuPoluSeVidi() throws Exception {
		klinSeVidiVOkneNaOse(84.5, 14.0, 0, false);
	}

	@Test
	public void klinPresPolAOddalenaMapaSeNakresliBezVyjimky() throws Exception {
		klinSeVidiVOkneNaOse(84.5, 14.0, 0, false);
		for (final int zoom : new int[] { 2, 3, 5, 8 }) {
			final BufferedImage img = nakresli(pod, new Coord(zoom, new Wgs(84.5, 14.0).toMou(), new Dimension(W, H), 0));
			int pixelu = 0;
			for (int y = 0; y < H; y += 2) {
				for (int x = 0; x < W; x += 2) {
					pixelu += img.getRGB(x, y) >>> 24 != 0 ? 1 : 0;
				}
			}
			Assert.assertTrue("klín nebo prstenec se u pólu nakreslil, zoom " + zoom + ": " + pixelu, pixelu > 20);
		}
	}

	@Test
	public void klinBlizkoJiznimuPoluSeVidi() throws Exception {
		klinSeVidiVOkneNaOse(-84.5, 14.0, 180, false);
	}

	@Test
	public void stitkyMajiJenKeseVybranehoSektoruVlevoOdIkony() throws Exception {
		zobraz360(250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		final Kes stejny = kesVAzimutu("GC2", 100.3, 1200, 3, 3);
		final Kes jiny = kesVAzimutu("GC3", 190.5, 1500, 2, 2);
		final Kes cizi = kesVAzimutu("OC1", 100.4, 1000, 2, 2);
		nad.setIndexatorKesi(index(vybrana, stejny, jiny, cizi));
		vyber(vybrana);
		final Coord c = mapa(14);
		nakresli(nad, c);
		final List<Rectangle> r = nad.obdelnikyStitkuZvyrazneni();
		Assert.assertEquals("štítek má vybraná keš a keš stejného sektoru, ne jiný sektor ani cizí kešoid", 2, r.size());
		final Point p = PixelyMapy.transform(c, stejny.getMainWpt().getMou(), new Point());
		boolean nalezen = false;
		for (final Rectangle q : r) {
			nalezen |= q.x + q.width == p.x - 8 - 2 && Math.abs(q.y + q.height / 2 - p.y) <= 1;
		}
		Assert.assertTrue("štítek těsně vlevo od ikony: " + r + " u " + p, nalezen);
	}

	@Test
	public void nahoreJeStitekVybraneKeseSPoctemKandidatu() throws Exception {
		zobraz360(100);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		nad.setIndexatorKesi(index(vybrana));
		vyber(vybrana);
		final Coord c = mapa(14);
		final BufferedImage img = nakresli(nad, c);
		final Point p = PixelyMapy.transform(c, vybrana.getMainWpt().getMou(), new Point());
		Rectangle nahore = null;
		for (final Rectangle q : nad.obdelnikyStitku()) {
			if (Math.abs(q.x + q.width / 2 - p.x) <= 1) {
				nahore = q;
			}
		}
		Assert.assertNotNull("štítek vybrané keše je vodorovně na ikoně", nahore);
		Assert.assertTrue("a celý nad ikonou, aby nepřekrýval popisek vpravo: " + nahore + " u " + p, nahore.y + nahore.height <= p.y - 8 + 1);
		Assert.assertEquals("štítek vybrané keše má barvy tooltipu", AkcentPlanovace.pozadiStitku().getRGB(), img.getRGB(nahore.x + 3, nahore.y + 2));
		Assert.assertEquals("Sektor 100° · 1 kandidát · 3,0 km", TextyKresleni.sektorVybrane(100, model.prehled()));
		Assert.assertEquals("sektor bez kandidátů", "Sektor 7°", TextyKresleni.sektorVybrane(7, model.prehled()));
	}

	@Test
	public void sektorVybranyVPlanovaciMaPrednostJenKdyzJeNovejsiNezKes() throws Exception {
		final Challenge c = zobraz360(100, 250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		final Kes jiny = kesVAzimutu("GC2", 250.5, 1500, 2, 2);
		nad.setIndexatorKesi(index(vybrana, jiny));
		vyber(vybrana);
		SwingUtilities.invokeAndWait(() -> model.setVybranySektor(c, 250));
		nad.onEvent(new ChallengeZmenenyEvent(ChallengeZmenenyEvent.Druh.POLICKO, c));
		pod.onEvent(new ChallengeZmenenyEvent(ChallengeZmenenyEvent.Druh.POLICKO, c));
		final Coord mapa = mapa(14);
		nakresli(nad, mapa);
		Assert.assertEquals("novější je sektor vybraný v plánovači: štítek má keš sektoru 250", 1, nad.obdelnikyStitkuZvyrazneni().size());
		final Point p250 = PixelyMapy.transform(mapa, jiny.getMainWpt().getMou(), new Point());
		Assert.assertEquals(p250.x - 10, nad.obdelnikyStitkuZvyrazneni().get(0).x + nad.obdelnikyStitkuZvyrazneni().get(0).width);
		final Kes dalsi = kesVAzimutu("GC3", 100.4, 900, 2, 2);
		nad.setIndexatorKesi(index(vybrana, jiny, dalsi));
		SwingUtilities.invokeAndWait(() -> model.setVybranaKes(vybrana(dalsi)));
		nakresli(nad, mapa);
		final Point p100 = PixelyMapy.transform(mapa, dalsi.getMainWpt().getMou(), new Point());
		boolean nalezen = false;
		for (final Rectangle q : nad.obdelnikyStitkuZvyrazneni()) {
			nalezen |= q.x + q.width == p100.x - 10;
		}
		Assert.assertTrue("po novém výběru keše platí její sektor 100: " + nad.obdelnikyStitkuZvyrazneni(), nalezen);
		Assert.assertEquals(2, nad.obdelnikyStitkuZvyrazneni().size());
	}

	@Test
	public void priSkrytychKesoidechAbezPrepinaceStitkuNejsouStitkyKeseSektoru() throws Exception {
		zobraz360(250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		nad.setIndexatorKesi(index(vybrana));
		vyber(vybrana);
		final Coord c = mapa(14);
		nakresli(nad, c);
		Assert.assertEquals(1, nad.obdelnikyStitkuZvyrazneni().size());
		model.setKresliStitky(false);
		nakresli(nad, c);
		Assert.assertEquals("bez přepínače Štítky", 0, nad.obdelnikyStitkuZvyrazneni().size());
		model.setKresliStitky(true);
		nad.onEvent(new cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent(false));
		nakresli(nad, c);
		Assert.assertEquals("při skrytých kešoidech", 0, nad.obdelnikyStitkuZvyrazneni().size());
	}

	@Test
	public void bezVybraneKeseASektoruSeNicNezvyraznuje() throws Exception {
		zobraz360(250);
		nad.setIndexatorKesi(index(kesVAzimutu("GC1", 100.5, 2000, 2, 2)));
		final Coord c = mapa(14);
		nakresli(nad, c);
		Assert.assertEquals(0, nad.obdelnikyStitkuZvyrazneni().size());
	}

	@Test
	public void dtZvyraznujeKeseStejnehoPolickaAStitekUVybraneKese() throws Exception {
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(dt);
		model.setZapnuto(true);
		model.nastavVysledekPolicek(dt, new VysledekPolicek(81, Arrays.asList("D3/T4"), Collections.<KandidatPolicka> emptyList()), STRED_LAT, STRED_LON);
		SwingUtilities.invokeAndWait(() -> {});
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 4, 6);
		final Kes stejna = kesVAzimutu("GC2", 200.5, 1500, 4, 6);
		final Kes jina = kesVAzimutu("GC3", 300.5, 1500, 4, 7);
		nad.setIndexatorKesi(index(vybrana, stejna, jina));
		vyber(vybrana);
		final Coord c = mapa(14);
		final BufferedImage img = nakresli(nad, c);
		Assert.assertEquals("štítek mají keše stejného políčka D3/T4 (vybraná a stejná), jiná ne", 2, nad.obdelnikyStitkuZvyrazneni().size());
		Assert.assertEquals("D3/T4", model.prehled().stitekPolicka(4, 6));
		final Point p = PixelyMapy.transform(c, stejna.getMainWpt().getMou(), new Point());
		final Rectangle r = (nad.obdelnikyStitkuZvyrazneni().get(0).x + nad.obdelnikyStitkuZvyrazneni().get(0).width) == p.x - 10 ? nad.obdelnikyStitkuZvyrazneni().get(0) : nad.obdelnikyStitkuZvyrazneni().get(1);
		Assert.assertEquals("výplň štítku člena je jiná než akcent kandidátů", JPlanovacNadKeskami.BARVA_CLENA.getRGB(), img.getRGB(r.x + 3, r.y + 3));
		final int okraj = img.getRGB(r.x - 1, r.y + r.height / 2);
		Assert.assertTrue("štítek člena má tmavý vnější okraj proti žluté mapě: " + Integer.toHexString(okraj), (okraj >>> 24) > 100 && ((okraj >> 16) & 0xff) < 140);
		final Color a = AkcentPlanovace.barva();
		final Color m = JPlanovacNadKeskami.BARVA_CLENA;
		Assert.assertTrue("člen je výrazně jiné barvy než kandidáti", Math.abs(a.getRed() - m.getRed()) + Math.abs(a.getGreen() - m.getGreen()) + Math.abs(a.getBlue() - m.getBlue()) > 250);
	}

	@Test
	public void stitkuKesiSektoruJeNejvicDveStaNejblizsichKeStrediOkna() throws Exception {
		zobraz360(250);
		final Kes[] kese = new Kes[300];
		for (int i = 0; i < kese.length; i++) {
			kese[i] = kesVAzimutu("GC" + (1000 + i), 100.4, 100 + i * 3, 2, 2);
		}
		nad.setIndexatorKesi(index(kese));
		vyber(kese[0]);
		nakresli(nad, mapa(16));
		Assert.assertEquals(TvarPlanovace.NEJVIC_ZNACEK, nad.obdelnikyStitkuZvyrazneni().size());
	}

	@Test
	public void bezPrepinaceSektoryNejsouStitkySektoruAleDTStitkyZustavaji() throws Exception {
		zobraz360(250);
		final Kes vybrana = kesVAzimutu("GC1", 100.5, 2000, 2, 2);
		nad.setIndexatorKesi(index(vybrana));
		vyber(vybrana);
		model.setKresliSektory(false);
		nakresli(nad, mapa(14));
		Assert.assertEquals("sektorové štítky patří k Sektorům", 0, nad.obdelnikyStitkuZvyrazneni().size());
		model.setKresliSektory(true);
		nakresli(nad, mapa(14));
		Assert.assertEquals(1, nad.obdelnikyStitkuZvyrazneni().size());
	}
}
