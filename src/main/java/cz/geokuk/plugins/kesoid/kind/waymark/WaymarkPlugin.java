package cz.geokuk.plugins.kesoid.kind.waymark;

import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.kind.*;

public class WaymarkPlugin implements KesoidPlugin {

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new WaymarkGpxWptProcak(ctx, builder);
	}

	@Override
	public JKesoidDetail0 createDetail() {
		return new JWaymarkDetail();
	}

}
