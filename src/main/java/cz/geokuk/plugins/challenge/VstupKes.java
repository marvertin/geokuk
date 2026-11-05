package cz.geokuk.plugins.challenge;

/** Keš tak, jak ji vidí výpočet challenge; nezávislé na zdroji dat (GeoGet, GSAK, OpenSAK, GPX). */
public interface VstupKes {

	String id();

	/** Původní souřadnice keše (ne finální umístění) ve stupních. */
	double lat();

	double lon();

	/** Uživatel keš našel. */
	boolean nalezena();

	/** Lab Cache (Adventure Lab). */
	boolean lab();

	boolean locationless();

	/** Vlastní keš uživatele (nemůže být cílem pro splnění). */
	default boolean vlastni() {
		return false;
	}

	/** Keš je zakázaná nebo archivovaná, nelze ji hledat. */
	default boolean neaktivni() {
		return false;
	}

	/** Typ, který se nenabízí jako kandidát na chybějící sektor (nález se počítá). */
	default boolean mimoKandidaty() {
		return false;
	}

	/** Má nález na gc.com log Found it, Attended nebo Webcam Photo Taken. */
	StavLogu logNaGc();

	enum StavLogu {
		ANO, NE,
		/** Zdroj dat to neříká; nález se počítá. */
		NEZNAMY
	}
}
