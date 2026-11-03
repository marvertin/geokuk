package cz.geokuk.core.program;

/**
 * Jména uzlů a hodnot v preferencích
 *
 * @author Martin Veverka
 *
 */
public class FPref {

	public static final String ZASTARALE_ANO_GGT_FILE_value = "anoGgtFile";
	public static final String BLOKOVANE_ZDROJE_value = "blokovaneZdroje";
	public static final String VYPNUTE_TYPY_ZDROJU_value = "vypnuteTypyZdroju";
	public static final String CASY_DAT_ZDROJU_value = "casyDatZdroju";
	public static final String ZNAME_GSAK_DATABAZE_value = "znameGsakDatabaze";
	public static final String ZNAME_OPENSAK_DATABAZE_value = "znameOpensakDatabaze";
	public static final String ZNAME_GEOGET_DATABAZE_value = "znameGeogetDatabaze";
	public static final String DOMACI_SOURADNICE_node = "domacisouradnice";
	public static final String GEOCACHING_COM_NICK_value = "geocachingComNick";
	public static final String GEOCACHING_COM_NICK_ID_value = "geocachingComNickId";
	public static final String GEOGET_DATA_DIR_value = "geogetDataDir";
	public static final String GSAK_DATA_DIR_value = "gsakDataDir";
	public static final String OPENSAK_DATA_DIR_value = "opensakDataDir";
	// Hodnoty
	public static final String HC_value = "hc";
	public static final String IMAGE_3RD_PARTY_DIR_value = "image3rdPartyDir";
	public static final String IMAGE_MY_DIR_value = "imageMyDir";
	public static final String JMENO_VYBRANE_SADY_IKON_node = "mapIconSetting";
	public static final String JMENO_VYBRANE_SADY_IKON_value = "vybraneSady";

	public static final String KACHLE_CACHE_DIR_value = "kachleCacheDir";
	public static final String KES_DIR_value = "kesDir";
	public static final String KESFILTER_structure_node = "kesfilter";
	// Jméno hodnot s vybranými alelami
	public static final String KESOID_FILTER_ALELY_value = "alely";
	public static final String KESOID_FILTER_NATOOLBARU_value = "alelyNaToolbaru";
	public static final String KESOID_FILTR_node = "kesoid/filtr";
	public static final String KESOID_node = "kesoid";
	public static final String ZVYRAZNENI_node = "kesoid/zvyrazneni";
	public static final String ZVYRAZNENI_value = "zvyrazneni";
	public static final String ZVYRAZNENI_PREDVOLBY_value = "predvolby";
	public static final String KESOID_OBSAZENOST_node = "obsazenost"; // z duvodu komaptibility zachovat

	public static final String KESOID_POPISKY_node = "kesoid/popisky";
	public static final String KESOID_VISIBLE_value = "visible";
	public static final String ZOBRAZENI_KESI_value = "zobrazeni";
	public static final String SLEDOVAT_SCHRANKU_value = "sledovatSchranku";
	public static final String ZVETSENI_IKON_value = "zvetseniIkon";
	public static final String LIMIT_IKON_value = "limitIkon";
	public static final String LIMIT_TECEK_value = "limitTecek";

	public static final String NEXT_UPDATE_CHECK_TIMESTAMP_value = "nextUpdateCheckTimestamp";
	// Uzly
	public static final String MAPICON_FENOTYP_node = "mapicon/fenotyp";

	public static final String MOUMER_value = "moumer";
	public static final String MOUSTRED_value = "moustre";
	public static final String MRIZKA_node = "mrizka";
	public static final String NASTAVENI_node = "nastaveni";
	public static final String NE_GGT_FILE_value = "neGgtFile";
	public static final String ANO_GGT_FILE_value = "anoGgtFile";
	public static final String NODE_KTERE_MAPY_node = "kteremapy";

	public static final String OKNO_structure_node = "okno";

	public static final String RENDER_KMZ_DIR_value = "kmzDir";
	public static final String RENDER_OZI_DIR_value = "oziDir";
	public static final String RENDER_PICTURE_DIR_value = "pictureDir";

	public static final String RENDER_structure_node = "render";
	public static final String STAV_OKNA_value = "stavokna";
	public static final String UMISTENI_SOUBORU_node = "umisteniSouboru";
	public static final String UVODNI_SOURADNICE_node = "uvodnisouradnice";
	public static final String VALUE_MAPOVE_DEKORACE_value = "mapoveDekorace";

	public static final String VALUE_MAPOVE_PODKLADY_value = "mapovePodklady";
	public static final String VSEOBECNE_node = "vseobecne";
	public static final String ZOBRAZ_OBSAZENOST_value = "zobrazitObsazenost";

	public static final String ZOBRAZ_POPISY_KESI_value = "zobrazPopisyKesi";
	public static final String ZOBRAZ_ZVYRAZNOVACI_KRUHY_value = "zobrazZvyraznovaciKruhy";

	public static final String ZOBRAZIT_MMRIZKU_valuePrefix = "zobrazitMrizku";
	public static final String ZVYRAZNOVACI_KRUHY_node = "zvyraznovaciKruhy";

	public static final String VYLET_node = "vylet";
	public static final String AKTUALNI_SOUBOR_value = "aktualniSoubor";
	public static final String JE_OTEVRENY_VYLET_value = "jeOtevrenyVylet";
	public static final String TYP_NOVYCH_USEKU_value = "typNovychUseku";
	public static final String CESTY_DIR_value = "cesty-dir";

	// Specifická nastavení pro GSAK:
	public static final String GSAK_node = "gsak";
	public static final String GSAK_CAS_NALEZU_value = "casNalezu";
	/** Zrušené nastavení Čas nenálezu; při uložení nastavení se ze starších profilů odstraní. */
	public static final String GSAK_CAS_NENALEZU_value = "casNenalezu";
	/** Klíč je stejný v uzlech {@link #GSAK_node}, {@link #OPENSAK_node} a {@link #GEOGET_node}; true (výchozí) = nové databáze se načtou hned, false = čekají na výběr v tabulce zdrojů. */
	public static final String NOVE_DATABAZE_HNED_value = "nacitatHnedVsechnyDatabaze";

	public static final String OPENSAK_node = "opensak";

	public static final String GEOGET_node = "geoget";
}
