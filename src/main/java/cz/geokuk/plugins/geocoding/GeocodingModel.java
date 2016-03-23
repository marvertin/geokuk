/**
 *
 */
package cz.geokuk.plugins.geocoding;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.HledaciSluzba;
import cz.geokuk.core.hledani.RefreshorVysledkuHledani;
import cz.geokuk.core.onoffline.OnofflineModelChangeEvent;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.refbody.ReferencniBodSeZmenilEvent;

/**
 * @author veverka
 *
 */
public class GeocodingModel extends Model0  {

	private final Hledac hledac = new Hledac();
	private HledaciSluzba hledaciSluzba;
	private boolean onlineMode;
	private Wgs referencniBod;

	public synchronized void spustHledani(final Wgs wgs, final RefreshorVysledkuHledani<Nalezenec> refreshor) {
		spustHledani(wgs.lat + "," + wgs.lon, refreshor);
	}

	public synchronized void spustHledani(final String coHledat, final RefreshorVysledkuHledani<Nalezenec> refreshor) {
		if (onlineMode) {
			final HledaciPodminka hledaciPodminka = new HledaciPodminka();
			if (referencniBod == null) {
				return;
			}
			hledaciPodminka.setStredHledani(referencniBod);
			hledaciPodminka.setVzorek(coHledat);
			hledaciSluzba.spustHledani(hledac, hledaciPodminka, refreshor);
		}
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.framework.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
		//asi není co dělat
	}


	public void onEvent(final OnofflineModelChangeEvent event) {
		onlineMode = event.isOnlineMOde();
	}

	public void inject(final HledaciSluzba hledaciSluzba) {
		this.hledaciSluzba = hledaciSluzba;
	}

	public void onEvent(final ReferencniBodSeZmenilEvent event) {
		referencniBod = event.wgs;
	}
}
