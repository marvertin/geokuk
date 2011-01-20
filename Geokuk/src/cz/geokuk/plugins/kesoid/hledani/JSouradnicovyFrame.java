/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.geokuk.plugins.kesoid.hledani;

/*
 * TextFieldDemo.java requires one additional file:
 * content.txt
 */


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.VyrezChangedEvent;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.coordinates.CoordinateConversionOriginal;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.refbody.ReferencniBodSeZmenilEvent;

public class JSouradnicovyFrame extends JMyDialog0 implements AfterEventReceiverRegistrationInit, DocumentListener {

  private static final long serialVersionUID = 7087453419069194768L;

  private static final double SIRKA_MIN = 30;
  private static final double SIRKA_MAX = 70;
  private static final double DELKA_MIN =  0;
  private static final double DELKA_MAX = 40;

  private static final Pattern pat =
    Pattern.compile(" *[nNeE]? *(?:([0-9]+\\.?[0-9]*) *[° ] *(?:([0-9]+\\.?[0-9]*) *[' ] *(?:([0-9]+\\.?[0-9]*) *[\" ] *)?)?)?[nNeE]? *");
  //  Pattern.compile(" *[nNeE]? *([0-9]+\\.?[0-9]*) *[° ] *(?:([0-9]+\\.?[0-9]*) *[°' ] *)?(?:([0-9]+\\.?[0-9]*) *[\" ])?");
  private static final Double SPATNY_FORMAT = Double.NEGATIVE_INFINITY;

  private JTextField jSirka;
  private JTextField jDelka;
  private JLabel jSirkaLabel;
  private JLabel jDelkaLabel;
  private JButton jButtonCentruj;
  private JLabel jHotovaSirka;
  private JLabel jHotovaDelka;
  private JLabel jUtm;

  final static Color  HILIT_COLOR = Color.LIGHT_GRAY;
  final static Color  ERROR_COLOR = Color.PINK;
  final static String CANCEL_ACTION = "cancel-search";

  private Color entryBg;

  private Wgs souradniceEditovane;
  private Wgs souradniceReferencni;

  private final CoordinateConversionOriginal konvertor = new CoordinateConversionOriginal();

  private PoziceModel poziceModel;

  private VyrezModel vyrezModel;

  private boolean souradniceNastavenyRukama;


  public JSouradnicovyFrame() {
    setTitle("Zadání souřadnic");
    init();
    jSirka.getDocument().addDocumentListener(this);
    jDelka.getDocument().addDocumentListener(this);

    registerEvents();


  }

  private void registerEvents() {

    jButtonCentruj.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        poziceModel.setPozice(souradniceEditovane);
        vyrezModel.vystredovatNaPozici();
      }
    });
  }

  public void onEvent(ReferencniBodSeZmenilEvent aEvent) {
    setSouradniceReferencni(aEvent.wgs);
    vyhodnotEnableCentrovacihoTlacitka();
  }

  public void onEvent(VyrezChangedEvent aEvent) {
    vyhodnotEnableCentrovacihoTlacitka();
  }


  /** This method is called from within the constructor to
   * initialize the form.
   */

  @Override
  protected void initComponents() {
    String tooltip = "Šířku i délku zadáváte jak jedno až tři celá nebo desetinná čísla (stupně, minuty, vteřiny)," +
    " jako oddělovač použijte mezeru nebo odpovídající značky °'\". Jako oddělovač desetin můžete použít tečku nebo čárku. " +
    " Písmena N nebo E můžete uvést na začátku, na knoci nebo je vynechat. (Nelze zadávat jižní šířku, či západní délku.)";
    jSirka = new JTextField();
    jSirka.setToolTipText(tooltip);
    jDelka = new JTextField();
    jDelka.setToolTipText(tooltip);

    jSirkaLabel = new JLabel("Šířka: ");
    jSirkaLabel.setLabelFor(jSirka);
    jDelkaLabel = new JLabel("Délka: ");
    jDelkaLabel.setLabelFor(jDelka);

    jButtonCentruj = new JButton("Centruj");
    jButtonCentruj.setToolTipText("Centruje mapu na zadaných souřadnicích.");
    getRootPane().setDefaultButton(jButtonCentruj);

    Font hotovoFont = new Font("Monospaced", Font.BOLD, 20);
    jHotovaSirka = new JLabel();
    jHotovaSirka.setFont(hotovoFont);
    jHotovaSirka.setOpaque(true);
    jHotovaDelka = new JLabel();
    jHotovaDelka.setFont(hotovoFont);
    jHotovaDelka.setOpaque(true);
    jUtm = new JLabel();

    setTitle("Zadání souřadnic");

    JPanel panel = new JPanel();
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    add(panel);

    jButtonCentruj.setAlignmentY(CENTER_ALIGNMENT);
    jUtm.setText("?");
    //    panel.add(jSirka);
    //    panel.add(jDelka);
    //    panel.add(jButtonCentruj);

    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jSirkaLabel)
                .addComponent(jDelkaLabel)
            )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jSirka)
                .addComponent(jDelka)
            )
        )
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jHotovaSirka)
                .addComponent(jHotovaDelka)
            )
            .addComponent(jButtonCentruj)
        )
        .addComponent(jUtm)
    );

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(jSirkaLabel)
                    .addComponent(jSirka)
                )
                .addGroup(layout.createParallelGroup()
                    .addComponent(jDelkaLabel)
                    .addComponent(jDelka)
                )
            )
        )
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jHotovaSirka)
                .addComponent(jHotovaDelka)
            )
            .addComponent(jButtonCentruj)
        )
        .addComponent(jUtm)
    );
    jSirka.setPreferredSize(new Dimension(100, jSirka.getPreferredSize().height));
    jDelka.setPreferredSize(new Dimension(100, jDelka.getPreferredSize().height));
    jSirka.setText("");
    jDelka.setText("");
    entryBg = jSirka.getBackground();
    edituj();

  }

  private void setSouradniceReferencni(Wgs wgs) {
    if (wgs.equals(souradniceReferencni)) return;
    souradniceReferencni = wgs;
    if (!souradniceNastavenyRukama) {
      jDelka.setText(Wgs.toGeoFormat(wgs.lon));
      jSirka.setText(Wgs.toGeoFormat(wgs.lat));
      jSirka.selectAll();
      jDelka.selectAll();
      souradniceNastavenyRukama = false; // toto se může zdát zbytečné, ale řádky před tím to změní
    }
    edituj();
  }

  private void edituj() {
    //if (souradnice == null) return;
    boolean ok;
    double sirka = parseCela(jSirka.getText());
    double delka = parseCela(jDelka.getText());
    boolean okSirka = aplikuj(jHotovaSirka, jSirka, sirka, SIRKA_MIN, SIRKA_MAX);
    boolean okDelka = aplikuj(jHotovaDelka, jDelka, delka, DELKA_MIN, DELKA_MAX);
    ok = okSirka && okDelka;
    if (ok) {
      souradniceEditovane = new Wgs(sirka, delka);
      jUtm.setText(konvertor.latLon2UTM(sirka, delka));
    } else {
      jUtm.setText("UTM = ?");
    }
    vyhodnotEnableCentrovacihoTlacitka();
  }

  /**
   * 
   */
  private void vyhodnotEnableCentrovacihoTlacitka() {
    boolean jsmeNaMiste = jsmeVycentrovaniSeZadanouPozici();

    //jsmeNaMiste = vyrezModel.isPoziceUprostred();
    jButtonCentruj.setEnabled(!jsmeNaMiste);
  }

  /**
   * @return
   */
  private boolean jsmeVycentrovaniSeZadanouPozici() {
    boolean jsmeNaMiste =
      (souradniceEditovane != null && souradniceEditovane.equals(souradniceReferencni))
      && vyrezModel.isPoziceUprostred();
    return jsmeNaMiste;
  }


  private boolean aplikuj(JLabel jHotova, JTextField editacni,  double val, double min, double max) {
    boolean ok;
    if (val == SPATNY_FORMAT) {
      jHotova.setText("Grrrr!");
      jHotova.setBackground(Color.RED);
      editacni.setBackground(ERROR_COLOR);
      ok = false;
    } else {
      jHotova.setText(Wgs.toGeoFormat(val));
      if (val >= min && val <= max) {
        jHotova.setBackground(Color.GREEN);
        editacni.setBackground(Color.WHITE);
        ok = true;
      } else {
        if (val < 10 && val == (long)val) {
          jHotova.setBackground(Color.GRAY);
          jHotova.setText(jHotova.getText().replaceAll("\\d", "?"));
          editacni.setBackground(Color.WHITE);
          ok = false;
        } else {
          jHotova.setBackground(Color.RED);
          editacni.setBackground(ERROR_COLOR);
          ok = false;
        }
      }
    }
    return ok;
  }

  private double parseCela(String s) {
    s = s.replace(',', '.').replaceAll("([nNeE])", " $1") + " "; // mezera, aby byl jistě nějaký ukončovač
    Matcher mat = pat.matcher(s);
    double x;
    if (mat.matches()) {
      x = parseOne(mat.group(1)) + parseOne(mat.group(2))/60 + parseOne(mat.group(3))/3600;
    } else {
      //System.out.println(s + " - " + mat.toMatchResult());
      x= SPATNY_FORMAT;
    }
    return x;
  }

  private double parseOne(String s) {
    if (s == null || s.length() == 0) return 0;
    double result = Double.parseDouble(s);
    return result;
  }

  // DocumentListener methods

  @Override
  public void insertUpdate(DocumentEvent ev) {
    souradniceNastavenyRukama = true;
    edituj();
  }

  @Override
  public void removeUpdate(DocumentEvent ev) {
    souradniceNastavenyRukama = true;
    edituj();
  }

  @Override
  public void changedUpdate(DocumentEvent ev) {
    souradniceNastavenyRukama = true;
    edituj();
  }

  //  private final class SpousteniVyhledavace implements ChangeListener {
  //    @Override
  //    public void stateChanged(ChangeEvent e) {
  //      search();
  //    }
  //  }

  class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -480129891208539096L;

    @Override
    public void actionPerformed(ActionEvent ev) {
      //      hilit.removeAllHighlights();
      jSirka.setText("");
      jDelka.setText("");
      jSirka.setBackground(entryBg);
      jDelka.setBackground(entryBg);
    }
  }


  //  public static void main(String args[]) {
  //    //Schedule a job for the event dispatch thread:
  //    //creating and showing this application's GUI.
  //
  //    SwingUtilities.invokeLater(new Runnable() {
  //      public void run() {
  //        //Turn off metal's use of bold fonts
  ////        UIManager.put("swing.boldMetal", Boolean.FALSE);
  //        new JSouradnicovyFrame(null).setVisible(true);
  //      }
  //    });
  //  }


  public void inject(PoziceModel poziceModel) {
    this.poziceModel = poziceModel;
  }

  public void inject(VyrezModel vyrezModel) {
    this.vyrezModel = vyrezModel;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    //super.ini
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return "JintNaSouradnice";
  }
}

