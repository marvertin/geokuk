package cz.geokuk.plugins.kesoid;

public enum EWptVztah {
	/**
	 * Normální vztah je také vzdtahem defaultním. Typicky mám k waypointů tento vztah, nestvořil jsem je, nic jsem s nimi nedělal.
	 */
	NORMAL,

	/**
	 * Waypoint jsem nějakým způsobem našel, získal, navštívil. Více lidí může mít k danému waypointu tento vztak.
	 * Je to nalezená keš, chcené munzee, koupená turistická známka.
	 */
	FOUND,

	/**
	 * Je to můj waypoint. Vytvořil jsem h, jsem jeho aautor. Nidky jiný nemá k waypointu tento vztah.
	 */
	OWN,


	/**
	 * Waypoint vlastně ještě neexistuje v tom smyslu, že by k němu mohl být vztah.
	 * Tento vztah má smysl u her, kdy waypointa tvoří předem danou množinu a někdo může tento waypoint "ukořistit" pro sebe
	 * a stát se tak "aturem". Příkaldem je český geodetický bod, který se ještě nestal waymarkem.
	 * Jiným příkaldem může být vrchol v připravované hře, který ještě nemá svého prvolezce, jenž by byl považován za autora.
	 *
	 * Keše v tomto vztahu být nemohou. Kdyby však někdo řekl: založme keše na všech hradech a jejich seznam je zde, tak bych k waypointům
	 * hradů, kde ještě není keš, měl tento vztah.
	 *
	 * Lze říct že NORMAL se liší od NOT v tom, že u NOT nemohu získat autorství (pomineme li adopci), u NOT ho získat mohu.
	 */
	NOT,
}
