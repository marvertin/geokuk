package cz.geokuk.plugins.kesoid.kind;

import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;

/**
 * Rozhraní, které musí implementovat každý kešoid plugin.
 * @author Martin
 *
 */
public interface KesoidPlugin {

	public GpxWptProcak createGpxWptProcak(GpxToWptContext ctx, GpxToWptBuilder builder);

	public JKesoidDetail0 createDetail();
}
