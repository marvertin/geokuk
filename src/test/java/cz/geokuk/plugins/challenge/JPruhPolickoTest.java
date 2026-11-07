package cz.geokuk.plugins.challenge;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.napoveda.UkazatelVykonu;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.framework.RozmisteniPrekryvu;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Tlačítko „D3/T4 ✕“ na liště: jen u D/T s vybraným políčkem, na konci lišty, zašedlé při vypnutém F4, klik i Esc zruší výběr. */
public class JPruhPolickoTest {

	private ChallengeModel model;
	private JPruhPlanovace pruh;
	private Challenge dt;

	@After
	public void uklid() throws Exception {
		SwingUtilities.invokeAndWait(() -> {});
		UkazatelVykonu.setPodPruhem(false);
	}

	private void priprav(final int sirka) throws Exception {
		model = new ChallengeModel();
		pruh = new JPruhPlanovace();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				pruh.onEvent((ChallengeZmenenyEvent) event);
			}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
		pruh.inject(model);
		pruh.setSize(sirka, 700);
		dt = model.ulozVlastni(new Challenge("DT", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(dt);
		model.setZapnuto(true);
		final List<KandidatPolicka> k = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			k.add(new KandidatPolicka("GC" + i, 50, 14, "D3/T4", "D3/T4", null));
		}
		model.nastavVysledekPolicek(dt, new VysledekPolicek(81, java.util.Arrays.asList("D3/T4", "D5/T5"), k, 50));
		SwingUtilities.invokeAndWait(() -> {});
	}

	private void vyber(final String stitek) throws Exception {
		model.setVybranePolicko(dt, stitek);
		SwingUtilities.invokeAndWait(() -> {});
		pruh.doLayout();
	}

	private AbstractButton policko() {
		for (final Component c : pruh.getComponents()) {
			if (c instanceof AbstractButton && ((AbstractButton) c).getText().endsWith("✕")) {
				return (AbstractButton) c;
			}
		}
		return null;
	}

	@Test
	public void vybranePolickoNaKonciListyOstatniPrvkyDrziPozici() throws Exception {
		priprav(1280);
		pruh.doLayout();
		final int bez = pruh.oblastPruhu().width;
		Assert.assertNull("bez výběru tlačítko není", policko());
		vyber("D3/T4");
		final AbstractButton p = policko();
		Assert.assertNotNull(p);
		Assert.assertTrue(p.isVisible());
		Assert.assertEquals("D3/T4 ✕", p.getText());
		for (final int sirka : new int[] { RozmisteniPrekryvu.PRUH_PLNY, RozmisteniPrekryvu.PRUH_IKONY }) {
			final Rectangle[] s = JPruhPlanovace.rozloz(sirka, true);
			final Rectangle[] b = JPruhPlanovace.rozloz(sirka, false);
			for (int i = 0; i < JPruhPlanovace.POLICKO; i++) {
				Assert.assertEquals("prvek " + i + " drží pozici", b[i], s[i]);
				Assert.assertFalse("tlačítko nezakrývá prvek " + i, s[i].intersects(s[JPruhPlanovace.POLICKO]));
			}
			Assert.assertTrue("za Nastavením", s[JPruhPlanovace.POLICKO].x > s[JPruhPlanovace.NASTAVENI].x + s[JPruhPlanovace.NASTAVENI].width);
			Assert.assertTrue("vejde se do širší lišty", s[JPruhPlanovace.POLICKO].x + s[JPruhPlanovace.POLICKO].width <= RozmisteniPrekryvu.OKRAJ + sirka + JPruhPlanovace.PRIDAVEK_POLICKA);
		}
		final Rectangle[] r = JPruhPlanovace.rozloz(RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(1280)), true);
		Assert.assertEquals(r[JPruhPlanovace.POLICKO], p.getBounds());
		Assert.assertEquals("lišta je o tlačítko širší, jen doprava", bez + JPruhPlanovace.PRIDAVEK_POLICKA, pruh.oblastPruhu().width);
		Assert.assertEquals(RozmisteniPrekryvu.OKRAJ, pruh.oblastPruhu().x);
	}

	@Test
	public void kliknutiZrusiVyberAPriVypnutemF4JeZasedle() throws Exception {
		priprav(1280);
		vyber("D3/T4");
		Assert.assertEquals("<html>Zrušit výběr políčka D3/T4 (Esc)<br>na mapě všechny keše, F4 ukáže jen jeho kandidáty (5 kandidátů)</html>", policko().getToolTipText());
		Assert.assertNotEquals("zašedlé", UIManager.getColor("Button.foreground"), policko().getForeground());
		model.setJenKandidatiPro(dt);
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("<html>Zrušit výběr políčka D3/T4 (Esc)<br>na mapě jen jeho kandidáti (5 kandidátů)</html>", policko().getToolTipText());
		Assert.assertEquals(UIManager.getColor("Button.foreground"), policko().getForeground());
		policko().doClick();
		SwingUtilities.invokeAndWait(() -> {});
		pruh.doLayout();
		Assert.assertNull(model.vybranePolicko(dt));
		Assert.assertEquals("filtr zůstane", dt, model.jenKandidatiPro());
		Assert.assertFalse("tlačítko zmizí", policko() != null && policko().isVisible());
	}

	@Test
	public void vNejuzsimStupniANaUzkeMapeTlacitkoNeni() throws Exception {
		priprav(RozmisteniPrekryvu.OKRAJ + RozmisteniPrekryvu.REZERVA_VPRAVO + RozmisteniPrekryvu.PRUH_UZKY + 50);
		vyber("D3/T4");
		Assert.assertFalse("nejužší stupeň bez tlačítka", policko() != null && policko().isVisible());
		Assert.assertTrue("výběr je vidět v tooltipu výběru challenge", tipVyberu().endsWith("vybrané políčko D3/T4"));
		Assert.assertEquals("lišta bez tlačítka", RozmisteniPrekryvu.PRUH_UZKY, pruh.oblastPruhu().width);
		priprav(RozmisteniPrekryvu.OKRAJ + RozmisteniPrekryvu.REZERVA_VPRAVO + RozmisteniPrekryvu.PRUH_PLNY + 10);
		vyber("D3/T4");
		Assert.assertFalse("s tlačítkem by se plná lišta nevešla", policko() != null && policko().isVisible());
		Assert.assertEquals(RozmisteniPrekryvu.PRUH_PLNY, pruh.oblastPruhu().width);
		Assert.assertTrue(tipVyberu().endsWith("vybrané políčko D3/T4"));
	}

	/** Tlačítko se ukáže, právě když se širší lišta vejde do volné šířky (hranice na pixel). */
	@Test
	public void hraniceSirkyProTlacitko() throws Exception {
		final int presne = RozmisteniPrekryvu.OKRAJ + RozmisteniPrekryvu.REZERVA_VPRAVO + RozmisteniPrekryvu.PRUH_PLNY + JPruhPlanovace.PRIDAVEK_POLICKA;
		priprav(presne);
		vyber("D3/T4");
		Assert.assertTrue("vejde se přesně", policko() != null && policko().isVisible());
		priprav(presne - 1);
		vyber("D3/T4");
		Assert.assertFalse("o pixel méně se nevejde", policko() != null && policko().isVisible());
	}

	/** Při vypnutém plánovači lišta políčko neukazuje a Esc výběr neruší. */
	@Test
	public void escPriVypnutemPlanovaciNic() throws Exception {
		priprav(1280);
		vyber("D3/T4");
		model.setZapnuto(false);
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertFalse(esc().isEnabled());
		Assert.assertEquals("výběr zůstane", "D3/T4", model.vybranePolicko(dt));
	}

	@Test
	public void u360AniPoVyberuSektoruNeni() throws Exception {
		priprav(1280);
		model.aktivuj(Sablony.cesko360());
		SwingUtilities.invokeAndWait(() -> {});
		pruh.doLayout();
		Assert.assertFalse(policko() != null && policko().isVisible());
	}

	private String tipVyberu() {
		for (final Component c : pruh.getComponents()) {
			if (c instanceof AbstractButton && ((AbstractButton) c).getText().endsWith("▾")) {
				return ((AbstractButton) c).getToolTipText();
			}
		}
		return null;
	}

	/** S násobností ukazuje odznak chybějící nálezy, ne políčka. */
	@Test
	public void odznakSNasobnostiPocitaNalezy() throws Exception {
		priprav(1280);
		final Challenge c = model.ulozVlastni(new Challenge("DT3", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC).sOpakovanim(3));
		model.aktivuj(c);
		model.nastavVysledekPolicek(c, JPopupPlanovaceTest.nasobny());
		SwingUtilities.invokeAndWait(() -> {});
		AbstractButton odznak = null;
		for (final Component k : pruh.getComponents()) {
			if (k instanceof AbstractButton && ((AbstractButton) k).getText().startsWith("chybí")) {
				odznak = (AbstractButton) k;
			}
		}
		Assert.assertNotNull("odznak na liště", odznak);
		Assert.assertEquals("chybí 239", odznak.getText());
		Assert.assertEquals("Chybí 239 nálezů v 80 políčkách z 81 – klik ukáže podrobnosti", odznak.getToolTipText());
	}

	/** Práh násobnosti: už při N = 2 počítá odznak nálezy. */
	@Test
	public void odznakSNasobnostiDvaPocitaNalezy() throws Exception {
		priprav(1280);
		final Challenge c = model.ulozVlastni(new Challenge("DT2", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC).sOpakovanim(2));
		model.aktivuj(c);
		final int[] nalezu = new int[81];
		nalezu[0] = 1;
		final java.util.List<String> chybi = new java.util.ArrayList<>();
		for (int i = 0; i < 81; i++) {
			chybi.add(VypocetMatice.stitek(i / 9, i % 9));
		}
		model.nastavVysledekPolicek(c, new VysledekPolicek(81, chybi, java.util.Collections.<KandidatPolicka> emptyList(), 50, 2, nalezu));
		SwingUtilities.invokeAndWait(() -> {});
		AbstractButton odznak = null;
		for (final Component k : pruh.getComponents()) {
			if (k instanceof AbstractButton && ((AbstractButton) k).getText().startsWith("chybí")) {
				odznak = (AbstractButton) k;
			}
		}
		Assert.assertNotNull("odznak na liště", odznak);
		Assert.assertEquals("81 × 2 − 1 nález", "chybí 161", odznak.getText());
	}

	@Test
	public void plnePolickoMa50Plus() {
		Assert.assertEquals("<html>Zrušit výběr políčka D1/T1 (Esc)<br>na mapě jen jeho kandidáti (50+ kandidátů)</html>", JPruhPlanovace.tipPolicka("D1/T1", 50, true, true));
	}

	private javax.swing.Action esc() {
		return pruh.getActionMap().get(pruh.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).get(javax.swing.KeyStroke.getKeyStroke("ESCAPE")));
	}

	@Test
	public void escZrusiVyberJenBezJinehoPopupu() throws Exception {
		priprav(1280);
		Assert.assertFalse("bez výběru Esc nepatří liště", esc().isEnabled());
		vyber("D3/T4");
		final boolean[] popup = { true };
		pruh.setJinyPopupOtevreny(() -> popup[0]);
		Assert.assertFalse("otevřený popup zavře Esc sám", esc().isEnabled());
		popup[0] = false;
		Assert.assertTrue(esc().isEnabled());
		esc().actionPerformed(new java.awt.event.ActionEvent(pruh, 0, ""));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertNull(model.vybranePolicko(dt));
		Assert.assertFalse(esc().isEnabled());
	}

	@Test
	public void nejdelsiStitekSeVejde() {
		final javax.swing.JButton b = new javax.swing.JButton("D4,5/T4,5 ✕");
		b.setMargin(new java.awt.Insets(0, 2, 0, 2));
		Assert.assertTrue(b.getPreferredSize().width + " > " + JPruhPlanovace.SIRKA_POLICKA, b.getPreferredSize().width <= JPruhPlanovace.SIRKA_POLICKA);
		if (java.util.Arrays.asList(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains("DejaVu Sans")) {
			b.setFont(new java.awt.Font("DejaVu Sans", b.getFont().getStyle(), b.getFont().getSize()));
			Assert.assertTrue("DejaVu Sans: " + b.getPreferredSize().width, b.getPreferredSize().width <= JPruhPlanovace.SIRKA_POLICKA);
		}
	}
}
