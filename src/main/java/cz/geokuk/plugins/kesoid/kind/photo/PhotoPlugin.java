package cz.geokuk.plugins.kesoid.kind.photo;

import cz.geokuk.plugins.kesoid.KesFilter;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.kind.*;

public class PhotoPlugin implements KesoidPlugin {

	public void inject (final KesFilter kesFilter) {

	}

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new PhotoGpxWptProcak(ctx, builder);
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
}
