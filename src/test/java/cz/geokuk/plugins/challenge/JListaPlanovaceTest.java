package cz.geokuk.plugins.challenge;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.lookandfeel.CharcoalTheme;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.framework.RozmisteniPrekryvu;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Lišta plánovače: výběr jediné aktivní challenge, přepínače kreslení, kopie šablony a čitelnost přepínačů ve skinech. */
public class JListaPlanovaceTest {

	private final List<ChallengeZmenenyEvent> udalosti = new ArrayList<>();
	private final MapoveUloziste uloziste = new MapoveUloziste();
	private ChallengeModel model;
	private JPruhPlanovace pruh;
	private int nastaveni;

	private void priprav(final int sirkaMapy) {
		model = new ChallengeModel();
		pruh = new JPruhPlanovace();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
				pruh.onEvent((ChallengeZmenenyEvent) event);
			}
		});
		model.setUloziste(() -> uloziste);
		model.initAfterInject();
		pruh.inject(model);
		pruh.setNaNastaveni(() -> nastaveni++);
		pruh.setSize(sirkaMapy, 700);
		model.setZapnuto(true);
		pruh.doLayout();
	}

	private static List<JRadioButtonMenuItem> challenge(final JPopupMenu m) {
		final List<JRadioButtonMenuItem> r = new ArrayList<>();
		for (final Component c : m.getComponents()) {
			if (c instanceof JRadioButtonMenuItem) {
				r.add((JRadioButtonMenuItem) c);
			}
		}
		return r;
	}

	private static JMenuItem polozka(final JPopupMenu m, final String text) {
		for (final Component c : m.getComponents()) {
			if (c instanceof JMenuItem && text.equals(((JMenuItem) c).getText())) {
				return (JMenuItem) c;
			}
		}
		return null;
	}

	@Test
	public void vyberUdelaZChallengeJedinouAktivniAZobrazenou() {
		priprav(1280);
		final Challenge praha = Sablony.praha360();
		model.aktivuj(Sablony.cesko360());
		final JPopupMenu m = pruh.sestavVyber();
		final List<JRadioButtonMenuItem> c = challenge(m);
		int vUi = 0;
		for (final Challenge x : model.vsechny()) {
			vUi += model.jeVUi(x) ? 1 : 0;
		}
		Assert.assertEquals("všechny nabízené challenge", vUi, c.size());
		int vybranych = 0;
		JRadioButtonMenuItem prahaPolozka = null;
		for (final JRadioButtonMenuItem p : c) {
			vybranych += p.isSelected() ? 1 : 0;
			if (p.getText().contains(praha.nazev())) {
				prahaPolozka = p;
			}
		}
		Assert.assertEquals("vybraná je jen zobrazená", 1, vybranych);
		Assert.assertNotNull(prahaPolozka);
		prahaPolozka.doClick();
		Assert.assertEquals("aktivní je jen vybraná", 1, model.aktivni().size());
		Assert.assertEquals(praha.nazev(), model.zobrazena().nazev());
	}

	@Test
	public void kalendarSeVeVyberuNenabizi() {
		priprav(1280);
		model.ulozVlastni(new Challenge("Kal", Challenge.Typ.KALENDAR, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		for (final JRadioButtonMenuItem p : challenge(pruh.sestavVyber())) {
			Assert.assertFalse(p.getText(), p.getText().contains("Kal"));
		}
	}

	@Test
	public void kopieSablonyJeAktivniAOtevreDialog() {
		priprav(1280);
		model.aktivuj(Sablony.cesko360());
		final JMenuItem kopie = polozka(pruh.sestavVyber(), "Kopírovat vybranou…");
		Assert.assertTrue(kopie.isEnabled());
		kopie.doClick();
		Assert.assertEquals(1, model.vlastni().size());
		Assert.assertEquals("kopie je zobrazená", model.vlastni().get(0), model.zobrazena());
		Assert.assertEquals(1, model.aktivni().size());
		Assert.assertEquals("otevře se dialog", 1, nastaveni);
		Assert.assertFalse("vlastní definici kopírovat nejde, upravuje se v dialogu", polozka(pruh.sestavVyber(), "Kopírovat vybranou…").isEnabled());
	}

	@Test
	public void planovacChallengeOtevreDialog() {
		priprav(1280);
		polozka(pruh.sestavVyber(), "Nastavení plánovače…").doClick();
		Assert.assertEquals(1, nastaveni);
	}

	@Test
	public void prepinaceKresleniMeniModelUkladajiSeAHlasiZobrazeni() {
		priprav(1280);
		model.aktivuj(Sablony.cesko360());
		Assert.assertTrue("výchozí zapnuto", model.isKresliSektory());
		Assert.assertTrue(model.isKresliStitky());
		udalosti.clear();
		final javax.swing.AbstractButton sektory = (javax.swing.AbstractButton) pruh.getComponent(6);
		final javax.swing.AbstractButton stitky = (javax.swing.AbstractButton) pruh.getComponent(7);
		Assert.assertEquals("Sektory", sektory.getText());
		Assert.assertEquals("Štítky", stitky.getText());
		sektory.doClick();
		Assert.assertFalse(model.isKresliSektory());
		Assert.assertEquals(ChallengeZmenenyEvent.Druh.ZOBRAZENI, udalosti.get(0).druh());
		stitky.doClick();
		Assert.assertFalse(model.isKresliStitky());
		final ChallengeModel znovu = new ChallengeModel();
		znovu.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {}
		});
		znovu.setUloziste(() -> uloziste);
		znovu.initAfterInject();
		Assert.assertFalse("uloženo", znovu.isKresliSektory());
		Assert.assertFalse(znovu.isKresliStitky());
		model.setKresliSektory(true);
		Assert.assertTrue("lišta sleduje model (i z menu)", sektory.isSelected());
	}

	@Test
	public void akcePrepinaceSleduje() {
		priprav(1280);
		model.aktivuj(Sablony.cesko360());
		final ChallengeSektoryAction a = new ChallengeSektoryAction();
		a.inject(model);
		model.setKresliSektory(false);
		final ChallengeZmenenyEvent e = new ChallengeZmenenyEvent(ChallengeZmenenyEvent.Druh.ZOBRAZENI, null);
		e.setModel(model);
		a.onEvent(e);
		Assert.assertFalse(a.isSelected());
		Assert.assertTrue("zapnutý plánovač s 360°", a.isEnabled());
	}

	@Test
	public void sektoryJsouUDtNedostupne() {
		priprav(1280);
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		pruh.vyber(dt);
		final javax.swing.AbstractButton sektory = (javax.swing.AbstractButton) pruh.getComponent(6);
		final javax.swing.AbstractButton stitky = (javax.swing.AbstractButton) pruh.getComponent(7);
		Assert.assertFalse("D/T sektory nemá", sektory.isEnabled());
		Assert.assertFalse("nedostupné Sektory nevypadají zapnuté", sektory.isSelected());
		Assert.assertTrue("volba v modelu zůstává", model.isKresliSektory());
		Assert.assertTrue(stitky.isEnabled());
		final ChallengeSektoryAction a = new ChallengeSektoryAction();
		final ChallengeZmenenyEvent e = new ChallengeZmenenyEvent(ChallengeZmenenyEvent.Druh.ZOBRAZENA, null);
		e.setModel(model);
		a.onEvent(e);
		Assert.assertFalse("ani v menu", a.isEnabled());
		pruh.vyber(Sablony.cesko360());
		Assert.assertTrue(sektory.isEnabled());
		a.onEvent(e);
		Assert.assertTrue(a.isEnabled());
	}

	/** Zapnutý přepínač se pozná od nedostupného: má proužek, který se od jeho pozadí výrazně liší jasem. */
	@Test
	public void zapnutyPrepinacSeLisiOdNedostupneho() throws Exception {
		final javax.swing.LookAndFeel puvodni = UIManager.getLookAndFeel();
		final MetalTheme puvodniTema = MetalLookAndFeel.getCurrentTheme();
		try {
			for (final MetalTheme t : new MetalTheme[] { new OceanTheme(), new DefaultMetalTheme(), new CharcoalTheme() }) {
				MetalLookAndFeel.setCurrentTheme(t);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				SwingUtilities.invokeAndWait(() -> {
					final java.awt.image.BufferedImage[] img = new java.awt.image.BufferedImage[2];
					for (int i = 0; i < 2; i++) {
						final JPruhPlanovace.Prepinac p = new JPruhPlanovace.Prepinac(null);
						p.setText("Sektory");
						p.setSelected(true);
						p.setEnabled(i == 0);
						p.setSize(84, 20);
						img[i] = new java.awt.image.BufferedImage(84, 20, java.awt.image.BufferedImage.TYPE_INT_RGB);
						final java.awt.Graphics2D g = img[i].createGraphics();
						p.paint(g);
						g.dispose();
					}
					final Color zapnuty = new Color(img[0].getRGB(6, 4));
					Assert.assertNotEquals(t.getName() + ": proužek jen u dostupného", img[0].getRGB(42, 16), img[1].getRGB(42, 16));
					final Color prouzek = new Color(img[0].getRGB(42, 16));
					Assert.assertTrue(t.getName() + ": proužek " + prouzek + " na " + zapnuty, Math.abs(JPruhPlanovace.jas(prouzek) - JPruhPlanovace.jas(zapnuty)) > 0.2);
				});
			}
		} finally {
			MetalLookAndFeel.setCurrentTheme(puvodniTema);
			UIManager.setLookAndFeel(puvodni);
		}
	}

	@Test
	public void vNejuzsimStupniJsouPrepinaceVeVyberu() {
		priprav(RozmisteniPrekryvu.OKRAJ + RozmisteniPrekryvu.REZERVA_VPRAVO + RozmisteniPrekryvu.PRUH_UZKY);
		model.aktivuj(Sablony.cesko360());
		pruh.doLayout();
		final JPopupMenu m = pruh.sestavVyber();
		final JMenuItem s = polozka(m, "Sektory na mapě");
		Assert.assertTrue(s instanceof JCheckBoxMenuItem);
		s.doClick();
		Assert.assertFalse(model.isKresliSektory());
		Assert.assertNotNull(polozka(m, "Štítky u kandidátů"));
		Assert.assertNotNull(polozka(m, "Jen kandidáti"));
		priprav(1280);
		Assert.assertNull("v širší liště jsou přepínače v liště", polozka(pruh.sestavVyber(), "Sektory na mapě"));
	}

	/** Zapnutý i vypnutý přepínač má text čitelný na svém pozadí ve všech skinech; zapnutý se pozná podle proužku v barvě challenge. */
	@Test
	public void prepinaceCitelneVeVsechSkinech() throws Exception {
		final javax.swing.LookAndFeel puvodni = UIManager.getLookAndFeel();
		final MetalTheme puvodniTema = MetalLookAndFeel.getCurrentTheme();
		try {
			for (final MetalTheme t : new MetalTheme[] { new OceanTheme(), new DefaultMetalTheme(), new CharcoalTheme() }) {
				MetalLookAndFeel.setCurrentTheme(t);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				SwingUtilities.invokeAndWait(() -> {
					final JPruhPlanovace.Prepinac p = new JPruhPlanovace.Prepinac(null);
					for (final boolean zap : new boolean[] { false, true }) {
						p.setSelected(zap);
						final Color pozadi = UIManager.getColor(zap ? "ToggleButton.select" : "ToggleButton.background");
						final double rozdil = Math.abs(JPruhPlanovace.jas(p.getForeground()) - JPruhPlanovace.jas(pozadi));
						Assert.assertTrue(t.getName() + " zapnuto=" + zap + ": rozdíl jasu " + rozdil, rozdil > 0.45);
					}
				});
			}
		} finally {
			MetalLookAndFeel.setCurrentTheme(puvodniTema);
			UIManager.setLookAndFeel(puvodni);
		}
	}
}
