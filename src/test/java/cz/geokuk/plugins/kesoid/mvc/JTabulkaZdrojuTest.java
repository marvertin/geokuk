package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.*;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.importek.StavPolozky;
import cz.geokuk.plugins.kesoid.importek.TypZdroje;

/** Tabulka zdrojů: řádky, texty, třístavový typ a akce, které klik vyvolá. */
public class JTabulkaZdrojuTest {

	private final StavyZdrojuProTesty data = StavyZdrojuProTesty.vzorek();
	private final StavyZdrojuProTesty.Zaznam ovladani = new StavyZdrojuProTesty.Zaznam();

	private JTabulkaZdroju tabulka(final TypZdroje typ) {
		final JTabulkaZdroju t = new JTabulkaZdroju(typ);
		t.setOvladani(ovladani);
		t.obnov(data.snimek());
		return t;
	}

	private int radek(final JTabulkaZdroju t, final TypZdroje typ, final String nazev) {
		for (int i = 0; i < t.getRadky().size(); i++) {
			final JTabulkaZdroju.Radek r = t.getRadky().get(i);
			if (r.typ == typ && (nazev == null ? r.polozka == null : r.polozka != null && r.polozka.getNazev().equals(nazev))) {
				return i;
			}
		}
		throw new AssertionError(typ + " " + nazev);
	}

	private String text(final JTabulkaZdroju t, final TypZdroje typ, final String nazev, final int sloupec) {
		return t.text(t.getRadky().get(radek(t, typ, nazev)), sloupec);
	}

	private void klikNaNacist(final JTabulkaZdroju t, final int r) {
		klikNaNacist(t, r, 0);
	}

	private void klikNaNacist(final JTabulkaZdroju t, final int r, final int modifikatory) {
		final JTable tab = t.getTabulka();
		tab.setSize(700, 400);
		tab.doLayout();
		final java.awt.Rectangle bunka = tab.getCellRect(r, 0, true);
		final MouseEvent e = new MouseEvent(tab, MouseEvent.MOUSE_CLICKED, 0, modifikatory, bunka.x + 5, bunka.y + 5, 1, false);
		for (final java.awt.event.MouseListener l : tab.getMouseListeners()) {
			l.mouseClicked(e);
		}
	}

	private static StavyZdrojuProTesty mnohoPolozek(final int pocet) {
		final StavyZdrojuProTesty s = new StavyZdrojuProTesty();
		for (int i = 0; i < pocet; i++) {
			s.pridej(TypZdroje.GPX, "trasa" + i + ".gpx");
		}
		return s.prepis();
	}

	@Test
	public void vyskaPodlePoctuRadkuAzKDostupneVysce() {
		final JTabulkaZdroju t = new JTabulkaZdroju(TypZdroje.GPX);
		t.obnov(mnohoPolozek(40).snimek());
		Assert.assertTrue(t.getPreferredSize().height > 41 * JTabulkaZdroju.VYSKA_RADKU);
		t.nastavDostupnouVysku(500);
		Assert.assertEquals(500, t.getPreferredSize().height);
		t.nastavDostupnouVysku(5000);
		Assert.assertTrue(t.getPreferredSize().height < 5000);
		Assert.assertTrue(t.getPreferredSize().height > 41 * JTabulkaZdroju.VYSKA_RADKU);
	}

	private java.awt.Color pozadi(final JTabulkaZdroju t, final int r) {
		final JTable tab = t.getTabulka();
		return tab.prepareRenderer(tab.getCellRenderer(r, JTabulkaZdroju.SL_ZDROJ), r, JTabulkaZdroju.SL_ZDROJ).getBackground();
	}

	@Test
	public void radkyStridavePodbarvene() {
		final JTabulkaZdroju t = tabulka(null);
		final int typ = radek(t, TypZdroje.GEOGET, null);
		final java.awt.Color prvni = pozadi(t, typ + 1);
		final java.awt.Color druha = pozadi(t, typ + 2);
		Assert.assertNotEquals(prvni, druha);
		Assert.assertNotEquals(pozadi(t, typ), prvni);
		Assert.assertNotEquals(pozadi(t, typ), druha);
		Assert.assertEquals("každá skupina začíná stejně", prvni, pozadi(t, radek(t, TypZdroje.GSAK, null) + 1));
	}

	@Test
	public void uplnaMaVsechnyTypyAPolozky() {
		final JTabulkaZdroju t = tabulka(null);
		Assert.assertEquals(4 + 5, t.getRadky().size());
		Assert.assertEquals("Cesko.db3", text(t, TypZdroje.GEOGET, "Cesko.db3", JTabulkaZdroju.SL_ZDROJ));
	}

	@Test
	public void radekTypuUkazujeZkratku() {
		final JTabulkaZdroju t = tabulka(null);
		Assert.assertEquals("GPX (Ctrl+1)", text(t, TypZdroje.GPX, null, JTabulkaZdroju.SL_ZDROJ));
		Assert.assertEquals("GSAK (Ctrl+3)", text(t, TypZdroje.GSAK, null, JTabulkaZdroju.SL_ZDROJ));
		Assert.assertEquals("OpenSAK (Ctrl+4)", text(t, TypZdroje.OPENSAK, null, JTabulkaZdroju.SL_ZDROJ));
	}

	@Test
	public void detailMaJenSvujTyp() {
		final JTabulkaZdroju t = tabulka(TypZdroje.GEOGET);
		Assert.assertEquals(3, t.getRadky().size());
		Assert.assertNull(t.getRadky().get(0).polozka);
	}

	@Test
	public void duplicityANeznamyPocet() {
		final JTabulkaZdroju t = tabulka(null);
		Assert.assertEquals("38 204 z 40 100  ≠", text(t, TypZdroje.GEOGET, "Cesko.db3", JTabulkaZdroju.SL_WP).replace(' ', ' '));
		Assert.assertEquals("42 715 z 44 611  ≠", text(t, TypZdroje.GEOGET, null, JTabulkaZdroju.SL_WP).replace(' ', ' '));
		Assert.assertEquals("1 240", text(t, TypZdroje.GPX, "praha.gpx", JTabulkaZdroju.SL_WP).replace(' ', ' '));
		Assert.assertEquals("–", JTabulkaZdroju.wp(0, StavPolozky.NEZNAMO));
		Assert.assertTrue(JTabulkaZdroju.textDuplicit(1896).contains("ve více zdrojích"));
	}

	@Test
	public void nikdyNenactenaPolozkaMaPomlcku() {
		final StavyZdrojuProTesty s = new StavyZdrojuProTesty();
		s.pridej(TypZdroje.GSAK, "novy.db3");
		s.prepis();
		final JTabulkaZdroju t = new JTabulkaZdroju(null);
		t.obnov(s.snimek());
		Assert.assertEquals("–", text(t, TypZdroje.GSAK, "novy.db3", JTabulkaZdroju.SL_WP));
		Assert.assertEquals("–", text(t, TypZdroje.GSAK, null, JTabulkaZdroju.SL_WP));
		Assert.assertEquals("–", text(t, TypZdroje.OPENSAK, null, JTabulkaZdroju.SL_WP));
	}

	@Test
	public void stavyTextem() {
		final File gsak = data.snimek().getPolozky(TypZdroje.GSAK).get(0).getSoubor();
		data.registr.cekaNaZapis(data.registr.getGenerace(), gsak);
		final File gg = data.snimek().getPolozky(TypZdroje.GEOGET).get(0).getSoubor();
		data.registr.zacina(data.registr.getGenerace(), gg);
		data.registr.postup(data.registr.getGenerace(), gg, 40);
		final JTabulkaZdroju t = tabulka(null);
		Assert.assertEquals("Zamčeno jiným programem", text(t, TypZdroje.GSAK, "Domov.db3", JTabulkaZdroju.SL_STAV));
		Assert.assertEquals("Zamčeno jiným programem", text(t, TypZdroje.GSAK, null, JTabulkaZdroju.SL_STAV));
		Assert.assertEquals("Načítá se 40 %", text(t, TypZdroje.GEOGET, "Cesko.db3", JTabulkaZdroju.SL_STAV));
		Assert.assertEquals("Načítá se 70 %", text(t, TypZdroje.GEOGET, null, JTabulkaZdroju.SL_STAV));
	}

	@Test
	public void vypnutyTypMaPolozkySeZachovanouVolbouKlikZapneJenTuto() {
		data.vypnuteTypy.add(TypZdroje.GEOGET);
		data.prepisZapnuti();
		final JTabulkaZdroju t = tabulka(null);
		Assert.assertEquals("Vypnuto", text(t, TypZdroje.GEOGET, "Cesko.db3", JTabulkaZdroju.SL_STAV));
		Assert.assertTrue(t.getRadky().get(radek(t, TypZdroje.GEOGET, "Cesko.db3")).polozka.isZapnuto());
		Assert.assertEquals("Zapnout GeoGet jen s tímto souborem", t.tooltip(t.getRadky().get(radek(t, TypZdroje.GEOGET, "Slovensko.db3")), JTabulkaZdroju.SL_NACIST));
		klikNaNacist(t, radek(t, TypZdroje.GEOGET, "Slovensko.db3"));
		Assert.assertEquals(Arrays.asList("jenPolozka GEOGET Slovensko.db3"), ovladani.volani);
	}

	@Test
	public void klikNaPolozkuJiPrepne() {
		final JTabulkaZdroju t = tabulka(null);
		klikNaNacist(t, radek(t, TypZdroje.GEOGET, "Cesko.db3"));
		Assert.assertEquals(Arrays.asList("polozka Cesko.db3 false"), ovladani.volani);
	}

	@Test
	public void ctrlKlikVybereJenPolozkuNeboTyp() {
		final JTabulkaZdroju t = tabulka(null);
		Assert.assertEquals(JTabulkaZdroju.KLAVESA_JEN + "+klik: načítat jen tento soubor", t.tooltip(t.getRadky().get(radek(t, TypZdroje.GEOGET, "Cesko.db3")), JTabulkaZdroju.SL_NACIST));
		klikNaNacist(t, radek(t, TypZdroje.GEOGET, "Cesko.db3"), JTabulkaZdroju.MASKA_JEN);
		klikNaNacist(t, radek(t, TypZdroje.GEOGET, null), JTabulkaZdroju.MASKA_JEN);
		Assert.assertEquals(Arrays.asList("jenPolozka GEOGET Cesko.db3", "jenTyp GEOGET"), ovladani.volani);
	}

	@Test
	public void kontextoveMenuRadku() {
		final JTabulkaZdroju t = tabulka(null);
		final javax.swing.JPopupMenu menu = t.menu(t.getRadky().get(radek(t, TypZdroje.GEOGET, "Cesko.db3")));
		Assert.assertEquals(3, menu.getComponentCount());
		for (final java.awt.Component c : menu.getComponents()) {
			((javax.swing.JMenuItem) c).doClick();
		}
		final javax.swing.JPopupMenu menuTypu = t.menu(t.getRadky().get(radek(t, TypZdroje.GEOGET, null)));
		((javax.swing.JMenuItem) menuTypu.getComponent(0)).doClick();
		Assert.assertEquals(Arrays.asList("jenPolozka GEOGET Cesko.db3", "vseVTypu GEOGET true", "polozka Cesko.db3 false", "jenTyp GEOGET"), ovladani.volani);
	}

	@Test
	public void trojstavovyTyp() {
		// zapnuto → vypnout typ
		JTabulkaZdroju.klikTyp(data.snimek(), TypZdroje.GEOGET, ovladani);
		// částečně → vypnout typ
		data.vypnute.add(data.snimek().getPolozky(TypZdroje.GEOGET).get(0).getSoubor());
		data.prepisZapnuti();
		JTabulkaZdroju.klikTyp(data.snimek(), TypZdroje.GEOGET, ovladani);
		// vypnutý typ s výběrem → zapnout typ (vrátí výběr)
		data.vypnuteTypy.add(TypZdroje.GEOGET);
		data.prepisZapnuti();
		JTabulkaZdroju.klikTyp(data.snimek(), TypZdroje.GEOGET, ovladani);
		// bez vybrané položky → zapnout vše v typu
		data.vypnute.add(data.snimek().getPolozky(TypZdroje.GEOGET).get(1).getSoubor());
		data.prepisZapnuti();
		JTabulkaZdroju.klikTyp(data.snimek(), TypZdroje.GEOGET, ovladani);
		Assert.assertEquals(Arrays.asList("typ GEOGET false", "typ GEOGET false", "typ GEOGET true", "vseVTypu GEOGET true"), ovladani.volani);
	}

	@Test
	public void vseZapnoutAVypnout() {
		final JTabulkaZdroju uplna = tabulka(null);
		final JTabulkaZdroju detail = tabulka(TypZdroje.GSAK);
		odkazy(uplna)[0].doClick();
		odkazy(uplna)[1].doClick();
		odkazy(detail)[1].doClick();
		Assert.assertEquals(Arrays.asList("vse true", "vse false", "vseVTypu GSAK false"), ovladani.volani);
	}

	private static JButton[] odkazy(final JTabulkaZdroju t) {
		final JPanel panel = (JPanel) t.getComponent(0);
		return new JButton[] { (JButton) panel.getComponent(0), (JButton) panel.getComponent(2) };
	}

	@Test
	public void vUzkemOkneBezVelikosti() {
		final JTabulkaZdroju t = tabulka(null);
		Assert.assertEquals(5, t.getTabulka().getColumnCount());
		t.nastavDostupnouSirku(800);
		Assert.assertEquals(4, t.getTabulka().getColumnCount());
		Assert.assertTrue(t.getPreferredSize().width <= 800);
		t.nastavDostupnouSirku(1400);
		Assert.assertEquals(5, t.getTabulka().getColumnCount());
		Assert.assertEquals(JTabulkaZdroju.SL_VELIKOST, t.getTabulka().convertColumnIndexToModel(2));
	}

	@Test
	public void velikost() {
		Assert.assertEquals("1,5 MB", JTabulkaZdroju.velikost(1_500_000));
		Assert.assertEquals("0 kB", JTabulkaZdroju.velikost(0));
	}

	@Test
	public void problemSlozkyVRadkuTypu() {
		data.registr.setProblemySlozek(data.registr.getGenerace(), java.util.Collections.singletonMap(TypZdroje.GSAK, "Ve složce nejsou databáze .db3."));
		final JTabulkaZdroju t = tabulka(null);
		Assert.assertEquals("Ve složce nejsou databáze .db3.", text(t, TypZdroje.GSAK, null, JTabulkaZdroju.SL_STAV));
		Assert.assertEquals("Načteno", text(t, TypZdroje.GEOGET, null, JTabulkaZdroju.SL_STAV));
	}
}
