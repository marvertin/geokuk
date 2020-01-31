package cz.geokuk.plugins.kesoid.kind.cgp;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.*;

public class CgpPlugin extends DefaultKesoidPlugin0 {

	public static final Kepodr CGP = Kepodr.of("cgp");

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder, final WptReceiver wpts) {
		return new CgpGpxWptProcak(ctx, builder, wpts);
	}

	@Override
	public JKesoidDetail0 createDetail() {
		return new JCgpDetail();
	}

	@Override
	public Set<Kepodr> getKepodrs() {
		return ImmutableSet.of(CGP);
	}

	@Override
	public PopiskyDef getPopiskyDef(final Kepodr kepodr) {
		return new CgpPopiskyDef().doInit().build();
	}

	@Override
	public int getOrder() {
		return 1000;
	}

}
