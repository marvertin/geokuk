package cz.geokuk.plugins.kesoid.kind.cgp;

import cz.geokuk.plugins.kesoid.kind.*;

public class CgpPlugin implements KesoidPlugin {

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new CgpGpxWptProcak(ctx, builder);
	}

}
