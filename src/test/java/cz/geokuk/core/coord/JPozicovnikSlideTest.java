package cz.geokuk.core.coord;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Factory;
import cz.geokuk.plugins.geocoding.OtevritVMapeAction;

/** Kontextová nabídka mapy: u kříže, mimo kříž, v ČR i mimo ni. */
public class JPozicovnikSlideTest {

	private static final Wgs CR = new Wgs(50.0755, 14.4378);
	private static final Wgs PARIZ = new Wgs(48.8566, 2.3522);

	private static JPozicovnikSlide slide() {
		final JPozicovnikSlide slide = new JPozicovnikSlide();
		slide.inject(new Factory() {
			@Override
			public <T> T init(final T obj) {
				return obj;
			}

			@Override
			public <T> T initNow(final T obj) {
				return obj;
			}
		});
		return slide;
	}

	/** Nabídka u kříže na daném místě. */
	static JPopupMenu menuUKrize(final Wgs misto) throws Exception {
		final JPozicovnikSlide slide = slide();
		slide.onEvent(new PoziceChangedEvent(new Poziceq(misto)));
		final Field blizko = JPozicovnikSlide.class.getDeclaredField("mysJePoblizKrize");
		blizko.setAccessible(true);
		blizko.setBoolean(slide, true);
		final JPopupMenu menu = new JPopupMenu();
		slide.addPopouItems(menu, null);
		return menu;
	}

	/** Nabídka mimo kříž s kurzorem na daném místě. */
	static JPopupMenu menuMimoKriz(final Wgs misto) {
		final JPozicovnikSlide slide = slide();
		slide.setSoord(new Coord(12, misto.toMou(), new Dimension(800, 600), 0));
		slide.onEvent(new ZmenaSouradnicMysiEvent(new Point(400, 300), null, null));
		final JPopupMenu menu = new JPopupMenu();
		slide.addPopouItems(menu, null);
		return menu;
	}

	private static List<String> texty(final JMenu menu) {
		final List<String> nazvy = new ArrayList<>();
		for (final Component c : menu.getMenuComponents()) {
			nazvy.add(((JMenuItem) c).getText());
		}
		return nazvy;
	}

	private static JMenu dalsiMapy(final JPopupMenu menu) {
		final Component posledni = menu.getComponent(menu.getComponentCount() - 1);
		Assert.assertTrue("podnabídka je poslední", posledni instanceof JMenu);
		Assert.assertEquals("Další mapy", ((JMenu) posledni).getText());
		return (JMenu) posledni;
	}

	/** Texty položek nejvyšší úrovně bez podnabídky. */
	private static List<String> vrchol(final JPopupMenu menu) {
		final List<String> texty = new ArrayList<>();
		for (final Component c : menu.getComponents()) {
			if (!(c instanceof JMenu)) {
				texty.add(((JMenuItem) c).getText());
			}
		}
		return texty;
	}

	private static int pocetOsm(final JPopupMenu menu) {
		int n = 0;
		for (final Component c : menu.getComponents()) {
			if (c instanceof JMenu) {
				for (final String t : texty((JMenu) c)) {
					n += t.contains("OpenStreetMap") ? 1 : 0;
				}
			} else if (((JMenuItem) c).getText().contains("OpenStreetMap")) {
				n++;
			}
		}
		return n;
	}

	@Test
	public void ukrizeVCrJsouNaVrcholuJenKrizovePolozkyAMapyComAGoogle() throws Exception {
		final JPopupMenu menu = menuUKrize(CR);
		final List<Object> akce = new ArrayList<>();
		for (final Component c : menu.getComponents()) {
			if (!(c instanceof JMenu)) {
				akce.add(((JMenuItem) c).getAction().getClass());
			}
		}
		Assert.assertEquals(Arrays.<Object>asList(CenterPoziceAction.class, ZoomPoziceAction.class, OdstranKrizAction.class, cz.geokuk.plugins.cesty.akce.PridatDoCestyAction.class,
				SouradniceDoClipboarduAction.class, OtevritVMapeAction.class, OtevritVMapeAction.class), akce);
		final List<String> vrchol = vrchol(menu);
		Assert.assertEquals(Arrays.asList("Otevřít v Mapy.com", "Otevřít v Google Mapách"), vrchol.subList(vrchol.size() - 2, vrchol.size()));
		Assert.assertEquals(Arrays.asList("OpenStreetMap", "ČÚZK Geoprohlížeč", "Otevřít StreetView"), texty(dalsiMapy(menu)));
		Assert.assertEquals("OpenStreetMap právě jednou", 1, pocetOsm(menu));
	}

	@Test
	public void ukrizeMimoCrNeniCuzk() throws Exception {
		final JPopupMenu menu = menuUKrize(PARIZ);
		Assert.assertEquals(Arrays.asList("OpenStreetMap", "Otevřít StreetView"), texty(dalsiMapy(menu)));
		Assert.assertEquals(1, pocetOsm(menu));
	}

	@Test
	public void mimoKrizVCrMaMapyComGoogleAPodnabidkuBezStreetView() {
		final JPopupMenu menu = menuMimoKriz(CR);
		Assert.assertEquals(3, menu.getComponentCount());
		Assert.assertEquals(Arrays.asList("Otevřít v Mapy.com", "Otevřít v Google Mapách"), vrchol(menu));
		Assert.assertEquals(Arrays.asList("OpenStreetMap", "ČÚZK Geoprohlížeč"), texty(dalsiMapy(menu)));
		Assert.assertEquals(1, pocetOsm(menu));
	}

	@Test
	public void mimoKrizMimoCrMaVPodnabidceJenOpenStreetMap() {
		final JPopupMenu menu = menuMimoKriz(PARIZ);
		Assert.assertEquals(Arrays.asList("Otevřít v Mapy.com", "Otevřít v Google Mapách"), vrchol(menu));
		Assert.assertEquals(Arrays.asList("OpenStreetMap"), texty(dalsiMapy(menu)));
	}

	/** Bez kříže a bez známé polohy myši se odkazy na další mapy nenabízejí. */
	@Test
	public void bezPolohyNeniNic() {
		final JPopupMenu menu = new JPopupMenu();
		slide().addPopouItems(menu, null);
		Assert.assertEquals(0, menu.getComponentCount());
	}

	@Test
	public void tooltipyPolozekPodnabidky() throws Exception {
		final JMenu dalsi = dalsiMapy(menuUKrize(CR));
		final List<String> tooltipy = new ArrayList<>();
		for (final Component c : dalsi.getMenuComponents()) {
			tooltipy.add(((JMenuItem) c).getToolTipText());
		}
		Assert.assertEquals(Arrays.asList("Otevře místo v prohlížeči na webu OpenStreetMap.", "Otevře místo v prohlížeči na webu ČÚZK Geoprohlížeč.", "Otevře defaultní prohlížeč se StreetView v daném místě."),
				tooltipy);
	}
}
