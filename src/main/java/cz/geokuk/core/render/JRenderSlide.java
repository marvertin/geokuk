/**
 *
 */
package cz.geokuk.core.render;

import java.awt.*;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.program.FConst;
import cz.geokuk.core.render.RenderModel.Dvoj;

/**
 * @author Martin Veverka
 *
 */
public class JRenderSlide extends JSingleSlide0 {

	/**
	 *
	 */
	private static final long serialVersionUID = -4370714043446396996L;

	private RenderModel renderModel;

	private Coord moord;

	public JRenderSlide() {

		if (FConst.ZAKAZAT_PRIPRAVOVANOU_FUNKCIONALITU) {
			setVisible(false);
		}
	}

	public void onEvent(final PripravaRendrovaniEvent event) {
		renderModel = event.getModel();
		setVisible(event.getStavRendrovani() != EStavRendrovani.NIC);
		repaint();
	}

	public void onEvent(final VyrezChangedEvent event) {
		moord = event.getMoord();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(final Graphics aG) {
		final Graphics2D g = (Graphics2D) aG.create();
		// paintRožek(g, renderModel.SZ);
		// paintRožek(g, renderModel.SV);
		// paintRožek(g, renderModel.JZ);
		// paintRožek(g, renderModel.JV);
		final int a = getWidth();
		final int b = getHeight();

		// Nakreslíme kříž vodorovný středový
		// g.drawLine(0, b/2, a, b/2);
		// g.drawLine(a/2, 0, a/2, b);
		// Vypíšeme o jaký se jedná úhel
		// g.drawString((uhel / Math.PI * 180 ) + "", a/2, b/2);

		// if (! probihaNastavovaniRendrovani) return;
		final double uhel = -renderModel.getOKolikNatacet();
		// Dvoj d = renderModel.spoctiOrezavaciOblednik(a, b);

		// // Vykreslíme bez transformací
		// g.setColor(Color.BLUE);
		// draw(g, d);

		g.drawLine(0, 0, getWidth(), getHeight());
		g.drawLine(0, getHeight(), getWidth(), 0);
		// Zajístíme, že se bude otáčet kolem středu
		g.translate(a / 2, b / 2);
		// g.setColor(Color.CYAN);
		// draw(g, d);

		// zajistíme rotaci o zadaný úhel
		g.rotate(uhel);
		g.setColor(Color.MAGENTA);
		g.setStroke(new BasicStroke(3));
		Dvoj orezavaci = new Dvoj();
		orezavaci = renderModel.spoctiOrezavaciOblednik();
		final int x0 = (int) -orezavaci.a / 2;
		final int y0 = (int) -orezavaci.b / 2;
		g.drawRect(x0, y0, (int) orezavaci.a, (int) orezavaci.b);
		// malý ořezávací obdélník
		final Dvoj d = renderModel.spoctiMalyOrezavaciOblednik();
		if (renderModel.getCurrentMoumer() != renderModel.getRenderedMoumer()) {
			g.drawRect(x0 + 50, y0 + 50, (int) d.a, (int) d.b);
		}

		// křížek
		g.drawLine(-20, -20, 20, 20);
		g.drawLine(-20, 20, 20, -20);

		g.drawString(d.a + " " + d.b + "", 0, 0);

		final EWhatRender whatRender = renderModel.getRenderSettings().getWhatRender();

		if (whatRender == EWhatRender.GOOGLE_EARTH) {
			// dlaždice pro KMZ
			drawDlazdice(g, orezavaci);
		}
		if (whatRender == EWhatRender.OZI_EXPLORER) {
			drawKalibody(aG);
		}
	}

	// /**
	// * @param g
	// * @param sZ
	// */
	// private void paintRožek(Graphics2D gg, Mou mou) {
	// if (mou == null) return;
	// Graphics2D g = (Graphics2D) gg.create();
	// g.setStroke(new BasicStroke(3));
	// g.setColor(Color.GREEN);
	//
	// Point p = getSoord().transform(mou);
	// // g.translate(p.x, p.y);
	// g.drawLine(p.x-20, p.y-20, p.x+20, p.y+20);
	// g.drawLine(p.x-20, p.y+20, p.x+20, p.y-20);
	// //g.fillOval(p.x-20, p.y-20, 40, 40);
	//
	// }

	/**
	 * @param g
	 * @param d
	 */
	private void drawDlazdice(final Graphics ag, final Dvoj d) {
		if (renderModel.getRenderSettings().getWhatRender() != EWhatRender.GOOGLE_EARTH) {
			return;
		}
		final Graphics g = ag.create();
		// g.translate(- getWidth()/2, -getHeight()/2);
		final DlazdicovaMetrikaXY dlas = renderModel.spoctiDlazdicovouMetriku();
		// System.out.println("33DLADLADLA: " + dlas.xx.dlaRoztec + " " + dlas.yy.dlaRoztec);
		// System.out.println("44DLADLADLA: " + dlas.xx.dlaRoztec + " " + dlas.yy.dlaRoztec);

		final double quotX = d.a / dlas.xx.sizeCele;
		final double quotY = d.b / dlas.yy.sizeCele;
		if (quotX * dlas.xx.dlaSize < 5 || quotY * dlas.yy.dlaSize < 5) {
			return;
		}
		if (quotX * dlas.xx.dlaRoztec < 5 || quotY * dlas.yy.dlaRoztec < 5) {
			return;
		}

		for (final DlazdicovaMetrikaXY.Dlazdice dla : dlas) {
			// System.out.println("DLA: " + dla.xn + " " + dla.yn + " " + dla.xs + " " + dla.ys);
			final Graphics2D gg = (Graphics2D) g.create();
			gg.scale(quotX, quotY);
			gg.translate(-dlas.xx.sizeCele / 2, -dlas.yy.sizeCele / 2);
			gg.translate(dla.xs, dla.ys);
			gg.drawRect(-dla.dim.width / 2, -dla.dim.height / 2, dla.dim.width, dla.dim.height);
		}
		// System.out.println("DLAHO: ");
		// System.out.println("22DLADLADLA: " + dlas.xx.dlaSize + " " + dlas.yy.dlaSize);

	}

	private void drawKalibody(final Graphics aG) {
		final Graphics g = aG.create();
		g.setColor(Color.MAGENTA);
		for (final Wgs wgs : renderModel.spocitejKalibracniBody()) {
			final Point p = moord.transform(wgs.toMou());
			g.fillOval(p.x - 15, p.y - 15, 30, 30);
		}

	}

	// public void inject(RenderModel stahovaciModel) {
	// this.stahovaciModel = stahovaciModel;
	// }

}
