package cz.geokuk.plugins.kesoid.kind.munzee;

import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.kind.*;

public class MunzeePlugin implements KesoidPlugin {

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new MunzeeGpxWptProcak(ctx, builder);
	}

	@Override
	public JKesoidDetail0 createDetail() {
		return new JMunzeeDetail();
	}

}
