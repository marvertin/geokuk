package cz.geokuk.core.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;

import javax.swing.SwingUtilities;

import cz.geokuk.core.coord.Coord;

public class PrintingSwingWorker extends RendererSwingWorker0 implements Printable {

	private int citac;

	PrintingSwingWorker() {
	}

	@Override
	public RenderResult doInBackground() throws Exception {
		progressor.setMax(Rendrovadlo.KOLIK_PROGRESUJEME_NA_KACHLICH);

		final PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setJobName("Geokuk tisk");
		pj.setPrintable(this);
		final boolean[] jedem = new boolean[1];
		SwingUtilities.invokeAndWait(() -> jedem[0] = pj.printDialog());
		if (!jedem[0]) {
			return null;
		}
		pj.print();
		// System.out.printf("Zapis obrazku [%d,%d] do souboru \"%s\"%n", image.getWidth(), image.getHeight(), imagePathName);

		System.out.println("Konec tisku");

		return new RenderResult();
	}

	@Override
	public int print(final Graphics graphics, final PageFormat pf, final int pageIndex) throws PrinterException {
		System.out.println("TISK STRANY " + pageIndex + " - " + graphics.getClipBounds());
		if (pageIndex > 0) {
			return NO_SUCH_PAGE;
			// if (posledneTistenaStrana == pageIndex) return PAGE_EXISTS;
			// posledneTistenaStrana = pageIndex;
			// pocetPokusu --;
			// if (pocetPokusu < 0) return PAGE_EXISTS;
		}

		Graphics2D g = (Graphics2D) graphics;
		final Rendrovadlo rendrovadlo = factory.init(new Rendrovadlo(this));
		final RenderParams p = new RenderParams();
		p.roord = renderModel.getRoord();
		p.natacetDoSeveru = renderModel.getRenderSettings().isSrovnatDoSeveru();
		progressor.setText("Tisk");
		progressor.setTooltip("Probíhá tisk");
		p.pruhledne = false;

		final Coord roord = renderModel.getRoord();
		// g.scale(pf.getImageableWidth() / roord.getWidth(),
		// pf.getImageableHeight() / roord.getHeight());
		// PapirovaMetrika pm = renderModel.getPapirovaMetrika();
		final int papiroveMeritko = renderModel.getRenderSettings().getPapiroveMeritko();
		final double pixluNaMetr = roord.getPixluNaMetr();
		final double pixluNaMilimetrMapy = pixluNaMetr / 1000 * papiroveMeritko;
		final double dpi = pixluNaMilimetrMapy * 25.4;
		// double vzdalenostBodu = 1000 / pixluNaMilimetrMapy;

		final double scale = 72 / dpi;

		g.translate(pf.getImageableX(), pf.getImageableY());
		g.translate(pf.getImageableWidth() / 2, pf.getImageableHeight() / 2);
		g.scale(scale, scale);
		// g.translate(-pf.getImageableWidth() / scale / 2 , -pf.getImageableHeight() / scale / 2);

		g.translate(-roord.getWidth() / 2, -roord.getHeight() / 2);

		// g.setClip(0, 0, roord.getWidth(), roord.getHeight());
		g = (Graphics2D) g.create();

		System.out.println("Chce to po me tisk: " + ++citac);
		try {
			final BufferedImage bi = rendrovadlo.rendruj(p, progressor);
			g.drawImage(bi, 0, 0, null);
			// g.setColor(Color.YELLOW);
			// g.fillRect(-10000, -10000, 10000, 30000);
			g.setColor(Color.MAGENTA);
			g.setStroke(new BasicStroke(4));
			g.drawRect(0, 0, roord.getWidth(), roord.getHeight());
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
		return PAGE_EXISTS;
	}

}