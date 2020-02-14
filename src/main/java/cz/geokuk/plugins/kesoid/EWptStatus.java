package cz.geokuk.plugins.kesoid;

public enum EWptStatus {
	/** Aktivní waypoint. To je defaultní stav u typů waypointů, které nedokážou toto rozlišit, například kopců. */
	ACTIVE,

	/** Dočasně zneaktivněný waypoint, u keše odpovídá stavu disabled, ostatní typy asi ntento stav mít nebude */
	DISABLED,

	/** Archivní waypoint je takový, který v podstatně neexistuje, byl zrušen. Z tohoto stavu se nelze vymanit.
	 * Patří sem třeba i zrušené turistické známky. Defaultně se zrušenci nezobrazují. */
	ARCHIVED,

}
