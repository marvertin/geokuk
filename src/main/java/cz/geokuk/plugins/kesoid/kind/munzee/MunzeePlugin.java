package cz.geokuk.plugins.kesoid.kind.munzee;

import java.util.*;

import com.google.common.collect.ImmutableSet;

import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.*;

public class MunzeePlugin extends DefaultKesoidPlugin0 {

	public static final Kepodr MUNZEE = Kepodr.of("munzee");

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder, final WptReceiver wpts) {
		return new MunzeeGpxWptProcak(ctx, builder, wpts);
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

	@Override
	public List<Action0> getPopupMenuActions(final Wpt wpt) {
		return Arrays.asList(
				new ZobrazNaMunzeeCom(wpt)
				);
	}
}
