package cz.geokuk.core.program;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import javax.swing.JLabel;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.DuvodNekresleni;
import cz.geokuk.plugins.kesoid.StavVrstev;
import cz.geokuk.plugins.kesoid.StavVrstevEvent;
import cz.geokuk.plugins.kesoid.VrstvaNadMapou;

/** Štítek „Kruhy skryté“ ve stavovém řádku se ukazuje jen při skrytých zapnutých vrstvách a v bublině říká proč. */
public class JStatusBarStitekTest {

	private static JLabel stitek(final JStatusBar radek) throws Exception {
		final Field f = JStatusBar.class.getDeclaredField("stitekSkrytychKruhu");
		f.setAccessible(true);
		return (JLabel) f.get(radek);
	}

	@Test
	public void stitekJeVidetJenKdyzNekterouVrstvuSkryva() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		final JStatusBar radek = new JStatusBar();
		final JLabel s = stitek(radek);
		Assert.assertFalse(s.isVisible());
		Assert.assertEquals("Kruhy skryté", s.getText());

		final StavVrstev skryto = StavVrstev.prazdny().s(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY, DuvodNekresleni.PRILIS_HUSTE, 0).s(VrstvaNadMapou.OBSAZENOST,
				DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		radek.onEvent(new StavVrstevEvent(skryto));
		Assert.assertTrue(s.isVisible());
		Assert.assertEquals(skryto.tooltip(), s.getToolTipText());

		radek.onEvent(new StavVrstevEvent(StavVrstev.prazdny()));
		Assert.assertFalse(s.isVisible());
		Assert.assertNull("bublina zmizí se štítkem", s.getToolTipText());
	}

	@Test
	public void plovouciStitekNemeniSirkuRadku() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		final JStatusBar radek = new JStatusBar();
		final int bez = radek.getPreferredSize().width;
		radek.onEvent(new StavVrstevEvent(StavVrstev.prazdny().s(VrstvaNadMapou.OBSAZENOST, DuvodNekresleni.POLOMER_PRILIS_MALY, 0)));
		Assert.assertEquals("plovoucí štítek nemění šířku řádku", bez, radek.getPreferredSize().width);
	}

	@Test
	public void omezeneKruhyDajiStitekOmezene() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		final JStatusBar radek = new JStatusBar();
		final JLabel s = stitek(radek);
		radek.onEvent(new StavVrstevEvent(StavVrstev.prazdny().sVynechanymi(4, 12)));
		Assert.assertTrue(s.isVisible());
		Assert.assertEquals("Kruhy omezené", s.getText());
		Assert.assertTrue(s.getToolTipText().contains("kresleno jen 12 nejbližších"));
		radek.onEvent(new StavVrstevEvent(StavVrstev.prazdny().s(VrstvaNadMapou.OBSAZENOST, DuvodNekresleni.POLOMER_PRILIS_MALY, 0)));
		Assert.assertEquals("Kruhy skryté", s.getText());
		radek.onEvent(new StavVrstevEvent(StavVrstev.prazdny()));
		Assert.assertFalse(s.isVisible());
	}

	@Test
	public void stitekJePlovouciARozlozeniRadkuNaNemZavisi() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		final JStatusBar radek = new JStatusBar();
		final Field l = JStatusBar.class.getDeclaredField("layout");
		l.setAccessible(true);
		final cz.geokuk.util.gui.ZalamovaciLayout layout = (cz.geokuk.util.gui.ZalamovaciLayout) l.get(radek);
		final int pred = layout.sirkaVJednomRadku(radek);
		final JLabel s = stitek(radek);
		s.setPreferredSize(new java.awt.Dimension(900, 20));
		radek.onEvent(new StavVrstevEvent(StavVrstev.prazdny().s(VrstvaNadMapou.OBSAZENOST, DuvodNekresleni.POLOMER_PRILIS_MALY, 0)));
		Assert.assertEquals("plovoucí štítek neposouvá ostatní a nezvětšuje řádek", pred, layout.sirkaVJednomRadku(radek));
	}

	@Test
	public void smisenyStavMaStitekSkrytyAOmezeny() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		final JStatusBar radek = new JStatusBar();
		final JLabel s = stitek(radek);
		radek.onEvent(new StavVrstevEvent(StavVrstev.prazdny().s(VrstvaNadMapou.OBSAZENOST, DuvodNekresleni.POLOMER_PRILIS_MALY, 0).sVynechanymi(5, 2000)));
		Assert.assertTrue(s.isVisible());
		Assert.assertEquals("Kruhy skryté a omezené", s.getText());
		Assert.assertTrue(s.getToolTipText(), s.getToolTipText().contains("Obsazenost (F9)"));
		Assert.assertTrue(s.getToolTipText(), s.getToolTipText().contains("kresleno jen 2000 nejbližších"));
		radek.onEvent(new StavVrstevEvent(StavVrstev.prazdny().sVynechanymi(5, 2000)));
		Assert.assertEquals("po zobrazení Obsazenosti zůstane jen omezení", "Kruhy omezené", s.getText());
	}
}
