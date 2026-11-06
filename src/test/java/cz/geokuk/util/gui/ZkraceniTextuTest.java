package cz.geokuk.util.gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

/** Zkrácení textu s „…“ podle šířky. */
public class ZkraceniTextuTest {

	private static FontMetrics fm() {
		final BufferedImage obraz = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		return obraz.createGraphics().getFontMetrics(new Font(Font.DIALOG, Font.PLAIN, 12));
	}

	@Test
	public void textKterySeVejdeSeNemeni() {
		final FontMetrics fm = fm();
		Assert.assertEquals("abc", ZkraceniTextu.zkrat("abc", fm, fm.stringWidth("abc")));
		Assert.assertEquals("", ZkraceniTextu.zkrat("", fm, 0));
	}

	@Test
	public void dlouhyTextSeZkratiAVejdeSeDoSirky() {
		final FontMetrics fm = fm();
		final String text = "GC4PCQB – Challenge - 360 degrees of the Czech Republic";
		for (int sirka = 20; sirka < fm.stringWidth(text); sirka += 13) {
			final String z = ZkraceniTextu.zkrat(text, fm, sirka);
			Assert.assertTrue(sirka + ": " + z, z.endsWith("…"));
			Assert.assertTrue(sirka + ": " + z, z.length() <= 2 || fm.stringWidth(z) <= sirka);
			Assert.assertTrue(text.startsWith(z.substring(0, z.length() - 1)));
		}
	}

	@Test
	public void vzdyZustaneAsponJedenZnak() {
		Assert.assertEquals("a…", ZkraceniTextu.zkrat("abc", fm(), 0));
	}

	@Test
	public void hranicePresneSirky() {
		final FontMetrics fm = fm();
		final String z = ZkraceniTextu.zkrat("abcdefghij", fm, fm.stringWidth("abcd…"));
		Assert.assertEquals("abcd…", z);
		Assert.assertEquals("abc…", ZkraceniTextu.zkrat("abcdefghij", fm, fm.stringWidth("abcd…") - 1));
	}
}
