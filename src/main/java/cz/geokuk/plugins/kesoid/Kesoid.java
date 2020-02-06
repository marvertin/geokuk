package cz.geokuk.plugins.kesoid;

import java.net.URL;

import javax.swing.Icon;

import cz.geokuk.plugins.kesoid.data.EKesoidKind;

public interface Kesoid {

	/**
	 * @return the firstWpt
	 */
	Wpt getFirstWpt();


	/** S tím identifikátorem je potíž, každý waypoint má svůj identifikátor a kešoid také, tak který použít? */
	String getIdentifier();


	/** Co to je mainWpt? Úvodky, fální souřadnice? A co u prodejních míst turistických známek. Nechceme zde mít stejný model, jako má geoget 1:n,
	 * toto se musí vymítit, ne vždy je jasný hlavní waypoint. */
	Wpt getMainWpt();

	/** S tím názvem je potíž, každý waypoint má svůj název a kešoid také, tak který použít? */
	String getNazev();

	/**
	 * Toto dává texty, které se budou prohledávat.
	 * ale asi to patří spíš k waypointům, chcme hledat waypointy a ne kešoidy.
	 * @return
	 */
	String[] getProhledavanci();



	/**
	 * @return the url
	 */
	String getUrl();

	Icon getUrlIcon();

	URL getUrlPrint();

	URL getUrlShow();

	/**
	 * Hlavní metoda, která poskytuje informaci o tom, které waypointy patří kešoidu.
	 * Asi by měla přejít na waypoint.
	 * @return
	 */
	Iterable<Wpt> getWpts();

	// Použito do titulku o počtu waypontů při zůmování keše
	int getWptsCount();

	// Tady jsou věci, které používá jen detailní okno v pravém dolním rohu, které by mělo být také poplatnédanému pluginu.

	/**
	 * @deprecated Zlikvidovat, až se dostane pravý dolní roh do pluginů.
	 */
	@Deprecated
	EKesStatus getStatus();

	/**
	 * @deprecated TODO [veverka] Zlikvidovat, použito jen na pro zjiětění priority a na pravý dolní roh, to má býát řešeno jinak  -- 6. 2. 2020 11:11:11 veverka
	 * @return
	 */
	@Deprecated
	EKesoidKind getKesoidKind();

	/**
	 * @deprecated To je datum založení, mělo by to být v luginu jen pro ty, kteří nějaké založení z principu mají. Nebo ho mají všichni?
	 * Nebo většina?
	 * A mělo by se to jmenovat logičtěji. Určitě promyslet.
	 * @return
	 */
	@Deprecated
	String getHidden();

	/**
	 * Vztahy jsou víceméně jen ke keším a možná waymarkům. Možná dát do pluginů. ale nevím.
	 * @return
	 */
	EKesVztah getVztah();


	/** Ne vše má autora, ale hodně věcí ano, tak budiž. Mělo by to být na waypointu.
	 * @deprecated Dát na waypoint, teroreticky může mít každý waypoint autora jiného.
	 */
	@Deprecated
	String getAuthor();



}