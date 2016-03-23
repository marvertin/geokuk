package cz.geokuk.core.coord;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import cz.geokuk.framework.JPrekryvnik;

/**
 * @author veverka
 *
 */
public class JCoordPrekryvnik0 extends JPrekryvnik {

	private static final long serialVersionUID = -4498307548625868036L;

	private Coord soord = Coord.prozatimniInicializacni();

	private final List<JSingleSlide0> slides = new ArrayList<>();

	/**
	 * @return the coord
	 */
	protected Coord getSoord() {
		return soord;
	}

	public JCoordPrekryvnik0() {
		registerEvents();
	}
	/**
	 *
	 */
	private void registerEvents() {
		// Listener zajístí, že za zapíše šířka i výška do použitého souřadničníku
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				setSoord(soord.derive(getSize()));
			}
		});
	}
	/* (non-Javadoc)
	 * @see cz.geokuk.framework.JPrekryvnik#add(java.awt.Component)
	 */
	@Override
	public Component add(Component aComp) {
		if (aComp instanceof JSingleSlide0) {
			// Nastavit coordináty do slidu, aby je mohli konkrétníci čerpat
			JSingleSlide0 slide = (JSingleSlide0) aComp;
			slides.add(slide);
		}
		return super.add(aComp);
	}

	/**
	 * @param newSoord
	 */
	protected void setSoord(Coord newSoord) {
		if (newSoord.equals(soord)) return; // je to to samé
		soord = newSoord;
		reinicializujVyrezy();
	}

	/**
	 *
	 */
	private void reinicializujVyrezy() {
		// Raději ve dvou cyklech, aby už ho měli správně, když se začne reinicializovat
		for (JSingleSlide0 slide : slides) {
			slide.setSoord(soord);
		}
		for (JSingleSlide0 slide : slides) {
			slide.onVyrezChanged();
		}
	}


}
