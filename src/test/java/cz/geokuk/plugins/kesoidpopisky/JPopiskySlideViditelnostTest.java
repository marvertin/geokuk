package cz.geokuk.plugins.kesoidpopisky;

import static org.junit.Assert.*;

import org.junit.Test;

import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;

/** Popisky na mapě jsou vidět, jen když jsou zapnuté kešoidy (F5) i vlastní vypínač popisků (F12). */
public class JPopiskySlideViditelnostTest {

	private static PopiskyOnoffEvent popisky(final boolean zapnuto) {
		final PopiskyOnoffEvent e = new PopiskyOnoffEvent();
		e.onoff = zapnuto;
		return e;
	}

	@Test
	public void vypnutiKesoiduSkrytePopiskyAZapnutiJeVratiBezZmenyVlastnihoVypinace() {
		final JPopiskySlide slide = new JPopiskySlide();
		slide.onEvent(popisky(true));
		assertTrue(slide.isVisible());
		slide.onEvent(new KesoidOnoffEvent(false));
		assertFalse("kešoidy vypnuty, popisky se nevykreslují", slide.isVisible());
		slide.onEvent(new KesoidOnoffEvent(true));
		assertTrue("kešoidy znovu zapnuty, popisky zpět", slide.isVisible());
	}

	@Test
	public void vlastniVypinacVypnutychPopiskuSeZapnutimKesoiduNeprepise() {
		final JPopiskySlide slide = new JPopiskySlide();
		slide.onEvent(popisky(false));
		slide.onEvent(new KesoidOnoffEvent(false));
		slide.onEvent(new KesoidOnoffEvent(true));
		assertFalse("popisky vypnuté uživatelem zůstávají vypnuté", slide.isVisible());
	}

	@Test
	public void zapnutiPopiskuPriVypnutychKesoidechPopiskyNeukaze() {
		final JPopiskySlide slide = new JPopiskySlide();
		slide.onEvent(new KesoidOnoffEvent(false));
		slide.onEvent(popisky(true));
		assertFalse(slide.isVisible());
		slide.onEvent(new KesoidOnoffEvent(true));
		assertTrue(slide.isVisible());
	}

	/** Start s vypnutými kešoidy: událost kešoidů i popisků přijde v libovolném pořadí a popisky nejsou vidět. */
	@Test
	public void pocatecniStavPriStartuSVypnutymiKesoidy() {
		final JPopiskySlide a = new JPopiskySlide();
		a.onEvent(new KesoidOnoffEvent(false));
		a.onEvent(popisky(true));
		final JPopiskySlide b = new JPopiskySlide();
		b.onEvent(popisky(true));
		b.onEvent(new KesoidOnoffEvent(false));
		assertFalse(a.isVisible());
		assertFalse(b.isVisible());
	}
}
