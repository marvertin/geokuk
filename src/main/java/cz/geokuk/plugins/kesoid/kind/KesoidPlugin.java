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

	/**
	 * Přiřadí pluginu relativní pořadí. Podle pořadí pluginů se určuje:
	 *   - Pořadí načítání kešoidů, špatné pořadí může znehodnotit vyhodncení některých typů kešoidů. Například CGP musí být před waymarky.
	 *   - V gui se prvky příslušející pluginům zobrazují v tomto pořadí.
	 * @return
	 */
	public int getOrder();
}
