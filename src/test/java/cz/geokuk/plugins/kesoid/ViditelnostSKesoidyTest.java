package cz.geokuk.plugins.kesoid;

import javax.swing.JPanel;

import org.junit.Assert;
import org.junit.Test;

/** Vrstva patřící ke kešoidům je vidět jen se zapnutými kešoidy i vlastním vypínačem; F5 vlastní vypínač nemění. */
public class ViditelnostSKesoidyTest {

	@Test
	public void bezVlastnihoVypinaceJeSkryta() {
		final JPanel vrstva = new JPanel();
		new ViditelnostSKesoidy(vrstva);
		Assert.assertFalse(vrstva.isVisible());
	}

	@Test
	public void vypnutiKesoiduSkryjeAZapnutiVratiStav() {
		final JPanel vrstva = new JPanel();
		final ViditelnostSKesoidy v = new ViditelnostSKesoidy(vrstva);
		v.setVlastni(true);
		Assert.assertTrue(vrstva.isVisible());
		v.setKesoidy(false);
		Assert.assertFalse(vrstva.isVisible());
		v.setKesoidy(true);
		Assert.assertTrue(vrstva.isVisible());
	}

	@Test
	public void vypnutaVrstvaZustaneVypnutaPoZapnutiKesoidu() {
		final JPanel vrstva = new JPanel();
		final ViditelnostSKesoidy v = new ViditelnostSKesoidy(vrstva);
		v.setKesoidy(false);
		v.setVlastni(false);
		v.setKesoidy(true);
		Assert.assertFalse(vrstva.isVisible());
	}

	/** Při startu s vypnutými kešoidy nezáleží na pořadí událostí. */
	@Test
	public void startSVypnutymiKesoidyVLibovolnemPoradi() {
		final JPanel a = new JPanel();
		final ViditelnostSKesoidy va = new ViditelnostSKesoidy(a);
		va.setKesoidy(false);
		va.setVlastni(true);
		Assert.assertFalse(a.isVisible());
		final JPanel b = new JPanel();
		final ViditelnostSKesoidy vb = new ViditelnostSKesoidy(b);
		vb.setVlastni(true);
		vb.setKesoidy(false);
		Assert.assertFalse(b.isVisible());
		vb.setKesoidy(true);
		Assert.assertTrue(b.isVisible());
	}
}
