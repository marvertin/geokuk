package cz.geokuk.plugins.kesoidkruhy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import cz.geokuk.framework.MyPreferences;

/** Nastavení kruhů v dialogu mění jen velikost, barvu, průhlednost a jednotkovost; zapnutí kruhů zůstane. Bez displeje. */
public class PanelKruhuTest {

	private final MyPreferences pref = MyPreferences.current().node("test-kruhy-panel");
	private final List<KruhySettings> nastaveno = new ArrayList<>();

	private final KruhyModel model = new KruhyModel() {
		@Override
		protected MyPreferences currPrefe() {
			return pref;
		}

		@Override
		public void setData(final KruhySettings data) {
			nastaveno.add(data.copy());
			super.setData(data);
		}
	};

	private PanelKruhu panel;

	@Before
	public void priprava() {
		model.inject(udalost -> {});
		model.reloadPreferences();
		panel = new PanelKruhu();
		panel.inject(model);
		panel.onEvent(new KruhyPreferencesChangeEvent(model.getData()));
		panel.registerEvents();
		nastaveno.clear();
	}

	@After
	public void uklid() throws Exception {
		pref.removeNode();
	}

	private void zapni(final boolean zapnuto) {
		final KruhySettings d = model.getData();
		d.setOnoff(zapnuto);
		model.setData(d);
		panel.onEvent(new KruhyPreferencesChangeEvent(model.getData()));
		nastaveno.clear();
	}

	@Test
	public void zmenaVelikostiZachovaZapnuti() {
		for (final boolean zapnuto : new boolean[] { true, false }) {
			zapni(zapnuto);
			panel.velikostSlider.setValue(60);
			Assert.assertEquals(zapnuto, model.getData().isOnoff());
			Assert.assertEquals(60, model.getData().getVelikost());
			panel.velikostSlider.setValue(30);
		}
	}

	@Test
	public void zmenaBarvyAJednotkovostiZachovaZapnuti() {
		for (final boolean zapnuto : new boolean[] { true, false }) {
			zapni(zapnuto);
			panel.barvaChooser.getSelectionModel().setSelectedColor(new Color(10, 200, 30));
			Assert.assertEquals(zapnuto, model.getData().isOnoff());
			Assert.assertEquals(new Color(10, 200, 30, model.getData().getBarva().getAlpha()), model.getData().getBarva());
			panel.vMetrech.setSelected(true);
			Assert.assertEquals(zapnuto, model.getData().isOnoff());
			Assert.assertTrue(model.getData().isJednotkovaVelikost());
			panel.vPixelech.setSelected(true);
		}
	}

	@Test
	public void pruhlednostMeniJenAlfuABarvuZachova() {
		zapni(true);
		final Color puvodni = model.getData().getBarva();
		panel.pruhlednostSlider.setValue(50);
		final Color po = model.getData().getBarva();
		Assert.assertEquals(KruhyZDialogu.alfa(50), po.getAlpha());
		Assert.assertEquals(puvodni.getRGB() & 0xFFFFFF, po.getRGB() & 0xFFFFFF);
		Assert.assertTrue(model.getData().isOnoff());
		Assert.assertEquals("50 %", panel.pruhlednostLabel.getText().replaceAll(".* (\\d+ %)$", "$1"));
	}

	@Test
	public void barvaPoZmenePruhlednostiZachovaAlfu() {
		panel.pruhlednostSlider.setValue(20);
		final int alfa = model.getData().getBarva().getAlpha();
		panel.barvaChooser.getSelectionModel().setSelectedColor(new Color(1, 2, 3));
		Assert.assertEquals(alfa, model.getData().getBarva().getAlpha());
	}

	/** Přepsání ovládacích prvků z modelu (třeba po F8 nebo Alt+F8) nesmí nic zapsat zpět do modelu. */
	@Test
	public void udalostZModeluSeNevratiDoModelu() {
		final KruhySettings d = model.getData();
		d.setVelikost(55);
		d.setJednotkovaVelikost(true);
		d.setBarva(new Color(5, 6, 7, 100));
		d.setOnoff(true);
		model.setData(d);
		nastaveno.clear();
		panel.onEvent(new KruhyPreferencesChangeEvent(model.getData()));
		Assert.assertEquals("dialog nic nezapsal", 0, nastaveno.size());
		Assert.assertEquals(d, model.getData());
		Assert.assertEquals(55, panel.velikostSlider.getValue());
		Assert.assertTrue(panel.vMetrech.isSelected());
	}

	/** Otevření dialogu s výchozím nastavením nezmění výchozí vzhled (alfa 70). */
	@Test
	public void vychoziVzhledSeOtevrenimNezmeni() {
		panel.onEvent(new KruhyPreferencesChangeEvent(new KruhySettings()));
		Assert.assertEquals(0, nastaveno.size());
		Assert.assertEquals(new KruhySettings().getBarva(), model.getData().getBarva());
		Assert.assertEquals(70, model.getData().getBarva().getAlpha());
	}

	@Test
	public void posuvnikPolomeruNastaviPresetAPole() {
		panel.vMetrech.setSelected(true);
		panel.polomerSlider.setValue(5);
		Assert.assertEquals(500, model.getData().getPolomerMetru());
		Assert.assertEquals("500 m", panel.polomerPole.getText());
		panel.polomerSlider.setValue(7);
		Assert.assertEquals(2000, model.getData().getPolomerMetru());
		Assert.assertEquals("2 km", panel.polomerPole.getText());
	}

	@Test
	public void poleNastaviLibovolnouHodnotuAPosuvnikJenNaNejblizsiPozici() {
		panel.vMetrech.setSelected(true);
		panel.polomerPole.setText("161");
		panel.potvrdPolomerZPole();
		Assert.assertEquals(161, model.getData().getPolomerMetru());
		Assert.assertEquals(4, panel.polomerSlider.getValue());
		Assert.assertEquals("161 m", panel.polomerPole.getText());
		panel.polomerPole.setText("1,5 km");
		panel.polomerPole.postActionEvent();
		Assert.assertEquals(1500, model.getData().getPolomerMetru());
		Assert.assertEquals(7, panel.polomerSlider.getValue());
		Assert.assertEquals("1,5 km", panel.polomerPole.getText());
	}

	@Test
	public void neplatnyVstupSeVratiNaPosledniPlatnouHodnotu() {
		panel.vMetrech.setSelected(true);
		panel.polomerPole.setText("333 m");
		panel.potvrdPolomerZPole();
		nastaveno.clear();
		for (final String t : new String[] { "abc", "0", "99 km", "" }) {
			panel.polomerPole.setText(t);
			panel.potvrdPolomerZPole();
			Assert.assertEquals("333 m", panel.polomerPole.getText());
		}
		Assert.assertEquals("neplatný vstup nic nezapsal", 0, nastaveno.size());
		Assert.assertEquals(333, model.getData().getPolomerMetru());
	}

	@Test
	public void zmenaPoleBezZmenyHodnotyNicNezapise() {
		panel.vMetrech.setSelected(true);
		nastaveno.clear();
		panel.polomerPole.setText("100 m");
		panel.potvrdPolomerZPole();
		panel.polomerPole.setText("0,1 km");
		panel.potvrdPolomerZPole();
		Assert.assertEquals(0, nastaveno.size());
		Assert.assertEquals("100 m", panel.polomerPole.getText());
	}

	@Test
	public void jednotkovostRozhodujeKtereOvladaniJeAktivni() {
		panel.vPixelech.setSelected(true);
		Assert.assertTrue(panel.velikostSlider.isEnabled());
		Assert.assertFalse(panel.polomerSlider.isEnabled());
		Assert.assertFalse(panel.polomerPole.isEnabled());
		panel.vMetrech.setSelected(true);
		Assert.assertFalse(panel.velikostSlider.isEnabled());
		Assert.assertTrue(panel.polomerSlider.isEnabled());
		Assert.assertTrue(panel.polomerPole.isEnabled());
		final KruhySettings d = model.getData();
		d.setJednotkovaVelikost(false);
		panel.onEvent(new KruhyPreferencesChangeEvent(d));
		Assert.assertTrue(panel.velikostSlider.isEnabled());
		Assert.assertFalse(panel.polomerPole.isEnabled());
	}

	@Test
	public void polomerZModeluSeZobrazi() {
		final KruhySettings d = model.getData();
		d.setPolomerMetru(333);
		d.setJednotkovaVelikost(true);
		model.setData(d);
		nastaveno.clear();
		panel.onEvent(new KruhyPreferencesChangeEvent(model.getData()));
		Assert.assertEquals("333 m", panel.polomerPole.getText());
		Assert.assertEquals(PolomerKruhu.pozice(333), panel.polomerSlider.getValue());
		Assert.assertEquals(0, nastaveno.size());
	}

	@Test
	public void zmenaPolomeruZachovaZapnuti() {
		for (final boolean zapnuto : new boolean[] { true, false }) {
			zapni(zapnuto);
			panel.vMetrech.setSelected(true);
			panel.polomerSlider.setValue(1);
			Assert.assertEquals(zapnuto, model.getData().isOnoff());
			Assert.assertEquals(25, model.getData().getPolomerMetru());
			panel.polomerPole.setText("42");
			panel.potvrdPolomerZPole();
			Assert.assertEquals(zapnuto, model.getData().isOnoff());
			panel.vPixelech.setSelected(true);
		}
	}

	@Test
	public void prepinacVelikostiMeniJenJednotkovostAZachovaZapnuti() {
		for (final boolean zapnuto : new boolean[] { true, false }) {
			zapni(zapnuto);
			panel.vMetrech.setSelected(true);
			Assert.assertTrue(model.getData().isJednotkovaVelikost());
			Assert.assertEquals(zapnuto, model.getData().isOnoff());
			Assert.assertFalse(panel.vPixelech.isSelected());
			panel.vPixelech.setSelected(true);
			Assert.assertFalse(model.getData().isJednotkovaVelikost());
			Assert.assertEquals(zapnuto, model.getData().isOnoff());
		}
	}

	@Test
	public void posuvnikPixelu() {
		panel.velikostSlider.setValue(65);
		Assert.assertEquals(65, model.getData().getVelikost());
		Assert.assertEquals("65 px", panel.velikostPole.getText());
	}

	@Test
	public void polePixeluNastaviPosuvnikAModel() {
		panel.velikostPole.setText("40");
		panel.potvrdPixelyZPole();
		Assert.assertEquals(40, model.getData().getVelikost());
		Assert.assertEquals(40, panel.velikostSlider.getValue());
		Assert.assertEquals("40 px", panel.velikostPole.getText());
		panel.velikostPole.setText("75 px");
		panel.velikostPole.postActionEvent();
		Assert.assertEquals(75, model.getData().getVelikost());
	}

	@Test
	public void neplatnyVstupPixeluSeVratiNaPosledniPlatnouHodnotu() {
		panel.velikostPole.setText("40");
		panel.potvrdPixelyZPole();
		nastaveno.clear();
		for (final String t : new String[] { "abc", "-3", "101", "4,5", "40 m", "" }) {
			panel.velikostPole.setText(t);
			panel.potvrdPixelyZPole();
			Assert.assertEquals("40 px", panel.velikostPole.getText());
		}
		Assert.assertEquals(0, nastaveno.size());
		Assert.assertEquals(40, model.getData().getVelikost());
	}

	@Test
	public void velikostZModeluSeZobraziVPoli() {
		final KruhySettings d = model.getData();
		d.setVelikost(77);
		model.setData(d);
		nastaveno.clear();
		panel.onEvent(new KruhyPreferencesChangeEvent(model.getData()));
		Assert.assertEquals("77 px", panel.velikostPole.getText());
		Assert.assertEquals(77, panel.velikostSlider.getValue());
		Assert.assertEquals(0, nastaveno.size());
	}

	@Test
	public void popiskyPrepinaceRikajiCoSeDeje() {
		Assert.assertEquals("v pixelech", panel.vPixelech.getText());
		Assert.assertEquals("v metrech", panel.vMetrech.getText());
		Assert.assertTrue(panel.vPixelech.getToolTipText().contains("na obrazovce"));
		Assert.assertTrue(panel.vMetrech.getToolTipText().contains("na zemi"));
	}

	private void overBezPruhlednosti() throws Exception {
		final java.lang.reflect.Method jeZapnuta;
		try {
			jeZapnuta = javax.swing.colorchooser.AbstractColorChooserPanel.class.getMethod("isColorTransparencySelectionEnabled");
		} catch (final NoSuchMethodException e) {
			Assume.assumeNoException("starší Java výběr barvy bez průhlednosti", e);
			return;
		}
		Assert.assertTrue(panel.barvaChooser.getChooserPanels().length > 1);
		for (final javax.swing.colorchooser.AbstractColorChooserPanel p : panel.barvaChooser.getChooserPanels()) {
			if (p.getClass().getSimpleName().contains("Swatch")) {
				continue; // vzorník průhlednost nemá
			}
			Assert.assertEquals(p.getDisplayName(), Boolean.FALSE, jeZapnuta.invoke(p));
		}
	}

	@Test
	public void vyberBarvyNenabiziPruhlednost() throws Exception {
		overBezPruhlednosti();
	}

	/** Přepnutí vzhledu za běhu vytvoří panely výběru barvy znovu. */
	@Test
	public void vyberBarvyNenabiziPruhlednostAniPoPrepnutiVzhledu() throws Exception {
		javax.swing.SwingUtilities.updateComponentTreeUI(panel);
		overBezPruhlednosti();
		panel.barvaChooser.updateUI();
		overBezPruhlednosti();
	}
}
