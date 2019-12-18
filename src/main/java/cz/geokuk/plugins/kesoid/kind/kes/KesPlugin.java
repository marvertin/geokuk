package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoid.kind.*;

public class KesPlugin implements KesoidPlugin {

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new KesGpxWptProcak(ctx, builder);
	}

}
