package cz.geokuk.plugins.kesoid;

import java.io.File;
import java.net.URL;
import java.util.Optional;

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


	/**
	 * Zoder určuje pořadí v jakém se vykreslují waypointy, aby nejzajímavější waypointy byly nakresléné co nejvýše.
	 * @author veverka
	 *
	 */
	public static enum EZOrder {
		OTHER, KESWPT, FIRST, FINAL,
	}

////////////////////// Datové polžky jedinečné, předpokládá se, že je má každý waypont jiné ////////////////////////
	/**
	 * @return Identifikátor waypointu, například GC14G57P. Jednoznačně identifikuje waypoint,
	 * ale na tu jednoznačnost se nemusí jít stoprocentně spolehnout.
	 */
	String getIdentifier();
	/**
	 * @return Pro člověka výstižný název waypoitu, jako je název keše, waymarku, munzee,
	 * název geodetického bodu (tady možná bude sestaven z čísla),
	 * název turistického místa, název a adresa prodejce turistických známek, jméno vrcholu atd.
	 * Unikátnost se nevyžaduje
	 *
	 */
	String getNazev();

	/** @return Souřadnice waypointu */
	Wgs getWgs();

	/** @return Symbol waypointu, určuje jakou základní ikonou bez dekorací se waypoint zobrazí.
	 * Také umožňuje filtrování.
	 * Příkladem jsou typy keší (tradička, multina), kategorie waymarku (hrady, sluneční hodiny), kategorie CGP (běžný bod, zhušťovací bod, nivelační bod).
	 * Odvozuje se z tohoho alelaSym.
	 */
	String getSym();


	/** @return Nadmořská výška waypointu přišlá v datech, tak jak přišla */
	int getElevation();

	/** @return Genotyp wypointu, má vliv na zobrazení ikon a na filtrování */
	Genotyp getGenotyp();

	/**
	 * Ručně přidaný  waypointje takový, který nebyl ve zdrojových datech ze serveru, ale byl přidán někde na cestě uživatelem.
	 * Položka je dost obtížně uchopitelná, tak by se neměla využívat k hulbším algoritmům.
	 * Typické ruční přidání se týká finálních waypointů vyluštěných mysterek přidaných v geogeteu.
	 * Implicitně se předpokládá false, což je i situace, kdy se ruční přidanost nerozlišuje.
	 */
	boolean isRucnePridany();

	/**
	 * Soubor, ze kterého byl waypoint načten, aby uživatel mohl tento soubor otevřít.
	 * @return
	 */
	File getSourceFile();

	/**
	 * Pokud daný waypoint nějakým způsobem omezuje umístění dalších nějakých waypointů, tak vrátí poloměr krhu,
	 * kam nesmí být tyto jiné waypointy umístěny. Například 161 m pro keše.
	 * @param wpt Waypoint, ke kterému se má kreslit kruh.
	 * @return Poloměr v metrech, nula, pokdu nic nobsazuje. nevrací záporné číslo.
	 */
	int getPolomerObsazenosti();

	/**
	 * @return Vrací waypointy, které jsou nějak svázané z daným waypoitem + tento waypoint.
	 */
	Iterable<Wpt> getRelatedWpts();

	/**
	 * Položka úzce svázaná s getRelatedWpts. Nepředpokládá se, že by se toto mělo přepisovat.
	 * @param wpt
	 * @return
	 */
	default boolean maTohotoMeziRelated(final Wpt wpt) {
		for (final Wpt w : getRelatedWpts()) {
			if (w == wpt) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Pořadí vykreslování waypointů, aby se vykreslily nejdůležitější waypointy co nejvýše.
	 * @return
	 */
	EZOrder getZorder();


///////////////////// Datové položky, které mnohdy budou sdíleny mezi waypinty stejného kešoidu, neboť nejsou rozlišitelné pro jednotlivé waypointyé ////////////////////////

	/**
	 * @return Jméno autora waypointu a možná i přidružených waypointů.
	 * U keše je autor keše také autorem všech additional waypoints. Některé waypointy nemusí mít autora,
	 * například cgp, pro které nebyly zřízeny waymarky, nebo turistické známky.
	 * Za autora nepovažujeme organizaci, která danou řídu waypointů tvoří. Autorem CGP není zeměměřičský ústav,
	 * autorem turznámky není příslušná firma. Abychom mohli hovořit o autorovi, musí být možnost mít individualní autory waypointů nebo kešoidů.
	 */
	Optional<String> getAuthor();

	/**
	 * @return Datum založení waypointu či kešoidu. Má smysl, jen když má waypoint autora.
	 * U keše je to datum založení ownerem, u turznámek datum vydání konkrétní známky.
	 * Nedávat sem datum vzniku nějaké evidence.
	 */
	Optional<String> getCreationDate();

	/**
	 * @return Stav waypointu případně celého kešoidu. Ne všechny waypointy musí mít možnost existovat ve všech stavech.
	 */
	EWptStatus getStatus();

	/**
	 * @return Můj subjektivní vztah k waypointu. Je možné ho mít, jen když geokuk ví, kdo jsem.
	 */
	EWptVztah getVztah();

	/**
	 * @return Když toto URL přijde do clipboardu, spuštěný geoget otevře listing.
	 */
	Optional<URL> getUrlProOtevreniListinguVGeogetu();

	/**
	 * @return Když toto URL přijde do clipboardu, spuštěný geoget přidá do seznamu bod neb owaypoint.
	 */
	Optional<URL> getUrlProPridaniDoSeznamuVGeogetu();

	/** Text, který má být zobrazen v tooltipu, bude se lišit waypoint od waypointu. a to je dobře */
	String getTextToolTipu();



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

/////////////////////////// Položky související s vykreslováním ////////////////////
/// Toto by zde vůbec nemělo být, vnikají dynamicky během práce s waypointy a vykreslováním
/// Je to zde především z výkonnostních důvodů.

	void invalidate();

	Sklivec getSklivec();

	void setSklivec(Sklivec sklivec);



////////////////////////////////////// Zatracené metody, vyhodit až se zlikviduje Wpti implementace, v nových implementacích nedělají nic

	@Deprecated
	void computeGenotypIfNotExistsForAllRing(Genom genom);

	@Deprecated
	void removeMeFromRing();

	/**
	 * Nesmí vzniknout waypoint z prázdnými souřadnicemi.
	 * @return
	 */
	@Deprecated
	boolean hasEmptyCoords();


}
