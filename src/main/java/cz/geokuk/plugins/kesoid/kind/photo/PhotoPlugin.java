package cz.geokuk.plugins.kesoid.kind.photo;

import cz.geokuk.plugins.kesoid.KesFilter;
import cz.geokuk.plugins.kesoid.kind.*;

public class PhotoPlugin implements KesoidPlugin {

	public void inject (final KesFilter kesFilter) {

	}

	@Override
	public GpxWptProcak createGpxWptProcak(final GpxToWptContext ctx, final GpxToWptBuilder builder) {
		return new PhotoGpxWptProcak(ctx, builder);
	}
}
