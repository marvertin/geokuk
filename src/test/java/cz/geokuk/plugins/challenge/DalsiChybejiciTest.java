package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.VysledekSektoru.KandidatKes;

public class DalsiChybejiciTest {

	private static VysledekSektoru vysledek(final double[][] souradnice) {
		final List<KandidatKes> v = new ArrayList<>();
		for (int i = 0; i < souradnice.length; i++) {
			v.add(new KandidatKes("GC" + i, souradnice[i][0], souradnice[i][1], i));
		}
		return new VysledekSektoru(new BitSet(360), new BitSet(360), new VysledekSektoru.Kandidati[360], 0, v);
	}

	@Test
	public void vybiraNejblizsiKStreduAPotomDalsiAzDoKonceAPakZnovu() {
		final VysledekSektoru v = vysledek(new double[][] { { 50.0, 14.0 }, { 50.1, 14.0 }, { 50.5, 14.0 }, { 51.0, 14.0 } });
		final DalsiChybejici d = new DalsiChybejici();
		Assert.assertEquals("GC1", d.dalsi(v, 50.12, 14.0).id());
		Assert.assertEquals("po vystředění na GC1 už se nevybere znovu, ale další nejbližší", "GC0", d.dalsi(v, 50.1, 14.0).id());
		Assert.assertEquals("GC2", d.dalsi(v, 50.0, 14.0).id());
		Assert.assertEquals("GC3", d.dalsi(v, 50.5, 14.0).id());
		Assert.assertEquals("kolo se uzavře a začne znovu nejbližším", "GC1", d.dalsi(v, 50.12, 14.0).id());
	}

	@Test
	public void vzdalenostJeKBoduZPrvniVolani() {
		final VysledekSektoru v = vysledek(new double[][] { { 50.0, 14.0 } });
		final VybranyKandidat k = new DalsiChybejici().dalsi(v, 51.0, 14.0);
		Assert.assertEquals(111195, k.vzdalenost(), 10);
		Assert.assertEquals(0, k.sektor());
		Assert.assertEquals(50.0, k.lat(), 0);
		Assert.assertEquals(14.0, k.lon(), 0);
	}

	@Test
	public void novyVysledekZacneKoloOdZacatku() {
		final DalsiChybejici d = new DalsiChybejici();
		final VysledekSektoru a = vysledek(new double[][] { { 50.0, 14.0 }, { 50.1, 14.0 } });
		Assert.assertEquals("GC0", d.dalsi(a, 50.0, 14.0).id());
		final VysledekSektoru b = vysledek(new double[][] { { 50.0, 14.0 }, { 50.1, 14.0 } });
		Assert.assertEquals("výsledek je nový, GC0 se smí vybrat znovu", "GC0", d.dalsi(b, 50.0, 14.0).id());
	}

	@Test
	public void bezKandidatuJeNull() {
		final DalsiChybejici d = new DalsiChybejici();
		Assert.assertNull(d.dalsi(vysledek(new double[0][]), 50, 14));
		Assert.assertNull(d.dalsi(null, 50, 14));
	}

	@Test
	public void resetZacneKoloZnovu() {
		final VysledekSektoru v = vysledek(new double[][] { { 50.0, 14.0 }, { 50.1, 14.0 } });
		final DalsiChybejici d = new DalsiChybejici();
		Assert.assertEquals("GC0", d.dalsi(v, 50.0, 14.0).id());
		d.reset();
		Assert.assertEquals("GC0", d.dalsi(v, 50.0, 14.0).id());
	}

	@Test
	public void jedinyKandidatSeVracíStaleDokola() {
		final VysledekSektoru v = vysledek(new double[][] { { 50.0, 14.0 } });
		final DalsiChybejici d = new DalsiChybejici();
		Assert.assertEquals("GC0", d.dalsi(v, 50, 14).id());
		Assert.assertEquals("GC0", d.dalsi(v, 50, 14).id());
		Assert.assertEquals(Collections.singletonList("GC0"), Collections.singletonList(d.dalsi(v, 50, 14).id()));
	}

	@Test
	public void predchoziVratiPredchozihoVybranehoAAktualniSeMuzeVybratZnovu() {
		final VysledekSektoru v = vysledek(new double[][] { { 50.0, 14.0 }, { 50.1, 14.0 }, { 50.5, 14.0 } });
		final DalsiChybejici d = new DalsiChybejici();
		Assert.assertNull("nic nebylo vybráno", d.predchozi(v, 50.0, 14.0));
		Assert.assertEquals("GC0", d.dalsi(v, 50.0, 14.0).id());
		Assert.assertNull("před prvním nic není", d.predchozi(v, 50.0, 14.0));
		Assert.assertEquals("GC1", d.dalsi(v, 50.0, 14.0).id());
		Assert.assertEquals("GC2", d.dalsi(v, 50.0, 14.0).id());
		final VybranyKandidat zpet = d.predchozi(v, 50.1, 14.0);
		Assert.assertEquals("GC1", zpet.id());
		Assert.assertEquals(0, zpet.vzdalenost(), 1);
		Assert.assertEquals("GC2 je zase k dispozici", "GC2", d.dalsi(v, 50.1, 14.0).id());
		Assert.assertEquals("GC1", d.predchozi(v, 50.0, 14.0).id());
		Assert.assertEquals("GC0", d.predchozi(v, 50.0, 14.0).id());
		Assert.assertNull(d.predchozi(v, 50.0, 14.0));
		Assert.assertNull("jiný výsledek", d.predchozi(vysledek(new double[][] { { 50.0, 14.0 } }), 50.0, 14.0));
	}
}
