package cz.geokuk.plugins.kesoid;

import java.io.File;

import cz.geokuk.core.coordinates.Uchopenec;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;
import cz.geokuk.plugins.kesoid.kind.KesoidPlugin;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;

/**
 * Waypoint je bod mající souřadnice a další atributy.
 * Rozhraní obsahuje metody pro ostatní části geokuku mimo kešoidových pluginů.
 * Proto poskytuje převážně jen metody poskytující nějaká data o waypointu.
 * Jednotlivé pluginy pak mohou mít různé implementace různých waypointů.
 * @author veverka
 *
 */
public interface Wpt extends Uchopenec {
	/**
	 * Umísťuje se do fronty, aby se poznalo, že je konec.
	 * Nic se na tom nevolá, jen se porovnává na objektovou identitu.
	 */
	public static Wpt ZARAZKA = new Wpti();


	public static enum EZOrder {
		OTHER, KESWPT, FIRST, FINAL,
	}

	/** Identifikátor waypointu, například GC14G57P. Jednoznačně identifikuje waypoint,
	 * ale na tu jednoznačnost se nemusí jít stoprocentně spolehnout.
	 * @return
	 */
	String getIdentifier();

	/** Souřadnice waypointu */
	Wgs getWgs();

	/** Nadmořská výška waypointu přišlá v datech, tak jak přišla */
	int getElevation();

	/** Genotyp wypointu, má vliv na zobrazení ikon a na filtrování */
	Genotyp getGenotyp();

	/**
	 * Ručně přidaný  waypointje takový, který nebyl ve zdrojových datech ze serveru, ale byl přidán někde na cestě uživatelem.
	 * Položka je dost obtížně uchopitelná, tak by se neměla využívat k hulbším algoritmům.
	 * Typické ruční přidání se týká finálních waypointů vyluštěných mysterek přidaných v geogeteu.
	 */
	boolean isRucnePridany();


	/**
	 * Soubor, ze kterého byl waypoint načten, aby uživatel mohl tento soubor otevřít.
	 * @return
	 */
	File getSourceFile();

	/**
	 * @return Vrací waypointy, které jsou nějak svázané z daným waypoitem + tento waypoint.
	 */

	default Iterable<Wpt> getRelatedWpts() {
		// // TODO [veverka] Implementovat přidání také tohoto waypointu do kolekce až se bude měnit zůsob získávání korelovaných wpts. -- 11. 2. 2020 13:36:28 veverka
		return getKesoid().getRelatedWpts();
	}

////////////////////// Položky infrastrukturní ///////////////////////////
	/**
	 * Plugin, ze kterého waypoint pochází. Každý waypoint pochzí z nějakého pluginu.
	 */
	KesoidPlugin getKesoidPlugin();

	/**
	 * Kešoid poddruh. Další členění typů kešoidů uvnitř pluginu.
	 * Například plugin keš rozlišuje hlavní wapyinty keší a dodatečné waypointy.
	 * Většina pluginů má jediný Kepodr.
	 */
	Kepodr getKepodr();


	/** Vrátí prioritu s jakou má být waypoint uchopen při práci s myší. Čím větší číslo, tím spíš bude uchopen. */
	int getPrioritaUchopovani();

/////////////////// Položky podivné a neroztřídené ////////////////////////////////
	Kesoid getKesoid();


	String getNazev();

	EZOrder getZorder();

	boolean isMainWpt();

	String getSym();

	boolean hasEmptyCoords();

	void computeGenotypIfNotExistsForAllRing(Genom genom);

	void removeMeFromRing();

	String textToolTipu();


/////////////////////////// Položky související s vykreslováním ////////////////////
/// Toto by zde vůbec nemělo být, vnikají dynamicky během práce s waypointy a vykreslováním
/// Je to zde především z výkonnostních důvodů.

	void invalidate();

	Sklivec getSklivec();

	void setSklivec(Sklivec sklivec);

	default boolean maTohotoMeziRelated(final Wpt wpt) {
		for (final Wpt w : getRelatedWpts()) {
			if (w == wpt) {
				return true;
			}
		}
		return false;
	}

}
