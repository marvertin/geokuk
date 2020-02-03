package cz.geokuk.plugins.kesoid.kind;

import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;

import cz.geokuk.framework.BeanBag;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.kesoid.importek.WptReceiver;

/**
 * Rozhraní, které musí implementovat každý kešoid plugin.
 * @author Martin
 *
 */
public interface KesoidPlugin {


	public GpxWptProcak createGpxWptProcak(GpxToWptContext ctx, GpxToWptBuilder builder, WptReceiver wpts);

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
	 * Sezam akcí, které mají být dány do kešoid menu.
	 */
	public List<Action> getSpecificKesoidMenuActions();

	/**
	 * Podrobí dané WPT filtrování. Jen Waypointy, které plugin stvořil jsou zde filtrovány.
	 * @param wpt
	 * @return
	 */
	public boolean filter(Wpt wpt);
	/**
	 * Registruje všechnysingletony v pluginu, především modely.
	 * Nemusí registrovat menu akce a toolbar komponenty.
	 * @param bb
	 */
	public void registerSingletons(BeanBag bb);

	/** Objekt, do kterého budou posílány posbírané i vyfiltrované waypointy pluginu */
	public WptSumarizer getWptSumarizer();

	/**
	 * Pokud daný waypoint nějakým způsobem omezuje umístění dalších nějakých waypointů, tak vrátí poloměr krhu,
	 * kam nesmí být tyto jiné waypointy umístěny. Například 161 m pro keše.
	 * @param wpt Waypoint, ke kterému se má kreslit kruh.
	 * @return Poloměr v metrech, nula, pokdu nic nobsazuje. nevrací záporné číslo.
	 */
	public int getPolomerObsazenosti(Wpt wpt);

	/**
	 * Přiřadí pluginu relativní pořadí. Podle pořadí pluginů se určuje:
	 *   - Pořadí načítání kešoidů, špatné pořadí může znehodnotit vyhodncení některých typů kešoidů. Například CGP musí být před waymarky.
	 *   - V gui se prvky příslušející pluginům zobrazují v tomto pořadí.
	 * @return
	 */
	public int getOrder();

}
