package cz.geokuk.plugins.kesoid.kind.munzee;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.kind.*;

public class MunzeePlugin implements KesoidPlugin {

	public static final Kepodr MUNZEE = Kepodr.of("munzee");

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new MunzeeGpxWptProcak(ctx, builder);
	}

	@Override
	public JKesoidDetail0 createDetail() {
		return new JMunzeeDetail();
	}

	@Override
	public Set<Kepodr> getKepodrs() {
		return ImmutableSet.of(MUNZEE);
	}

	@Override
	public PopiskyDef getPopiskyDef(final Kepodr kepodr) {
		return new MunzeePopiskyDef().doInit().build();
	}

	@Override
	public int getOrder() {
		return 4000;
	}

}
