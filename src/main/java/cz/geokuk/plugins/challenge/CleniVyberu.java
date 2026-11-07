package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.concurrent.Executor;

import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.kes.EKesDiffTerRating;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;
import cz.geokuk.util.index2d.Indexator;

/**
 * Všechny keše z indexu, které patří do zvýrazněného sektoru 360° nebo políčka D/T. Nezávisí na výřezu mapy, takže se staví jednou pro výběr (mimo EDT, když je k dispozici
 * vlákno) a posun mapy už jen filtruje tento seznam.
 */
final class CleniVyberu {

	private static final int KROK_KONTROLY = 4096;

	final PrehledChallenge prehled;
	final Indexator<Wpt> indexator;
	final VyberZvyrazneni vyber;
	private volatile Wpt[] clenove;
	private volatile boolean zruseno;

	private CleniVyberu(final PrehledChallenge prehled, final Indexator<Wpt> indexator, final VyberZvyrazneni vyber) {
		this.prehled = prehled;
		this.indexator = indexator;
		this.vyber = vyber;
	}

	/** Zahájí stavbu; bez vlákna se postaví hned. Po dokončení zavolá poHotovo. */
	static CleniVyberu spust(final PrehledChallenge prehled, final Indexator<Wpt> indexator, final VyberZvyrazneni vyber, final Executor stavba, final Runnable poHotovo) {
		final CleniVyberu c = new CleniVyberu(prehled, indexator, vyber);
		if (stavba == null) {
			c.postav();
		} else {
			stavba.execute(() -> {
				c.postav();
				if (!c.zruseno && poHotovo != null) {
					poHotovo.run();
				}
			});
		}
		return c;
	}

	boolean jeStejny(final PrehledChallenge p, final Indexator<Wpt> i, final VyberZvyrazneni v) {
		return prehled == p && indexator == i && vyber.equals(v);
	}

	/** Seznam členů, nebo null, dokud se staví. */
	Wpt[] clenove() {
		return clenove;
	}

	boolean jeZrusena() {
		return zruseno;
	}

	void zrus() {
		zruseno = true;
	}

	private void postav() {
		Wpt[] pole = new Wpt[256];
		int n = 0;
		int projito = 0;
		for (final java.util.Iterator<Wpt> it = indexator.bound(BoundingRect.ALL).stream().iterator(); it.hasNext();) {
			if (++projito % KROK_KONTROLY == 0 && zruseno) {
				return;
			}
			final Wpt wpt = it.next();
			final Kesoid kesoid = wpt.getKesoid();
			if (kesoid == null || wpt != kesoid.getMainWpt() || !KesVstup.jeVhodny(kesoid) || !patri(prehled, vyber, kesoid, wpt)) {
				continue;
			}
			if (n == pole.length) {
				pole = Arrays.copyOf(pole, n * 2);
			}
			pole[n++] = wpt;
		}
		clenove = Arrays.copyOf(pole, n);
	}

	/** Patří keš do zvýrazněného sektoru (a oblasti challenge) nebo políčka? */
	private static boolean patri(final PrehledChallenge p, final VyberZvyrazneni vyber, final Kesoid kesoid, final Wpt wpt) {
		if (vyber.sektor >= 0) {
			final double lat = wpt.getWgs().lat;
			final double lon = wpt.getWgs().lon;
			return p.sektorBodu(lat, lon) == vyber.sektor && p.jeVOblasti(lat, lon);
		}
		final Kes kes = (Kes) kesoid;
		final EKesDiffTerRating d = kes.getDifficulty();
		final EKesDiffTerRating t = kes.getTerrain();
		return d != null && t != null && vyber.stitek.equals(p.stitekPolicka(d.ordinal(), t.ordinal()));
	}
}
