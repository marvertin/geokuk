package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.kind.*;

public class KesPlugin implements KesoidPlugin {

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new KesGpxWptProcak(ctx, builder);
	}

	@Override
	public JKesoidDetail0 createDetail() {
		return new JKesDetail();
	}

}
