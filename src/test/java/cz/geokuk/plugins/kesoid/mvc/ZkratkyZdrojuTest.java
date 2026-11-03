package cz.geokuk.plugins.kesoid.mvc;

import static org.junit.Assert.*;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import org.junit.Test;

import cz.geokuk.plugins.kesoid.importek.StavZdroju;
import cz.geokuk.plugins.kesoid.importek.TypZdroje;

public class ZkratkyZdrojuTest {

	@Test
	public void cislaJsouPevnaPodlePoradiTypu() {
		assertEquals(1, ZkratkyZdroju.cislo(TypZdroje.GPX));
		assertEquals(2, ZkratkyZdroju.cislo(TypZdroje.GEOGET));
		assertEquals(3, ZkratkyZdroju.cislo(TypZdroje.GSAK));
		assertEquals(4, ZkratkyZdroju.cislo(TypZdroje.OPENSAK));
		assertEquals("Ctrl+3", ZkratkyZdroju.text(TypZdroje.GSAK));
	}

	@Test
	public void zaDevatymTypemZkratkaNeni() {
		assertEquals(9, ZkratkyZdroju.cislo(8));
		assertEquals(0, ZkratkyZdroju.cislo(9));
		assertEquals(0, ZkratkyZdroju.cislo(40));
	}

	@Test
	public void zkratkyVazanyNaKodKlavesyNeNaZnak() {
		final KeyStroke ctrl3 = ZkratkyZdroju.zkratka(TypZdroje.GSAK);
		assertEquals(KeyEvent.VK_3, ctrl3.getKeyCode());
		assertEquals(InputEvent.CTRL_DOWN_MASK, ctrl3.getModifiers() & InputEvent.CTRL_DOWN_MASK);
		assertEquals(KeyEvent.CHAR_UNDEFINED, ctrl3.getKeyChar());
		final KeyStroke numpad = ZkratkyZdroju.zkratkaNumpad(TypZdroje.GSAK);
		assertEquals(KeyEvent.VK_NUMPAD3, numpad.getKeyCode());
	}

	/** Na CZ, SK a DE klávesnici dává horní řada bez Shiftu jiné znaky; zkratka se řídí kódem klávesy, takže událost s VK_3 a libovolným znakem ji vyvolá. */
	@Test
	public void zkratkuVyvolaUdalostSKodemKlavesyIKdyzKlavesaPiseJinyZnak() {
		final KeyStroke ctrl3 = ZkratkyZdroju.zkratka(TypZdroje.GSAK);
		final JButton tlacitko = new JButton();
		for (final char znak : new char[] { '3', 'š', '§', KeyEvent.CHAR_UNDEFINED }) {
			final KeyEvent e = new KeyEvent(tlacitko, KeyEvent.KEY_PRESSED, 0, InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_3, znak);
			assertEquals("znak " + znak, ctrl3, KeyStroke.getKeyStrokeForEvent(e));
		}
	}

	@Test
	public void vsechnyZkratkyJsouRuzneAAkceJeMaji() {
		final Set<KeyStroke> videne = new HashSet<>();
		for (final TypZdroje typ : TypZdroje.values()) {
			assertTrue(typ.toString(), videne.add(ZkratkyZdroju.zkratka(typ)));
			assertEquals(ZkratkyZdroju.zkratka(typ), new PrepniZdrojAction(typ).getValue(Action.ACCELERATOR_KEY));
			assertTrue(videne.add(ZkratkyZdroju.zkratkaNumpad(typ)));
		}
	}

	@Test
	public void tabulkaAPrepinacUkazujiZkratku() {
		assertEquals("GSAK (Ctrl+3)", JTabulkaZdroju.nazevTypuSeZkratkou(TypZdroje.GSAK));
		assertTrue(JPrepinaceZdroju.tooltipZaskrtavatka(TypZdroje.GPX, StavZdroju.StavVyberu.VYPNUTO).contains("(Ctrl+1)"));
	}

	/** Model, který si pamatuje, co se po přepnutí typu změnilo. */
	private static KesoidModel zaznamenavajiciModel(final java.util.List<String> volani) {
		return new KesoidModel() {
			@Override
			public void setNacitatTyp(final TypZdroje typ, final boolean nacitat) {
				volani.add("typ " + typ + " " + nacitat);
			}

			@Override
			public void setNacitatVseVTypu(final TypZdroje typ, final boolean nacitat) {
				volani.add("vse " + typ + " " + nacitat);
			}

			@Override
			public void setNacitatJenTyp(final TypZdroje typ) {
				volani.add("jen " + typ);
			}

			@Override
			public void setNacitatVse(final boolean nacitat) {
				volani.add("vsechno " + nacitat);
			}
		};
	}

	@Test
	public void akceSeZkratkouPrepnouTypStejneJakoKlikNaZaskrtavatko() {
		final java.util.List<String> volani = new java.util.ArrayList<>();
		final PrepniZdrojAction akce = new PrepniZdrojAction(TypZdroje.GSAK);
		akce.inject(zaznamenavajiciModel(volani));
		akce.onEvent(new StavZdrojuEvent(StavyZdrojuProTesty.vzorek().snimek()));
		akce.prepni();
		assertEquals("zapnutý typ se vypne, ostatní typy se nemění", java.util.Arrays.asList("typ GSAK false"), volani);
	}

	/** Všechny čtyři zkratky z číselné klávesnice vyvolané událostí klávesy přepnou jen svůj typ. */
	@Test
	public void kazdaNumerickaZkratkaPrepneJenSvujTypPresUdalostKlavesy() {
		final java.util.List<String> volani = new java.util.ArrayList<>();
		final KesoidModel model = zaznamenavajiciModel(volani);
		final StavZdroju stav = StavyZdrojuProTesty.vzorek().snimek();
		final PrepniZdrojAction[] akce = new PrepniZdrojAction[TypZdroje.values().length];
		for (int i = 0; i < akce.length; i++) {
			akce[i] = new PrepniZdrojAction(TypZdroje.values()[i]);
			akce[i].inject(model);
			akce[i].onEvent(new StavZdrojuEvent(stav));
		}
		final JRootPane koren = new JRootPane();
		PrepniZdrojAction.navazNumpad(koren, akce);
		final JButton zdroj = new JButton();
		for (int i = 0; i < akce.length; i++) {
			volani.clear();
			final KeyEvent udalost = new KeyEvent(zdroj, KeyEvent.KEY_PRESSED, 0, InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_NUMPAD1 + i, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_NUMPAD);
			final Object klic = koren.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStrokeForEvent(udalost));
			assertNotNull("Ctrl+Num" + (i + 1) + " je navázáno", klic);
			koren.getActionMap().get(klic).actionPerformed(null);
			assertEquals("Ctrl+Num" + (i + 1), java.util.Arrays.asList("typ " + TypZdroje.values()[i] + " false"), volani);
		}
	}

	/** Typ bez položek (třeba zdroj, který není aktivní) zkratku nemá k čemu použít: položka menu je vypnutá, zkratka nic nedělá a zaškrtnutí zůstane vypnuté. */
	@Test
	public void typBezPolozekMaVypnutouPolozkuAZkratkaNicNedela() {
		final StavyZdrojuProTesty jenGpx = new StavyZdrojuProTesty();
		jenGpx.pridej(TypZdroje.GPX, "praha.gpx");
		jenGpx.prepis();
		final java.util.List<String> volani = new java.util.ArrayList<>();
		final KesoidModel model = zaznamenavajiciModel(volani);
		final PrepniZdrojAction gpx = new PrepniZdrojAction(TypZdroje.GPX);
		final PrepniZdrojAction gsak = new PrepniZdrojAction(TypZdroje.GSAK);
		final JCheckBoxMenuItem polozkaGsak = new JCheckBoxMenuItem();
		gsak.join(polozkaGsak);
		for (final PrepniZdrojAction a : new PrepniZdrojAction[] { gpx, gsak }) {
			a.inject(model);
			a.onEvent(new StavZdrojuEvent(jenGpx.snimek()));
		}
		assertTrue(gpx.isEnabled());
		assertFalse("GSAK nemá žádné položky", gsak.isEnabled());
		assertFalse(polozkaGsak.isEnabled());
		gsak.prepni();
		assertTrue("bez položek se nic nemění", volani.isEmpty());
		gpx.prepni();
		assertEquals(java.util.Arrays.asList("typ GPX false"), volani);
	}

	/** Vypnutý typ, který položky má, se zkratkou i menu zapne; typ, který položky dostane až později, se po události znovu povolí. */
	@Test
	public void vypnutyTypSPolozkamiJdeZapnoutAPrazdnyTypSePoPrichoduPolozekPovoli() {
		final StavyZdrojuProTesty data = StavyZdrojuProTesty.vzorek();
		data.vypnuteTypy.add(TypZdroje.GSAK);
		data.prepisZapnuti();
		final java.util.List<String> volani = new java.util.ArrayList<>();
		final PrepniZdrojAction gsak = new PrepniZdrojAction(TypZdroje.GSAK);
		final JCheckBoxMenuItem polozka = new JCheckBoxMenuItem();
		gsak.join(polozka);
		gsak.inject(zaznamenavajiciModel(volani));

		// Nejdřív prázdný stav: vypnuto, položky nejsou.
		gsak.onEvent(new StavZdrojuEvent(StavZdroju.PRAZDNY));
		assertFalse(gsak.isEnabled());
		assertFalse(polozka.isEnabled());
		gsak.prepni();
		assertTrue(volani.isEmpty());

		// Pak stav s položkami, GSAK vypnutý: akce se znovu povolí a typ jde zapnout zkratkou (prepni) i položkou menu.
		gsak.onEvent(new StavZdrojuEvent(data.snimek()));
		assertTrue("vypnutý typ s položkami je povolený", gsak.isEnabled());
		assertTrue(polozka.isEnabled());
		assertFalse("typ je vypnutý, položka není zaškrtnutá", polozka.isSelected());
		gsak.prepni();
		assertEquals(1, volani.size());
		assertTrue(volani.get(0), volani.get(0).contains("GSAK") && volani.get(0).endsWith("true"));
		volani.clear();
		polozka.doClick();
		assertEquals("klik na položku menu typ zapne", 1, volani.size());
		assertTrue(volani.get(0), volani.get(0).contains("GSAK") && volani.get(0).endsWith("true"));
	}

	/** Zaškrtnutí položky menu Zdroje keší odpovídá stavu typu: zapnutý typ zaškrtnutý, vypnutý ne. */
	@Test
	public void zaskrtnutiPolozkyMenuOdpovidaStavuZdroje() {
		final StavyZdrojuProTesty data = StavyZdrojuProTesty.vzorek();
		final StavZdroju stav = data.snimek();
		final java.util.Map<TypZdroje, JCheckBoxMenuItem> polozky = new java.util.EnumMap<>(TypZdroje.class);
		final java.util.Map<TypZdroje, PrepniZdrojAction> akce = new java.util.EnumMap<>(TypZdroje.class);
		for (final TypZdroje typ : TypZdroje.values()) {
			final PrepniZdrojAction a = new PrepniZdrojAction(typ);
			final JCheckBoxMenuItem polozka = new JCheckBoxMenuItem();
			a.join(polozka);
			polozky.put(typ, polozka);
			akce.put(typ, a);
			a.onEvent(new StavZdrojuEvent(stav));
		}
		for (final TypZdroje typ : TypZdroje.values()) {
			assertEquals(typ.toString(), stav.getStavVyberuTypu(typ) != StavZdroju.StavVyberu.VYPNUTO, polozky.get(typ).isSelected());
			assertEquals(polozky.get(typ).isSelected(), akce.get(typ).isSelected());
		}
		// Typ, který se vypne, se v menu odškrtne.
		data.vypnuteTypy.add(TypZdroje.GSAK);
		data.prepisZapnuti();
		for (final PrepniZdrojAction a : akce.values()) {
			a.onEvent(new StavZdrojuEvent(data.snimek()));
		}
		assertFalse(polozky.get(TypZdroje.GSAK).isSelected());
		assertTrue(polozky.get(TypZdroje.GPX).isSelected());
	}
}
