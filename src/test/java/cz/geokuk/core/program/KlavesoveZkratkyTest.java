package cz.geokuk.core.program;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.*;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.junit.Test;

import cz.geokuk.plugins.mapy.PodkladAction;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;

/**
 * Každá klávesová zkratka v menu smí patřit jen jedné akci.
 */
public class KlavesoveZkratkyTest {

	@Test
	public void zadnaZkratkaNeniPrirazenaDvakrat() throws Exception {
		final Map<KeyStroke, List<String>> podleZkratky = new LinkedHashMap<>();
		final Akce akce = new Akce();
		for (final Field f : Akce.class.getFields()) {
			final Object hodnota = f.get(akce);
			if (hodnota instanceof Action) {
				pridej(podleZkratky, (Action) hodnota, f.getName());
			}
		}
		for (final EKaType ka : EKaType.values()) {
			pridej(podleZkratky, new PodkladAction(ka), "mapa " + ka.getNazev());
		}
		assertTrue("Našlo se jen " + podleZkratky.size() + " zkratek", podleZkratky.size() > 40);

		final List<String> kolize = new ArrayList<>();
		podleZkratky.forEach((zkratka, akceSeZkratkou) -> {
			if (akceSeZkratkou.size() > 1) {
				kolize.add(zkratka + ": " + akceSeZkratkou);
			}
		});
		assertTrue("Kolize klávesových zkratek:\n" + String.join("\n", kolize), kolize.isEmpty());
	}

	@Test
	public void zobrazovaniKesoiduMaZkratkuF5() {
		assertEquals(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0), new Akce().kesoidyOnoffAction.getValue(Action.ACCELERATOR_KEY));
	}

	private static void pridej(final Map<KeyStroke, List<String>> podleZkratky, final Action akce, final String jmeno) {
		final KeyStroke zkratka = (KeyStroke) akce.getValue(Action.ACCELERATOR_KEY);
		if (zkratka != null) {
			podleZkratky.computeIfAbsent(zkratka, k -> new ArrayList<>()).add(jmeno);
		}
	}
}
