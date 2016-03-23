package cz.geokuk.core.program;

import java.awt.Dimension;
import java.io.File;
import java.util.EnumMap;

import javax.swing.*;

import cz.geokuk.core.render.*;
import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboru;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboruChangedEvent;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.Filex;

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
	private JJedenSouborPanel jKesDir;
	private JJedenSouborPanel jCestyDir;
	private JJedenSouborPanel jNeGgtFile;
	private JJedenSouborPanel jAnoGgtFile;

	private JJedenSouborPanel jKachleCacheDir;
	private JJedenSouborPanel jGeogetDataDir;
	private JJedenSouborPanel jImage3rdPartyDir;

	private JJedenSouborPanel jImageMyDir;
	private JJedenSouborPanel jOziDir;
	private JJedenSouborPanel jKmzDir;

	private JJedenSouborPanel jPictureDir;

	private KesoidModel kesoidModel;

	private KachleModel kachleModel;

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

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void inject(final RenderModel renderModel) {
		this.renderModel = renderModel;
	}

	public void onEvent(final KachleUmisteniSouboruChangedEvent event) {
		final KachleUmisteniSouboru u = event.getUmisteniSouboru();
		jKachleCacheDir.setFilex(u.getKachleCacheDir());
	}

	public void onEvent(final KesoidUmisteniSouboruChangedEvent event) {
		final KesoidUmisteniSouboru u = event.getUmisteniSouboru();
		jKesDir.setFilex(u.getKesDir());
		jCestyDir.setFilex(u.getCestyDir());
		jGeogetDataDir.setFilex(u.getGeogetDataDir());

		jNeGgtFile.setFilex(u.getNeGgtFile());
		jAnoGgtFile.setFilex(u.getAnoGgtFile());
		jImage3rdPartyDir.setFilex(u.getImage3rdPartyDir());
		jImageMyDir.setFilex(u.getImageMyDir());
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
		jTabbedPane.addTab("Data", null, tab1, "Základní datové složky.");
		final JComponent tab2 = createTab();
		jTabbedPane.addTab("Ikony", null, tab2, "Složky s uživatelskými ikonami.");
		final JComponent tab3 = createTab();
		jTabbedPane.addTab("Rendr", null, tab3, "Výstupní složky pro rendrování.");
		final JComponent tab4 = createTab();
		jTabbedPane.addTab("Technické", null, tab4, "Technické složky jako jseou programové cache a podobně.");
		// tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		jKesDir = pridejJednuPolozkuproEdit(null, tab1, "Složka s keškami získaný z Geogetu nebo jiného programu.", true, false);
		jCestyDir = pridejJednuPolozkuproEdit(null, tab1, "Složka, do které se implicitně ukládají cesty.", true, false);
		jGeogetDataDir = pridejJednuPolozkuproEdit(null, tab1, "Datová složka geogetu.", true, true);

		jNeGgtFile = pridejJednuPolozkuproEdit(null, tab1, "Seznam keší, na které teď na výlet nepůjdeme (GGT pro Geoget).", false, false);
		jAnoGgtFile = pridejJednuPolozkuproEdit(null, tab1, "Seznam keší, které se chysáme jít lovit (GGT pro Geoget).", false, false);
		jImage3rdPartyDir = pridejJednuPolozkuproEdit(null, tab2, "Složka s rozšiřujícími obrázky jiných geokolegů.", true, true);
		jImageMyDir = pridejJednuPolozkuproEdit(null, tab2, "Složka s mými vlastními rozšiřujícími obrázky.", true, true);

		jOziDir = pridejJednuPolozkuproEdit(ESouborPanelName.OZI, tab3, "Složka pro rendrování kalibrovaných mapy pro OziExplorer", true, false);
		jKmzDir = pridejJednuPolozkuproEdit(ESouborPanelName.KMZ, tab3, "Složka pro rendrování KMZ souborů (GoogleEarthj)", true, false);
		jPictureDir = pridejJednuPolozkuproEdit(ESouborPanelName.PICTURE, tab3, "Složka pro rendrování obrázků map", true, false);

		jKachleCacheDir = pridejJednuPolozkuproEdit(null, tab4, "Složka s kachlemi uloženými na disk (možno promazávat).", true, false);

		pridejJednuPolozkuProCteni(null, tab4, "Složka s exporty výjimek (chybových hlášení)", new Filex(FExceptionDumper.EXCEPTION_DIR, false, true), true);
		if (FConst.JAR_DIR_EXISTUJE) {
			pridejJednuPolozkuProCteni(null, tab4, "Složka s programem (zde je geokuk.jar)", new Filex(FConst.JAR_DIR, false, true), true);
		}
		pridejJednuPolozkuProCteni(null, tab4, "Aktuální složka", new Filex(new File("").getAbsoluteFile(), false, true), true);
		ukonciPanel(tab1);
		ukonciPanel(tab2);
		ukonciPanel(tab3);
		ukonciPanel(tab4);
		add(jTabbedPane);
		add(Box.createVerticalGlue());

		final Box ulobox = Box.createHorizontalBox();
		final JButton ulozit = new JButton("Uložit");
		// ulozit.setAlignmentX(CENTER_ALIGNMENT);
		// JLabel ulozlabel = new JLabel("Po uložení změn v souborech bude program ukončen");
		// ulozlabel.setForeground(Color.RED);

		// ulobox.add(ulozlabel);
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

	private void registerEvents(final JButton ulozit) {
		ulozit.addActionListener(aE -> {
			try {
				{
					final KesoidUmisteniSouboru u1 = new KesoidUmisteniSouboru();
					u1.setKesDir(jKesDir.vezmiSouborAProver());
					u1.setCestyDir(jCestyDir.vezmiSouborAProver());

					u1.setGeogetDataDir(jGeogetDataDir.vezmiSouborAProver());
					u1.setImage3rdPartyDir(jImage3rdPartyDir.vezmiSouborAProver());
					u1.setImageMyDir(jImageMyDir.vezmiSouborAProver());
					u1.setNeGgtFile(jNeGgtFile.vezmiSouborAProver());
					u1.setAnoGgtFile(jAnoGgtFile.vezmiSouborAProver());
					kesoidModel.setUmisteniSouboru(u1);
				}
				{
					final KachleUmisteniSouboru u2 = new KachleUmisteniSouboru();
					u2.setKachleCacheDir(jKachleCacheDir.vezmiSouborAProver());
					kachleModel.setUmisteniSouboru(u2);
				}
				{
					final RenderUmisteniSouboru u3 = new RenderUmisteniSouboru();
					u3.setOziDir(jOziDir.vezmiSouborAProver());
					u3.setKmzDir(jKmzDir.vezmiSouborAProver());
					u3.setPictureDir(jPictureDir.vezmiSouborAProver());
					renderModel.setUmisteniSouboru(u3);
				}

				// Board.multiNacitacLoaderManager.startLoad(true);

				((JUmisteniSouboruDialog) SwingUtilities.getRoot(JPrehledSouboru.this)).dispose();
			} catch (final YNejdeTo e) {
				Dlg.error(e.getMessage());
			}
		});
	}

	private void ukonciPanel(final JComponent tab) {
		tab.remove(tab.getComponentCount() - 1); // odstranit mezeru za poslední podtovoru
		tab.add(Box.createVerticalGlue());
	}
}
