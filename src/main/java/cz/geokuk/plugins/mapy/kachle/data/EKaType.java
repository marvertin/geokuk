package cz.geokuk.plugins.mapy.kachle.data;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public enum EKaType {

    // Mapové podklady, dále neprůhledné
	BASE_M(false, 0, 19, 19, "Základní", "Základní mapa se silnicemi.", KeyEvent.VK_Z, KeyStroke.getKeyStroke('z'), new MapyCzUrlBuilder("base-m")),
	TURIST_M(false, 0, 19, 19, "Turistická", "Turistická mapa.", KeyEvent.VK_T, KeyStroke.getKeyStroke('t'), new MapyCzUrlBuilder("turist-m")),
	OPHOTO_M(true, 0, 20, 20, "Letecká", "Letecká ortho foto mapa", KeyEvent.VK_L, KeyStroke.getKeyStroke('f'), new MapyCzUrlBuilder("ophoto-m")),
	WTURIST_WINTER_M(false, 0, 19, 19, "Turistická zimní", "Zimní turistická mapa.", KeyEvent.VK_M, KeyStroke.getKeyStroke('w'), new MapyCzUrlBuilder("wturist_winter-m")),
	OPHOTO1415_M(true, 0, 20, 20, "Letecká 2015", "Starší fotomapa", 0, null, new MapyCzUrlBuilder("ophoto1415-m")),
	OPHOTO1012_M(true, 0, 19, 19, "Letecká 2012", "Starší fotomapa", 0, null, new MapyCzUrlBuilder("ophoto1012-m")),
	OPHOTO0406_M(true, 0, 19, 19, "Letecká 2006", "Starší fotomapa", KeyEvent.VK_6, null, new MapyCzUrlBuilder("ophoto0406-m")),
	OPHOTO0203_M(true, 0, 18, 18, "Letecká 2003", "Starší fotomapa", KeyEvent.VK_3, null, new MapyCzUrlBuilder("ophoto0203-m")),
	ZEMEPIS_M(false, 0, 18, 18, "Zeměpisná", "Zeměpisná mapa", KeyEvent.VK_G, KeyStroke.getKeyStroke('g'), new MapyCzUrlBuilder("zemepis-m")),
	BASE_M_TRAF_DOWN(false, 0, 19, 19, "Dopravní", "Dopravní mapa taková vyšedlá.", 0, null, new MapyCzUrlBuilder("base-m-traf-down")),
	ARMY2_M(true, 0, 15, 15, "Historická", "Historická mapa z let 1836-52", KeyEvent.VK_H, KeyStroke.getKeyStroke('h'), new MapyCzUrlBuilder("army2-m")),

// Nefunkční mapy k 16.11.2019
//	OPEN_STREAT(false, 0, 18, 18, "Openstreetmap", "Openstreetmap.", KeyEvent.VK_O, KeyStroke.getKeyStroke('o'), new OpenStreatMapUrlBuilder("https://b.tile.openstreetmap.org/")),
//    // http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/osm/{zoom}/{x}/{y}.png
//	MAPBOX(false, 0, 18, 18, "Map box", "Open streat map box.", KeyEvent.VK_O, KeyStroke.getKeyStroke('o'), new OpenStreatMapUrlBuilder("http://otile1.mqcdn.com/tiles/1.0.0/osm/")),
//	TUR_FREEMAP_SK_T(false, 0, 18, 18, "Slovensko turistická", "turistika.freemap.sk - turisktická mapa", 0, null, new OpenStreatMapUrlBuilder("http://c.freemap.sk/T/")),
//	TUR_FREEMAP_SK_A(false, 0, 18, 18, "Slovensko automapa", "turistika.freemap.sk - automapa", 0, null, new OpenStreatMapUrlBuilder("http://c.freemap.sk/A/")),
//	TUR_FREEMAP_SK_C(false, 0, 18, 18, "Slovensko cyklomapa", "turistika.freemap.sk - cyklomapa", 0, null, new OpenStreatMapUrlBuilder("http://c.freemap.sk/C/")),
//	TUR_FREEMAP_SK_K(false, 0, 18, 18, "Slovensko lyžařská  ", "turistika.freemap.sk - lyžařská mapa", 0, null, new OpenStreatMapUrlBuilder("http://c.freemap.sk/K/")),
//

	// Nefunguje, jakási ochrana přes kukačku
	// HIKING_SK_TOPO (true, false, 0, 18, 18, "Slovensko turistická ", "mapy.hiking.sk - topo", 0, null, new OpenStreatMapUrlBuilder("http://mapy.hiking.sk/layers/topo/")),

	// Todo vyřešit problémy s certifikátem
	// OPEN_CYKLO(true, false, 0, 18, 18, "Open cyclo", "Open streat map.", KeyEvent.VK_Y, KeyStroke.getKeyStroke('c') , new OpenStreatMapUrlBuilder("https://b.tile.thunderforest.com/cycle/")),

	;

	// super("Turistické trasy");
	// putValue(SHORT_DESCRIPTION, "Turistické trasy, červená, modrá, zelená, žlutá.");
	// putValue(MNEMONIC_KEY, KeyEvent.VK_U);
	// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('u'));

	private final int minMoumer;
	private final int maxMoumer;
	private final int maxAutoMoumer;
	private final String nazev;
	private final String popis;
	private final int klavesa;
	private final KeyStroke keyStroke;
	private KachleUrlBuilder urlBuilder;

	/**
	 * @param podklad
	 *            true, pokud se jedná o neprůhledný podlad, false jinak.
	 * @param jeMozneNavrsitTexty
	 *            zde je možné ještě aplikovat samostatnou vrstvu s texty. Zadejte true, pokud mapa texty neobsahuje (napříkald fotomapy).
	 * @param minMoumer
	 *            Minimáklní měřítko, ke kterému jsou podklady. Věřme, že to bude dne světšinou 0. (U starých seznamových map to bylo 3 nebo 4).
	 * @param maxMoumer
	 *            Maximální měřítko, pro který jsou kachle.
	 * @param maxAutoMoumer
	 *            už přesně nevím, nutno analyzovat, nastavujem to stejnějako maxMoumer
	 * @param nazev
	 *            Název mapy, tak se objeví v menu.
	 * @param popis
	 *            Bližší popis mapy, objeví se jako tooltip v menu.
	 * @param klavesa
	 *            Hot-key, která mapu vyvolá.
	 * @param keyStroke
	 *            Písmeno z nazev, které lze použít pro výběr při rozbaleném menu.
	 * @param urlBuilder
	 *            Implementace třídy, která sestaví URL pro zobrazení mapy.
	 */
	private EKaType(final boolean jeMozneNavrsitTexty, final int minMoumer, final int maxMoumer, final int maxAutoMoumer, final String nazev, final String popis, final int klavesa,
	        final KeyStroke keyStroke, final KachleUrlBuilder urlBuilder) {
		this.minMoumer = minMoumer;
		this.maxMoumer = maxMoumer;
		this.maxAutoMoumer = maxAutoMoumer;
		this.nazev = nazev;
		this.popis = popis;
		this.klavesa = klavesa;
		this.keyStroke = keyStroke;
		this.urlBuilder = urlBuilder;

	}

	public int fitMoumer(int moumer) {
		if (moumer < minMoumer) {
			moumer = minMoumer;
		}
		if (moumer > maxMoumer) {
			moumer = maxMoumer;
		}
		return moumer;
	}

	public KeyStroke getKeyStroke() {
		return keyStroke;
	}

	public int getKlavesa() {
		return klavesa;
	}

	public int getMaxAutoMoumer() {
		return maxAutoMoumer;
	}

	public int getMaxMoumer() {
		return maxMoumer;
	}

	public int getMinMoumer() {
		return minMoumer;
	}

	public String getNazev() {
		return nazev;
	}

	public String getPopis() {
		return popis;
	}

	public KachleUrlBuilder getUrlBuilder() {
		return urlBuilder;
	}

}
