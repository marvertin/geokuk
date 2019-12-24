package cz.geokuk.plugins.kesoid.kind;

import java.util.Set;

import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;

/**
 * Rozhraní, které musí implementovat každý kešoid plugin.
 * @author Martin
 *
 */
public interface KesoidPlugin {

	public GpxWptProcak createGpxWptProcak(GpxToWptContext ctx, GpxToWptBuilder builder);

	public JKesoidDetail0 createDetail();


	public PopiskyDef getPopiskyDef(final Kepodr kepodr);

	public Set<Kepodr> getKepodrs();
}
