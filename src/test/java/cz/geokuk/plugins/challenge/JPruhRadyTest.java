package cz.geokuk.plugins.challenge;

import java.awt.Rectangle;
import java.util.Collections;

import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.napoveda.UkazatelVykonu;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.framework.RozmisteniPrekryvu;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Jednorázová rada vedle vypnutého úchytu: jednou pro nového i stávajícího uživatele, uloží se do nastavení, nezakrývá lištu. */
public class JPruhRadyTest {

	private ChallengeModel model;
	private JPruhPlanovace pruh;
	private Rectangle prekazka;

	@After
	public void uklid() throws Exception {
		SwingUtilities.invokeAndWait(() -> {});
		UkazatelVykonu.setPodPruhem(false);
	}

	/** Start programu se zadaným úložištěm nastavení. */
	private void start(final MapoveUloziste u, final int sirka) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			model = new ChallengeModel();
			pruh = new JPruhPlanovace();
			model.inject(new EventFirer() {
				@Override
				public void fire(final Event0<?> event) {
					pruh.onEvent((ChallengeZmenenyEvent) event);
				}
			});
			model.setUloziste(() -> u);
			pruh.inject(model);
			pruh.setPrekazky(() -> prekazka == null ? Collections.<Rectangle> emptyList() : Collections.singletonList(prekazka));
			pruh.setSize(sirka, 700);
			model.initAfterInject();
			pruh.doLayout();
		});
	}

	private boolean radaVidet() throws Exception {
		final boolean[] v = new boolean[1];
		SwingUtilities.invokeAndWait(() -> {
			pruh.doLayout();
			v[0] = pruh.rada.isVisible();
		});
		return v[0];
	}

	@Test
	public void novyUzivatelVidiRaduJednouAUloziSe() throws Exception {
		final MapoveUloziste u = new MapoveUloziste();
		start(u, 1280);
		Assert.assertTrue("první start s vypnutým plánovačem", radaVidet());
		Assert.assertEquals("zobrazení se uloží hned", "true", u.get("rada.zobrazena"));
		start(u, 1280);
		Assert.assertFalse("druhý start radu neukáže", radaVidet());
	}

	@Test
	public void radaNeukazujeAniPriDalsichUdalostechPoZavreni() throws Exception {
		final MapoveUloziste u = new MapoveUloziste();
		start(u, 1280);
		SwingUtilities.invokeAndWait(() -> pruh.zavritRadu.doClick());
		Assert.assertFalse("✕ radu zavře", radaVidet());
		SwingUtilities.invokeAndWait(() -> model.setKresliStitky(false));
		Assert.assertFalse("další změna v modelu radu nevrátí", radaVidet());
		Assert.assertEquals("true", u.get("rada.zobrazena"));
	}

	@Test
	public void stavajiciUzivatelSeZapnutymPlanovacemRaduNedostane() throws Exception {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("zapnuto", "true");
		start(u, 1280);
		Assert.assertTrue(model.jeZapnuto());
		Assert.assertFalse(radaVidet());
		Assert.assertEquals("ani po vypnutí se neukáže", "true", u.get("rada.zobrazena"));
		SwingUtilities.invokeAndWait(() -> model.setZapnuto(false));
		Assert.assertFalse(radaVidet());
	}

	@Test
	public void stavajiciUzivatelSVypnutymPlanovacemVidiRaduJednou() throws Exception {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("zapnuto", "false");
		u.put("kresli.stitky", "false");
		start(u, 1280);
		Assert.assertTrue(radaVidet());
		start(u, 1280);
		Assert.assertFalse(radaVidet());
	}

	@Test
	public void zapnutiPlanovaceRaduSkryje() throws Exception {
		start(new MapoveUloziste(), 1280);
		Assert.assertTrue(radaVidet());
		SwingUtilities.invokeAndWait(() -> model.setZapnuto(true));
		Assert.assertFalse("lišta je vidět, rada ne", radaVidet());
		SwingUtilities.invokeAndWait(() -> model.setZapnuto(false));
		Assert.assertFalse("po vypnutí se nevrátí", radaVidet());
	}

	@Test
	public void radaJeVpravoOdUchytuVeVysceListyANezakryvaJi() throws Exception {
		start(new MapoveUloziste(), 1280);
		Assert.assertTrue(radaVidet());
		final Rectangle r = pruh.rada.getBounds();
		final Rectangle uchyt = pruh.oblastPruhu();
		Assert.assertFalse("nezakrývá úchyt", r.intersects(uchyt));
		Assert.assertTrue("vpravo od úchytu", r.x >= uchyt.x + uchyt.width);
		Assert.assertTrue("ve výšce lišty", r.y >= RozmisteniPrekryvu.OKRAJ && r.y + r.height <= RozmisteniPrekryvu.OKRAJ + RozmisteniPrekryvu.VYSKA_PRUHU);
		Assert.assertFalse("nezasahuje do ukazatele výkonu pod lištou", r.intersects(RozmisteniPrekryvu.UKAZATEL_VYKONU));
		Assert.assertTrue("myš na radě patří liště", pruh.contains(r.x + 2, r.y + 2));
		Assert.assertFalse("mapa vedle rady zůstává mapě", pruh.contains(r.x + r.width + 2, r.y + 2));
		Assert.assertFalse("mapa pod radou zůstává mapě", pruh.contains(r.x + 2, r.y + r.height + 4));
		Assert.assertTrue("text se vejde celý", pruh.rada.getWidth() >= pruh.rada.getPreferredSize().width);
	}

	@Test
	public void kdyzSeNevejdeNeukazeSe() throws Exception {
		start(new MapoveUloziste(), 240);
		Assert.assertFalse("úzká mapa", radaVidet());
		prekazka = null;
		start(new MapoveUloziste(), 1280);
		prekazka = new Rectangle(120, 0, 200, 60);
		Assert.assertFalse("panel v pásu lišty", radaVidet());
		prekazka = null;
		Assert.assertTrue("po uvolnění místa se ukáže", radaVidet());
	}

	/** Když se rada nevejde, příznak se nezapíše; v širším okně se ukáže a teprve pak zapíše. */
	@Test
	public void nevejdeSeTakSeNezapiseAUkazeSePozdeji() throws Exception {
		final MapoveUloziste u = new MapoveUloziste();
		start(u, 240);
		Assert.assertFalse(radaVidet());
		Assert.assertNull("nevešla se, příznak se nezapíše", u.get("rada.zobrazena"));
		start(u, 240);
		Assert.assertFalse(radaVidet());
		Assert.assertNull("ani při dalším úzkém startu", u.get("rada.zobrazena"));
		SwingUtilities.invokeAndWait(() -> pruh.setSize(1280, 700));
		Assert.assertTrue("v širším okně se ukáže", radaVidet());
		Assert.assertEquals("a zapíše", "true", u.get("rada.zobrazena"));
		start(u, 1280);
		Assert.assertFalse("další start už ne", radaVidet());
	}

	/** Panel v pásu lišty radu odsune; příznak se zapíše, až když se opravdu ukáže. */
	@Test
	public void prekazkaOdloziZapis() throws Exception {
		final MapoveUloziste u = new MapoveUloziste();
		prekazka = new Rectangle(120, 0, 200, 60);
		start(u, 1280);
		Assert.assertFalse(radaVidet());
		Assert.assertNull(u.get("rada.zobrazena"));
		prekazka = null;
		Assert.assertTrue(radaVidet());
		Assert.assertEquals("true", u.get("rada.zobrazena"));
	}

	@Test
	public void predNactenimNastaveniSeNeukaze() throws Exception {
		final ChallengeModel m = new ChallengeModel();
		Assert.assertTrue("do načtení platí, že už byla vidět", m.jeRadaZobrazena());
	}

	@Test
	public void neciteleNastaveniRaduNeukaze() throws Exception {
		final MapoveUloziste u = new MapoveUloziste() {
			@Override
			public String get(final String klic) {
				throw new IllegalStateException("nečitelné");
			}
		};
		start(u, 1280);
		Assert.assertFalse(radaVidet());
	}

	/** Po přepnutí skinu má rada barvy tooltipu nového skinu, odkaz „Změnit domov…“ akcent nového skinu; text a pozadí rady se liší. */
	@Test
	public void barvyRadyAOdkazuSlediSkin() throws Exception {
		final javax.swing.LookAndFeel puvodni = javax.swing.UIManager.getLookAndFeel();
		final javax.swing.plaf.metal.MetalTheme puvodniTema = javax.swing.plaf.metal.MetalLookAndFeel.getCurrentTheme();
		start(new MapoveUloziste(), 1280);
		final JPopupPlanovace popup = new JPopupPlanovace();
		try {
			for (final javax.swing.plaf.metal.MetalTheme t : new javax.swing.plaf.metal.MetalTheme[] { new javax.swing.plaf.metal.OceanTheme(), new javax.swing.plaf.metal.DefaultMetalTheme(),
					new cz.geokuk.core.lookandfeel.CharcoalTheme() }) {
				javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(t);
				javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
				SwingUtilities.invokeAndWait(() -> {
					SwingUtilities.updateComponentTreeUI(pruh);
					SwingUtilities.updateComponentTreeUI(popup);
					final java.awt.Color pozadi = javax.swing.UIManager.getColor("ToolTip.background");
					Assert.assertEquals(t.getName(), pozadi.getRGB(), pruh.rada.getBackground().getRGB());
					final java.awt.Color text = ((javax.swing.JLabel) pruh.rada.getComponent(0)).getForeground();
					Assert.assertEquals(t.getName(), javax.swing.UIManager.getColor("ToolTip.foreground").getRGB(), text.getRGB());
					Assert.assertTrue(t.getName() + " text rady proti pozadí", Math.abs(jas(text) - jas(pozadi)) > 100);
					final java.awt.Color pozadiPopupu = javax.swing.UIManager.getColor("Panel.background");
					final java.awt.Color odkaz = popup.obsah.zmenitDomov.getForeground();
					Assert.assertEquals(t.getName(), JPopupPlanovace.Obsah.barvaOdkazu(pozadiPopupu, javax.swing.UIManager.getColor("Label.foreground")).getRGB(), odkaz.getRGB());
					Assert.assertTrue(t.getName() + " odkaz proti pozadí popupu " + odkaz, JPopupPlanovace.Obsah.kontrast(odkaz, pozadiPopupu) >= JPopupPlanovace.Obsah.KONTRAST_ODKAZU);
					if (JPopupPlanovace.Obsah.jeTmave(pozadiPopupu)) {
						Assert.assertEquals(t.getName() + " v tmavém skinu barva textu", javax.swing.UIManager.getColor("Label.foreground").getRGB(), odkaz.getRGB());
					} else {
						Assert.assertNotEquals(t.getName() + " ve světlém skinu akcent, ne text", javax.swing.UIManager.getColor("Label.foreground").getRGB(), odkaz.getRGB());
					}
					Assert.assertTrue("odkaz je podtržený",
							java.awt.font.TextAttribute.UNDERLINE_ON.equals(popup.obsah.zmenitDomov.getFont().getAttributes().get(java.awt.font.TextAttribute.UNDERLINE)));
				});
			}
		} finally {
			javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(puvodniTema);
			javax.swing.UIManager.setLookAndFeel(puvodni);
		}
	}

	private static double jas(final java.awt.Color c) {
		return 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
	}

	/** Novější formát nastavení se nepřepisuje: rada se ukáže, příznak se nezapíše (platí jen do konce běhu). */
	@Test
	public void priZakazanemUkladaniSePriznakNezapise() throws Exception {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("format", "2");
		start(u, 1280);
		Assert.assertTrue("rada se ukáže", radaVidet());
		Assert.assertNull("nastavení novějšího formátu se nepřepisuje", u.get("rada.zobrazena"));
		Assert.assertTrue("v tomto běhu už platí jako zobrazená", model.jeRadaZobrazena());
		SwingUtilities.invokeAndWait(() -> pruh.zavritRadu.doClick());
		SwingUtilities.invokeAndWait(() -> model.setKresliStitky(false));
		Assert.assertFalse("v témže běhu se nevrátí", radaVidet());
		Assert.assertEquals("klíče se nemění", new java.util.LinkedHashSet<>(java.util.Arrays.asList("format")), u.klice());
	}

	/** Šířka rady je změřený obsah a rezerva pro zvětšení; vejde se právě do volné šířky, o 1 px užší mapa už ne. */
	@Test
	public void rezervaSirkyRadyAHraniceVolneSirky() throws Exception {
		start(new MapoveUloziste(), 1280);
		Assert.assertTrue(radaVidet());
		final int sirkaObsahu = pruh.rada.getPreferredSize().width;
		Assert.assertEquals("rezerva pro zvětšení", sirkaObsahu + JPruhPlanovace.REZERVA_RADY, pruh.rada.getWidth());
		Assert.assertEquals(8, JPruhPlanovace.REZERVA_RADY);
		final int x = pruh.rada.getX();
		// Konec rady má padnout přesně na konec volné šířky: OKRAJ + volnaSirkaPruhu(mapa); volná šířka roste s mapou 1:1.
		final int presne = x + sirkaObsahu + JPruhPlanovace.REZERVA_RADY - RozmisteniPrekryvu.OKRAJ - RozmisteniPrekryvu.volnaSirkaPruhu(0);
		SwingUtilities.invokeAndWait(() -> pruh.setSize(presne, 700));
		Assert.assertTrue("vejde se přesně", radaVidet());
		Assert.assertEquals(RozmisteniPrekryvu.OKRAJ + RozmisteniPrekryvu.volnaSirkaPruhu(presne), pruh.rada.getX() + pruh.rada.getWidth());
		SwingUtilities.invokeAndWait(() -> pruh.setSize(presne - 1, 700));
		Assert.assertFalse("o 1 px užší mapa už ne", radaVidet());
	}
}
