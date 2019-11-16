package cz.geokuk.plugins.mapy.kachle.gui;

import java.awt.Component;
import java.awt.Graphics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.plugins.mapy.kachle.data.Ka;
import cz.geokuk.plugins.mapy.kachle.podklady.Priority;

public class JKachlovnikRendrovaci extends JKachlovnik {

	public interface Progressor {
		void setProgress(int value, int maxlue);
	}

	private static final Logger log = LogManager.getLogger(JKachlovnikRendrovaci.class.getSimpleName());

	private static final long serialVersionUID = -3170605712662727739L;
	private Progressor progressor;
	private int citacZpracovanychKachli;
	private int celkovyPocetKachliKtereRendruejeme;

	public JKachlovnikRendrovaci() {
		super("Renderovací kachlovník", Priority.STAHOVANI);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.core.coord.JSingleSlide0#render(java.awt.Graphics)
	 */
	@Override
	public void render(final Graphics g) throws InterruptedException {
		try {
			celkovyPocetKachliKtereRendruejeme = 0;
			citacZpracovanychKachli = 0;
			paintComponent(g);
			// if (true) return;
			vykreslovatokamzite = true;
			init(false);
			paint(g);
			final Component[] components = getComponents();
			celkovyPocetKachliKtereRendruejeme = components.length;
			System.out.println("waitNaDotazeni: start");
			for (final Component component : components) {
				if (component instanceof JKachle) {
					final JKachle kachle = (JKachle) component;
					System.out.println("waitNaDotazeni: " + kachle.getKaLoc());
					kachle.waitNaDotazeniDlazdice();
				}
			}
			System.out.println("waitNaDotazeni: stop");
		} finally {
			// KDyž končíme, třeba i výjimkou, rychle kachlím řekneme, že je nepotřebujeme
			// a ona se v mžiku vyprázdní front
			for (final Component component : getComponents()) {
				if (component instanceof JKachle) {
					final JKachle kachle = (JKachle) component;
					kachle.uzTeNepotrebuju();
				}
			}
			log.trace("Opoustim cekani");
		}
		// paint(g);
	}

	public void setProgressor(final Progressor progressor) {
		this.progressor = progressor;
	}

	@Override
	protected JKachle createJKachle(final Ka ka) {
		return new JKachleRendrovaci(this, ka);
	}
	// @Override
	// protected JKachle createKachle(KaAll plny, KachleModel kachleModel, boolean vykreslovatOkamzite, JKachlovnik jKachlovnik) {
	// JKachleRendrovaci jkachle = new JKachleRendrovaci(jKachlovnik);
	// jkachle.setKachle(new KachleRendrovaci(plny, kachleModel, vykreslovatOkamzite, jkachle));
	// return jkachle;
	// }

	@Override
	void kachleZpracovana(final JKachle jKachle) {
		++citacZpracovanychKachli;
		System.out.printf("CITACE: %d/%d\n", citacZpracovanychKachli, celkovyPocetKachliKtereRendruejeme);
		if (progressor != null) {
			progressor.setProgress(++citacZpracovanychKachli, celkovyPocetKachliKtereRendruejeme);
		}
	}

}
