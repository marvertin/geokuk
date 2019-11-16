/**
 *
 */
package cz.geokuk.core.render;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Future;

import cz.geokuk.core.coord.*;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.mapy.kachle.gui.JKachlovnikRendrovaci;

/**
 * @author Martin Veverka
 *
 */
public class Rendrovadlo {

	private static int citac;
	static final int KOLIK_PROGRESUJEME_NA_KACHLICH = 10000;

	// Používané služby
	private Factory factory;

	// pracovní oblast

	// parametry
	private List<JSingleSlide0> slides;
	private final Future<?> future;

	public Rendrovadlo(final Future<?> future) {
		this.future = future;
	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void inject(final SlideListProvider slideListProvider) {
		slides = slideListProvider.getSlides();
	}

	public synchronized BufferedImage rendruj(final RenderParams p, final Progressor progressor) throws InterruptedException {

		System.out.printf("Vytvarim obrazek [%d,%d]\n", p.roord.getWidth(), p.roord.getHeight());
		final BufferedImage image = createImage(p);
		synchronized (image) {
			System.out.printf("Vytvoren obrazek [%d,%d]\n", image.getWidth(), image.getHeight());
			final Graphics ggOriginal = image.getGraphics();
			// final Graphics gg = ggOriginal.create();
			// gg.setColor(Color.CYAN);
			// gg.fillOval(0, 0, image.getWidth(), image.getHeight());
			System.out.printf("Vybarvern obrazek [%d,%d]\n", image.getWidth(), image.getHeight());
			rendruj(p, progressor, ggOriginal);
		}
		return image;
	}

	public void rendruj(final RenderParams p, final Progressor progressor, final Graphics ggOriginal) throws InterruptedException {
		// resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Graphics g = p.resultImagex.getGraphics();
		// rendrujPsanicko(g, Color.MAGENTA);

		// a teď projet směrem nahoru
		// postahujAVyrendrujKachleMap();

		// TODO řešit kanclování
		// if (isCancelled()) return null;

		System.out.println("Rendrovani rendrovadlem spusteno: " + ++citac);
		for (final JSingleSlide0 slidePuvodni : slides) {
			if (slidePuvodni.isVisible()) {
				// for (int i=0; i<100; i++) {
				// slidePuvodni.createRenderableSlide();
				// }
				final JSingleSlide0 slide = slidePuvodni.createRenderableSlide();
				if (slide != null) {
					//// if (slide instanceof JKachlovnikRendrovaci) {
					//// continue;
					// }
					// if (slide instanceof JKesoidy) {
					// continue;
					// }
					System.out.println("    RENDROVANI: " + slide.getClass());
					final Graphics2D g = (Graphics2D) ggOriginal.create();
					Coord coco = p.roord;
					switch (slide.jakOtacetProRendrovani()) {
					case COORD:
						break;
					case GRAPH2D:
						coco = coco.derive(0.0);
						if (p.natacetDoSeveru) {
							g.translate(coco.getWidth() / 2, coco.getHeight() / 2);
							g.rotate(-p.roord.computNataceciUhel());
							g.translate(-coco.getWidth() / 2, -coco.getHeight() / 2);
						}
						break;
					default:
						coco = coco.derive(0.0);
					}
					slide.setSoord(coco);
					slide.setSize(coco.getWidth(), coco.getHeight());
					factory.init(slide); // eventy raději až po nastavení veliksot
					nastavProgressorKachlovniku(slide, progressor);
					slide.render(g);
				}
			}
		}
	}

	BufferedImage createImage(final RenderParams p) throws InterruptedException {
		try {
			final BufferedImage result = new BufferedImage(p.roord.getWidth(), p.roord.getHeight(), p.pruhledne ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_3BYTE_BGR);
			return result;
		} catch (final OutOfMemoryError e) {
			Dlg.error("Nedostatek operační paměti pro rendrování. Zmenši obrázek nebo přidej paměť pro Java Heap");
			future.cancel(true);
			return null;
			// throw new InterruptedException("Není paměť: " + e.toString());
		}
	}

	private void nastavProgressorKachlovniku(final JSingleSlide0 slide, final Progressor progressor) {

		final int progressPocatek = progressor.getProgress();
		// final Progressor prgs = progressModel.start(500, "Rendrování map");
		if (slide instanceof JKachlovnikRendrovaci) {
			final JKachlovnikRendrovaci kach = (JKachlovnikRendrovaci) slide;
			kach.setProgressor((value, maxlue) -> {
				// System.out.println("PROGRESEK: " + value + "/" + maxlue);
				// prgs.setMax(maxlue);

				if (maxlue != 0) {
					// Může se stát, že bude načten obrázek nějaké dlaždice ještě dřív, než budou vydány požadavky na všechny dlaždice,
					// v tom případě se neví, kolik máme dlaždic a přijde nula. Lepší je pak neprogresovat.
					progressor.setProgress(progressPocatek + value * KOLIK_PROGRESUJEME_NA_KACHLICH / maxlue);
				}
				// progressor.setText("Mapiska " + citac);
			});

		}
	}

}
