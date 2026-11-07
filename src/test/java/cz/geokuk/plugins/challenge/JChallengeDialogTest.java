package cz.geokuk.plugins.challenge;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assume;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Dialog plánovače challenge: seznam, kopie šablony, úprava vlastní definice, zapnutí; potřebuje displej. */
public class JChallengeDialogTest {

	private ChallengeModel model;
	private JChallengeDialog dialog;

	@Before
	public void setUp() throws Exception {
		Assume.assumeFalse("bez displeje", java.awt.GraphicsEnvironment.isHeadless());
		SwingUtilities.invokeAndWait(() -> {
			model = new ChallengeModel();
			model.inject(new EventFirer() {
				@Override
				public void fire(final Event0<?> event) {
					if (dialog != null) {
						dialog.onEvent((ChallengeZmenenyEvent) event);
					}
				}
			});
			final MapoveUloziste u = new MapoveUloziste();
			model.setUloziste(() -> u);
			model.initAfterInject();
			dialog = new JChallengeDialog();
			dialog.inject(model);
		});
	}

	@After
	public void tearDown() throws Exception {
		if (dialog != null) {
			SwingUtilities.invokeAndWait(() -> dialog.dispose());
		}
	}

	private static <T extends Component> List<T> najdi(final Container c, final Class<T> typ, final List<T> vysledek) {
		for (final Component k : c.getComponents()) {
			if (typ.isInstance(k)) {
				vysledek.add(typ.cast(k));
			}
			if (k instanceof Container) {
				najdi((Container) k, typ, vysledek);
			}
		}
		return vysledek;
	}

	private AbstractButton tlacitko(final String text) {
		for (final AbstractButton b : najdi(dialog.getContentPane(), AbstractButton.class, new ArrayList<AbstractButton>())) {
			if (text.equals(b.getText())) {
				return b;
			}
		}
		throw new AssertionError("tlačítko " + text);
	}

	private JTextField pole(final int index) {
		return najdi(dialog.getContentPane(), JTextField.class, new ArrayList<JTextField>()).get(index);
	}

	private String chyba() {
		for (final JLabel l : najdi(dialog.getContentPane(), JLabel.class, new ArrayList<JLabel>())) {
			if (l.getForeground().getRed() == 180 && l.getForeground().getGreen() == 30) {
				return l.getText().trim();
			}
		}
		throw new AssertionError("štítek s chybou");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Challenge vybrana(final JChallengeDialog d) {
		return ((JList<Challenge>) najdi(d.getContentPane(), JList.class, new ArrayList<JList>()).get(0)).getSelectedValue();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JList<Challenge> seznam() {
		return najdi(dialog.getContentPane(), JList.class, new ArrayList<JList>()).get(0);
	}

	private void naEdt(final Runnable r) throws Exception {
		SwingUtilities.invokeAndWait(r);
	}

	@Test
	public void seznamMaSablonyAPrvniJeVybranaAJenKProhlizeni() throws Exception {
		naEdt(() -> {
			Assert.assertEquals(4, dialog.polozky().size());
			Assert.assertEquals(0, seznam().getSelectedIndex());
			Assert.assertTrue("šablona se kopíruje", tlacitko("Vytvořit kopii…").isEnabled());
			Assert.assertFalse("šablonu nejde uložit", tlacitko("Uložit změny").isEnabled());
			Assert.assertFalse("šablonu nejde smazat", tlacitko("Smazat").isEnabled());
			Assert.assertFalse("název šablony nejde měnit", pole(0).isEnabled());
		});
	}

	@Test
	public void kopieSablonyJeVybranaAJdeUpravit() throws Exception {
		naEdt(() -> {
			tlacitko("Vytvořit kopii…").doClick();
			Assert.assertEquals(5, dialog.polozky().size());
			final Challenge vybrana = seznam().getSelectedValue();
			Assert.assertEquals("360° od domova (vlastní)", vybrana.nazev());
			Assert.assertTrue(tlacitko("Uložit změny").isEnabled());
			Assert.assertTrue(tlacitko("Smazat").isEnabled());
			Assert.assertTrue(tlacitko("Vytvořit kopii…").isEnabled());
			Assert.assertEquals(1, model.vlastni().size());
		});
	}

	@Test
	public void ulozeniZmeniNazevASouradniceVlastniDefinice() throws Exception {
		naEdt(() -> {
			tlacitko("Vytvořit kopii…").doClick();
			tlacitko("Souřadnice").doClick();
			pole(0).setText("Moje 360");
			pole(1).setText("N50°06.000 E14°30.000");
			tlacitko("Uložit změny").doClick();
			Assert.assertEquals(chyba(), " ".trim(), chyba());
			Assert.assertEquals(1, model.vlastni().size());
			final Challenge c = model.vlastni().get(0);
			Assert.assertEquals("Moje 360", c.nazev());
			Assert.assertEquals(Challenge.DruhStredu.SOURADNICE, c.stred().druh());
			Assert.assertEquals(50.1, c.stred().lat(), 1e-9);
			Assert.assertEquals(14.5, c.stred().lon(), 1e-9);
			Assert.assertEquals("po uložení je vybrána přejmenovaná", "Moje 360", seznam().getSelectedValue().nazev());
			Assert.assertEquals(5, dialog.polozky().size());
		});
	}

	@Test
	public void chybnyVstupSeHlasiAModelSeNezmeni() throws Exception {
		naEdt(() -> {
			tlacitko("Vytvořit kopii…").doClick();
			final Challenge pred = model.vlastni().get(0);
			tlacitko("Souřadnice").doClick();
			pole(1).setText("N95°00.000 E14°00.000");
			tlacitko("Uložit změny").doClick();
			Assert.assertTrue(chyba(), chyba().contains("Souřadnice neznám"));
			pole(1).setText("nesmysl");
			tlacitko("Uložit změny").doClick();
			Assert.assertTrue(chyba(), chyba().contains("Souřadnice neznám. Zadejte např. N50°03.253 E14°23.399."));
			Assert.assertEquals(pred, model.vlastni().get(0));
			pole(0).setText("");
			tlacitko("Domov").doClick();
			tlacitko("Uložit změny").doClick();
			Assert.assertTrue(chyba(), chyba().contains("název"));
			Assert.assertEquals(pred, model.vlastni().get(0));
		});
	}

	@Test
	public void zapnutiSeProjeviVModelu() throws Exception {
		naEdt(() -> {
			final AbstractButton zapnuto = tlacitko("Zapnuto v plánovači");
			Assert.assertFalse(model.jeAktivni(seznam().getSelectedValue()));
			zapnuto.doClick();
			Assert.assertTrue(model.jeAktivni(Sablony.od360Domova()));
			zapnuto.doClick();
			Assert.assertFalse(model.jeAktivni(Sablony.od360Domova()));
		});
	}

	@Test
	public void zmenaVModeluSePromitneDoSeznamu() throws Exception {
		naEdt(() -> {
			model.ulozVlastni(new Challenge("Zvenku", Challenge.Typ.SEKTORY_360, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.STATOR));
			Assert.assertEquals(5, dialog.polozky().size());
			Assert.assertEquals("Zvenku", dialog.polozky().get(4).nazev());
		});
	}

	@Test
	public void ulozeniNadSablonouNicNezmeniAniNehlasi() throws Exception {
		naEdt(() -> {
			final AbstractButton uloz = tlacitko("Uložit změny");
			Assert.assertFalse(uloz.isEnabled());
			for (final java.awt.event.ActionListener l : uloz.getActionListeners()) {
				l.actionPerformed(new java.awt.event.ActionEvent(uloz, 0, ""));
			}
			Assert.assertTrue("šablona se nepřepíše ani nezkopíruje", model.vlastni().isEmpty());
			Assert.assertEquals("bez chybové hlášky", "", chyba());
			Assert.assertEquals(4, dialog.polozky().size());
		});
	}

	@Test
	public void dialogMaViditelneTlacitkoZavritPodObsahem() throws Exception {
		naEdt(() -> {
			dialog.setSize(760, 420);
			dialog.validate();
			final AbstractButton zavrit = tlacitko("Zavřít");
			Assert.assertTrue(zavrit.isShowing() || zavrit.isVisible());
			Assert.assertTrue("tlačítko má velikost", zavrit.getWidth() > 0 && zavrit.getHeight() > 0);
			final java.awt.Point p = javax.swing.SwingUtilities.convertPoint(zavrit, 0, 0, dialog.getContentPane());
			final java.awt.Point uloz = javax.swing.SwingUtilities.convertPoint(tlacitko("Uložit změny"), 0, 0, dialog.getContentPane());
			Assert.assertTrue("Zavřít je pod formulářem: " + p + " × " + uloz, p.y > uloz.y);
			Assert.assertTrue("a uvnitř dialogu", p.y + zavrit.getHeight() <= dialog.getContentPane().getHeight());
			zavrit.doClick();
			Assert.assertFalse("zavře dialog", dialog.isDisplayable());
		});
	}

	/** Je komponenta viditelná, když by byl dialog zobrazen: viditelná je sama i všichni její rodiče. */
	private static boolean viditelne(final Component c) {
		for (Component k = c; k != null; k = k.getParent()) {
			if (!k.isVisible() && !(k instanceof java.awt.Window)) {
				return false;
			}
		}
		return true;
	}

	private Challenge dt(final String nazev) {
		return new Challenge(nazev, Challenge.Typ.DT_MATICE, Challenge.Stred.souradnice(50, 14), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC);
	}

	@Test
	public void dtJdeVDialoguUpravitZapnoutASmazat() throws Exception {
		naEdt(() -> {
			model.ulozVlastni(dt("Moje DT"));
			model.ulozVlastni(new Challenge("Kal", Challenge.Typ.KALENDAR, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
			Assert.assertEquals("šablony, D/T a kalendář (nedostupný)", 6, dialog.polozky().size());
			seznam().setSelectedValue(dialog.polozky().get(4), true);
			Assert.assertEquals("Moje DT", seznam().getSelectedValue().nazev());
			Assert.assertTrue("D/T jde uložit", tlacitko("Uložit změny").isEnabled());
			Assert.assertFalse("pole 360° jsou u D/T skrytá", viditelne(tlacitko("Domov")));
			Assert.assertFalse(viditelne(tlacitko("Souřadnice")));
			Assert.assertFalse(viditelne(tlacitko("Keš (kód)")));
			Assert.assertTrue("název je vidět", viditelne(pole(0)));
			pole(0).setText("Přejmenované DT");
			tlacitko("Uložit změny").doClick();
			Assert.assertEquals(chyba(), "", chyba());
			final Challenge ulozena = model.vlastni().get(0);
			Assert.assertEquals("Přejmenované DT", ulozena.nazev());
			Assert.assertEquals("typ zůstal", Challenge.Typ.DT_MATICE, ulozena.typ());
			Assert.assertEquals("střed zůstal", Challenge.Stred.souradnice(50, 14), ulozena.stred());
			final AbstractButton zapnuto = tlacitko("Zapnuto v plánovači");
			zapnuto.doClick();
			Assert.assertTrue(model.jeAktivni(ulozena));
			tlacitko("Vytvořit kopii…");
			seznam().setSelectedIndex(0);
			Assert.assertTrue("u šablony 360° jsou pole vidět", viditelne(tlacitko("Domov")));
			Assert.assertTrue(viditelne(tlacitko("Souřadnice")));
		});
	}

	private String textPolozky(final Challenge c) {
		final javax.swing.ListCellRenderer<? super Challenge> r = seznam().getCellRenderer();
		return ((JLabel) r.getListCellRendererComponent(seznam(), c, 0, false, false)).getText();
	}

	@Test
	public void ulozenyKalendarJdeVDialoguJenVypnoutASmazat() throws Exception {
		naEdt(() -> {
			final Challenge kal = model.ulozVlastni(new Challenge("Kal", Challenge.Typ.KALENDAR, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
			model.aktivuj(kal);
			Assert.assertTrue(dialog.polozky().contains(kal));
			Assert.assertTrue(textPolozky(kal), textPolozky(kal).contains("(nedostupný)"));
			seznam().setSelectedValue(kal, true);
			Assert.assertFalse("úpravy ne", tlacitko("Uložit změny").isEnabled());
			Assert.assertFalse(pole(0).isEnabled());
			Assert.assertTrue("smazat ano", tlacitko("Smazat").isEnabled());
			final AbstractButton zapnuto = tlacitko("Zapnuto v plánovači");
			Assert.assertTrue("vypnout ano", zapnuto.isEnabled());
			Assert.assertTrue(zapnuto.isSelected());
			zapnuto.doClick();
			Assert.assertFalse(model.jeAktivni(kal));
			Assert.assertFalse("zapnout znovu už ne", zapnuto.isEnabled());
			Assert.assertTrue("data zůstala", model.vlastni().contains(kal));
		});
	}

	@Test
	public void kopieSablonyMaticeDtJeDtAJdeUpravit() throws Exception {
		naEdt(() -> {
			Challenge sablona = null;
			for (final Challenge c : dialog.polozky()) {
				if (c.typ() == Challenge.Typ.DT_MATICE) {
					sablona = c;
				}
			}
			Assert.assertNotNull("šablona D/T je v seznamu", sablona);
			seznam().setSelectedValue(sablona, true);
			Assert.assertFalse("šablonu nejde upravit", tlacitko("Uložit změny").isEnabled());
			tlacitko("Vytvořit kopii…").doClick();
			final Challenge kopie = seznam().getSelectedValue();
			Assert.assertEquals(Challenge.Typ.DT_MATICE, kopie.typ());
			Assert.assertEquals("Matice D/T (vlastní)", kopie.nazev());
			Assert.assertTrue(tlacitko("Uložit změny").isEnabled());
			tlacitko("Zapnuto v plánovači").doClick();
			Assert.assertTrue(model.jeAktivni(kopie));
		});
	}

	@Test
	public void poOtevreniJeVybranaChallengeZobrazenaVPruhu() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final List<Challenge> vsechny = model.vsechny();
			final Challenge druha = vsechny.get(1);
			model.aktivuj(vsechny.get(0));
			model.aktivuj(druha);
			model.setZobrazena(druha);
			final JChallengeDialog novy = new JChallengeDialog();
			try {
				novy.inject(model);
				Assert.assertEquals(druha.nazev(), vybrana(novy).nazev());
			} finally {
				novy.dispose();
			}
		});
	}

	/** Pole souřadnic se nesmí oříznout ani při výchozí velikosti dialogu a písmu 150 %. */
	@Test
	public void poleSouradnicSeVejdeCeleVeVychoziVelikostiA150Procent() throws Exception {
		naEdt(() -> {
			tlacitko("Vytvořit kopii…").doClick();
			tlacitko("Souřadnice").doClick();
			final JTextField p = pole(1);
			for (final double meritko : new double[] { 1.0, 1.5 }) {
				zvetsiPismo(dialog.getContentPane(), meritko);
				p.setText(FormularDefinice.UKAZKA_SOURADNIC);
				dialog.pack();
				dialog.validate();
				final int potreba = p.getFontMetrics(p.getFont()).stringWidth(FormularDefinice.UKAZKA_SOURADNIC) + p.getInsets().left + p.getInsets().right;
				Assert.assertTrue(meritko + ": šířka pole " + p.getWidth() + " < " + potreba, p.getWidth() >= potreba);
				Assert.assertTrue(meritko + ": pole je celé v dialogu", SwingUtilities.convertRectangle(p.getParent(), p.getBounds(), dialog.getContentPane()).getMaxX() <= dialog.getContentPane().getWidth());
			}
		});
	}

	private static void zvetsiPismo(final Component c, final double meritko) {
		if (c.getFont() != null) {
			c.setFont(c.getFont().deriveFont((float) (12 * meritko)));
		}
		if (c instanceof Container) {
			for (final Component k : ((Container) c).getComponents()) {
				zvetsiPismo(k, meritko);
			}
		}
	}

	@Test
	public void kopieVlastniChallengeJeVybranaNeaktivniAJdeUpravit() throws Exception {
		naEdt(() -> {
			tlacitko("Vytvořit kopii…").doClick();
			final Challenge vlastni = seznam().getSelectedValue();
			Assert.assertTrue("kopii jde vytvořit i z vlastní", tlacitko("Vytvořit kopii…").isEnabled());
			tlacitko("Vytvořit kopii…").doClick();
			final Challenge kopie = seznam().getSelectedValue();
			Assert.assertEquals(vlastni.nazev() + " (kopie)", kopie.nazev());
			Assert.assertEquals(vlastni.typ(), kopie.typ());
			Assert.assertFalse(model.jeAktivni(kopie));
			Assert.assertTrue(tlacitko("Uložit změny").isEnabled());
			Assert.assertEquals(2, model.vlastni().size());
		});
	}

	/** Seznam nemá vodorovný posuvník a přípona „(šablona)“ je u každé šablony vidět. */
	@Test
	public void seznamBezPosuvnikuSPriponouSablony() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			dialog.pack();
			dialog.setSize(dialog.getWidth(), dialog.getHeight());
			dialog.validate();
			final javax.swing.JScrollPane sp = najdi(dialog.getContentPane(), javax.swing.JScrollPane.class, new ArrayList<javax.swing.JScrollPane>()).get(0);
			final JList<?> seznam = (JList<?>) sp.getViewport().getView();
			Assert.assertFalse("vodorovný posuvník", sp.getHorizontalScrollBar().isVisible());
			final int sirka = sp.getViewport().getWidth();
			for (int i = 0; i < seznam.getModel().getSize(); i++) {
				final JLabel l = (JLabel) vykresli(seznam, i);
				l.setSize(sirka, l.getPreferredSize().height);
				Assert.assertEquals("položka " + i, 1, l.getPreferredSize().width);
				Assert.assertTrue(l.getText(), l.getText().contains("(šablona)"));
			}
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Component vykresli(final JList<?> seznam, final int i) {
		return ((javax.swing.ListCellRenderer) seznam.getCellRenderer()).getListCellRendererComponent(seznam, seznam.getModel().getElementAt(i), i, false, false);
	}

	/** Volba pravidel jmenuje služby i tvar Země a tooltip vysvětluje rozdíl. */
	@Test
	public void volbaPravidelMaNazvySluzebAVysvetleni() throws Exception {
		naEdt(() -> {
			javax.swing.JComponent pravidla = null;
			for (final Component k : vsechnyKomponenty(dialog.getContentPane(), new ArrayList<Component>())) {
				if (k instanceof javax.swing.JComboBox && ((javax.swing.JComboBox<?>) k).getItemCount() == 2 && "Project-GC (koule)".equals(((javax.swing.JComboBox<?>) k).getItemAt(0))) {
					pravidla = (javax.swing.JComponent) k;
				}
			}
			Assert.assertNotNull("volba pravidel s položkami Koule a Elipsoid", pravidla);
			Assert.assertEquals("Stator (elipsoid)", ((javax.swing.JComboBox<?>) pravidla).getItemAt(1));
			Assert.assertTrue(pravidla.getToolTipText(), pravidla.getToolTipText().contains("Stator: elipsoid WGS84, včetně Lab, bez Locationless.") && !pravidla.getToolTipText().contains("ignorovaných"));
		});
	}

	private static List<Component> vsechnyKomponenty(final Container c, final List<Component> vysledek) {
		for (final Component k : c.getComponents()) {
			vysledek.add(k);
			if (k instanceof Container) {
				vsechnyKomponenty((Container) k, vysledek);
			}
		}
		return vysledek;
	}
}
