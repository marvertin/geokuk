package cz.geokuk.plugins.kesoid.kind.simplewaypoint;

import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.kind.*;
import cz.geokuk.util.procak.EProcakResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleWaypointGpxWptProcak implements GpxWptProcak {
	private static final String DEFAULT_SYM = "Waypoint";

	private final GpxToWptContext ctx;
	private final GpxToWptBuilder builder;


	@Override
	public EProcakResult process(final GpxWpt gpxwpt) {
		builder.expose(createSimpleWaypoint(gpxwpt).getMainWpt());
		return EProcakResult.DONE; // funguje jako výlevka, je jedno co poleme, ale musíme to už zpracovat
	}



	private SimpleWaypoint createSimpleWaypoint(final GpxWpt gpxwpt) {
		final Wpt wpt = ctx.createWpt(gpxwpt);
		wpt.setSym(gpxwpt.sym == null ? DEFAULT_SYM : gpxwpt.sym);

		final SimpleWaypoint simpleWaypoint = new SimpleWaypoint();
		simpleWaypoint.setIdentifier(gpxwpt.name);
		simpleWaypoint.setUrl(gpxwpt.link.href);
		simpleWaypoint.addWpt(wpt);
		simpleWaypoint.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));
		return simpleWaypoint;
	}



	@Override
	public void roundDone() {
	}


	@Override
	public void allDone() {
	}



}
