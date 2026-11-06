package cz.geokuk.framework;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import cz.geokuk.util.gui.CornerLayoutManager;

/**
 * Jediné místo, kde se rozhoduje, kam na mapě smí který překryvný prvek. Souřadnice jsou v bodech mapy (levý horní roh mapy je 0,0), mapa je oblast pod stavovým řádkem. Prvky, které se
 * kreslí samy (ukazatel výkonu, zpráva po zkratce, měřítko), si polohu berou odtud; panely v rozích mají rozměry z {@link CornerLayoutManager}.
 *
 * <pre>
 * vlevo nahoře   pruh plánovače (4,4), pod ním ukazatel výkonu (4,40); popup plánovače pod pruhem (smí překrýt ukazatel)
 * vpravo nahoře  panel cest        vpravo dole  detail kešoidu       vlevo dole  náhled fotomapy
 * uprostřed dole měřítko, nad ním zpráva po zkratce
 * vpravo         legenda posuvníků Obsazenosti (pod panely v pravých rozích)
 * </pre>
 */
public final class RozmisteniPrekryvu {

	/** Prvky na mapě; pořadí je pořadí kontroly. */
	public enum Prvek {
		PRUH_PLANOVACE, POPUP_PLANOVACE, UKAZATEL_VYKONU, MERITKO, ZPRAVA, NAHLED_FOTOMAPY, DETAIL_KESOIDU, PANEL_CEST, LEGENDA_OBSAZENOSTI
	}

	/** Odsazení prvků vlevo nahoře od okraje mapy. */
	public static final int OKRAJ = 4;
	/** Výška pruhu plánovače; končí na y = 32. */
	public static final int VYSKA_PRUHU = 28;
	/** Možné šířky pruhu plánovače od nejširší; užší stupeň se použije, když se širší nevejde. */
	/** Šířky lišty plánovače podle stupně: plná s texty, s ikonami a nejužší. */
	public static final int PRUH_PLNY = 600, PRUH_IKONY = 328, PRUH_UZKY = 234;
	/** Šířka úchytu, který zůstane z lišty, když je plánovač vypnutý. */
	public static final int PRUH_UCHYT = 34;
	/** Ukazatel výkonu leží pod pruhem plánovače, když je pruh vidět. */
	public static final Rectangle UKAZATEL_VYKONU = new Rectangle(OKRAJ, OKRAJ + VYSKA_PRUHU + 4, 330, 20);
	/** Ukazatel výkonu bez pruhu plánovače. */
	public static final Rectangle UKAZATEL_VYKONU_BEZ_PRUHU = new Rectangle(OKRAJ, OKRAJ, 330, 20);
	/** Šířka vpravo nahoře, kterou pruh plánovače nesmí zabrat (spinner a ovladače vpravo nahoře). */
	public static final int REZERVA_VPRAVO = 250;
	/** Výchozí svislá poloha rady kachlovníku od horního okraje mapy. */
	public static final int Y_RADY = 8;
	/** Výška místa dole pro měřítko včetně stupnice, popisků a odsazení od spodního okraje. */
	public static final int VYSKA_MERITKA = 76;
	/** Odsazení měřítka od spodního okraje mapy. */
	public static final int ODSAZENI_MERITKA = 20;
	/** Největší šířka legendy Obsazenosti od pravého okraje (popisky, nadpis Průhlednost, posuvník). */
	public static final int SIRKA_LEGENDY = 90;
	/** Mezera mezi zprávou a tím, nad čím stojí. */
	public static final int MEZERA = 12;
	/** Mezera mezi zprávou a překážkou, kterou přeskočí. */
	private static final int MEZERA_PREKAZKY = 8;
	/** Odstup zprávy od levého a pravého okraje mapy v úzkém okně. */
	public static final int OKRAJ_ZPRAVY = 12;

	private RozmisteniPrekryvu() {}

	/** Největší šířka měřítka: polovina mapy, aby nezasahovalo do rohů (ty mají nejvýš čtvrtinu šířky). */
	public static int maximalniSirkaMeritka(final int sirkaMapy) {
		return sirkaMapy / 2;
	}

	/** Šířka lišty plánovače: nejširší stupeň, který se vejde do volné šířky; nejužší, když se nevejde žádný. */
	public static int sirkaPruhu(final int volnaSirka) {
		if (volnaSirka >= PRUH_PLNY) {
			return PRUH_PLNY;
		}
		return volnaSirka >= PRUH_IKONY ? PRUH_IKONY : PRUH_UZKY;
	}

	/** Volná šířka pro pruh plánovače na mapě dané šířky. */
	public static int volnaSirkaPruhu(final int sirkaMapy) {
		return sirkaMapy - OKRAJ - REZERVA_VPRAVO;
	}

	/** Volná šířka pro pruh plánovače: navíc končí před panelem nebo legendou, které zasahují do jeho výšky. */
	public static int volnaSirkaPruhu(final int sirkaMapy, final Collection<Rectangle> prekazky) {
		int volna = volnaSirkaPruhu(sirkaMapy);
		final Rectangle pas = new Rectangle(OKRAJ, OKRAJ, sirkaMapy, VYSKA_PRUHU);
		for (final Rectangle p : prekazky) {
			if (p.intersects(pas) && p.x > OKRAJ + PRUH_UCHYT) {
				volna = Math.min(volna, p.x - 2 * OKRAJ);
			}
		}
		return volna;
	}

	/** Ukazatel výkonu: pod pruhem plánovače, když je vidět, jinak úplně nahoře. */
	public static Rectangle ukazatelVykonu(final boolean pruh) {
		return new Rectangle(pruh ? UKAZATEL_VYKONU : UKAZATEL_VYKONU_BEZ_PRUHU);
	}

	/**
	 * Svislá poloha rady kachlovníku: nahoře, ale zasahuje-li zleva do zapnuté lišty plánovače nebo ukazatele výkonu, pod ně. Bez lišty je vždy {@link #Y_RADY}.
	 */
	public static int yRady(final int xRady, final boolean pruh, final int sirkaMapy) {
		final Rectangle lista = pruhPlanovace(volnaSirkaPruhu(sirkaMapy));
		final int konec = Math.max(UKAZATEL_VYKONU.x + UKAZATEL_VYKONU.width, lista.x + lista.width);
		if (pruh && xRady < konec + MEZERA_PREKAZKY) {
			return UKAZATEL_VYKONU.y + UKAZATEL_VYKONU.height + OKRAJ;
		}
		return Y_RADY;
	}

	/** Obdélník zapnuté lišty plánovače. */
	public static Rectangle pruhPlanovace(final int volnaSirka) {
		return new Rectangle(OKRAJ, OKRAJ, sirkaPruhu(volnaSirka), VYSKA_PRUHU);
	}

	/** Obdélník lišty plánovače: zapnutá podle volné šířky, vypnutá jen úchyt. */
	public static Rectangle pruhPlanovace(final int volnaSirka, final boolean zapnuto) {
		return new Rectangle(OKRAJ, OKRAJ, zapnuto ? sirkaPruhu(volnaSirka) : PRUH_UCHYT, VYSKA_PRUHU);
	}

	/** Mezera mezi spodním okrajem pruhu plánovače a jeho popupem. */
	public static final int POD_PRUHEM = 2;
	/** O kolik popup plánovače přesahuje doleva před odznak. */
	public static final int PRED_ODZNAKEM = 20;
	/** Menší popup už není k užitku; když by se kvůli překážce zmenšil pod tuto mez, překážku překryje. */
	public static final Dimension NEJMENSI_POPUP = new Dimension(220, 120);

	/**
	 * Popup plánovače pod pruhem, levým okrajem kousek před odznakem ({@code odznak} v souřadnicích mapy). Leží celý v mapě s odstupem {@link #OKRAJ}. Překážka vedle něj (panel vpravo
	 * nahoře, legenda) ho posune doleva a pak zúží, překážka pod ním (panely dole, měřítko) ho zkrátí; seznam se pak posouvá. Překážku, kvůli které by byl menší než
	 * {@link #NEJMENSI_POPUP}, překryje.
	 */
	public static Rectangle popupPlanovace(final Dimension mapa, final Rectangle odznak, final Dimension pozadovana, final Collection<Rectangle> prekazky) {
		final int y = OKRAJ + VYSKA_PRUHU + POD_PRUHEM;
		final int w0 = Math.max(0, Math.min(pozadovana.width, mapa.width - 2 * OKRAJ));
		Rectangle r = new Rectangle(Math.max(OKRAJ, Math.min(odznak.x - PRED_ODZNAKEM, mapa.width - OKRAJ - w0)), y, w0, Math.max(0, Math.min(pozadovana.height, mapa.height - y - OKRAJ)));
		for (final Rectangle p : prekazky) {
			if (!p.intersects(r)) {
				continue;
			}
			final Rectangle upraveny;
			if (p.y <= r.y) {
				final int pravy = p.x - OKRAJ;
				final int x = Math.max(OKRAJ, Math.min(r.x, pravy - r.width));
				upraveny = new Rectangle(x, r.y, Math.min(r.width, pravy - x), r.height);
			} else {
				upraveny = new Rectangle(r.x, r.y, r.width, Math.min(r.height, p.y - OKRAJ - r.y));
			}
			if (upraveny.width >= Math.min(NEJMENSI_POPUP.width, w0) && upraveny.height >= Math.min(NEJMENSI_POPUP.height, r.height)) {
				r = upraveny;
			}
		}
		return r;
	}

	/** Oblast měřítka: uprostřed dole, nejvýš polovina šířky mapy. */
	public static Rectangle meritko(final Dimension mapa) {
		final int w = maximalniSirkaMeritka(mapa.width);
		return new Rectangle((mapa.width - w) / 2, mapa.height - VYSKA_MERITKA, w, VYSKA_MERITKA);
	}

	/** Levý dolní roh (náhled fotomapy) podle toho, jak ho skládá {@link CornerLayoutManager}. */
	public static Rectangle levyDolniRoh(final Dimension mapa, final Dimension preferovana, final Dimension nejmensi, final Dimension nejvetsi) {
		final Dimension d = CornerLayoutManager.rozmerRohu(mapa, preferovana, nejmensi, nejvetsi);
		return new Rectangle(0, mapa.height - d.height, d.width, d.height);
	}

	public static Rectangle pravyDolniRoh(final Dimension mapa, final Dimension preferovana, final Dimension nejmensi, final Dimension nejvetsi) {
		final Dimension d = CornerLayoutManager.rozmerRohu(mapa, preferovana, nejmensi, nejvetsi);
		return new Rectangle(mapa.width - d.width, mapa.height - d.height, d.width, d.height);
	}

	public static Rectangle pravyHorniRoh(final Dimension mapa, final Dimension preferovana, final Dimension nejmensi, final Dimension nejvetsi) {
		final Dimension d = CornerLayoutManager.rozmerRohu(mapa, preferovana, nejmensi, nejvetsi);
		return new Rectangle(mapa.width - d.width, 0, d.width, d.height);
	}

	/** Legenda Obsazenosti u pravého okraje, od horního po dolní okraj mapy. */
	public static Rectangle legendaObsazenosti(final Dimension mapa) {
		return new Rectangle(mapa.width - SIRKA_LEGENDY, 0, SIRKA_LEGENDY, mapa.height);
	}

	/** Nejširší zpráva: mezi okraji mapy, bez místa pro legendu Obsazenosti u pravého okraje; delší se zkrátí. */
	public static int nejvetsiSirkaZpravy(final int sirkaMapy) {
		return Math.max(100, sirkaMapy - 2 * OKRAJ_ZPRAVY - SIRKA_LEGENDY);
	}

	/**
	 * Zpráva po zkratce: uprostřed mapy nad měřítkem. Delší zpráva se posune doleva, aby nezasáhla do pásu legendy, a nejdelší se zkrátí. Když by překrývala některou z překážek (panel v rohu, legenda),
	 * posune se nad ni; nad horní okraj mapy se nikdy nedostane, a pokud by se tam dostala, zůstane těsně pod ním.
	 */
	public static Rectangle zprava(final Dimension mapa, final Dimension pozadovana, final Collection<Rectangle> prekazky) {
		final Dimension velikost = new Dimension(Math.min(pozadovana.width, nejvetsiSirkaZpravy(mapa.width)), pozadovana.height);
		// Uprostřed, ale tak, aby nezasáhla do pásu legendy u pravého okraje; v úzkém okně u levého okraje.
		final int x = Math.max(OKRAJ_ZPRAVY, Math.min((mapa.width - velikost.width) / 2, mapa.width - SIRKA_LEGENDY - OKRAJ_ZPRAVY - velikost.width));
		final Rectangle meritko = meritko(mapa);
		Rectangle r = new Rectangle(x, meritko.y - MEZERA - velikost.height, velikost.width, velikost.height);
		for (int krok = 0; krok < 8; krok++) {
			Rectangle zavazi = null;
			for (final Rectangle p : prekazky) {
				if (p.intersects(r) && (zavazi == null || p.y < zavazi.y)) {
					zavazi = p;
				}
			}
			if (zavazi == null) {
				break;
			}
			final int y = zavazi.y - MEZERA_PREKAZKY - velikost.height;
			if (y < 0) {
				break;
			}
			r = new Rectangle(r.x, y, r.width, r.height);
		}
		return r;
	}

	/**
	 * Překážky pro zprávu v souřadnicích podkladu: viditelné panely v rozích kontejneru, který skládá {@link CornerLayoutManager}, a případně legenda. Souřadnice se přepočtou o polohu podkladu,
	 * takže platí, i kdyby podklad nezačínal v levém horním rohu kontejneru.
	 */
	public static List<Rectangle> prekazky(final Container rohy, final Component podklad, final boolean legenda) {
		final List<Rectangle> prekazky = new ArrayList<>();
		for (final Component roh : rohy.getComponents()) {
			if (roh != podklad && roh.isVisible() && !roh.getBounds().isEmpty()) {
				final Rectangle r = roh.getBounds();
				r.translate(-podklad.getX(), -podklad.getY());
				prekazky.add(r);
			}
		}
		if (legenda) {
			prekazky.add(legendaObsazenosti(podklad.getSize()));
		}
		return prekazky;
	}

	/** Dvojice prvků, jejichž oblasti se protínají; prázdný seznam, když se žádné nekryjí. */
	public static List<String> kolize(final Map<Prvek, Rectangle> oblasti) {
		final List<String> kolize = new ArrayList<>();
		final Prvek[] vse = oblasti.keySet().toArray(new Prvek[0]);
		for (int i = 0; i < vse.length; i++) {
			for (int j = i + 1; j < vse.length; j++) {
				if (oblasti.get(vse[i]).intersects(oblasti.get(vse[j])) && !jeDovolenePrekryti(vse[i], vse[j])) {
					kolize.add(vse[i] + " × " + vse[j]);
				}
			}
		}
		return kolize;
	}

	/** Legenda leží pod panely v pravých rozích a tomu se nebrání; panely ji zakryjí. Otevřený popup plánovače smí na chvíli překrýt ukazatel výkonu a zprávu po zkratce. */
	static boolean jeDovolenePrekryti(final Prvek a, final Prvek b) {
		return jePar(a, b, Prvek.LEGENDA_OBSAZENOSTI, Prvek.PANEL_CEST) || jePar(a, b, Prvek.LEGENDA_OBSAZENOSTI, Prvek.DETAIL_KESOIDU) || jePar(a, b, Prvek.POPUP_PLANOVACE, Prvek.UKAZATEL_VYKONU)
				|| jePar(a, b, Prvek.POPUP_PLANOVACE, Prvek.ZPRAVA);
	}

	private static boolean jePar(final Prvek a, final Prvek b, final Prvek x, final Prvek y) {
		return a == x && b == y || a == y && b == x;
	}

	/** Prázdná mapa oblastí ve stálém pořadí prvků. */
	public static Map<Prvek, Rectangle> prazdne() {
		return new EnumMap<>(Prvek.class);
	}
}
