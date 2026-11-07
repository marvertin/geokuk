package cz.geokuk.plugins.challenge;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.kes.EKesDiffTerRating;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;

/** Keš vybraná programem pro plánovač: z čeho se bere a co se přeskakuje; bez displeje. */
public class SledovaniVybraneKeseTest {

	private static Kes kes(final String id, final EKesDiffTerRating d, final EKesDiffTerRating t) {
		final Kes k = new Kes();
		k.setIdentifier(id);
		k.setVztahx(EKesVztah.NORMAL);
		k.setDifficulty(d);
		k.setTerrain(t);
		final Wpt w = new Wpt();
		w.setWgs(new Wgs(50.1, 14.2));
		k.addWpt(w);
		return k;
	}

	@Test
	public void kesZGcComSePrevezmeSPolohouAPulkamiDT() {
		final VybranaKes v = SledovaniVybraneKese.zWpt(kes("GC1ABC", EKesDiffTerRating.values()[4], EKesDiffTerRating.values()[6]).getMainWpt());
		Assert.assertEquals("GC1ABC", v.id());
		Assert.assertEquals(50.1, v.lat(), 1e-9);
		Assert.assertEquals(14.2, v.lon(), 1e-9);
		Assert.assertEquals(4, v.obtiznostPulek());
		Assert.assertEquals(6, v.terenPulek());
	}

	@Test
	public void bezObtiznostiATerenuJsouPulkyMinusJedna() {
		final VybranaKes v = SledovaniVybraneKese.zWpt(kes("GC1", null, null).getMainWpt());
		Assert.assertEquals(-1, v.obtiznostPulek());
		Assert.assertEquals(-1, v.terenPulek());
	}

	@Test
	public void jinyKesoidNeboNicNeniVybranaKes() {
		Assert.assertNull(SledovaniVybraneKese.zWpt(null));
		Assert.assertNull("kešoid mimo gc.com", SledovaniVybraneKese.zWpt(kes("OC1234", EKesDiffTerRating.values()[2], EKesDiffTerRating.values()[2]).getMainWpt()));
	}

	@Test
	public void modelOhlasujeJenZmenuVybraneKese() {
		final ChallengeModel m = new ChallengeModel();
		final java.util.List<ChallengeZmenenyEvent.Druh> druhy = new java.util.ArrayList<>();
		m.inject(new cz.geokuk.framework.EventFirer() {
			@Override
			public void fire(final cz.geokuk.framework.Event0<?> event) {
				druhy.add(((ChallengeZmenenyEvent) event).druh());
			}
		});
		final VybranaKes a = new VybranaKes("GC1", 50, 14, 2, 2);
		m.setVybranaKes(a);
		final long cas = m.casVyberuKese();
		m.setVybranaKes(new VybranaKes("GC1", 50, 14, 2, 2));
		Assert.assertEquals("stejná keš se neohlašuje", cas, m.casVyberuKese());
		m.setVybranaKes(null);
		Assert.assertNull(m.vybranaKes());
		Assert.assertEquals(java.util.Arrays.asList(ChallengeZmenenyEvent.Druh.VYBRANA_KES, ChallengeZmenenyEvent.Druh.VYBRANA_KES), druhy.subList(Math.max(0, druhy.size() - 2), druhy.size()));
	}
}
