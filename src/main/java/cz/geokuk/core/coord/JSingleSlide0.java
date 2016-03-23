/**
 *
 */
package cz.geokuk.core.coord;

import java.awt.Graphics;

import cz.geokuk.framework.Factory;
import cz.geokuk.framework.JSlide0;

/**
 * Předek všech jednoduchých, tedy nesložených slidů
 */
public abstract class JSingleSlide0 extends JSlide0 {

	private static final long	serialVersionUID	= 8758817189971703053L;

	private Coord				soord;

	protected Factory			factory;

	/**
	 * Potomek musí vytvořit novou instanci slidu, který bude rendrovatelný. Obvykle postačí, když vytvoří prázdnou instanci své vlastní třídy. Pokud vrátí null, nic se rendrovat nebude
	 *
	 * @return
	 */
	public JSingleSlide0 createRenderableSlide() {
		return null;
	}

	/**
	 * @return the coord
	 */
	public Coord getSoord() {
		assert soord != null;
		return soord;
	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public EJakOtacetPriRendrovani jakOtacetProRendrovani() {
		return EJakOtacetPriRendrovani.GRAPH2D;
	}

	// protected final void onEvent(VyrezChangedEvent0 event ) {
	//
	// }

	public void render(final Graphics g) throws InterruptedException {
		paintComponent(g);
	}

	/**
	 * @param coord
	 *            the coord to set
	 */
	public void setSoord(final Coord soord) {
		assert soord != null;
		this.soord = soord;
	}

	protected boolean isSoordInitialized() {
		return soord != null;
	}

	protected void onVyrezChanged() {
	}

}
