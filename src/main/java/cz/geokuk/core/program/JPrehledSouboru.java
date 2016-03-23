package cz.geokuk.core.program;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EnumMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import cz.geokuk.core.render.RenderModel;
import cz.geokuk.core.render.RenderUmisteniSouboru;
import cz.geokuk.core.render.RenderUmisteniSouboruChangedEvent;
import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidUmisteniSouboru;
import cz.geokuk.plugins.kesoid.mvc.KesoidUmisteniSouboruChangedEvent;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboru;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboruChangedEvent;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.Filex;

public class JPrehledSouboru extends JPanel {

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


	public JPrehledSouboru(Void v) {
		initComponents();
	}

	private JComponent createTab() {
		JComponent tab = new JPanel();
		tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));
		return tab;
	}

	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		int ho = 8;
		setBorder(BorderFactory.createEmptyBorder(ho, ho, ho, ho));

		jTabbedPane = new JTabbedPane();

		JComponent tab1 = createTab();
		jTabbedPane.addTab("Data", null, tab1, "Základní datové složky.");
		JComponent tab2 = createTab();
		jTabbedPane.addTab("Ikony", null, tab2, "Složky s uživatelskými ikonami.");
		JComponent tab3 = createTab();
		jTabbedPane.addTab("Rendr", null, tab3, "Výstupní složky pro rendrování.");
		JComponent tab4 = createTab();
		jTabbedPane.addTab("Technické", null, tab4, "Technické složky jako jseou programové cache a podobně.");
		//tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);


		jKesDir =  pridejJednuPolozkuproEdit(null, tab1, "Složka s keškami získaný z Geogetu nebo jiného programu.",  true, false);
		jCestyDir =  pridejJednuPolozkuproEdit(null, tab1, "Složka, do které se implicitně ukládají cesty.",  true, false);
		jGeogetDataDir =  pridejJednuPolozkuproEdit(null, tab1, "Datová složka geogetu.", true, true);

		jNeGgtFile = pridejJednuPolozkuproEdit(null, tab1, "Seznam keší, na které teď na výlet nepůjdeme (GGT pro Geoget).",  false,  false);
		jAnoGgtFile = pridejJednuPolozkuproEdit(null, tab1, "Seznam keší, které se chysáme jít lovit (GGT pro Geoget).",  false,  false);
		jImage3rdPartyDir = pridejJednuPolozkuproEdit(null, tab2, "Složka s rozšiřujícími obrázky jiných geokolegů.",  true, true);
		jImageMyDir = pridejJednuPolozkuproEdit(null, tab2, "Složka s mými vlastními rozšiřujícími obrázky.", true, true);

		jOziDir = pridejJednuPolozkuproEdit(ESouborPanelName.OZI, tab3, "Složka pro rendrování kalibrovaných mapy pro OziExplorer", true, false);
		jKmzDir = pridejJednuPolozkuproEdit(ESouborPanelName.KMZ, tab3, "Složka pro rendrování KMZ souborů (GoogleEarthj)", true, false);
		jPictureDir = pridejJednuPolozkuproEdit(ESouborPanelName.PICTURE, tab3, "Složka pro rendrování obrázků map", true, false);

		jKachleCacheDir = pridejJednuPolozkuproEdit(null, tab4, "Složka s kachlemi uloženými na disk (možno promazávat).",  true,  false);

		pridejJednuPolozkuProCteni(null, tab4, "Složka s exporty výjimek (chybových hlášení)", new Filex(FExceptionDumper.EXCEPTION_DIR, false, true), true);
		if (FConst.JAR_DIR_EXISTUJE) {
			pridejJednuPolozkuProCteni(null, tab4,"Složka s programem (zde je geokuk.jar)", new Filex(FConst.JAR_DIR, false, true), true);
		}
		pridejJednuPolozkuProCteni(null, tab4, "Aktuální složka", new Filex(new File("").getAbsoluteFile(), false, true), true);
		ukonciPanel(tab1);
		ukonciPanel(tab2);
		ukonciPanel(tab3);
		ukonciPanel(tab4);
		add(jTabbedPane);
		add(Box.createVerticalGlue());

		Box ulobox = Box.createHorizontalBox();
		JButton ulozit = new JButton("Uložit");
		//ulozit.setAlignmentX(CENTER_ALIGNMENT);
		//    JLabel ulozlabel = new JLabel("Po uložení změn v souborech bude program ukončen");
		//    ulozlabel.setForeground(Color.RED);

		//    ulobox.add(ulozlabel);
		ulobox.add(Box.createHorizontalGlue());
		ulobox.add(ulozit);
		ulobox.setAlignmentX(LEFT_ALIGNMENT);
		add(ulobox);

		//    jTabbedPane.setSelectedIndex(2);
		//    jKmzDir.requestFocus();
		registerEvents(ulozit);
	}

	private void ukonciPanel(JComponent tab) {
		tab.remove(tab.getComponentCount()-1); // odstranit mezeru za poslední podtovoru
		tab.add(Box.createVerticalGlue());
	}

	private void registerEvents(JButton ulozit) {
		ulozit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent aE) {
				try {
					{
						KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
						u.setKesDir(jKesDir.vezmiSouborAProver());
						u.setCestyDir(jCestyDir.vezmiSouborAProver());

						u.setGeogetDataDir(jGeogetDataDir.vezmiSouborAProver());
						u.setImage3rdPartyDir(jImage3rdPartyDir.vezmiSouborAProver());
						u.setImageMyDir(jImageMyDir.vezmiSouborAProver());
						u.setNeGgtFile(jNeGgtFile.vezmiSouborAProver());
						u.setAnoGgtFile(jAnoGgtFile.vezmiSouborAProver());
						kesoidModel.setUmisteniSouboru(u);
					}
					{
						KachleUmisteniSouboru u = new KachleUmisteniSouboru();
						u.setKachleCacheDir(jKachleCacheDir.vezmiSouborAProver());
						kachleModel.setUmisteniSouboru(u);
					}
					{
						RenderUmisteniSouboru u = new RenderUmisteniSouboru();
						u.setOziDir(jOziDir.vezmiSouborAProver());
						u.setKmzDir(jKmzDir.vezmiSouborAProver());
						u.setPictureDir(jPictureDir.vezmiSouborAProver());
						renderModel.setUmisteniSouboru(u);
					}

					//Board.multiNacitacLoaderManager.startLoad(true);

					((JUmisteniSouboruDialog)SwingUtilities.getRoot(JPrehledSouboru.this)).dispose();
				} catch (YNejdeTo e) {
					Dlg.error(e.getMessage());
				}
			}
		});
	}



	private JJedenSouborPanel pridejJednuPolozkuProCteni(ESouborPanelName souborPanelName, JComponent tab, String label,  Filex hodnota,
			final boolean jenAdresare) {
		JJedenSouborPanel panel = new JJedenSouborPanel(souborPanelName, label, jenAdresare, false, false);
		panel.setFilex(hodnota);
		tab.add(panel);
		tab.add(Box.createRigidArea(new Dimension(0,20)));
		panel.setMaximumSize(new Dimension(1000, 40));
		if (souborPanelName != null) {
			mapaProFokusovani.put(souborPanelName, panel);
		}
		return panel;
	}

	private JJedenSouborPanel pridejJednuPolozkuproEdit(ESouborPanelName souborPanelName, JComponent tab, String label,
			final boolean jenAdresare, boolean lzeDeaktivovat) {
		JJedenSouborPanel panel = new JJedenSouborPanel(souborPanelName, label, jenAdresare, true, lzeDeaktivovat);
		tab.add(panel);
		tab.add(Box.createRigidArea(new Dimension(0,20)));
		// Zjištěno, že to funguje, pokud je tam i lepidlo
		//panel.setMaximumSize(new Dimension(1000, 40));
		panel.setMaximumSize(new Dimension(panel.getMaximumSize().width, panel.getPreferredSize().height));
		if (souborPanelName != null) {
			mapaProFokusovani.put(souborPanelName, panel);
		}
		return panel;
	}

	public void onEvent(RenderUmisteniSouboruChangedEvent event) {
		RenderUmisteniSouboru u = event.getUmisteniSouboru();
		jPictureDir.setFilex(u.getPictureDir());
		jKmzDir.setFilex(u.getKmzDir());
		jOziDir.setFilex(u.getOziDir());
	}


	public void onEvent(KachleUmisteniSouboruChangedEvent event) {
		KachleUmisteniSouboru u = event.getUmisteniSouboru();
		jKachleCacheDir.setFilex(u.getKachleCacheDir());
	}

	public void onEvent(KesoidUmisteniSouboruChangedEvent event) {
		KesoidUmisteniSouboru u = event.getUmisteniSouboru();
		jKesDir.setFilex(u.getKesDir());
		jCestyDir.setFilex(u.getCestyDir());
		jGeogetDataDir.setFilex(u.getGeogetDataDir());

		jNeGgtFile.setFilex(u.getNeGgtFile());
		jAnoGgtFile.setFilex(u.getAnoGgtFile());
		jImage3rdPartyDir.setFilex(u.getImage3rdPartyDir());
		jImageMyDir.setFilex(u.getImageMyDir());
	}

	public void inject(KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void inject(RenderModel renderModel) {
		this.renderModel = renderModel;
	}

	public void inject(KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	public void fokusni(ESouborPanelName panelName) {
		JJedenSouborPanel panel = mapaProFokusovani.get(panelName);
		if (panel == null) return;
		panel.fokusniSe();
	}

	public static class YNejdeTo extends Exception {

		private static final long serialVersionUID = -4020543178329529443L;

		/**
		 * @param aMessage
		 */
		public YNejdeTo(String aMessage) {
			super(aMessage);
		}
	}
}
