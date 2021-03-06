package cz.geokuk.plugins.kesoid.kind.simplewaypoint;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.kind.*;
import cz.geokuk.util.procak.EProcakResult;

public class SimpleWaypointPlugin implements KesoidPlugin {

	public static final Kepodr SIMPLEWAYPOINT = Kepodr.of("simplewaypoint");

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		// to je ten interní, nějaký být musí a nesmí škodit, nezpracováváme a už pak nechceme.
		// Pravé naplnění se dělá jako sink z toho co zbude.
		return new GpxWptProcak() {

			@Override
			public void roundDone() {
			}

			@Override
			public EProcakResult process(final GpxWpt obj) {
				return EProcakResult.NEVER;
			}

			@Override
			public void allDone() {
			}
		};
	}

	@Override
	public JKesoidDetail0 createDetail() {
		return new JSimpleWaypointDetail();
	}

	@Override
	public Set<Kepodr> getKepodrs() {
		return ImmutableSet.of(SIMPLEWAYPOINT);
	}

	@Override
	public PopiskyDef getPopiskyDef(final Kepodr kepodr) {
		return new SimpleWaypointPopiskyDef().doInit().build();
	}

	@Override
	public int getOrder() {
		return 6000;
	}

}
