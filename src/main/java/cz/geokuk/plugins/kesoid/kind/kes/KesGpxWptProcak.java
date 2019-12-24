package cz.geokuk.plugins.kesoid.kind.kes;

import java.util.HashMap;
import java.util.Map;

import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.Wpt.EZOrder;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.kind.*;
import cz.geokuk.util.procak.EProcakResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KesGpxWptProcak implements GpxWptProcak {


	private static final String GC = "GC";
	static final String GEOCACHE = "Geocache";
	static final String GEOCACHE_FOUND = "Geocache Found";
	private static final String DEFAULT_SYM = "Waypoint";

	private final GpxToWptContext ctx;
	private final GpxToWptBuilder builder;

	/** Zde shromažďujeme stvořené keše */
	private final Map<String, Kesoid> resultKesoidsByName = new HashMap<>();
	private boolean druheKolo;

	@Override
	public EProcakResult process(final GpxWpt gpxwpt) {
		if (isGeocache(gpxwpt)) {
			final Kes kes = createKes(gpxwpt);
			resultKesoidsByName.put(gpxwpt.name, kes);
			ctx.expose(kes.getFirstWpt());
			return EProcakResult.DONE;
		} else {
			// TODO dodatečné waypointy též pro waymarky
			final Kesoid kesoid = resultKesoidsByName.get(potentialGcCode(gpxwpt));
			// TODO přísnější test na to, co může být additional waypoint
			if (kesoid != null) {
				final Wpt wpt = createAditionalWpt(gpxwpt);
				if (wpt.getType() == EKesWptType.FINAL_LOCATION) {
					wpt.setZorder(EZOrder.FINAL);
					if (kesoid instanceof Kes) {
						final Kes kes = (Kes) kesoid;
						if (Wpt.TRADITIONAL_CACHE.equals(kes.getFirstWpt().getSym()) && Math.abs(kes.getFirstWpt().lat - wpt.lat) < 0.001 && Math.abs(kes.getFirstWpt().lon - wpt.lon) < 0.001) {
							log.debug("Vypouštíme finální waypointy tradičních keší na úvodních souřadnicích: {} {} {} {} {}", kes.getNazev(), kes.getFirstWpt().lat, wpt.lat, kes.getFirstWpt().lon,
									wpt.lon);
							return  EProcakResult.NEVER;
						} else {
							kes.setMainWpt(wpt);
							kesoid.addWpt(wpt);
						}
					} else {
						kesoid.addWpt(wpt);
					}
				} else {
					kesoid.addWpt(wpt);
				}
				ctx.expose(wpt);
				return EProcakResult.DONE;
			} else {
				return druheKolo ? EProcakResult.NEVER // když to neumíme teď, nebudeme to umět nikdy
						: EProcakResult.NEXT_ROUND; // teď to neumíme spárovat
			}

		}

	}

	/**
	 * Vyrobí kód odtržením prvních dvou znaků a přidání GC.
	 * @param gpxwpt
	 * @return
	 */
	private String potentialGcCode(final GpxWpt gpxwpt) {
		return "GC" + gpxwpt.name.substring(Math.min(2,  gpxwpt.name.length()));
	}


	@Override
	public void roundDone() {
		druheKolo = true;
	}

	private boolean isGeocache(final GpxWpt gpxWpt) {
		// TODO: Je špatné rozpoznávat geokeše podle prefixu GC - jsou systémy (Geocaching.su, OpenCaching, ...) které tento prefix nemají a přitom to jsou keše! [2016-04-09, Bohusz]
		return (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym)) && gpxWpt.groundspeak != null && gpxWpt.name.startsWith(GC);
	}

	private Kes createKes(final GpxWpt gpxwpt) {
		final Kes kes = new Kes();
		kes.setIdentifier(gpxwpt.name);
		kes.setAuthor(gpxwpt.groundspeak.placedBy);
		// kes.setState(gpxwpt.groundspeak.state);
		// kes.setCountry(gpxwpt.groundspeak.country);
		kes.setHidden(gpxwpt.time);
		kes.setHint(gpxwpt.groundspeak.encodedHints);

		kes.setTerrain(EKesDiffTerRating.parse(gpxwpt.groundspeak.terrain));
		kes.setDifficulty(EKesDiffTerRating.parse(gpxwpt.groundspeak.difficulty));
		kes.setSize(EKesSize.decode(gpxwpt.groundspeak.container));
		// kes.set(gpxwpt.groundspeak.);
		// kes.set(gpxwpt.groundspeak.);
		kes.setStatus(urciStatus(gpxwpt.groundspeak.archived, gpxwpt.groundspeak.availaible));
		kes.setVztahx(urciVztah(gpxwpt));
		kes.setUrl(gpxwpt.link.href);

		kes.setHodnoceni(gpxwpt.gpxg.hodnoceni);
		kes.setHodnoceniPocet(gpxwpt.gpxg.hodnoceniPocet);
		kes.setZnamka(gpxwpt.gpxg.znamka);
		kes.setBestOf(gpxwpt.gpxg.bestOf);
		kes.setFavorit(gpxwpt.gpxg.favorites);
		kes.setFoundTime(gpxwpt.gpxg.found);

		final Wpt wpt = builder.createWpt(gpxwpt, KesPlugin.KES);
		wpt.setSym(gpxwpt.groundspeak.type);
		wpt.setNazev(gpxwpt.groundspeak.name); // název hlavního waypointu shodný s názvem keše
		wpt.setZorder(EZOrder.FIRST);

		kes.addWpt(wpt);
		kes.setMainWpt(wpt);

		kes.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));
		return kes;
	}


	private Wpt createAditionalWpt(final GpxWpt gpxwpt) {
		final Wpt wpt = builder.createWpt(gpxwpt, KesPlugin.KESADWPT);
		wpt.setSym(gpxwpt.sym == null ? DEFAULT_SYM : gpxwpt.sym);
		final boolean rucnePridany = (gpxwpt.gpxg.flag & 1) == 0;
		wpt.setRucnePridany(rucnePridany);
		wpt.setZorder(EZOrder.KESWPT);
		return wpt;
	}

	private EKesVztah urciVztah(final GpxWpt gpxwpt) {
		if (gpxwpt.explicitneUrcenoVlastnictvi) {
			return EKesVztah.OWN;
		}
		if (ctx.getGccomNick().name.equals(gpxwpt.groundspeak.owner)) {
			return EKesVztah.OWN;
		}
		if (ctx.getGccomNick().id == gpxwpt.groundspeak.ownerid) {
			return EKesVztah.OWN;
		}
		if (ctx.getGccomNick().name.equals(gpxwpt.groundspeak.placedBy)) {
			return EKesVztah.OWN;
		} else if (GEOCACHE_FOUND.equals(gpxwpt.sym)) {
			return EKesVztah.FOUND;
		} else {
			return EKesVztah.NORMAL;
		}
	}

	private EKesStatus urciStatus(final boolean archived, final boolean availaible) {
		if (archived) {
			return EKesStatus.ARCHIVED;
		} else if (!availaible) {
			return EKesStatus.DISABLED;
		} else {
			return EKesStatus.ACTIVE;
		}
	}

	@Override
	public void allDone() {
		// TODO Auto-generated method stub

	}


}
