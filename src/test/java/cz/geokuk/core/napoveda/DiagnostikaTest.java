package cz.geokuk.core.napoveda;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import javax.swing.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.core.program.FConst;
import cz.geokuk.util.pocitadla.PocitadloRoste;

public class DiagnostikaTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void udalostMaCasHodinyMinutySekundy() {
		Diagnostika.zaznamenej("znacka-casu");
		Assert.assertTrue(Diagnostika.text().matches("(?s).*\\n\\d{2}:\\d{2}:\\d{2} znacka-casu\\n.*"));
	}

	@Test
	public void udalostiZVicevlaken() throws Exception {
		final List<Thread> vlakna = new ArrayList<>();
		final List<Throwable> chyby = Collections.synchronizedList(new ArrayList<>());
		for (int v = 0; v < 8; v++) {
			final int cislo = v;
			final Thread vlakno = new Thread(() -> {
				try {
					for (int i = 0; i < 50; i++) {
						Diagnostika.zaznamenej("souběh " + cislo + "/" + i);
					}
				} catch (final Throwable e) {
					chyby.add(e);
				}
			});
			vlakna.add(vlakno);
			vlakno.start();
		}
		for (final Thread vlakno : vlakna) {
			vlakno.join();
		}
		Assert.assertEquals(Collections.emptyList(), chyby);
		for (final String radek : Diagnostika.text().split("\\n")) {
			if (radek.contains(" souběh ")) {
				Assert.assertTrue(radek, radek.matches("\\d{2}:\\d{2}:\\d{2} souběh \\d/\\d+"));
			}
		}
	}

	@Test
	public void textObsahujeVerziAUdalosti() {
		Diagnostika.zaznamenej("první");
		Diagnostika.zaznamenejChybu("chyba stahování");
		for (int i = 0; i < 120; i++) {
			Diagnostika.zaznamenej("událost " + i);
		}
		final String text = Diagnostika.text();
		Assert.assertTrue(text.startsWith("GeoKuk " + FConst.VERSION));
		Assert.assertTrue(text.contains("událost 119"));
		Assert.assertFalse(text.contains("první"));
		Assert.assertTrue(text.contains("chyba stahování"));
	}

	@Test
	public void praceSMapouSeZapiseSouhrnne() {
		Diagnostika.zaznamenej("před mapou");
		Diagnostika.zaznamenejVyrez(13, "a");
		Diagnostika.zaznamenejVyrez(13, "b");
		Diagnostika.zaznamenejVyrez(13, "c");
		Diagnostika.zaznamenejVyrez(14, "c");
		Diagnostika.zaznamenej("po mapě");
		final String text = Diagnostika.text();
		Assert.assertTrue(text, text.contains("Mapa: 2× posun, měřítko 13 → 14"));
		Assert.assertTrue(text.indexOf("Mapa: 2×") < text.indexOf("po mapě"));
	}

	@Test
	public void cestaVMenu() {
		final javax.swing.JMenu soubor = new javax.swing.JMenu("Soubor");
		final javax.swing.JMenu podmenu = new javax.swing.JMenu("Mapy");
		final javax.swing.JMenuItem polozka = new javax.swing.JMenuItem("<html>Základní <i>mapa</i>");
		soubor.add(podmenu);
		podmenu.add(polozka);
		Assert.assertEquals("Soubor > Mapy > Základní mapa", Diagnostika.cesta(polozka));
	}

	@Test
	public void textObsahujeServisniHodnoty() {
		final PocitadloRoste pocitadlo = new PocitadloRoste("zk01 zkušební počítadlo", "Jen pro test.");
		pocitadlo.add(7);
		final String text = Diagnostika.text();
		Assert.assertTrue(text.contains("Servisní hodnoty:"));
		Assert.assertTrue(text.contains("zk01 zkušební počítadlo: 7"));
	}

	@Test
	public void domovskaSlozkaSeZkrati() {
		Assert.assertEquals("~" + File.separator + "geokuk", Diagnostika.bezDomova(new File(FConst.HOME_DIR, "geokuk")));
	}

	@Test
	public void konecLoguBezDomova() throws Exception {
		final File log = tmp.newFile("geokuk.log");
		final List<String> radky = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			radky.add("řádek " + i + " " + FConst.HOME_DIR.getAbsolutePath());
		}
		Files.write(log.toPath(), radky, StandardCharsets.UTF_8);
		final Deque<String> konec = Diagnostika.konecLogu(log, 3);
		Assert.assertEquals(Arrays.asList("řádek 27 ~", "řádek 28 ~", "řádek 29 ~"), new ArrayList<>(konec));
	}

	@Test
	public void konecLoguBezParametruAdres() throws Exception {
		final File log = tmp.newFile("geokuk-url.log");
		Files.write(log.toPath(), Arrays.asList("Chyba https://tile.example.org/1/2/3.png?apikey=TAJNE&x=1 při stahování"), StandardCharsets.UTF_8);
		final String radek = Diagnostika.konecLogu(log, 3).getFirst();
		Assert.assertFalse(radek, radek.contains("TAJNE"));
		Assert.assertEquals("Chyba https://tile.example.org/1/2/3.png?… při stahování", radek);
	}

	@Test
	public void chybejiciLogJePrazdny() {
		Assert.assertTrue(Diagnostika.konecLogu(new File(tmp.getRoot(), "neni.log"), 3).isEmpty());
	}

	@Test
	public void bezCestUzivateleAParametruAdres() {
		final String text = Diagnostika.soukrome("Chyby v souboru " + new File(FConst.HOME_DIR, "geokuk").getAbsolutePath() + ", https://tile.example.org/1/2/3.png?apikey=TAJNE");
		Assert.assertFalse(text, text.contains(FConst.HOME_DIR.getAbsolutePath()));
		Assert.assertFalse(text, text.contains("TAJNE"));
		Assert.assertTrue(text, text.contains("https://tile.example.org/1/2/3.png?…"));
	}

	@Test
	public void textPoleVHlasceNeniPopisKomponenty() {
		final javax.swing.JTextField pole = new javax.swing.JTextField("https://example.org/");
		Assert.assertEquals("https://example.org/", Diagnostika.textZpravy(pole));
	}

	@Test
	public void kontextoveMenuNeniVListe() {
		final javax.swing.JMenuBar lista = new javax.swing.JMenuBar();
		final javax.swing.JMenu soubor = new javax.swing.JMenu("Soubor");
		final javax.swing.JMenuItem vListe = new javax.swing.JMenuItem("Konec");
		lista.add(soubor);
		soubor.add(vListe);
		final javax.swing.JPopupMenu kontextove = new javax.swing.JPopupMenu();
		final javax.swing.JMenuItem vKontextovem = new javax.swing.JMenuItem("Přidat do cesty");
		kontextove.add(vKontextovem);
		Assert.assertTrue(Diagnostika.jeVListeMenu(vListe));
		Assert.assertFalse(Diagnostika.jeVListeMenu(vKontextovem));
	}

	@Test
	public void polozkaPridanaZnovuSeSledujeJednou() {
		final javax.swing.JMenuBar lista = new javax.swing.JMenuBar();
		final javax.swing.JMenu ikony = new javax.swing.JMenu("Ikony");
		final javax.swing.JMenuItem sada = new javax.swing.JMenuItem("Standardní");
		lista.add(ikony);
		Diagnostika.sledujMenu(lista);
		ikony.add(sada);
		ikony.remove(sada);
		ikony.insert(sada, 0);
		Diagnostika.sledujMenu(lista);
		Assert.assertEquals(1, sada.getActionListeners().length);
	}

	@Test
	public void chybySeVejdouDoOdkazuIPoMnohaUdalostech() throws Exception {
		Diagnostika.zaznamenejChybu("důležitá chyba");
		for (int i = 0; i < 150; i++) {
			Diagnostika.zaznamenej("Otevřeno okno: Přehled problémů – ěščřžýáíé ěščřžýáíé ěščřžýáíé " + i);
		}
		final String odkaz = ZadatProblemAction.odkaz();
		Assert.assertTrue(odkaz.length() <= 6000);
		Assert.assertTrue(java.net.URLDecoder.decode(odkaz, "UTF-8").contains("důležitá chyba"));
	}

	@Test
	public void kontextoveMenuSeNezaznamenaJakoOkno() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		final JFrame okno = new JFrame("Mapa");
		final String[] udalost = new String[1];
		try {
			SwingUtilities.invokeAndWait(() -> {
				okno.setSize(200, 200);
				okno.setVisible(true);
				final JPopupMenu menu = new JPopupMenu();
				for (int i = 0; i < 40; i++) {
					menu.add("položka " + i);
				}
				menu.show(okno.getContentPane(), 10, 10);
				final Window popup = SwingUtilities.getWindowAncestor(menu);
				Assert.assertNotSame("menu přesahuje okno, je v samostatném okně", okno, popup);
				udalost[0] = Diagnostika.udalostOkna(new WindowEvent(popup, WindowEvent.WINDOW_OPENED));
				menu.setVisible(false);
			});
		} finally {
			SwingUtilities.invokeAndWait(okno::dispose);
		}
		Assert.assertNull(udalost[0]);
	}

	@Test
	public void oknoInformaciProHlaseniJenTitulek() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		Diagnostika.zaznamenej("událost s dlouhým textem");
		SwingUtilities.invokeLater(() -> DiagnostikaAction.ukaz(null));
		Dialog dialog = null;
		for (int i = 0; i < 200 && dialog == null; i++) {
			Thread.sleep(25);
			for (final Window w : Window.getWindows()) {
				if (w instanceof Dialog && w.isShowing() && "Informace pro hlášení chyby".equals(((Dialog) w).getTitle())) {
					dialog = (Dialog) w;
				}
			}
		}
		Assert.assertNotNull(dialog);
		final Dialog d = dialog;
		final String[] udalost = new String[1];
		SwingUtilities.invokeAndWait(() -> {
			udalost[0] = Diagnostika.udalostOkna(new WindowEvent(d, WindowEvent.WINDOW_OPENED));
			d.dispose();
		});
		Assert.assertEquals("Otevřeno okno: Informace pro hlášení chyby", udalost[0]);
	}

	@Test
	public void hlaskaSeZaznamenaSTextem() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		final String[] udalost = new String[1];
		SwingUtilities.invokeAndWait(() -> {
			final JDialog dialog = new JOptionPane("Soubor nelze načíst.", JOptionPane.ERROR_MESSAGE).createDialog("Chyba");
			udalost[0] = Diagnostika.udalostOkna(new WindowEvent(dialog, WindowEvent.WINDOW_OPENED));
			dialog.dispose();
		});
		Assert.assertEquals("Otevřeno okno: Chyba – Soubor nelze načíst.", udalost[0]);
	}

	@Test
	public void zobrazeniSkutecneNastaveni() {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		final String uiScale = System.getProperty("sun.java2d.uiScale");
		Diagnostika.setPopisOfflineMapy(() -> "offline mapa ostrá, písmo 100 %");
		try {
			System.setProperty("sun.java2d.uiScale", "1");
			final String popis = Diagnostika.popisZobrazeni();
			Assert.assertTrue(popis, popis.matches("\\d+ %, zvětšení podle systému ne(, Direct3D (ano|ne))?, offline mapa ostrá, písmo 100 %"));
			Assert.assertTrue(Diagnostika.text().contains("\nZobrazení: " + popis + "\n"));
		} finally {
			Diagnostika.setPopisOfflineMapy(null);
			if (uiScale == null) {
				System.clearProperty("sun.java2d.uiScale");
			} else {
				System.setProperty("sun.java2d.uiScale", uiScale);
			}
		}
	}

	@Test
	public void tlacitkoVynulujeMereniAObnoviText() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		Vykon.vynuluj();
		Vykon.zaznamenej(Vykon.Velicina.EDT, 2_000_000_000L);
		SwingUtilities.invokeLater(() -> DiagnostikaAction.ukaz(null));
		Dialog dialog = null;
		for (int i = 0; i < 200 && dialog == null; i++) {
			Thread.sleep(25);
			for (final Window w : Window.getWindows()) {
				if (w instanceof Dialog && w.isShowing() && "Informace pro hlášení chyby".equals(((Dialog) w).getTitle())) {
					dialog = (Dialog) w;
				}
			}
		}
		Assert.assertNotNull(dialog);
		final Dialog d = dialog;
		final String[] text = new String[1];
		SwingUtilities.invokeAndWait(() -> {
			najdi(d, AbstractButton.class, b -> DiagnostikaAction.VYNULOVAT.equals(b.getText())).doClick();
			text[0] = najdi(d, JTextArea.class, a -> true).getText();
			Assert.assertTrue("okno zůstane otevřené", d.isShowing());
			d.dispose();
		});
		Assert.assertTrue("záznam před vynulováním zmizel", Vykon.souhrn(Vykon.Velicina.EDT).max < 1000);
		Assert.assertTrue(text[0], text[0].contains("Měření výkonu vynulováno"));
		Assert.assertFalse(text[0], text[0].contains("max 2000.0 ms"));
	}

	@Test
	public void oknoJeMenitelneAObsahSeRoztahujeTlacitkaZustanouDole() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		SwingUtilities.invokeAndWait(() -> {
			final JDialog d = DiagnostikaAction.sestav(null);
			try {
				Assert.assertTrue("okno jde zvětšovat", d.isResizable());
				Assert.assertTrue(d.getMinimumSize().width <= DiagnostikaAction.NEJMENSI.width);
				final JScrollPane posuvnik = najdi(d, JScrollPane.class, p -> true);
				final JButton zavrit = najdi(d, JButton.class, b -> "Zavřít".equals(b.getText()));
				d.setSize(700, 500);
				d.validate();
				final int sirka = posuvnik.getWidth();
				final int vyska = posuvnik.getHeight();
				Assert.assertTrue(sirka > 600 && vyska > 350);
				d.setSize(1000, 800);
				d.validate();
				Assert.assertTrue("text se roztahuje do šířky", posuvnik.getWidth() >= sirka + 250);
				Assert.assertTrue("text se roztahuje do výšky", posuvnik.getHeight() >= vyska + 250);
				final java.awt.Container tlacitka = zavrit.getParent();
				Assert.assertEquals("tlačítka mají stálou výšku", tlacitka.getPreferredSize().height, tlacitka.getHeight());
				Assert.assertEquals("tlačítka zůstávají dole", d.getContentPane().getHeight(), tlacitka.getY() + tlacitka.getHeight());
				Assert.assertTrue(najdi(d, JTextArea.class, a -> true).getText().length() > 0);
			} finally {
				d.dispose();
			}
		});
	}

	@Test
	public void vychoziVelikostOknaNepresahnePracovniPlochu() {
		Assert.assertEquals(new java.awt.Dimension(900, 650), DiagnostikaAction.vychoziVelikost(new java.awt.Rectangle(0, 0, 1920, 1080)));
		Assert.assertEquals(new java.awt.Dimension(720, 540), DiagnostikaAction.vychoziVelikost(new java.awt.Rectangle(0, 0, 800, 600)));
		Assert.assertEquals(new java.awt.Dimension(900, 450), DiagnostikaAction.vychoziVelikost(new java.awt.Rectangle(0, 0, 1280, 500)));
	}

	private static <T extends java.awt.Component> T najdi(final java.awt.Container kde, final Class<T> typ, final java.util.function.Predicate<T> podminka) {
		for (final java.awt.Component c : kde.getComponents()) {
			if (typ.isInstance(c) && podminka.test(typ.cast(c))) {
				return typ.cast(c);
			}
			if (c instanceof java.awt.Container) {
				final T t = najdi((java.awt.Container) c, typ, podminka);
				if (t != null) {
					return t;
				}
			}
		}
		return null;
	}
}
