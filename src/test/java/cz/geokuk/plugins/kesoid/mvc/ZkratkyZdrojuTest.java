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

	@Test
	public void akceSeZkratkouPrepnouTypStejneJakoKlikNaZaskrtavatko() {
		final java.util.List<String> volani = new java.util.ArrayList<>();
		final KesoidModel model = new KesoidModel() {
			@Override
			public void setNacitatTyp(final TypZdroje typ, final boolean nacitat) {
				volani.add("typ " + typ + " " + nacitat);
			}

			@Override
			public void setNacitatVseVTypu(final TypZdroje typ, final boolean nacitat) {
				volani.add("vse " + typ + " " + nacitat);
			}
		};
		final PrepniZdrojAction akce = new PrepniZdrojAction(TypZdroje.GSAK);
		akce.inject(model);
		akce.prepni();
		assertEquals("typ bez položek se zapne se všemi položkami", java.util.Arrays.asList("vse GSAK true"), volani);
	}

	@Test
	public void numerickaKlavesniceVyvolaPrepnutiTypuPresKorenOkna() {
		final java.util.List<String> volani = new java.util.ArrayList<>();
		final KesoidModel model = new KesoidModel() {
			@Override
			public void setNacitatVseVTypu(final TypZdroje typ, final boolean nacitat) {
				volani.add(typ + " " + nacitat);
			}
		};
		final PrepniZdrojAction[] akce = { new PrepniZdrojAction(TypZdroje.GPX), new PrepniZdrojAction(TypZdroje.GSAK) };
		for (final PrepniZdrojAction a : akce) {
			a.inject(model);
		}
		final JRootPane koren = new JRootPane();
		PrepniZdrojAction.navazNumpad(koren, akce);
		final Object klic = koren.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(ZkratkyZdroju.zkratkaNumpad(TypZdroje.GSAK));
		assertNotNull(klic);
		koren.getActionMap().get(klic).actionPerformed(null);
		assertEquals(java.util.Arrays.asList("GSAK true"), volani);
		assertNull("Ctrl+4 (OpenSAK) nebyl navázán", koren.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(ZkratkyZdroju.zkratkaNumpad(TypZdroje.OPENSAK)));
	}
}
