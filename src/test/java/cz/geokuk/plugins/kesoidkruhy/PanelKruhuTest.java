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
			panel.jednotkoveKruhy.setSelected(!panel.jednotkoveKruhy.isSelected());
			Assert.assertEquals(zapnuto, model.getData().isOnoff());
			Assert.assertTrue(model.getData().isJednotkovaVelikost());
			panel.jednotkoveKruhy.setSelected(false);
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
		Assert.assertTrue(panel.jednotkoveKruhy.isSelected());
	}

	/** Otevření dialogu s výchozím nastavením nezmění výchozí vzhled (alfa 70). */
	@Test
	public void vychoziVzhledSeOtevrenimNezmeni() {
		panel.onEvent(new KruhyPreferencesChangeEvent(new KruhySettings()));
		Assert.assertEquals(0, nastaveno.size());
		Assert.assertEquals(new KruhySettings().getBarva(), model.getData().getBarva());
		Assert.assertEquals(70, model.getData().getBarva().getAlpha());
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
