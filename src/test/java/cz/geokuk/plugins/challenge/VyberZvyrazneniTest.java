package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Co se zvýrazní podle výběru: keš programu, sektor nebo políčko z plánovače, vybraný kandidát; bez displeje. */
public class VyberZvyrazneniTest {

	private ChallengeModel model;

	@Before
	public void setUp() {
		model = new ChallengeModel();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
	}

	private PrehledChallenge prehled360() throws Exception {
		final BitSet ob = new BitSet(360);
		ob.set(0, 360);
		ob.clear(250);
		final VysledekSektoru.Kandidati[] k = new VysledekSektoru.Kandidati[360];
		k[250] = new VysledekSektoru.Kandidati(250);
		k[250].pridej("GC250", 1000);
		final Challenge c = model.ulozVlastni(new Challenge("A", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(c);
		model.nastavVysledek(c, new VysledekSektoru(ob, new BitSet(360), k, 359, new ArrayList<VysledekSektoru.KandidatKes>()), 50.0, 14.0);
		javax.swing.SwingUtilities.invokeAndWait(() -> {});
		return model.prehled();
	}

	/** Keš východně od středu: sektor 90. */
	private static VybranaKes vychodni() {
		return new VybranaKes("GC1", 50.0, 14.05, 2, 2);
	}

	@Test
	public void kesVybranaProgramemDavaSvujSektor() throws Exception {
		final PrehledChallenge p = prehled360();
		javax.swing.SwingUtilities.invokeAndWait(() -> model.setVybranaKes(vychodni()));
		final VyberZvyrazneni v = VyberZvyrazneni.vyres(p, model, 0, -1);
		Assert.assertEquals(p.sektorBodu(50.0, 14.05), v.sektor);
		Assert.assertTrue("východní keš leží v sektoru kolem 90: " + v.sektor, v.sektor >= 88 && v.sektor <= 90);
		Assert.assertEquals(vychodni(), v.kes);
	}

	@Test
	public void bezKeseSektorZPlanovaceJinakSektorKandidata() throws Exception {
		final PrehledChallenge p = prehled360();
		Assert.assertTrue(VyberZvyrazneni.vyres(p, model, 0, -1).jeNic());
		Assert.assertEquals("sektor vybraného kandidáta", 77, VyberZvyrazneni.vyres(p, model, 0, 77).sektor);
		javax.swing.SwingUtilities.invokeAndWait(() -> model.setVybranySektor(model.zobrazena(), 250));
		final VyberZvyrazneni v = VyberZvyrazneni.vyres(p, model, 0, 77);
		Assert.assertEquals("sektor z plánovače má přednost před sektorem kandidáta", 250, v.sektor);
		Assert.assertNull(v.kes);
	}

	@Test
	public void novejsiJeBudKesNeboSektorZPlanovace() throws Exception {
		final PrehledChallenge p = prehled360();
		javax.swing.SwingUtilities.invokeAndWait(() -> {
			model.setVybranySektor(model.zobrazena(), 250);
			model.setVybranaKes(vychodni());
		});
		final long cas = model.casVyberuKese();
		Assert.assertEquals("keš je novější než výběr z plánovače", p.sektorBodu(50.0, 14.05), VyberZvyrazneni.vyres(p, model, cas - 1, -1).sektor);
		Assert.assertEquals("výběr z plánovače je novější než keš", 250, VyberZvyrazneni.vyres(p, model, cas + 1, -1).sektor);
	}

	@Test
	public void kesVeStreduNemaSektorAPlatiJinyVyber() throws Exception {
		final PrehledChallenge p = prehled360();
		javax.swing.SwingUtilities.invokeAndWait(() -> model.setVybranaKes(new VybranaKes("GC9", 50.0, 14.0, 2, 2)));
		Assert.assertEquals(55, VyberZvyrazneni.vyres(p, model, 0, 55).sektor);
	}

	@Test
	public void maticeDTBereStitekPolickaZKese() throws Exception {
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(dt);
		model.nastavVysledekPolicek(dt, new VysledekPolicek(81, Arrays.asList("D3/T4"), Collections.<KandidatPolicka> emptyList()), 50, 14);
		javax.swing.SwingUtilities.invokeAndWait(() -> model.setVybranaKes(new VybranaKes("GC1", 50.1, 14, 4, 6)));
		final VyberZvyrazneni v = VyberZvyrazneni.vyres(model.prehled(), model, 0, -1);
		Assert.assertEquals("D3/T4", v.stitek);
		Assert.assertEquals(-1, v.sektor);
		Assert.assertNotNull(v.kes);
	}

	@Test
	public void kalendarNicNezvyraznuje() throws Exception {
		final Challenge k = model.ulozVlastni(new Challenge("K", Typ.KALENDAR, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.setKalendarVUi(true);
		model.aktivuj(k);
		model.nastavVysledekPolicek(k, new VysledekPolicek(366, new ArrayList<String>(), new ArrayList<KandidatPolicka>()), 50, 14);
		javax.swing.SwingUtilities.invokeAndWait(() -> model.setVybranaKes(new VybranaKes("GC1", 50.1, 14, 4, 6)));
		final PrehledChallenge p = model.prehled();
		Assert.assertNotNull(p);
		Assert.assertTrue(VyberZvyrazneni.vyres(p, model, 0, -1).jeNic());
	}
}
