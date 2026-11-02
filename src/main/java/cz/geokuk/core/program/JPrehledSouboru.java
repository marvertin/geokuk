package cz.geokuk.core.program;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.*;

import cz.geokuk.core.render.*;
import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboru;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboruChangedEvent;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.util.file.Filex;
import cz.geokuk.util.lang.StringUtils;

public class JPrehledSouboru extends JPanel {

	public static class YNejdeTo extends Exception {

		private static final long serialVersionUID = -4020543178329529443L;

		/**
		 * @param aMessage
		 */
		public YNejdeTo(final String aMessage) {
			super(aMessage);
		}
	}

	private static final long serialVersionUID = -2491414463002815835L;
	/** Okno dialogu je ještě otevřené; zavřené během kontroly složek se nic neuloží. */
	java.util.function.BooleanSupplier oknoOtevrene = () -> {
		final java.awt.Window w = SwingUtilities.getWindowAncestor(this);
		return w == null || w.isShowing();
	};
	final JLabel kontroluji = new JLabel("Kontroluji složky…");
	private JJedenSouborPanel jKesDir;
	private JJedenSouborPanel jGeogetDataDir;

	private JJedenSouborPanel jGsakDataDir;
	private JCheckBox jGsakNacitatAzPoVybrani;
	private JTextField jGsakCasNalezu;

	private JJedenSouborPanel jOpensakDataDir;
	private JCheckBox jOpensakNacitatAzPoVybrani;

	private JJedenSouborPanel jOziDir;
	private JJedenSouborPanel jKmzDir;

	private JJedenSouborPanel jPictureDir;

	private JJedenSouborPanel jOfflineMapyDir;

	private KachleModel kachleModel;

	private KesoidModel kesoidModel;

	private RenderModel renderModel;

	private JTabbedPane jTabbedPane;

	private final EnumMap<ESouborPanelName, JJedenSouborPanel> mapaProFokusovani = new EnumMap<>(ESouborPanelName.class);

	public JPrehledSouboru(final Void v) {
		initComponents();
	}

	public void fokusni(final ESouborPanelName panelName) {
		final JJedenSouborPanel panel = mapaProFokusovani.get(panelName);
		if (panel == null) {
			return;
		}
		panel.fokusniSe();
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	public void inject(final RenderModel renderModel) {
		this.renderModel = renderModel;
	}

	public void onEvent(final KesoidUmisteniSouboruChangedEvent event) {
		final KesoidUmisteniSouboru u = event.getUmisteniSouboru();
		jKesDir.setFilex(u.getKesDir());
		jGeogetDataDir.setFilex(u.getGeogetDataDir());
		jGsakDataDir.setFilex(u.getGsakDataDir());
		jOpensakDataDir.setFilex(u.getOpensakDataDir());
	}

	public void onEvent(final GsakParametryNacitaniChangedEvent event) {
		final GsakParametryNacitani g = event.getGsakParametryNacitani();
		jGsakCasNalezu.setText(_join(g.getCasNalezu()));
		jGsakNacitatAzPoVybrani.setSelected(!g.isNoveDatabazeHned());
		jOpensakNacitatAzPoVybrani.setSelected(!g.isNoveDatabazeOpensakuHned());
	}

	public void onEvent(final KachleUmisteniSouboruChangedEvent event) {
		jOfflineMapyDir.setFilex(event.getUmisteniSouboru().getOfflineMapyDir());
	}

	public void onEvent(final RenderUmisteniSouboruChangedEvent event) {
		final RenderUmisteniSouboru u = event.getUmisteniSouboru();
		jPictureDir.setFilex(u.getPictureDir());
		jKmzDir.setFilex(u.getKmzDir());
		jOziDir.setFilex(u.getOziDir());
	}

	private JComponent createTab() {
		final JComponent tab = new JPanel();
		tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));
		return tab;
	}

	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		final int ho = 8;
		setBorder(BorderFactory.createEmptyBorder(ho, ho, ho, ho));

		jTabbedPane = new JTabbedPane();

		final JComponent tab1 = createTab();
		jTabbedPane.addTab("Keše", null, tab1, "Odkud program načítá keše.");
		final JComponent tab1a = createTab();
		jTabbedPane.addTab("GSAK", null, tab1a, "Načítání keší z GSAK.");
		final JComponent tab1b = createTab();
		jTabbedPane.addTab("OpenSAK", null, tab1b, "Načítání keší z OpenSAKu.");
		final JComponent tab2 = createTab();
		jTabbedPane.addTab("Mapy", null, tab2, "Offline mapy v počítači.");
		final JComponent tab3 = createTab();
		jTabbedPane.addTab("Rendr", null, tab3, "Výstupní složky pro rendrování.");
		final JComponent tab4 = createTab();
		jTabbedPane.addTab("Program", null, tab4, "Složky a soubory programu, všechny jsou ve složce s programem.");
		// tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		jKesDir = pridejJednuPolozkuproEdit(null, tab1, "Složka s keškami (GPX) získanými z Geogetu nebo jiného programu.", true, false);
		jGeogetDataDir = pridejJednuPolozkuproEdit(null, tab1, "Datová složka GeoGetu.", true, true).nezakladat();

		jGsakDataDir = pridejJednuPolozkuproEdit(null, tab1a, "Datová složka GSAK.", true, true).nezakladat();
		jGsakNacitatAzPoVybrani = pridejLogickePole(jGsakDataDir, "Nové databáze načíst až po vybrání",
		        "<html>" //
		                + "Databáze GSAKu, které GeoKuk ještě neviděl, se nenačtou hned, ale zůstanou blokované," //
		                + "<br/>dokud je nevyberete v tabulce zdrojů: najeďte myší na „Zdroje:“ ve stavovém řádku" //
		                + "<br/>nebo otevřete Soubor &gt; Přehled zdrojů." //
		                + "<br/>Po změně datové složky GSAK jsou blokované všechny databáze." //
		                + "</html>");
		jGsakCasNalezu = pridejTextovePole(jGsakDataDir, "Čas nálezu",
		        "<html>" //
		                + "Vestavěné nebo vlastní políčko v GSAK, které obsahuje čas nálezu keše ve tvaru <tt>HH:MM</tt>." //
		                + "<br/>Pokud obsahuje kromě časového údaje i další text, bude použit jen časový údaj." //
		                + "<br/>Můžete uvést více políček, použije se první časový údaj, který bude nalezen." //
		                + "</html>");

		jOpensakDataDir = pridejJednuPolozkuproEdit(null, tab1b, "Datová složka OpenSAKu.", true, true).nezakladat();
		jOpensakNacitatAzPoVybrani = pridejLogickePole(jOpensakDataDir, "Nové databáze načíst až po vybrání",
		        "<html>Databáze OpenSAKu, které GeoKuk ještě neviděl, se nenačtou hned, ale zůstanou blokované," //
		                + "<br/>dokud je nevyberete v tabulce zdrojů: najeďte myší na „Zdroje:“ ve stavovém řádku" //
		                + "<br/>nebo otevřete Soubor &gt; Přehled zdrojů.</html>");

		jOfflineMapyDir = pridejJednuPolozkuproEdit(null, tab2, "Offline mapy: soubory .map (třeba z osm.paws.cz) a k nim stažená témata (.zip, .xml).", true, false);

		jOziDir = pridejJednuPolozkuproEdit(ESouborPanelName.OZI, tab3, "Složka pro rendrování kalibrovaných map pro OziExplorer", true, false);
		jKmzDir = pridejJednuPolozkuproEdit(ESouborPanelName.KMZ, tab3, "Složka pro rendrování KMZ souborů (Google Earth)", true, false);
		jPictureDir = pridejJednuPolozkuproEdit(ESouborPanelName.PICTURE, tab3, "Složka pro rendrování obrázků map", true, false);

		pridejJednuPolozkuProCteni(null, tab4, "Složka s programem (zde je geokuk.jar)", new Filex(FConst.JAR_DIR, false, true), true);
		pridejJednuPolozkuProCteni(null, tab4, "Data programu: nastavení, cache, cesty, ikony, výlety a logy", new Filex(FConst.DATA_DIR, false, true), true);
		pridejJednuPolozkuProCteni(null, tab4, "Cesty: výchozí složka pro ukládání cest", KesoidUmisteniSouboru.CESTY_DIR, true);
		pridejJednuPolozkuProCteni(null, tab4, "Ikony: vlastní ikony (moje) a ikony od jiných geokolegů (ostatni)", new Filex(KesoidUmisteniSouboru.IKONY_DIR, false, true), true);
		pridejJednuPolozkuProCteni(null, tab4, "Výlety: keše, které jdeme lovit (lovim.ggt) a na které teď nepůjdeme (tedne.ggt)", new Filex(KesoidUmisteniSouboru.VYLETY_DIR, false, true), true);
		pridejJednuPolozkuProCteni(null, tab4, "Cache: dlaždice map uložené na disk (lze smazat)", KachleUmisteniSouboru.KACHLE_CACHE_DIR, true);
		pridejJednuPolozkuProCteni(null, tab4, "Log a chybová hlášení", new Filex(UmisteniProgramu.log(), false, true), true);
		ukonciPanel(tab1);
		ukonciPanel(tab1a);
		ukonciPanel(tab1b);
		ukonciPanel(tab2);
		ukonciPanel(tab3);
		ukonciPanel(tab4);
		add(jTabbedPane);
		add(Box.createVerticalGlue());

		final Box ulobox = Box.createHorizontalBox();
		final JButton ulozit = new JButton("Uložit");

		kontroluji.setVisible(false);
		ulobox.add(kontroluji);
		ulobox.add(Box.createHorizontalGlue());
		ulobox.add(ulozit);
		ulobox.setAlignmentX(LEFT_ALIGNMENT);
		add(ulobox);

		// jTabbedPane.setSelectedIndex(2);
		// jKmzDir.requestFocus();
		registerEvents(ulozit);
	}

	private JJedenSouborPanel pridejJednuPolozkuProCteni(final ESouborPanelName souborPanelName, final JComponent tab, final String label, final Filex hodnota, final boolean jenAdresare) {
		final JJedenSouborPanel panel = new JJedenSouborPanel(souborPanelName, label, jenAdresare, false, false);
		panel.setFilex(hodnota);
		tab.add(panel);
		tab.add(Box.createRigidArea(new Dimension(0, 20)));
		panel.setMaximumSize(new Dimension(1000, 40));
		if (souborPanelName != null) {
			mapaProFokusovani.put(souborPanelName, panel);
		}
		return panel;
	}

	private JJedenSouborPanel pridejJednuPolozkuproEdit(final ESouborPanelName souborPanelName, final JComponent tab, final String label, final boolean jenAdresare, final boolean lzeDeaktivovat) {
		final JJedenSouborPanel panel = new JJedenSouborPanel(souborPanelName, label, jenAdresare, true, lzeDeaktivovat);
		tab.add(panel);
		tab.add(Box.createRigidArea(new Dimension(0, 20)));
		// Zjištěno, že to funguje, pokud je tam i lepidlo
		// panel.setMaximumSize(new Dimension(1000, 40));
		panel.setMaximumSize(new Dimension(panel.getMaximumSize().width, panel.getPreferredSize().height));
		if (souborPanelName != null) {
			mapaProFokusovani.put(souborPanelName, panel);
		}
		return panel;
	}

	private JTextField pridejTextovePole(final JComponent aParent, final String aLabel, final String aHint) {
		final JTextField input = new JTextField();
		input.setToolTipText(aHint);

		final JLabel label = new JLabel(aLabel);
		label.setToolTipText(aHint);
		label.setLabelFor(input);

		aParent.add(label);
		aParent.add(input);

		return input;
	}

	private JCheckBox pridejLogickePole(final JComponent aParent, final String aLabel, final String aHint) {
		final JCheckBox input = new JCheckBox(aLabel);
		input.setToolTipText(aHint);

		aParent.add(input);

		return input;
	}

	private void registerEvents(final JButton ulozit) {
		ulozit.addActionListener(aE -> {
			final Ulozeni ulozeni = priprav();
			// Prověření složek sahá na disk (síťový disk může dlouho neodpovídat), proto mimo EDT; dialog mezitím ukazuje, že pracuje.
			final Component okno = SwingUtilities.getRoot(this);
			ulozit.setEnabled(false);
			kontroluji.setVisible(true);
			if (okno != null) {
				okno.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws YNejdeTo {
					ulozeni.prover();
					return null;
				}

				@Override
				protected void done() {
					if (okno != null) {
						okno.setCursor(Cursor.getDefaultCursor());
					}
					kontroluji.setVisible(false);
					ulozit.setEnabled(true);
					if (!oknoOtevrene.getAsBoolean()) {
						return; // dialog zavřený křížkem během kontroly = zrušené uložení, ani chyba se už neukazuje
					}
					try {
						get();
						ulozeni.zapis();
						if (okno instanceof JUmisteniSouboruDialog) {
							((JUmisteniSouboruDialog) okno).dispose();
						}
					} catch (final ExecutionException e) {
						Dlg.error(e.getCause() instanceof YNejdeTo ? e.getCause().getMessage() : "Složky nejde prověřit.");
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}.execute();
		});
	}

	void uloz() throws YNejdeTo {
		final Ulozeni ulozeni = priprav();
		ulozeni.prover();
		ulozeni.zapis();
	}

	/** Hodnoty z dialogu (EDT), jejich prověření na disku (mimo EDT) a zápis do modelů (EDT). */
	private final class Ulozeni {
		final GsakParametryNacitani g = new GsakParametryNacitani();
		final KesoidUmisteniSouboru u1 = new KesoidUmisteniSouboru();
		final RenderUmisteniSouboru u3 = new RenderUmisteniSouboru();
		final KachleUmisteniSouboru u2 = new KachleUmisteniSouboru();
		final Map<JJedenSouborPanel, Filex> kProvereni = new LinkedHashMap<>();

		Filex vezmi(final JJedenSouborPanel panel) {
			final Filex f = panel.vezmiSoubor();
			kProvereni.put(panel, f);
			return f;
		}

		void prover() throws YNejdeTo {
			for (final Map.Entry<JJedenSouborPanel, Filex> e : kProvereni.entrySet()) {
				e.getKey().prover(e.getValue());
			}
		}

		void zapis() {
			// Změna složky GSAK podle volby „Nové databáze načíst až po vybrání“ zakazuje nové databáze, volba proto musí být uložená dřív.
			kesoidModel.setGsakParametryNacitani(g);
			kesoidModel.setUmisteniSouboru(u1);
			renderModel.setUmisteniSouboru(u3);
			kachleModel.setUmisteniSouboru(u2);
		}
	}

	private Ulozeni priprav() {
		final Ulozeni u = new Ulozeni();
		u.g.setCasNalezu(_split(jGsakCasNalezu.getText()));
		u.g.setNoveDatabazeHned(!jGsakNacitatAzPoVybrani.isSelected());
		u.g.setNoveDatabazeOpensakuHned(!jOpensakNacitatAzPoVybrani.isSelected());

		u.u1.setKesDir(u.vezmi(jKesDir));
		u.u1.setCestyDir(KesoidUmisteniSouboru.CESTY_DIR);
		u.u1.setGeogetDataDir(u.vezmi(jGeogetDataDir));
		u.u1.setGsakDataDir(u.vezmi(jGsakDataDir));
		u.u1.setOpensakDataDir(u.vezmi(jOpensakDataDir));
		u.u1.setImage3rdPartyDir(KesoidUmisteniSouboru.IMAGE_3RDPARTY_DIR);
		u.u1.setImageMyDir(KesoidUmisteniSouboru.IMAGE_MY_DIR);
		u.u1.setNeGgtFile(KesoidUmisteniSouboru.NE_GGT);
		u.u1.setAnoGgtFile(KesoidUmisteniSouboru.ANO_GGT);

		u.u2.setKachleCacheDir(KachleUmisteniSouboru.KACHLE_CACHE_DIR);
		u.u2.setOfflineMapyDir(u.vezmi(jOfflineMapyDir));

		u.u3.setOziDir(u.vezmi(jOziDir));
		u.u3.setKmzDir(u.vezmi(jKmzDir));
		u.u3.setPictureDir(u.vezmi(jPictureDir));
		return u;
	}

	private String _join(final Collection<String> aValues) {
		return aValues.stream().collect(Collectors.joining(";"));
	}

	private Collection<String> _split(final String text) {
		return Arrays.stream((text == null ? "" : text).split("[;:,]"))//
		        .filter(s -> !StringUtils.isBlank(s))//
		        .map(s -> s.trim())//
		        .collect(Collectors.toList())//
		;
	}

	private void ukonciPanel(final JComponent tab) {
		tab.remove(tab.getComponentCount() - 1); // odstranit mezeru za poslední podtovoru
		tab.add(Box.createVerticalGlue());
	}
}
