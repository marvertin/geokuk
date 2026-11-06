package cz.geokuk.plugins.challenge;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

/** Filtr „jen kandidáti“: kdo projde a jak se promítá do filtru keší na mapě. */
public class FiltrKandidatuTest {

	private ChallengeModel model;
	private KesoidModel kesoidModel;
	private PlanovacChallengeSluzba sluzba;

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
		kesoidModel = mock(KesoidModel.class);
		sluzba = new PlanovacChallengeSluzba();
		sluzba.inject(model);
		sluzba.inject(kesoidModel);
	}

	private static Kesoid kes(final String id) {
		final Kesoid k = mock(Kesoid.class);
		when(k.getIdentifier()).thenReturn(id);
		return k;
	}

	private void vysledek360(final String... ids) throws Exception {
		final List<VysledekSektoru.KandidatKes> kandidati = new ArrayList<>();
		for (int i = 0; i < ids.length; i++) {
			kandidati.add(new VysledekSektoru.KandidatKes(ids[i], 50 + i * 0.01, 14, i));
		}
		model.nastavVysledek(Sablony.cesko360(), new VysledekSektoru(new BitSet(360), new BitSet(360), new VysledekSektoru.Kandidati[360], 0, kandidati));
		SwingUtilities.invokeAndWait(() -> {});
	}

	@Test
	public void projdouJenKandidatiBezOhleduNaVelikostPismen() throws Exception {
		model.aktivuj(Sablony.cesko360());
		vysledek360("GC1AAA", "gc2bbb");
		final FiltrKandidatu f = FiltrKandidatu.pro(model, Sablony.cesko360());
		Assert.assertEquals(2, f.pocet());
		Assert.assertTrue(f.test(kes("GC1AAA")));
		Assert.assertTrue(f.test(kes("gc1aaa")));
		Assert.assertTrue(f.test(kes("GC2BBB")));
		Assert.assertFalse(f.test(kes("GC3CCC")));
		Assert.assertFalse(f.test(kes(null)));
		Assert.assertFalse(f.test(null));
	}

	@Test
	public void bezVysledkuSeNefiltruje() {
		model.aktivuj(Sablony.cesko360());
		Assert.assertNull(FiltrKandidatu.pro(model, Sablony.cesko360()));
	}

	@Test
	public void kandidatiPolickaDtAKalendare() throws Exception {
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(dt);
		Assert.assertNull("bez výsledku", FiltrKandidatu.pro(model, dt));
		final List<KandidatPolicka> k = new ArrayList<>();
		k.add(new KandidatPolicka("GC9ZZZ", 50, 14, "D3/T4", "popis", null));
		model.nastavVysledekPolicek(dt, new VysledekPolicek(81, Collections.singletonList("D3/T4"), k));
		SwingUtilities.invokeAndWait(() -> {});
		final FiltrKandidatu f = FiltrKandidatu.pro(model, dt);
		Assert.assertTrue(f.test(kes("GC9ZZZ")));
		Assert.assertFalse(f.test(kes("GC1AAA")));
	}

	@Test
	public void rovnostPodleMnozinyKandidatu() throws Exception {
		model.aktivuj(Sablony.cesko360());
		vysledek360("GC1AAA");
		final FiltrKandidatu a = FiltrKandidatu.pro(model, Sablony.cesko360());
		final FiltrKandidatu b = FiltrKandidatu.pro(model, Sablony.cesko360());
		Assert.assertEquals(a, b);
		Assert.assertEquals(a.hashCode(), b.hashCode());
		vysledek360("GC1AAA", "GC2BBB");
		Assert.assertNotEquals(a, FiltrKandidatu.pro(model, Sablony.cesko360()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void sluzbaPromitneFiltrDoFiltruKesiAZrusiHo() throws Exception {
		model.aktivuj(Sablony.cesko360());
		vysledek360("GC1AAA");
		sluzba.obnovFiltrKandidatu();
		model.setJenKandidatiPro(Sablony.cesko360());
		sluzba.obnovFiltrKandidatu();
		model.setJenKandidatiPro(null);
		sluzba.obnovFiltrKandidatu();
		final ArgumentCaptor<java.util.function.Predicate<Kesoid>> zachyceny = ArgumentCaptor.forClass(java.util.function.Predicate.class);
		verify(kesoidModel, times(3)).setDoplnkovyFiltr(zachyceny.capture());
		final List<java.util.function.Predicate<Kesoid>> volani = zachyceny.getAllValues();
		Assert.assertNull("filtr vypnutý", volani.get(0));
		Assert.assertTrue(volani.get(1).test(kes("GC1AAA")));
		Assert.assertFalse(volani.get(1).test(kes("GC2BBB")));
		Assert.assertNull("filtr zrušen", volani.get(2));
	}

	@Test
	public void bezVysledkuSluzbaNefiltruje() {
		model.aktivuj(Sablony.cesko360());
		model.setJenKandidatiPro(Sablony.cesko360());
		sluzba.obnovFiltrKandidatu();
		verify(kesoidModel).setDoplnkovyFiltr(null);
	}

	@Test
	public void filtrSeObnovujeJenPriUdalostechKtereMuMeniVstup() {
		model.aktivuj(Sablony.cesko360());
		model.setJenKandidatiPro(Sablony.cesko360());
		for (final ChallengeZmenenyEvent.Druh druh : ChallengeZmenenyEvent.Druh.values()) {
			org.mockito.Mockito.clearInvocations(kesoidModel);
			sluzba.onEvent(new ChallengeZmenenyEvent(druh, Sablony.cesko360()));
			final boolean obnovuje = druh == ChallengeZmenenyEvent.Druh.JEN_KANDIDATI || druh == ChallengeZmenenyEvent.Druh.VYSLEDEK || druh == ChallengeZmenenyEvent.Druh.AKTIVNI
					|| druh == ChallengeZmenenyEvent.Druh.DEFINICE || druh == ChallengeZmenenyEvent.Druh.NACTENO;
			verify(kesoidModel, times(obnovuje ? 1 : 0)).setDoplnkovyFiltr(null);
		}
	}
}
