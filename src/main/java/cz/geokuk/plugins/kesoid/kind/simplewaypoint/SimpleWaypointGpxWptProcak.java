package cz.geokuk.plugins.kesoid.kind.simplewaypoint;

import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.Wpti;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.*;
import cz.geokuk.util.procak.EProcakResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleWaypointGpxWptProcak implements GpxWptProcak {

	private final GpxToWptContext ctx;
	private final GpxToWptBuilder builder;
	private final WptReceiver wpts;

	@Override
	public EProcakResult process(final GpxWpt gpxwpt) {
		wpts.expose(createSimpleWaypoint(gpxwpt));
		return EProcakResult.DONE; // funguje jako výlevka, je jedno co poleme, ale musíme to už zpracovat
	}



	private Wpt createSimpleWaypoint(final GpxWpt gpxwpt) {
		final Wpti wpti = builder.createWpt(gpxwpt, SimpleWaypointPlugin.SIMPLEWAYPOINT);
		wpti.setSym(gpxwpt.sym == null ? WptDefaults.DEFAULT_SYM : gpxwpt.sym);

		final SimpleWaypoint simpleWaypoint = new SimpleWaypoint();
		simpleWaypoint.setIdentifier(gpxwpt.name);
		simpleWaypoint.setUrl(gpxwpt.link.href);
		simpleWaypoint.addWpt(wpti);
		simpleWaypoint.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));
		return wpti;
	}



	@Override
	public void roundDone() {
	}


	@Override
	public void allDone() {
	}



}
