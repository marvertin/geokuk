package cz.geokuk.plugins.kesoid.kind.munzee;

import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.kind.*;
import cz.geokuk.util.procak.EProcakResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MunzeeGpxWptProcak implements GpxWptProcak {
	private static final String MZ = "MZ";
	private static final String MU = "MU";

	private static final String GEOCACHE = "Geocache";
	private static final String GEOCACHE_FOUND = "Geocache Found";

	private final GpxToWptContext ctx;
	private final GpxToWptBuilder builder;


	@Override
	public EProcakResult process(final GpxWpt gpxwpt) {
		if (isMunzee(gpxwpt)) {
			final Munzee munzee = createMunzee(gpxwpt);
			ctx.expose(munzee.getMainWpt());
			return EProcakResult.DONE;
		} else {
			return EProcakResult.NEVER;
		}

	}

	private boolean isMunzee(final GpxWpt gpxWpt) {
		return (gpxWpt.name.startsWith(MZ) || gpxWpt.name.startsWith(MU)) && (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym));
	}



	private Munzee createMunzee(final GpxWpt gpxwpt) {
		final Munzee mz = new Munzee();
		mz.setIdentifier(gpxwpt.name);
		if (ctx.getGccomNick().name.equals(gpxwpt.groundspeak.placedBy)) {
			mz.setVztahx(EKesVztah.OWN);
		} else if (GEOCACHE_FOUND.equals(gpxwpt.sym)) {
			mz.setVztahx(EKesVztah.FOUND);
		} else {
			mz.setVztahx(EKesVztah.NORMAL);
		}
		mz.setUrl(gpxwpt.link.href);
		mz.setAuthor(gpxwpt.groundspeak.placedBy);
		mz.setHidden(gpxwpt.time);

		final Wpt wpt = builder.createWpt(gpxwpt, MunzeePlugin.MUNZEE);
		wpt.setNazev(gpxwpt.groundspeak.name);
		if (gpxwpt.name.startsWith(MZ)) {
			wpt.setSym("MZ " + odstranNadbytecneMezery(gpxwpt.groundspeak.type));
		} else {
			wpt.setSym(odstranNadbytecneMezery(gpxwpt.groundspeak.type));
		}

		mz.addWpt(wpt);
		mz.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));
		return mz;
	}


	/**
	 * @param aType
	 * @return
	 */
	private String odstranNadbytecneMezery(final String s) {
		return s == null ? null : s.replaceAll(" +", " ");
	}


	@Override
	public void roundDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void allDone() {
		// TODO Auto-generated method stub

	}



}
