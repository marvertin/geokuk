package cz.geokuk.core.render;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.RefreshorVysledkuHledani;
import cz.geokuk.core.hledani.VysledekHledani;
import cz.geokuk.core.program.ESouborPanelName;
import cz.geokuk.core.program.NastavUmisteniSouboru;
import cz.geokuk.core.render.JGeocodingComboBox.Listener;
import cz.geokuk.core.render.RenderSettings.Patterned;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.framework.ProgressEvent;
import cz.geokuk.plugins.geocoding.GeocodingModel;
import cz.geokuk.plugins.geocoding.Nalezenec;
import cz.geokuk.plugins.mapy.kachle.JKachlovnik;
import cz.geokuk.plugins.refbody.ReferencniBodSeZmenilEvent;
import cz.geokuk.util.gui.FComponent;
import cz.geokuk.util.gui.JMvRadioPanel;
import cz.geokuk.util.gui.SelectionEvent;
import cz.geokuk.util.gui.SelectionListener;
import cz.geokuk.util.gui.SelectionModel;
import cz.geokuk.util.lang.FUtil;

public class JRenderDialog extends JMyDialog0
implements AfterInjectInit, AfterEventReceiverRegistrationInit {

  private static final long serialVersionUID = 7180968190465321695L;

  private JButton jSpustit;
  private JButton jPrerusit;

  private JProgressBar jProgressBar;
  private RenderModel renderModel;

  private JMvRadioPanel<EWhatRender> jWhatRender;
  private JMvRadioPanel<EImageType> jImgType;

  //  private JTextField jRendrovaneMeritko;

  private JButton jNastaveniAktualnihoMeritka;

  private JNastavovecMeritka jNastavovecMeritka;

  private JGeocodingComboBox jPureJmenoSouboru;
  private JGeocodingComboBox jKmzFolderNazev;
  private JNastavovacVelikostiDlazdic jNastavovacVelikostiDlazdicX;

  private JNastavovacVelikostiDlazdic jNastavovacVelikostiDlazdicY;

  private JLabel jJakouHustotuNaPadesatku;

  private JSpinner jKmzDrawOrder;
  private JCheckBox jSrovnatDoSeveru;
  private JTextField jKmzFolderDescription;

  private JPapirMeritkoComboBox jPapirMeritkoComboBox;
  private JKalibrBoduSpinner jKalibrBodu;

  private JLabel jPriponaSouboru;

  private GeocodingModel geocodingModel;

  protected SortedMap<String, String> geotagingPatterns;

  static int citacUlozeni;

  private Wgs referecniBod;

  private JTwoColumnsPanel jPanOzi;

  private JTwoColumnsPanel jPanKmz;

  private JLabel jOutputFolder;
  private JButton jChangeOutputFolder;

  private JIkonkaPapiru jIkonkaPapiru;

  private JTextField jTerenniRozmer;


  public JRenderDialog() {
    setTitle("Hromadné dotažení mapových dlaždic");
  }


  @Override
  protected void initComponents() {
    // Napřed registrovat, aby při inicializaci už byl výsledek tady
    getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    Box box = Box.createVerticalBox();
    add(box);

    //    jUvod = new JTextPane();
    //    jUvod.setContentType("text/html");
    //    jUvod.setText("Tady bude kecání");
    //    jUvod.setPreferredSize(new Dimension(500, 250));
    //    jUvod.setAlignmentX(CENTER_ALIGNMENT);

    jSrovnatDoSeveru = new JCheckBox();
    jSrovnatDoSeveru.setText("Srovnat do severu");
    jNastaveniAktualnihoMeritka = new JButton("čudl bude něco umět");
    jNastavovecMeritka = new JNastavovecMeritka();
    jTerenniRozmer = new JTextField();
    jTerenniRozmer.setEditable(false);


    JPanel jPanMeritko = new JPanel(new BorderLayout());
    jPanMeritko.setBorder(createBorder("Měřítko"));

    jPanMeritko.setName("Měřítko");
    jPanMeritko.add(createMoumerNahled());

    JPanel meritkoveNastavovace = new JPanel();
    meritkoveNastavovace.add(jSrovnatDoSeveru);
    meritkoveNastavovace.add(new JLabel("Měřítko:"));
    meritkoveNastavovace.add(jNastavovecMeritka);
    meritkoveNastavovace.add(jNastaveniAktualnihoMeritka);
    meritkoveNastavovace.add(jTerenniRozmer);

    jPanMeritko.add(meritkoveNastavovace, BorderLayout.SOUTH);


    box.add(jPanMeritko);


    Box jNastaTypu = Box.createHorizontalBox();

    //box.add(Box.createVerticalStrut(10));
    createrWhatRender();
    jNastaTypu.add(jWhatRender);
    //box.add(Box.createVerticalStrut(10));
    createImgType();
    jNastaTypu.add(jImgType);
    //box.add(Box.createVerticalStrut(5));
    box.add(jNastaTypu);


    box.add(Box.createVerticalStrut(5));



    initOziComponents();
    box.add(jPanOzi);

    // KMZ speciality
    intKmzComponents();
    box.add(jPanKmz);

    box.add(Box.createVerticalGlue());

    Box jVystup = Box.createVerticalBox();
    jVystup.setBorder(createBorder("Výstup"));



    jVystup.add(initVystupComponents());

    // spouštěcí a zastavovací tlačítka
    jSpustit = new JButton("Vyberte nějaký výřez");
    jSpustit.setEnabled(false);
    jSpustit.setAlignmentX(CENTER_ALIGNMENT);
    jPrerusit = new JButton("Přerušit rendrování");
    jPrerusit.setEnabled(false);
    jPrerusit.setVisible(false);
    jPrerusit.setAlignmentX(CENTER_ALIGNMENT);
    jProgressBar = new JProgressBar();
    jVystup.add(jSpustit);
    jVystup.add(jProgressBar);
    jVystup.add(jPrerusit);
    box.add(jVystup);

    add(box);
    //    for (Component comp : getComponents()) {
    //      ((JComponent)comp).setAlignmentX(CENTER_ALIGNMENT);
    //    }

  }

  private JPanel initVystupComponents () {
    JPanel pan = new JPanel(new GridBagLayout());
    jOutputFolder = new JLabel();
    //jOutputFolder.setEditable(false);
    Dimension dm1 = getMinimumSize();
    dm1.width = 300;
    //jOutputFolder.setColumns(50);
    //jOutputFolder.setMinimumSize(dm1);
    jChangeOutputFolder = new JButton("Změň");
    jPureJmenoSouboru = new JGeocodingComboBox();
    jPriponaSouboru = new JLabel();
    jIkonkaPapiru = new JIkonkaPapiru();
    jPapirMeritkoComboBox = new JPapirMeritkoComboBox();
    jJakouHustotuNaPadesatku = new JLabel("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    //jJakouHustotuNaPadesatku.setEditable(false);
    //jJakouHustotuNaPadesatku.setAlignmentX(CENTER_ALIGNMENT);
    Box box1 = Box.createHorizontalBox();
    box1.add(jPapirMeritkoComboBox);
    box1.add(Box.createHorizontalStrut(5));
    box1.add(jJakouHustotuNaPadesatku);

    Box box2 = Box.createHorizontalBox();
    box2.add(jOutputFolder);
    box2.add(Box.createRigidArea(new Dimension(5, 0)));
    box2.add(jChangeOutputFolder);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = -1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(2, 3, 2, 3);
    gbc.weightx = 0;

    gbc.gridy ++;
    gbc.gridx = 0;
    pan.add(new JLabel("Papírové měřítko:"), gbc);
    gbc.gridx = 1;   gbc.weightx = 0; gbc.gridwidth = 2;
    pan.add(box1, gbc);
    gbc.weightx = 0; gbc.gridwidth = 1;

    gbc.gridy ++;
    gbc.gridx = 0;
    pan.add(new JLabel("Výstupní složka:"), gbc);
    gbc.gridx = 1; gbc.weightx = 0;
    pan.add(box2, gbc);
    gbc.weightx = 0;
    gbc.gridx = 2;
    //pan.add(jChangeOutputFolder, gbc);

    gbc.gridy ++;
    gbc.gridx = 0;
    pan.add(new JLabel("Výstupní soubor: "), gbc);
    gbc.gridx = 1; gbc.weightx = 0;
    pan.add(jPureJmenoSouboru, gbc);
    gbc.weightx = 0;
    gbc.gridx = 2;
    pan.add(jPriponaSouboru, gbc);

    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.gridheight = 3;
    pan.add(jIkonkaPapiru, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 0;
    gbc.gridheight = 3;
    gbc.weightx = 1.5;
    pan.add(new JLabel(), gbc);
    return pan;


  }

  private void initOziComponents() {
    jPanOzi =  new JTwoColumnsPanel("OZI Explorer");
    jKalibrBodu = new JKalibrBoduSpinner();
    jPanOzi.addx("Počet kalibračních bodů", jKalibrBodu);
  }


  private void intKmzComponents() {
    jPanKmz = new JTwoColumnsPanel("KMZx (GoogleEarth či Oregon)");
    jPanKmz.setFont(getFont().deriveFont(Font.BOLD));
    jKmzFolderNazev = new JGeocodingComboBox();
    jKmzFolderDescription = new JTextField();
    jNastavovacVelikostiDlazdicX = new JNastavovacVelikostiDlazdic("vodorovném");
    jNastavovacVelikostiDlazdicY = new JNastavovacVelikostiDlazdic("svislém");
    jKmzDrawOrder = new JSpinner(new SpinnerNumberModel(0,0,100,1));

    //    jPanKmz = new JPanel();
    //    jPanKmz.setBorder(BorderFactory.createTitledBorder("KMZ (GoogleEarth či Oregon)"));

    //jPanKmz.gbc.insets = new Insets(0, 0, 0, 0);
    jPanKmz.addx("Název:", jKmzFolderNazev);
    jPanKmz.gbc.fill = GridBagConstraints.HORIZONTAL;
    jPanKmz.addx("Popis:", jKmzFolderDescription);
    jPanKmz.addx("Draw order:", jKmzDrawOrder);
    jPanKmz.addx("Dlaždice X:", jNastavovacVelikostiDlazdicX);
    //jPanKmz.gbc.insets = new Insets(0, 0, 0, 0);
    jPanKmz.addx("Dlaždice Y:", jNastavovacVelikostiDlazdicY);
  }


  private void registerEvents() {

    jSpustit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        RenderSettings ss = renderModel.getRenderSettings().copy();
        ss.setKmzFolderDescription(jKmzFolderDescription.getText());
        renderModel.setRenderSettings(ss);
        renderModel.spustRendrovani();
      }
    });

    jPrerusit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        renderModel.prerusRendrovani();
      }
    });

    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        renderModel.finishRenderingProcess();
      }

      @Override
      public void windowClosed(WindowEvent e) {
        renderModel.finishRenderingProcess();
      }
    });

    jNastaveniAktualnihoMeritka.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        //int renderedMoumer = renderModel.getRenderedMoumer();
        renderModel.uschovejAktualniMeritko();
        //jRendrovaneMeritko.setText(renderedMoumer + "");

      }
    });

    jWhatRender.getSelectionModel().addListener(new SelectionListener<EWhatRender>() {
      @Override
      public void selectionChanged(SelectionEvent<EWhatRender> event) {
        System.out.println("Vybrano: " + event.getSelected());
        RenderSettings ss = renderModel.getRenderSettings().copy();
        ss.setWhatRender(event.getSelected());
        renderModel.setRenderSettings(ss);
      }
    });


    jImgType.getSelectionModel().addListener(new SelectionListener<EImageType>() {
      @Override
      public void selectionChanged(SelectionEvent<EImageType> event) {
        System.out.println("Vybrano: " + event.getSelected());
        RenderSettings ss = renderModel.getRenderSettings().copy();
        ss.setImageType(event.getSelected());
        renderModel.setRenderSettings(ss);
      }
    });


    jKmzFolderNazev.addListener(new Listener() {
      @Override
      public void patternChanged(Patterned patterned) {
        RenderSettings rs = renderModel.getRenderSettings();
        System.out.println("pattern 1: " + rs.getKmzFolder());
        System.out.println("pattern 2: " + patterned);
        rs.setKmzFolder(patterned);
        renderModel.setRenderSettings(rs);
      }
    });

    jPureJmenoSouboru.addListener(new Listener() {
      @Override
      public void patternChanged(Patterned patterned) {
        RenderSettings rs = renderModel.getRenderSettings();
        String vycisteneJmeno = FUtil.vycistiJmenoSouboru(patterned.getText());
        if (! patterned.getText().equals(vycisteneJmeno)) {
          patterned.setText(vycisteneJmeno);
          jPureJmenoSouboru.setSelectedItem(vycisteneJmeno);
        }
        rs.setPureFileName(patterned);
        renderModel.setRenderSettings(rs);
      }
    });

    jNastavovacVelikostiDlazdicX.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        RenderSettings settings = renderModel.getRenderSettings();
        settings.setKmzMaxDlazdiceX(jNastavovacVelikostiDlazdicX.getMaximalniVelikost());
        renderModel.setRenderSettings(settings);
      }
    });

    jNastavovacVelikostiDlazdicY.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        RenderSettings settings = renderModel.getRenderSettings();
        settings.setKmzMaxDlazdiceY(jNastavovacVelikostiDlazdicY.getMaximalniVelikost());
        renderModel.setRenderSettings(settings);
      }
    });

    jSrovnatDoSeveru.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        RenderSettings settings = renderModel.getRenderSettings();
        settings.setSrovnatDoSeveru(jSrovnatDoSeveru.isSelected());
        renderModel.setRenderSettings(settings);
      }
    });

    //    jKmzFolderDescription.addChangeListener(new ChangeListener() {
    //      @Override
    //      public void stateChanged(ChangeEvent e) {
    //        RenderSettings settings = renderModel.getRenderSettings();
    //        settings.setSrovnatDoSeveru(jSrovnatDoSeveru.isSelected());
    //        renderModel.setRenderSettings(settings);
    //      }
    //    });

    jKmzDrawOrder.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        RenderSettings settings = renderModel.getRenderSettings();
        settings.setKmzDrawOrder((Integer) jKmzDrawOrder.getValue());
        renderModel.setRenderSettings(settings);
      }
    });

    jPapirMeritkoComboBox.addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        RenderSettings settings = renderModel.getRenderSettings();
        settings.setPapiroveMeritko(jPapirMeritkoComboBox.getMeritko());
        renderModel.setRenderSettings(settings);
      }
    });

    jKalibrBodu.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        RenderSettings settings = renderModel.getRenderSettings();
        settings.setKalibrBodu((Integer) jKalibrBodu.getValue());
        renderModel.setRenderSettings(settings);
      }
    });


  }


  public void onEvent(PripravaRendrovaniEvent event) {
    boolean maBytEnablovano = event.getStavRendrovani() == EStavRendrovani.PRIPRAVA;

    FComponent.setEnabledChildren(this, maBytEnablovano);
    RenderSettings renderSettings = event.getRenderSettings();

    jWhatRender.getSelectionModel().setSelected(renderSettings.getWhatRender());
    jImgType.getSelectionModel().setSelected(renderSettings.getImageType());
    EStavRendrovani stavRendrovani = event.getStavRendrovani();
    jPrerusit.setVisible(stavRendrovani == EStavRendrovani.BEH);
    jPrerusit.setEnabled(stavRendrovani == EStavRendrovani.BEH);
    jProgressBar.setVisible(stavRendrovani == EStavRendrovani.BEH || stavRendrovani == EStavRendrovani.PRERUSOVANO);
    jSpustit.setVisible(stavRendrovani == EStavRendrovani.PRIPRAVA);

    long pametMiB = renderModel.odhadniMnozstviZabranePameti() / 1024  / 1024 + 1;
    //jSpustit.setEnabled(true);
    jSpustit.setText(String.format("<html>Rendrovat <b>%d * %d px</b> - (%d MiB)",
        renderModel.getDim().width, renderModel.getDim().height, pametMiB));
    //jRendrovaneMeritko.setText(renderModel.getRenderedMoumer() + "");
    jNastaveniAktualnihoMeritka.setText("Nastav na meritko: " + renderModel.getCurrentMoumer());
    jNastaveniAktualnihoMeritka.setEnabled(maBytEnablovano && renderModel.getCurrentMoumer() != renderModel.getRenderedMoumer());

    jPureJmenoSouboru.setPatterned(renderSettings.getPureFileName());
    jKmzFolderNazev.setPatterned(renderSettings.getKmzFolder());

    jNastavovacVelikostiDlazdicX.setMaximalniVelikost(renderSettings.getKmzMaxDlazdiceX());
    jNastavovacVelikostiDlazdicY.setMaximalniVelikost(renderSettings.getKmzMaxDlazdiceY());
    DlazdicovaMetrikaXY dlazdicovaMetrika = renderModel.spoctiDlazdicovouMetriku();
    jNastavovacVelikostiDlazdicX.setMetrika(dlazdicovaMetrika.xx);
    jNastavovacVelikostiDlazdicY.setMetrika(dlazdicovaMetrika.yy);

    Coord roord = event.getModel().getRoord();
    double pixluNaMetr = roord.getPixluNaMetr();
    double pixluNaMilimetrMapy = pixluNaMetr / 1000 * renderSettings.getPapiroveMeritko();
    double dpi = pixluNaMilimetrMapy * 25.4;
    double vzdalenostBodu = 1000 / pixluNaMilimetrMapy;
    PapirovaMetrika papirovaMetrika = renderModel.getPapirovaMetrika();
    jJakouHustotuNaPadesatku.setText(String.format("<html>%.0f * %.0f mm - %.0f DPI = %.2f px/mm = %.1f \u03BCm/px",
        papirovaMetrika.xsize * 1000,
        papirovaMetrika.ysize * 1000,
        dpi, pixluNaMilimetrMapy, vzdalenostBodu));

    jTerenniRozmer.setText(String.format("%.1f * %.1f km", roord.getWidth() / pixluNaMetr / 100, roord.getHeight() / pixluNaMetr / 100));

    jIkonkaPapiru.setMetrikia(papirovaMetrika);

    jKmzDrawOrder.setValue(renderSettings.getKmzDrawOrder());
    jSrovnatDoSeveru.setSelected(renderSettings.isSrovnatDoSeveru());
    jPapirMeritkoComboBox.setMeritko(renderSettings.getPapiroveMeritko());
    jKalibrBodu.setValue(renderSettings.getKalibrBodu());

    jPriponaSouboru.setText("." + urciPriponuSouboru(renderSettings));
    nastavZakladyDoComboboxu(false);
    nastavViditelnost(renderSettings.getWhatRender());

    jOutputFolder.setText(renderModel.getOutputFolder().toString());
    jChangeOutputFolder.setAction(factory.init(new NastavUmisteniSouboru(urciFokusovanouSlozku(renderSettings))));
    jChangeOutputFolder.setText("Změň...");
  }

  private String urciPriponuSouboru(RenderSettings renderSettings) {
    switch (renderSettings.getWhatRender()) {
    case GOOGLE_EARTH: return "kmz";
    case OZI_EXPLORER: return "map";
    case JEN_OBRAZEK: return renderSettings.getImageType().getType();
    default:
      return "???";
    }
  }

  private ESouborPanelName urciFokusovanouSlozku(RenderSettings renderSettings) {
    switch (renderSettings.getWhatRender()) {
    case GOOGLE_EARTH: return ESouborPanelName.KMZ;
    case OZI_EXPLORER: return ESouborPanelName.OZI;
    case JEN_OBRAZEK: return ESouborPanelName.PICTURE;
    default:
      return null;
    }
  }

  public void onEvent(ReferencniBodSeZmenilEvent event) {
    referecniBod = event.wgs;
    nastavZakladyDoComboboxu(true);
    geocodingModel.spustHledani(event.wgs, new RefreshorVysledkuHledani<Nalezenec>() {

      private SortedMap<String, String> patsPureFileName;
      private SortedMap<String, String> patsFolderName;

      @Override
      public void refreshVysledekHledani(VysledekHledani<Nalezenec> vysledekHledani) {
        patsPureFileName = new TreeMap<String, String>();
        patsFolderName = new TreeMap<String, String>();
        if (vysledekHledani.nalezenci != null) {
          int poradi=0;
          for (Nalezenec nalezenec : vysledekHledani.nalezenci) {
            polozkuDoObou(poradi, "A20-geocoding", spoj(nalezenec.administrativeArea, nalezenec.subAdministrativeArea, nalezenec.locality, nalezenec.thoroughfare));
            polozkuDoObou(poradi, "A22-geocoding", spoj(nalezenec.administrativeArea, nalezenec.subAdministrativeArea, nalezenec.locality));
            polozkuDoObou(poradi, "A26-geocoding", spoj(nalezenec.administrativeArea, nalezenec.subAdministrativeArea));
            polozkuDoObou(poradi, "A28-geocoding", spoj(nalezenec.administrativeArea));
            polozkuDoObou(poradi, "A30-geocoding", spoj(nalezenec.subAdministrativeArea, nalezenec.locality, nalezenec.thoroughfare));
            polozkuDoObou(poradi, "A32-geocoding", spoj(nalezenec.subAdministrativeArea, nalezenec.locality));
            polozkuDoObou(poradi, "A36-geocoding", spoj(nalezenec.subAdministrativeArea));
            polozkuDoObou(poradi, "A88-geocoding", nalezenec.adresa);
            poradi++;
          }
          jKmzFolderNazev.addPatterns(null, patsFolderName);
          jPureJmenoSouboru.addPatterns(null, patsPureFileName);
        }
      }

      private void polozkuDoObou(int poradi, String klicek, String textik) {
        patsPureFileName.put(klicek + poradi, FUtil.vycistiJmenoSouboru(textik));
        patsFolderName.put(klicek + poradi,  textik);
      }

      private String spoj(String... jmena) {
        StringBuilder sb = new StringBuilder();
        boolean prvni = true;
        for (String jmeno : jmena) {
          if (jmeno == null || jmeno.isEmpty()) {
            continue;
          }
          if (!prvni) {
            sb.append(", ");
          }
          sb.append(jmeno);
          prvni = false;
        }
        return sb.toString();
      }
    });
  }


  public void onEvent(ProgressEvent event) {
    // FIXME vyřešit problém, kdy se třeba během rendrování načtou kešule
    jProgressBar.setIndeterminate(! event.isVisible());
    jProgressBar.setValue(event.getProgress());
    jProgressBar.setMaximum(event.getMax());
    jProgressBar.setStringPainted(true);
    jProgressBar.setString(event.getText());
    jProgressBar.setToolTipText(event.getTooltip());

  }


  private void nastavViditelnost(EWhatRender whatRender) {
    boolean jenOzi = whatRender == EWhatRender.OZI_EXPLORER;
    boolean jenKmz = whatRender == EWhatRender.GOOGLE_EARTH;
    jKmzFolderNazev.setVisible(jenKmz);
    jNastavovacVelikostiDlazdicX.setVisible(jenKmz);
    jNastavovacVelikostiDlazdicY.setVisible(jenKmz);
    jKmzDrawOrder.setVisible(jenKmz);
    jKmzFolderDescription.setVisible(jenKmz);

    jKalibrBodu.setVisible(jenOzi);
    jPanKmz.setVisible(jenKmz);
    jPanOzi.setVisible(jenOzi);
  }


  /**
   * @param event
   * @return
   */
  private void nastavZakladyDoComboboxu(boolean smazatGeocodingPatterns) {
    if (referecniBod == null) return;
    Wgs wgs = referecniBod;
    int moumer = renderModel.getRenderedMoumer();
    {
      SortedMap<String, String> pats = new TreeMap<String, String>();
      pats.put("C1-wgs",  wgs + " z" + moumer);
      pats.put("C2-utm",  wgs.toUtm() + " z" + moumer);
      pats.put("C3-vter",  "N" + Wgs.toDdMmSsFormat(wgs.lat) + " E" + Wgs.toDdMmSsFormat(wgs.lon) + " z" + moumer);
      jKmzFolderNazev.addPatterns(pats, smazatGeocodingPatterns ? JGeocodingComboBox.PRAZDNE_GEOTAGGINGG_PATTERNS : null);
    }
    {
      SortedMap<String, String> pats = new TreeMap<String, String>();
      pats.put("C0-compact", String.format(Locale.ENGLISH, "n%7fe%7fz%d", wgs.lat, wgs.lon, moumer).replace(".", ""));
      pats.put("C1-wgs", wgs  + " z" + moumer);
      pats.put("C2-utm",  wgs.toUtm()  + " z" + moumer);
      pats.put("C3-vter",  FUtil.vycistiJmenoSouboru("N" + Wgs.toDdMmSsFormat(wgs.lat) + " E" + Wgs.toDdMmSsFormat(wgs.lon) + " z" + moumer));
      jPureJmenoSouboru.addPatterns(pats, smazatGeocodingPatterns ? JGeocodingComboBox.PRAZDNE_GEOTAGGINGG_PATTERNS : null);
    }
  }

  private void createImgType() {
    SelectionModel<EImageType> whrm = new SelectionModel<EImageType>();
    whrm.add(EImageType.bmp, "<html><i>BMP</i> - nekomprimovaný obrázek (pro volný OziExplorer");
    whrm.add(EImageType.jpg, "<html><i>JPG</i> - ztrátová komprimace, vhodné pro fotky, nutné pro Garmin.");
    whrm.add(EImageType.png, "<html><i>PNG</i> - bezeztrátová komprimace, umožňuje průhlednost.");
    jImgType = new JMvRadioPanel<EImageType>("Typ obrázku");
    jImgType.setBorder(createBorder("Typ obrázku"));
    jImgType.setSelectionModel(whrm);
    jImgType.setAlignmentX(0.5f);
  }

  private void createrWhatRender() {
    SelectionModel<EWhatRender> whrm = new SelectionModel<EWhatRender>();
    whrm.add(EWhatRender.JEN_OBRAZEK, "<html><i>Obrázek mapy</i> - pro tisk nebo prohlížení");
    whrm.add(EWhatRender.OZI_EXPLORER, "<html><i>OziExplorer</i> - obrázek a kalibrační map soubor.");
    whrm.add(EWhatRender.GOOGLE_EARTH, "<html><i>KMZ</i> soubor pro Google Earth nebo Garmin Oregon.");
    jWhatRender = new JMvRadioPanel<EWhatRender>("Co rendrovat");
    jWhatRender.setBorder(createBorder("Co rendrovat"));
    jWhatRender.setSelectionModel(whrm);
    jWhatRender.setAlignmentX(0.5f);
  }


  private JComponent createMoumerNahled() {
    JComponent nahled;
    //      JComponent sv = new JPrekryvnik();
    nahled = factory.init(new JRenderNahledPrekryvnik());
    //detailRoh.setBackground(new Color(0,255,120));
    nahled.setMinimumSize(new Dimension(100,100));
    nahled.setPreferredSize(new Dimension(200,200));
    nahled.setMaximumSize(new Dimension(300,300));


    JKachlovnik nahledKachlovnik = new JRenderNahledKachlovnik();
    nahled.add(nahledKachlovnik);
    factory.init(nahledKachlovnik);
    nahled.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    nahled.setAlignmentX(0.5f);
    return nahled;
  }




  @Override
  protected String getTemaNapovedyDialogu() {
    return "Render";
  }

  public void inject(RenderModel renderModel) {
    this.renderModel = renderModel;
  }


  public void inject(GeocodingModel geocodingModel) {
    this.geocodingModel = geocodingModel;
  }


  @Override
  public void initAfterInject() {
    init();
  }


  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    renderModel.startRenderingProcess();
    factory.init(jNastavovecMeritka);
    registerEvents();
    //prepocitat();
  }

  static Border createBorder(String titleText) {
    TitledBorder border = BorderFactory.createTitledBorder(titleText);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD | Font.ITALIC));
    return border;

  }
}
