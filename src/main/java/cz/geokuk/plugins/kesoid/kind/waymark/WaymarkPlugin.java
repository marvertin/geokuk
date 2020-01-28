package cz.geokuk.plugins.kesoid.kind.waymark;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.kind.*;

public class WaymarkPlugin implements KesoidPlugin {

	public static final Kepodr WAYMARK = Kepodr.of("waymark");

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new WaymarkGpxWptProcak(ctx, builder);
	}

	@Override
	public JKesoidDetail0 createDetail() {
		return new JWaymarkDetail();
	}

	@Override
	public Set<Kepodr> getKepodrs() {
		return ImmutableSet.of(WAYMARK);
	}

	@Override
	public PopiskyDef getPopiskyDef(final Kepodr kepodr) {
		return new WaymarkPopiskyDef().doInit().build();
	}

	@Override
	public int getOrder() {
		return 3000;
	}

}
