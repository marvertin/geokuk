package cz.geokuk.util.gui;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.*;

/** Stavový automat viditelnosti legendy: najetí se zpožděním, tažení, kolečko, fokus; plynulý náběh a zánik; časovač se zastaví. */
public class ViditelnostLegendyTest {

	private final AtomicLong cas = new AtomicLong(1000);
	private final AtomicInteger prekresleni = new AtomicInteger();
	private boolean bezi;
	private int spusteni;
	private int zastaveni;

	private final BublinkaHodnoty.Casovac casovac = new BublinkaHodnoty.Casovac() {
		@Override
		public void spust() {
			bezi = true;
			spusteni++;
		}

		@Override
		public void zastav() {
			bezi = false;
			zastaveni++;
		}

		@Override
		public boolean bezi() {
			return bezi;
		}
	};

	private final ViditelnostLegendy legenda = new ViditelnostLegendy(cas::get, casovac, prekresleni::incrementAndGet);

	private void ubehne(final long ms) {
		cas.addAndGet(ms);
		legenda.tik();
	}

	private static final long PLNA = BublinkaHodnoty.ZACATEK_MS;
	private static final long KONEC = BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS;

	@Test
	public void bezVstupuJeLegendaSkrytaAMrtva() {
		Assert.assertEquals(0f, legenda.viditelnost(), 0f);
		Assert.assertFalse(legenda.jeAktivni());
		Assert.assertFalse(legenda.casovacBezi());
	}

	@Test
	public void zpozdeniNajetiJeDvaCetMilisekund() {
		Assert.assertEquals(200, ViditelnostLegendy.ZPOZDENI_NAJETI_MS);
	}

	@Test
	public void najetiMysiUkazeLegenduAzPoZpozdeni() {
		legenda.mys(true);
		Assert.assertTrue("časovač čeká na zpoždění", legenda.casovacBezi());
		ubehne(ViditelnostLegendy.ZPOZDENI_NAJETI_MS - 1);
		Assert.assertEquals(0f, legenda.viditelnost(), 0f);
		Assert.assertFalse(legenda.jeAktivni());
		ubehne(1);
		Assert.assertTrue(legenda.jeAktivni());
		Assert.assertEquals(0f, legenda.viditelnost(), 0.01f);
		ubehne(PLNA / 2);
		Assert.assertEquals(0.5f, legenda.viditelnost(), 0.02f);
		ubehne(PLNA);
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
	}

	@Test
	public void krátkeNajetiBezZpozdeniNicNeukaze() {
		legenda.mys(true);
		ubehne(ViditelnostLegendy.ZPOZDENI_NAJETI_MS / 2);
		legenda.mys(false);
		ubehne(ViditelnostLegendy.ZPOZDENI_NAJETI_MS * 3);
		Assert.assertFalse(legenda.jeAktivni());
		Assert.assertEquals(0f, legenda.viditelnost(), 0f);
		Assert.assertFalse("časovač se zastavil", legenda.casovacBezi());
	}

	@Test
	public void poOdchoduMyšiSeLegendaDrziAZmizi() {
		legenda.mys(true);
		ubehne(ViditelnostLegendy.ZPOZDENI_NAJETI_MS + PLNA);
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
		legenda.mys(false);
		ubehne(BublinkaHodnoty.DRZENI_MS - 1);
		Assert.assertEquals("drží se", 1f, legenda.viditelnost(), 0f);
		ubehne(1 + BublinkaHodnoty.ZANIKNUTI_MS / 2);
		Assert.assertEquals(0.5f, legenda.viditelnost(), 0.02f);
		ubehne(BublinkaHodnoty.ZANIKNUTI_MS);
		Assert.assertEquals(0f, legenda.viditelnost(), 0f);
		Assert.assertFalse(legenda.jeAktivni());
		Assert.assertFalse("časovač se zastavil na konci", legenda.casovacBezi());
	}

	@Test
	public void behemUstalenehoZobrazeniSeNepreklresluje() {
		legenda.mys(true);
		ubehne(ViditelnostLegendy.ZPOZDENI_NAJETI_MS + PLNA + 5);
		final int po = prekresleni.get();
		for (int i = 0; i < 20; i++) {
			ubehne(16);
		}
		Assert.assertEquals("při viditelnosti 1 se zbytečně nepřekresluje", po, prekresleni.get());
		Assert.assertFalse("časovač v ustáleném stavu stojí", legenda.casovacBezi());
		legenda.mys(false);
		Assert.assertTrue("po odchodu myši se znovu spustí", legenda.casovacBezi());
	}

	@Test
	public void tazeniUkazeLegenduHnedBezZpozdeni() {
		legenda.tahne(true);
		Assert.assertTrue(legenda.jeAktivni());
		ubehne(PLNA);
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
		ubehne(60_000);
		Assert.assertEquals("při tažení se drží", 1f, legenda.viditelnost(), 0f);
		legenda.tahne(false);
		ubehne(KONEC);
		Assert.assertFalse(legenda.jeAktivni());
		Assert.assertFalse(legenda.casovacBezi());
	}

	@Test
	public void tazeniMimoMysDrziLegenduDokudSeTahne() {
		legenda.mys(true);
		ubehne(ViditelnostLegendy.ZPOZDENI_NAJETI_MS + PLNA);
		legenda.tahne(true);
		legenda.mys(false);
		ubehne(KONEC * 3);
		Assert.assertEquals("při tažení se drží i mimo pruh", 1f, legenda.viditelnost(), 0f);
		legenda.tahne(false);
		ubehne(KONEC);
		Assert.assertFalse(legenda.jeAktivni());
	}

	@Test
	public void kolecko() {
		legenda.dotyk();
		Assert.assertTrue(legenda.jeAktivni());
		Assert.assertTrue(legenda.casovacBezi());
		ubehne(PLNA);
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
		ubehne(BublinkaHodnoty.DRZENI_MS - PLNA);
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
		ubehne(BublinkaHodnoty.ZANIKNUTI_MS + 1);
		Assert.assertEquals(0f, legenda.viditelnost(), 0f);
		Assert.assertFalse(legenda.casovacBezi());
	}

	@Test
	public void fokus() {
		legenda.fokus(true);
		ubehne(PLNA + 10_000);
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
		legenda.fokus(false);
		ubehne(KONEC);
		Assert.assertEquals(0f, legenda.viditelnost(), 0f);
	}

	@Test
	public void nahoruOpetNajetiBehemZanikuNavaziBezSkoku() {
		legenda.tahne(true);
		ubehne(PLNA);
		legenda.tahne(false);
		ubehne(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS / 2);
		final float pred = legenda.viditelnost();
		Assert.assertEquals(0.5f, pred, 0.03f);
		legenda.mys(true);
		Assert.assertEquals("hned po najetí se zpoždění neuplatní, legenda je ještě vidět", pred, legenda.viditelnost(), 0.03f);
		ubehne(PLNA);
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
	}

	@Test
	public void zrusUplneVypne() {
		legenda.tahne(true);
		legenda.mys(true);
		ubehne(PLNA);
		legenda.zrus();
		Assert.assertEquals(0f, legenda.viditelnost(), 0f);
		Assert.assertFalse(legenda.jeAktivni());
		Assert.assertFalse(legenda.casovacBezi());
		ubehne(5000);
		Assert.assertEquals("po zrušení se bez nového vstupu nic neobjeví", 0f, legenda.viditelnost(), 0f);
	}

	@Test
	public void casovacSeNespoustiZnovuPriDalsichVstupech() {
		legenda.mys(true);
		legenda.mys(true);
		legenda.tahne(true);
		legenda.fokus(true);
		Assert.assertEquals(1, spusteni);
	}

	@Test
	public void casovacBeziBehemDrzeniAZanikuAPoKonciLegendaZmizi() {
		legenda.dotyk();
		ubehne(PLNA);
		Assert.assertTrue("při držení časovač běží", legenda.casovacBezi());
		ubehne(BublinkaHodnoty.DRZENI_MS - PLNA);
		Assert.assertTrue(legenda.casovacBezi());
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
		ubehne(BublinkaHodnoty.ZANIKNUTI_MS / 2);
		Assert.assertTrue("při zániku časovač běží, aby se legenda plynule ztrácela", legenda.casovacBezi());
		Assert.assertEquals(0.5f, legenda.viditelnost(), 0.05f);
		ubehne(BublinkaHodnoty.ZANIKNUTI_MS);
		Assert.assertFalse("po zániku se časovač zastaví", legenda.casovacBezi());
		Assert.assertEquals("poslední krok legendu skryje", 0f, legenda.viditelnost(), 0f);
		Assert.assertFalse(legenda.jeAktivni());
	}

	@Test
	public void dotykAZrusPrekresli() {
		legenda.dotyk();
		Assert.assertEquals(1, prekresleni.get());
		legenda.zrus();
		Assert.assertEquals(2, prekresleni.get());
	}

	@Test
	public void krokPrekresliJenKdyzSeViditelnostZmenila() {
		legenda.fokus(true);
		final int po = prekresleni.get();
		ubehne(PLNA / 2);
		Assert.assertEquals(po + 1, prekresleni.get());
		ubehne(PLNA);
		Assert.assertEquals(po + 2, prekresleni.get());
		ubehne(10);
		Assert.assertEquals(po + 2, prekresleni.get());
	}

	@Test
	public void beziciKrokBezZmenyViditelnostiNeprekresluje() {
		legenda.fokus(true);
		ubehne(PLNA);
		final int po = prekresleni.get();
		ubehne(10);
		ubehne(10);
		Assert.assertEquals(po, prekresleni.get());
	}

	@Test
	public void vstupBezZmenyViditelnostiNeprekresluje() {
		legenda.mys(true);
		Assert.assertEquals("první vstup poprvé překreslí", 1, prekresleni.get());
		legenda.mys(true);
		Assert.assertEquals(1, prekresleni.get());
	}

	@Test
	public void plneViditelnaLegendaZastaviCasovacPriTazeni() {
		legenda.tahne(true);
		ubehne(PLNA);
		Assert.assertEquals(1f, legenda.viditelnost(), 0f);
		Assert.assertFalse(legenda.casovacBezi());
	}

	@Test
	public void interakceSkoncenaVNulovemCaseSeNepovazujeZaTrvajici() {
		cas.set(0);
		legenda.dotyk();
		cas.set(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS / 2);
		final float v = legenda.viditelnost();
		Assert.assertTrue("viditelnost " + v, v > 0f && v < 1f);
	}

	@Test
	public void legendaPustenaVNulovemCaseZaniknePresneKonci() {
		cas.set(0);
		legenda.tahne(true);
		legenda.tahne(false);
		ubehne(KONEC - 1);
		Assert.assertTrue(legenda.jeAktivni());
		ubehne(1);
		Assert.assertFalse(legenda.jeAktivni());
	}

	@Test
	public void legendaZanikaPresneVCasePoDrzeniAZaniknuti() {
		legenda.dotyk();
		ubehne(KONEC - 1);
		Assert.assertTrue(legenda.jeAktivni());
		ubehne(1);
		Assert.assertFalse(legenda.jeAktivni());
	}

	@Test
	public void verejnyKonstruktorPouzivaSkutecneHodinyACasovac() {
		final ViditelnostLegendy l = new ViditelnostLegendy(prekresleni::incrementAndGet);
		try {
			l.mys(true);
			Assert.assertTrue("čeká na zpoždění", l.casovacBezi());
			Assert.assertFalse(l.jeAktivni());
			l.zrus();
			Assert.assertFalse(l.casovacBezi());
		} finally {
			l.zrus();
		}
	}
}
