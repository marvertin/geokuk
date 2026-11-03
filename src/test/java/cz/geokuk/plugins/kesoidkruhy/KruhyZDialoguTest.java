package cz.geokuk.plugins.kesoidkruhy;

import java.awt.Color;

import org.junit.*;

/** Skládání nastavení kruhů z ovládacích prvků dialogu. */
public class KruhyZDialoguTest {

	@Test
	public void zapnutiSeNeMeni() {
		for (final boolean zapnuto : new boolean[] { true, false }) {
			final KruhySettings a = new KruhySettings();
			a.setOnoff(zapnuto);
			final KruhySettings k = KruhyZDialogu.zmen(a, new Color(1, 2, 3), 40, 33, true);
			Assert.assertEquals(zapnuto, k.isOnoff());
			Assert.assertEquals(33, k.getVelikost());
			Assert.assertTrue(k.isJednotkovaVelikost());
			Assert.assertEquals(new Color(1, 2, 3, KruhyZDialogu.alfa(40)), k.getBarva());
		}
	}

	@Test
	public void puvodniNastaveniSeNemeni() {
		final KruhySettings a = new KruhySettings();
		a.setOnoff(true);
		KruhyZDialogu.zmen(a, Color.RED, 0, 99, true);
		Assert.assertEquals(new KruhySettings().getBarva(), a.getBarva());
		Assert.assertEquals(20, a.getVelikost());
	}

	@Test
	public void vychoziAlfaSePrepisemNezmeni() {
		final KruhySettings v = new KruhySettings();
		final int procent = KruhyZDialogu.pruhlednostProcent(v.getBarva().getAlpha());
		Assert.assertEquals(73, procent);
		Assert.assertEquals(v.getBarva(), KruhyZDialogu.slozBarvu(v.getBarva(), procent, v.getBarva().getAlpha()));
	}

	@Test
	public void krajniHodnotyPruhlednosti() {
		Assert.assertEquals(255, KruhyZDialogu.alfa(0));
		Assert.assertEquals(0, KruhyZDialogu.alfa(100));
		Assert.assertEquals(0, KruhyZDialogu.pruhlednostProcent(255));
		Assert.assertEquals(100, KruhyZDialogu.pruhlednostProcent(0));
		for (int p = 0; p <= 100; p++) {
			Assert.assertEquals(p, KruhyZDialogu.pruhlednostProcent(KruhyZDialogu.alfa(p)));
		}
	}
}
