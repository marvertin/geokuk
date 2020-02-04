package cz.geokuk.plugins.kesoid.kind.kes;

import java.util.HashMap;
import java.util.Map;

import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.Wpt.EZOrder;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
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
	private final WptReceiver wpts;


	/** Zde shromažďujeme stvořené keše */
	private final Map<String, Kesoid> resultKesoidsByName = new HashMap<>();
	private boolean druheKolo;

	@Override
	public EProcakResult process(final GpxWpt gpxwpt) {
		if (isGeocache(gpxwpt)) {
			final Kes kes = createKes(gpxwpt);
			resultKesoidsByName.put(gpxwpt.name, kes);
			wpts.expose(kes.getFirstWpt());
			return EProcakResult.DONE;
		} else {
			// TODO dodatečné waypointy též pro waymarky
			final Kesoid kesoid = resultKesoidsByName.get(potentialGcCode(gpxwpt));
			// TODO přísnější test na to, co může být additional waypoint
			if (kesoid != null) {
				final Wpti wpti = createAditionalWpt(gpxwpt);
				if (EKesWptType.decode(wpti.getSym()) == EKesWptType.FINAL_LOCATION) {
					wpti.setZorder(EZOrder.FINAL);
					if (kesoid instanceof Kes) {
						final Kes kes = (Kes) kesoid;
						// // TODO [veverka] Tady toto je dost grozné -- 4. 2. 2020 8:47:34 veverka
						final Wpti firstWpt = (Wpti) kes.getFirstWpt();
						if (Wpti.TRADITIONAL_CACHE.equals(firstWpt.getSym()) && Math.abs(firstWpt.lat - wpti.lat) < 0.001 && Math.abs(firstWpt.lon - wpti.lon) < 0.001) {
							log.debug("Vypouštíme finální waypointy tradičních keší na úvodních souřadnicích: {} {} {} {} {}", kes.getNazev(), firstWpt.lat, wpti.lat, firstWpt.lon,
									wpti.lon);
							return  EProcakResult.NEVER;
						} else {
							kes.setMainWpt(wpti);
							kesoid.addWpt(wpti);
						}
					} else {
						kesoid.addWpt(wpti);
					}
				} else {
					kesoid.addWpt(wpti);
				}
				wpts.expose(wpti);
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

		final Wpti wpti = builder.createWpt(gpxwpt, KesPlugin.KES);
		wpti.setSym(gpxwpt.groundspeak.type);
		wpti.setNazev(gpxwpt.groundspeak.name); // název hlavního waypointu shodný s názvem keše
		wpti.setZorder(EZOrder.FIRST);

		kes.addWpt(wpti);
		kes.setMainWpt(wpti);

		kes.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));
		return kes;
	}


	private Wpti createAditionalWpt(final GpxWpt gpxwpt) {
		final Wpti wpti = builder.createWpt(gpxwpt, KesPlugin.KESADWPT);
		wpti.setSym(gpxwpt.sym == null ? DEFAULT_SYM : gpxwpt.sym);
		final boolean rucnePridany = (gpxwpt.gpxg.flag & 1) == 0;
		wpti.setRucnePridany(rucnePridany);
		wpti.setZorder(EZOrder.KESWPT);
		return wpti;
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
