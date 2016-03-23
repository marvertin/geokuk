package cz.geokuk.core.render;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.Dlg;
import cz.geokuk.framework.Progressor;

public class GoogleEarthRenderSwingWorker extends RendererSwingWorker0 {

	GoogleEarthRenderSwingWorker() {
	}

	@Override
	protected RenderResult doInBackground() throws Exception {
		System.out.println("Ukladam KMZ");
		final File file = computeFileName();
		if (!Dlg.prepsatSoubor(file)) {
			return null;
		}
		try {
			final KmzWriter kmzwrt = new KmzWriter(file, renderModel.getRenderSettings().getKmzFolder().getText(), renderModel.getRenderSettings().getKmzFolderDescription());
			final RenderParams pp = new RenderParams();
			pp.roord = renderModel.createRenderedCoord();
			final EImageType imageType = renderModel.getRenderSettings().getImageType();
			pp.pruhledne = imageType.isUmoznujePruhlednost();
			final Coord coordCely = pp.roord;
			int citac = 0;
			// int xDlazdice = 0, yDlazdice = 0;
			final DlazdicovaMetrikaXY dlas = renderModel.spoctiDlazdicovouMetriku();
			progressor.setMax(dlas.getPcoetDlazdic() * Rendrovadlo.KOLIK_PROGRESUJEME_NA_KACHLICH);
			// System.out.println("DLADLADLA: " + xDlaSize + " " + yDlaSize);
			for (final DlazdicovaMetrikaXY.Dlazdice dla : dlas) {
				// System.out.println("DLA: " + dla.xn + " " + dla.yn + " " + dla.xs + " " + dla.ys);
				final RenderParams p = new RenderParams();
				p.natacetDoSeveru = renderModel.getRenderSettings().isSrovnatDoSeveru();
				p.roord = pp.roord.derive(coordCely.transform(new Point(dla.xs, dla.ys)), new Dimension(dla.dim.width, dla.dim.height));
				p.pruhledne = imageType.isUmoznujePruhlednost();
				p.drawOrder = renderModel.getRenderSettings().getKmzDrawOrder();
				progressor.setText(String.format("Rendruji %d/%d", ++citac, dlas.getPcoetDlazdic()));
				renderDlazdice(kmzwrt, p, dla.xn, dla.yn, progressor);
			}

			kmzwrt.finish();
			System.out.println("Konec rendrovani");
			final RenderResult result = new RenderResult();
			result.file = file;
			return result;
		} catch (final Exception e) {
			try {
				file.delete();
			} catch (final Exception e1) {
				e.fillInStackTrace();
			}
			throw e;
		}
	}

	private void renderDlazdice(final KmzWriter kmzwrt, final RenderParams p, final int xDlazdice, final int yDlazdice, final Progressor progressor)
			throws InterruptedException, IOException, ParserConfigurationException, TransformerException {
		final KmzParams kmzpar = new KmzParams();
		kmzpar.imageType = renderModel.getRenderSettings().getImageType();

		final Rendrovadlo rendrovadlo = factory.init(new Rendrovadlo(this));
		final BufferedImage image = rendrovadlo.rendruj(p, progressor);

		final Coord roord = p.roord;
		// TODO kontrola souřadnic zde býti musí, zjištění chyby, případné průměrování
		final Mou SZ = roord.transform(new Point(0, 0));
		@SuppressWarnings("unused")
		final Mou SV = roord.transform(new Point(p.roord.getWidth(), 0));
		@SuppressWarnings("unused")
		final Mou JZ = roord.transform(new Point(0, p.roord.getHeight()));
		final Mou JV = roord.transform(new Point(p.roord.getWidth(), p.roord.getHeight()));
		kmzpar.sever = SZ.toWgs().lat;
		kmzpar.jih = JV.toWgs().lat;
		kmzpar.zapad = SZ.toWgs().lon;
		kmzpar.vychod = JV.toWgs().lon;
		kmzpar.xDlazdice = xDlazdice;
		kmzpar.yDlazdice = yDlazdice;
		if (!renderModel.getRenderSettings().isSrovnatDoSeveru()) {
			kmzpar.rotation = roord.computNataceciUhel() * 180 / Math.PI;
		}
		kmzpar.drawOrder = p.drawOrder;
		kmzwrt.appendDlazdici(image, kmzpar);

	}

	private File computeFileName() throws IOException {
		File file;
		{
			final File kmzDir = renderModel.getOutputFolder();
			final String pureName = renderModel.getRenderSettings().getPureFileName().getText();
			kmzDir.mkdirs();
			file = new File(kmzDir, pureName + ".kmz").getCanonicalFile();
		}
		return file;
	}

}