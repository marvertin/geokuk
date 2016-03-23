/**
 *
 */
package cz.geokuk.plugins.refbody;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.EventFirer;

/**
 * Mění událost změny sředu mapy a změnu pozice na událost jedinou, podle které se rohodují referenceři.
 *
 * @author veverka
 *
 */
public class HlidacReferencnihoBodu {

	private Poziceq	poziceq	= new Poziceq();
	private Wgs		stredMapy;
	private Object	minulyStredHledani;
	private Coord	moord;

	public void onEvent(final VyrezChangedEvent aEvent) {
		moord = aEvent.getMoord();
		setStredMapy(aEvent.getMoord().getMoustred().toWgs());
		fairujSpolecnou(aEvent.getEventFirer());
	}

	public void onEvent(final PoziceChangedEvent aEvent) {
		poziceq = aEvent.poziceq;
		fairujSpolecnou(aEvent.getEventFirer());
	}

	private void setStredMapy(final Wgs wgs) {
		stredMapy = wgs;
	}

	private void fairujSpolecnou(final EventFirer eventFirer) {
		final Wgs referencniBod = getReferencniBod();
		if (minulyStredHledani != null && minulyStredHledani.equals(referencniBod)) {
			return;
		}
		eventFirer.fire(new ReferencniBodSeZmenilEvent(referencniBod, moord));
		minulyStredHledani = referencniBod;
	}

	private Wgs getReferencniBod() {
		if (poziceq.isNoPosition()) {
			return stredMapy;
		} else {
			return poziceq.getWgs();
		}
	}

}
