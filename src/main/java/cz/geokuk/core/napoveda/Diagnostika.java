package cz.geokuk.core.napoveda;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import cz.geokuk.core.program.FConst;
import cz.geokuk.core.program.FPref;
import cz.geokuk.core.program.UmisteniProgramu;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.SpravcePocitadel;
import lombok.extern.slf4j.Slf4j;

/**
 * Informace pro hlášení chyby: verze, prostředí a posledních pár událostí v programu.
 */
@Slf4j
public final class Diagnostika {

	private static final int MAX_ZAZNAMU = 100;
	private static final int RADKU_LOGU = 50;
	/** Stejné umístění jako v logback.xml. */
	static final File LOG = new File(UmisteniProgramu.log(), "geokuk.log");
	private static final long START = System.currentTimeMillis();
	private static final Deque<String> udalosti = new ArrayDeque<>();
	private static final Deque<String> chyby = new ArrayDeque<>();
	private static int pocetChyb;

	public static final String COMMIT = commit();

	private static final String BETA_KANAL_value = "betaKanal";
	/** Soubor beta vedle jaru z předchozích verzí, převezme se do nastavení. */
	private static final File SOUBOR_BETA = new File(FConst.KOREN, "beta");
	private static final java.util.List<Runnable> poZmeneBetaKanalu = new java.util.concurrent.CopyOnWriteArrayList<>();

	/** Nabízet při kontrole aktualizací i testovací verze. */
	public static boolean betaKanal() {
		if (SOUBOR_BETA.isFile() && SOUBOR_BETA.delete()) {
			setBetaKanal(true);
		}
		return MyPreferences.current().node(FPref.VSEOBECNE_node).getBoolean(BETA_KANAL_value, false);
	}

	public static void setBetaKanal(final boolean zapnuto) {
		MyPreferences.current().node(FPref.VSEOBECNE_node).putBoolean(BETA_KANAL_value, zapnuto);
		poZmeneBetaKanalu.forEach(Runnable::run);
	}

	public static void poZmeneBetaKanalu(final Runnable akce) {
		poZmeneBetaKanalu.add(akce);
	}

	/** Testovací verze a instalace s beta kanálem ukazují verzi trvale v okně. */
	public static boolean zobrazovatVerzi() {
		return FConst.VERSION.contains("-") || betaKanal();
	}

	public static String popisVerze() {
		final String verze = COMMIT.isEmpty() ? FConst.VERSION : FConst.VERSION + " (" + COMMIT + ")";
		return betaKanal() ? verze + " · beta kanál" : verze;
	}

	/** Milisekundy od spuštění programu. */
	public static long dobaBehuMs() {
		return System.currentTimeMillis() - START;
	}

	/** Časová značka kroku startu do logu: milisekundy od načtení diagnostiky (začátek startu programu). */
	public static void znackaStartu(final String krok) {
		log.info("Start: {} za {} ms", krok, System.currentTimeMillis() - START);
	}

	public static void zaznamenej(final String udalost) {
		synchronized (udalosti) {
			dokonciMapu();
			zapis(udalost);
		}
	}

	private static void zapis(final String udalost) {
		log.info("Událost: {}", soukrome(udalost));
		pridej(udalosti, udalost);
	}

	// Práce s mapou se zapisuje souhrnně, jinak by každý posun vytlačil ostatní události.
	private static int posunu;
	private static int meritkoOd = -1;
	private static int meritko = -1;
	private static Object stred;

	/** Změna výřezu mapy, bez polohy. */
	public static void zaznamenejVyrez(final int noveMeritko, final Object novyStred) {
		synchronized (udalosti) {
			if (meritkoOd < 0) {
				meritkoOd = meritko < 0 ? noveMeritko : meritko;
			}
			if (meritko == noveMeritko && !Objects.equals(stred, novyStred)) {
				posunu++;
			}
			meritko = noveMeritko;
			stred = novyStred;
		}
	}

	private static void dokonciMapu() {
		if (meritkoOd >= 0 && (posunu > 0 || meritkoOd != meritko)) {
			zapis("Mapa: " + (posunu > 0 ? posunu + "× posun, " : "") + (meritkoOd != meritko ? "měřítko " + meritkoOd + " → " + meritko : "měřítko " + meritko));
		}
		posunu = 0;
		meritkoOd = -1;
	}

	public static void zaznamenejChybu(final String chyba) {
		synchronized (chyby) {
			pocetChyb++;
		}
		pridej(chyby, chyba);
	}

	private static void pridej(final Deque<String> seznam, final String text) {
		final String radek = Cas.FORMAT.format(LocalTime.now()) + " " + soukrome(text);
		synchronized (seznam) {
			seznam.addLast(radek);
			if (seznam.size() > MAX_ZAZNAMU) {
				seznam.removeFirst();
			}
		}
	}

	/** Formátovač vzniká až při první události, ne při načtení třídy. */
	private static final class Cas {
		static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
	}

	/** Zaznamenává kliknutí na tlačítka a otevírání a zavírání oken. Položky menu sleduje {@link #sledujMenu(JMenuBar)}. */
	public static void sledujKliknuti() {
		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			if (event.getID() == MouseEvent.MOUSE_RELEASED && event.getSource() instanceof AbstractButton) {
				final AbstractButton button = (AbstractButton) event.getSource();
				if (!button.isEnabled()) {
					return;
				}
				if (button instanceof JMenuItem) {
					if (!(button instanceof JMenu) && !jeVListeMenu((JMenuItem) button)) {
						SwingUtilities.invokeLater(() -> zaznamenej("Kontextové menu: " + cesta((JMenuItem) button)));
					}
				} else {
					// Stav přepínače je známý až po obsloužení kliknutí.
					SwingUtilities.invokeLater(() -> zaznamenej("Klik: " + text(button) + stav(button)));
				}
			}
		}, AWTEvent.MOUSE_EVENT_MASK);
		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			final String udalost = udalostOkna((WindowEvent) event);
			if (udalost != null) {
				zaznamenej(udalost);
			}
		}, AWTEvent.WINDOW_EVENT_MASK);
	}

	/** Zaznamenává spuštění položek menu i s cestou v menu, způsobem spuštění a stavem přepínače. */
	public static void sledujMenu(final JMenuBar lista) {
		for (int i = 0; i < lista.getMenuCount(); i++) {
			if (lista.getMenu(i) != null) {
				sledujMenu(lista.getMenu(i));
			}
		}
	}

	private static final String SLEDOVANO = "geokuk.diagnostika.sledovano";

	/** Obsah komponenty s touto vlastností se do události o otevření okna nevypisuje. */
	static final String BEZ_TEXTU = "geokuk.diagnostika.bezTextu";

	/** Položka se může do menu přidat víckrát (menu Ikony), zaznamenat se má jen jednou. */
	private static boolean uzSledovano(final JComponent c) {
		if (c.getClientProperty(SLEDOVANO) != null) {
			return true;
		}
		c.putClientProperty(SLEDOVANO, Boolean.TRUE);
		return false;
	}

	private static void sledujMenu(final JMenu menu) {
		if (uzSledovano(menu)) {
			return;
		}
		for (final Component c : menu.getMenuComponents()) {
			sledujPolozku(c);
		}
		menu.getPopupMenu().addContainerListener(new ContainerAdapter() {
			@Override
			public void componentAdded(final ContainerEvent e) {
				sledujPolozku(e.getChild());
			}
		});
	}

	private static void sledujPolozku(final Component c) {
		if (c instanceof JMenu) {
			sledujMenu((JMenu) c);
		} else if (c instanceof JMenuItem) {
			final JMenuItem polozka = (JMenuItem) c;
			if (uzSledovano(polozka)) {
				return;
			}
			polozka.addActionListener(e -> zaznamenej(zpusob(polozka) + ": " + cesta(polozka) + stav(polozka)));
		}
	}

	private static String zpusob(final JMenuItem polozka) {
		final AWTEvent udalost = EventQueue.getCurrentEvent();
		if (udalost instanceof KeyEvent) {
			final KeyEvent ke = (KeyEvent) udalost;
			final KeyStroke zkratka = polozka.getAccelerator();
			// Napsaný znak (+, *) přijde se Shiftem, zkratka ho má bez modifikátorů.
			final boolean znak = zkratka != null && zkratka.getKeyEventType() == KeyEvent.KEY_TYPED && ke.getID() == KeyEvent.KEY_TYPED && zkratka.getKeyChar() == ke.getKeyChar();
			return znak || zkratka != null && zkratka.equals(KeyStroke.getKeyStrokeForEvent(ke)) ? "Klávesa " + popisZkratky(zkratka) : "Menu klávesnicí";
		}
		return "Menu";
	}

	private static String popisZkratky(final KeyStroke zkratka) {
		final String modifikatory = InputEvent.getModifiersExText(zkratka.getModifiers());
		final String klavesa = zkratka.getKeyCode() == KeyEvent.VK_UNDEFINED ? String.valueOf(zkratka.getKeyChar()) : KeyEvent.getKeyText(zkratka.getKeyCode());
		return modifikatory.isEmpty() ? klavesa : modifikatory + "+" + klavesa;
	}

	/** Položky hlavního menu zaznamenává {@link #sledujMenu(JMenuBar)}, kliknutím se zaznamenají jen položky kontextových menu. */
	static boolean jeVListeMenu(final JMenuItem polozka) {
		Component c = polozka;
		while (c instanceof JMenuItem) {
			c = c.getParent() instanceof JPopupMenu ? ((JPopupMenu) c.getParent()).getInvoker() : c.getParent();
		}
		return c instanceof JMenuBar;
	}

	static String cesta(final JMenuItem polozka) {
		final Deque<String> cesta = new ArrayDeque<>();
		Component c = polozka;
		while (c instanceof JMenuItem) {
			cesta.addFirst(text((JMenuItem) c));
			c = c.getParent() instanceof JPopupMenu ? ((JPopupMenu) c.getParent()).getInvoker() : null;
		}
		return String.join(" > ", cesta);
	}

	private static String text(final AbstractButton b) {
		return popis(b).replaceAll("<[^>]*>", "").trim();
	}

	private static String stav(final AbstractButton b) {
		return b instanceof JCheckBoxMenuItem || b instanceof JToggleButton ? (b.isSelected() ? " → zapnuto" : " → vypnuto") : "";
	}

	/** Kontextová menu a bubliny jsou také okna; jejich položky se zaznamenávají zvlášť. */
	static String udalostOkna(final WindowEvent event) {
		final Window w = event.getWindow();
		if (w.getType() == Window.Type.POPUP) {
			return null;
		}
		if (event.getID() == WindowEvent.WINDOW_OPENED) {
			return "Otevřeno okno: " + popisOkna(w);
		}
		if (event.getID() == WindowEvent.WINDOW_CLOSED) {
			return "Zavřeno okno: " + titulek(w);
		}
		return null;
	}

	private static String titulek(final Window w) {
		final String titulek = w instanceof Frame ? ((Frame) w).getTitle() : w instanceof Dialog ? ((Dialog) w).getTitle() : null;
		return titulek == null || titulek.isEmpty() ? w.getClass().getSimpleName() : titulek;
	}

	private static String popisOkna(final Window w) {
		final StringBuilder zprava = new StringBuilder();
		if (w instanceof RootPaneContainer) {
			hledejZpravu(((RootPaneContainer) w).getContentPane(), zprava);
		}
		final String text = zprava.toString().replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
		return titulek(w) + (text.isEmpty() ? "" : " – " + (text.length() > 200 ? text.substring(0, 200) + "…" : text));
	}

	private static void hledejZpravu(final Container kde, final StringBuilder zprava) {
		for (final Component c : kde.getComponents()) {
			if (c instanceof JOptionPane) {
				final Object m = ((JOptionPane) c).getMessage();
				for (final Object cast : m instanceof Object[] ? (Object[]) m : new Object[] { m }) {
					zprava.append(textZpravy(cast)).append(' ');
				}
			} else if (c instanceof Container) {
				hledejZpravu((Container) c, zprava);
			}
		}
	}

	static String textZpravy(final Object cast) {
		if (cast instanceof JComponent && ((JComponent) cast).getClientProperty(BEZ_TEXTU) != null) {
			return "";
		}
		if (cast instanceof JTextComponent) {
			return ((JTextComponent) cast).getText();
		}
		if (cast instanceof JLabel) {
			return ((JLabel) cast).getText();
		}
		if (cast instanceof Container) {
			final StringBuilder sb = new StringBuilder();
			for (final Component c : ((Container) cast).getComponents()) {
				sb.append(textZpravy(c)).append(' ');
			}
			return sb.toString();
		}
		return cast instanceof Component ? "" : String.valueOf(cast);
	}

	private static String popis(final AbstractButton button) {
		if (button.getText() != null && !button.getText().isEmpty()) {
			return button.getText();
		}
		final Action action = button.getAction();
		if (action != null && action.getValue(Action.NAME) != null) {
			return String.valueOf(action.getValue(Action.NAME));
		}
		return button.getToolTipText() != null ? button.getToolTipText() : button.getClass().getSimpleName();
	}

	public static String text() {
		return text(MAX_ZAZNAMU, RADKU_LOGU);
	}

	/** Text s nejvýš daným počtem posledních událostí, chyb a řádků logu, ať se vejde do odkazu na nové issue. */
	public static String text(final int zaznamu, final int radkuLogu) {
		final StringBuilder sb = new StringBuilder();
		final Runtime runtime = Runtime.getRuntime();
		final long mb = 1024 * 1024;
		sb.append("GeoKuk ").append(popisVerze()).append('\n');
		sb.append("Beta kanál: ").append(ano(betaKanal())).append('\n');
		sb.append("Java: ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")\n");
		sb.append("Systém: ").append(System.getProperty("os.name")).append(' ').append(System.getProperty("os.version")).append(' ').append(System.getProperty("os.arch")).append('\n');
		sb.append("Paměť: ").append((runtime.totalMemory() - runtime.freeMemory()) / mb).append(" MB z max ").append(runtime.maxMemory() / mb).append(" MB\n");
		if (!GraphicsEnvironment.isHeadless()) {
			final Dimension obrazovka = Toolkit.getDefaultToolkit().getScreenSize();
			sb.append("Obrazovka: ").append(obrazovka.width).append('x').append(obrazovka.height).append(", monitorů ").append(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length).append('\n');
			sb.append("Zobrazení: ").append(popisZobrazeni()).append('\n');
		}
		sb.append("Složka programu: ").append(bezDomova(FConst.JAR_DIR)).append(FConst.JAR_DIR_EXISTUJE ? "" : " (nerozpoznána)").append('\n');
		sb.append("Data: ").append(bezDomova(FConst.DATA_DIR)).append('\n');
		sb.append("Přenosná verze (start.jar): ").append(ano(new File(FConst.JAR_DIR, "start.jar").exists())).append(", přibalená Java: ").append(ano(VerzeJavy.jePribalena())).append('\n');
		sb.append("Výpisy chyb: ").append(bezDomova(FExceptionDumper.getExcrepFolder())).append('\n');
		sb.append("Log: ").append(bezDomova(LOG)).append('\n');
		sb.append("Běží: ").append((System.currentTimeMillis() - START) / 60000).append(" min\n");
		final java.util.function.Supplier<String> offline = offlineMapy;
		if (offline != null) {
			sb.append('\n').append(offline.get());
		}
		synchronized (udalosti) {
			dokonciMapu();
		}
		synchronized (chyby) {
			vypis(sb, "Poslední chyby (celkem " + pocetChyb + ")", chyby, zaznamu);
		}
		vypis(sb, "Poslední události", udalosti, zaznamu);
		sb.append(Vykon.text());
		sb.append(ZobrazeneDlazdice.text());
		vypisPocitadla(sb);
		vypis(sb, "Konec logu", konecLogu(LOG, radkuLogu), radkuLogu);
		return sb.toString();
	}

	/** Skutečně použité zvětšení a vykreslování, nejen uložené nastavení (spouštěč ho předá až po restartu). */
	static String popisZobrazeni() {
		final double meritko = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();
		final StringBuilder sb = new StringBuilder();
		sb.append(Math.round(meritko * 100)).append(" %");
		sb.append(", zvětšení podle systému ").append(ano(!"1".equals(System.getProperty("sun.java2d.uiScale"))));
		if (System.getProperty("os.name", "").startsWith("Windows")) {
			sb.append(", Direct3D ").append(ano(!"false".equals(System.getProperty("sun.java2d.d3d"))));
		}
		final java.util.function.Supplier<String> mapa = popisOfflineMapy;
		if (mapa != null) {
			sb.append(", ").append(mapa.get());
		}
		return sb.toString();
	}

	private static volatile java.util.function.Supplier<String> popisOfflineMapy;

	static void setPopisOfflineMapy(final java.util.function.Supplier<String> popis) {
		popisOfflineMapy = popis;
	}

	/** Oddíl o offline mapách (soubory, téma, chyby); celý text včetně nadpisu a konce řádku. */
	private static volatile java.util.function.Supplier<String> offlineMapy;

	static void setOfflineMapy(final java.util.function.Supplier<String> popis) {
		offlineMapy = popis;
	}

	/** Totéž, co ukazuje servisní okno, ať to uživatel nemusí opisovat ze snímku obrazovky. */
	private static void vypisPocitadla(final StringBuilder sb) {
		final Map<String, java.util.List<Pocitadlo>> podleTypu = new TreeMap<>();
		for (final Pocitadlo pocitadlo : new ArrayList<>(SpravcePocitadel.getPocitadla())) {
			podleTypu.computeIfAbsent(pocitadlo.getTextovyPopisTypu(), typ -> new ArrayList<>()).add(pocitadlo);
		}
		if (podleTypu.isEmpty()) {
			return;
		}
		sb.append("\nServisní hodnoty:\n");
		for (final Map.Entry<String, java.util.List<Pocitadlo>> skupina : podleTypu.entrySet()) {
			sb.append("  ").append(skupina.getKey()).append(":\n");
			skupina.getValue().sort(Comparator.comparing(Pocitadlo::getName));
			for (final Pocitadlo pocitadlo : skupina.getValue()) {
				sb.append("    ").append(pocitadlo.getName()).append(": ").append(pocitadlo.get()).append('\n');
			}
		}
	}

	private static void vypis(final StringBuilder sb, final String nadpis, final Deque<String> seznam, final int nejvys) {
		sb.append('\n').append(nadpis).append(":\n");
		synchronized (seznam) {
			if (seznam.isEmpty()) {
				sb.append("(žádné)\n");
			}
			int preskocit = seznam.size() - nejvys;
			if (preskocit > 0) {
				sb.append("… ").append(preskocit).append(" starších vynecháno\n");
			}
			for (final String s : seznam) {
				if (preskocit-- <= 0) {
					sb.append(s).append('\n');
				}
			}
		}
	}

	static Deque<String> konecLogu(final File soubor, final int radku) {
		final Deque<String> konec = new ArrayDeque<>();
		if (!soubor.isFile()) {
			return konec;
		}
		try (BufferedReader reader = Files.newBufferedReader(soubor.toPath(), StandardCharsets.UTF_8)) {
			String radek;
			while ((radek = reader.readLine()) != null) {
				konec.addLast(soukrome(radek));
				if (konec.size() > radku) {
					konec.removeFirst();
				}
			}
		} catch (final IOException e) {
			konec.addLast("Log nelze přečíst: " + e);
		}
		return konec;
	}

	private static String ano(final boolean b) {
		return b ? "ano" : "ne";
	}

	/** Hlášení je veřejné: bez jména uživatele v cestách a bez parametrů v adresách (mohou obsahovat klíč k mapám). */
	static String soukrome(final String text) {
		return text == null ? null : text.replace(FConst.HOME_DIR.getAbsolutePath(), "~").replaceAll("(https?://[^\\s?\"']++)\\?[^\\s\"']*+", "$1?…");
	}

	/** Domovskou složku zkrátí na ~, ať text neobsahuje jméno uživatele. */
	/** Text (třeba zpráva výjimky) s domovskou složkou nahrazenou „~“. */
	static String textBezDomova(final String text) {
		if (text == null) {
			return null;
		}
		String vysledek = text;
		for (final String domov : new String[] { FConst.HOME_DIR.getAbsolutePath(), FConst.HOME_DIR.getPath() }) {
			if (!domov.isEmpty()) {
				vysledek = vysledek.replace(domov, "~");
			}
		}
		return vysledek;
	}

	static String bezDomova(final File soubor) {
		final String cesta = soubor.getAbsolutePath();
		final String domov = FConst.HOME_DIR.getAbsolutePath();
		return cesta.startsWith(domov) ? "~" + cesta.substring(domov.length()) : cesta;
	}

	private Diagnostika() {}

	private static String commit() {
		try {
			return FConst.atributManifestu("Geokuk-Commit");
		} catch (final IllegalArgumentException e) {
			return "";
		}
	}
}
