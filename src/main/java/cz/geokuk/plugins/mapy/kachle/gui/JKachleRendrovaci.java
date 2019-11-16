package cz.geokuk.plugins.mapy.kachle.gui;

import java.awt.Graphics;
import java.awt.Image;

import cz.geokuk.plugins.mapy.kachle.data.Ka;

public class JKachleRendrovaci extends JKachle {

	/**
	 * Celá tato třída je jen kvůli zmenšení paměti při rendrování na polovinu, jinak by nebyla potřeba.
	 */
	private static final long serialVersionUID = -4855904714968272822L;
	private Graphics grf;

	public JKachleRendrovaci(final JKachlovnik jKachlovnik, final Ka ka) {
		super(jKachlovnik, ka);

	}

	@Override
	protected synchronized void paintComponent(final Graphics aG) {
		super.paintComponent(aG);

		if (jeTamUzCelyObrazek()) {
			super.paintComponent(aG);
			uzTeNepotrebuju();
			grf = null;
		} else {
			grf = aG.create();
		}
	}

	@Override
	protected void ziskanPlnyObrazek(final Image img) {
		if (grf != null) {
			super.paintComponent(grf);
			uzTeNepotrebuju();
		}
	}
}
