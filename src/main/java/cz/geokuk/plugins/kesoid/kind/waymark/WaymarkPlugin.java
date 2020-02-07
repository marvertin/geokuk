package cz.geokuk.plugins.kesoid.kind.waymark;

import java.util.*;

import com.google.common.collect.ImmutableSet;

import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.*;

public class WaymarkPlugin extends DefaultKesoidPlugin0 {

	public static final Kepodr WAYMARK = Kepodr.of("waymark");

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder, final WptReceiver wpts) {
		return new WaymarkGpxWptProcak(ctx, builder, wpts);
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

	@Override
	public List<Action0> getPopupMenuActions(final Wpt wpt) {
		return Arrays.asList(
				new ZobrazNaWaymarkingComAction(wpt)
				);
	}

}
