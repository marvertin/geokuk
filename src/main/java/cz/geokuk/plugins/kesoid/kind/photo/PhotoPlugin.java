package cz.geokuk.plugins.kesoid.kind.photo;

import java.util.*;

import com.google.common.collect.ImmutableSet;

import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;
import cz.geokuk.plugins.kesoid.kind.*;

public class PhotoPlugin extends DefaultKesoidPlugin0 {

	public static final Kepodr PHOTO = Kepodr.of("photo");

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder, final WptReceiver wpts) {
		return new PhotoGpxWptProcak(ctx, builder, wpts);
	}

	@SuppressWarnings("serial")
	@Override
	public JKesoidDetail0 createDetail() {
		return new JKesoidDetail0() {

			@Override
			public void napln(final Wpt wpt) {
			}
		};
	}


	@Override
	public Set<Kepodr> getKepodrs() {
		return ImmutableSet.of(PHOTO);
	}

	@Override
	public PopiskyDef getPopiskyDef(final Kepodr kepodr) {
		return new PhotoPopiskyDef().doInit().build();
	}

	@Override
	public int getOrder() {
		return 5000;
	}

	@Override
	public List<Action0> getPopupMenuActions(final Wpt wpt) {
		return Arrays.asList(
				new ZobrazNaUrlSpojenemSFotkouAction(wpt)
				);
	}
}
