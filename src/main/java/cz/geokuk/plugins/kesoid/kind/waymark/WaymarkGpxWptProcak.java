package cz.geokuk.plugins.kesoid.kind.waymark;

import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.*;
import cz.geokuk.util.procak.EProcakResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WaymarkGpxWptProcak implements GpxWptProcak {
	private static final String WAYMARK = "Waymark";
	private static final String WM = "WM";

	private static final String GEOCACHE = "Geocache";
	private static final String GEOCACHE_FOUND = "Geocache Found";

	private final GpxToWptContext ctx;
	private final GpxToWptBuilder builder;
	private final WptReceiver wpts;


	@Override
	public EProcakResult process(final GpxWpt gpxwpt) {

		Wpt wm;
		if (isWaymarkNormal(gpxwpt)) {
			wm = createWaymarkNormal(gpxwpt);
		} else if (isWaymarkGeoget(gpxwpt)) {
			wm = createWaymarkGeoget(gpxwpt);
		} else {
			return EProcakResult.NEVER;
		}
		wpts.expose(wm);
		return EProcakResult.DONE;
	}


	@Override
	public void roundDone() {
	}

	private boolean isWaymarkNormal(final GpxWpt gpxWpt) {
		return WAYMARK.equals(gpxWpt.sym) && gpxWpt.name.startsWith(WM);
	}

	private boolean isWaymarkGeoget(final GpxWpt gpxWpt) {
		return gpxWpt.name.startsWith(WM) && (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym));
	}


	private Wpt createWaymarkGeoget(final GpxWpt gpxwpt) {
		final Waymark wm = new Waymark();
		wm.setIdentifier(gpxwpt.name);
		if (ctx.getGccomNick().name.equals(gpxwpt.groundspeak.placedBy)) {
			wm.setVztahx(EKesVztah.OWN);
		} else {
			wm.setVztahx(EKesVztah.NORMAL);
		}
		wm.setUrl(gpxwpt.link.href);
		wm.setAuthor(gpxwpt.groundspeak.placedBy);
		wm.setHidden(gpxwpt.time);

		final Wpti wpti = createWpt(gpxwpt);
		wpti.setNazev(gpxwpt.groundspeak.name);
		wpti.setSym(odstranNadbytecneMezery(gpxwpt.groundspeak.type));

		wm.addWpt(wpti);
		wm.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));
		return wpti;
	}

	private Wpt createWaymarkNormal(final GpxWpt gpxwpt) {
		final Waymark wm = new Waymark();
		wm.setIdentifier(gpxwpt.name);
		if (gpxwpt.groundspeak != null) {
			if (ctx.getGccomNick().name.equals(gpxwpt.groundspeak.placedBy)) {
				wm.setVztahx(EKesVztah.OWN);
			} else {
				wm.setVztahx(EKesVztah.NORMAL);
			}
			wm.setAuthor(gpxwpt.groundspeak.placedBy);
		} else {
			wm.setVztahx(EKesVztah.NORMAL);
		}
		wm.setUrl(gpxwpt.link.href);

		final Wpti wpti = createWpt(gpxwpt);
		wpti.setNazev(gpxwpt.link.text);
		wpti.setSym(odstranNadbytecneMezery(gpxwpt.type));

		wm.addWpt(wpti);
		wm.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));

		return wpti;
	}


	private Wpti createWpt(final GpxWpt gpxwpt) {
		return builder.createWpt(gpxwpt, WaymarkPlugin.WAYMARK);
	}

	/**
	 * @param aType
	 * @return
	 */
	private String odstranNadbytecneMezery(final String s) {
		return s == null ? null : s.replaceAll(" +", " ");
	}


	@Override
	public void allDone() {
		// TODO Auto-generated method stub

	}

}
