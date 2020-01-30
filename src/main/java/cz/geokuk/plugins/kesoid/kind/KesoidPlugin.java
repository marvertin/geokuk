package cz.geokuk.plugins.kesoid.kind;

import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

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
	 * Seznam komponent, které plugin dá do tollbaru.
	 * Komponenty musí být malé, aby tam rozumně vešly.
	 * Komponenty jso upřidány vždy, plugin musí komponenty zneviditelnit, pokud nejsou k dispozici pro tento plugin rozumná data, aby se nevyčerpal prostor pro toolbar,
	 * když je geokuk používán jen určitým specifickým způsobem.
	 * Většina komponent na toolbaru odpovídá filtru. Akce se řeší pře hlavní nebo kontextové menu s odpovídajícími zkratkovými kláveami.
	 * @return Seznam komponent. Vrací prázdný seznam, pokud není žádná komponenta potřeba.
	 */
	public List<JComponent> getSpecificToolbarComponents();

	/**
	 * Přiřadí pluginu relativní pořadí. Podle pořadí pluginů se určuje:
	 *   - Pořadí načítání kešoidů, špatné pořadí může znehodnotit vyhodncení některých typů kešoidů. Například CGP musí být před waymarky.
	 *   - V gui se prvky příslušející pluginům zobrazují v tomto pořadí.
	 * @return
	 */
	public int getOrder();
}
