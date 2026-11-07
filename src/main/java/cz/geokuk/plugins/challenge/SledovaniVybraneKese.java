package cz.geokuk.plugins.challenge;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.kes.EKesDiffTerRating;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;

/** Předává plánovači keš, kterou program vybral (klik na mapě, detail); žádný vlastní klikací handler. */
public class SledovaniVybraneKese {

	private ChallengeModel model;

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void onEvent(final PoziceChangedEvent event) {
		if (model != null) {
			model.setVybranaKes(zWpt(event.poziceq.getWpt()));
		}
	}

	/** Vybraná keš z gc.com, nebo null (nic nevybráno, jiný kešoid, waypoint bez keše). */
	static VybranaKes zWpt(final Wpt wpt) {
		if (wpt == null) {
			return null;
		}
		final Kesoid kesoid = wpt.getKesoid();
		if (kesoid == null || !KesVstup.jeVhodny(kesoid)) {
			return null;
		}
		final Wgs w = kesoid.getMainWpt().getWgs();
		final Kes kes = (Kes) kesoid;
		final EKesDiffTerRating d = kes.getDifficulty();
		final EKesDiffTerRating t = kes.getTerrain();
		return new VybranaKes(kesoid.getIdentifier(), w.lat, w.lon, d == null ? -1 : d.ordinal(), t == null ? -1 : t.ordinal());
	}
}
