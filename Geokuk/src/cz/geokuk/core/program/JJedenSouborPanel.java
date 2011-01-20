package cz.geokuk.core.program;


import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cz.geokuk.core.program.JPrehledSouboru.YNejdeTo;
import cz.geokuk.util.file.Filex;

public class JJedenSouborPanel extends JPanel implements DocumentListener {

  private static final long serialVersionUID = -3579395922979423765L;

  private final boolean jenAdresare;

  private final String label;
  private JTextField jtext;
  private JCheckBox jRelativneKProgramu;
  private JCheckBox jActive;
  private JTextField jCurrVal;

  private Filex filex;

  private final boolean editovatelne;

  private final boolean lzeDeaktivovat;

  private final ESouborPanelName souborPanelName;

  /**
   * 
   * @param souborPanelName identifikátor panelu. Je jen proto, aby se podle něj mohl panel najít.
   * @param label
   * @param jenAdresare
   * @param editovatelne
   * @param lzeDeaktivovat
   */
  public JJedenSouborPanel(ESouborPanelName souborPanelName, String label, final boolean jenAdresare, boolean editovatelne, boolean lzeDeaktivovat) {
    this.souborPanelName = souborPanelName;
    this.label = label;
    this.jenAdresare = jenAdresare;
    this.editovatelne = editovatelne;
    this.lzeDeaktivovat = lzeDeaktivovat;
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    initComponents();
  }

  private void initComponents() {

    TitledBorder border = BorderFactory.createTitledBorder(label);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    jtext = new JTextField();
    //jtext.setText(defalt.getFile().getPath());
    jRelativneKProgramu = new JCheckBox("Relativně k umístění programu");
    jRelativneKProgramu.setEnabled(FConst.JAR_DIR_EXISTUJE);
    jActive = new JCheckBox("Aktivní");
    jActive.setEnabled(lzeDeaktivovat);
    jCurrVal = new JTextField();
    jCurrVal.setForeground(Color.BLUE);
    jCurrVal.setEditable(false);
    jCurrVal.setBorder(null);
    final JButton jbut = new JButton("...");
    //jtext.setText(defalt.getFile().getPath());
    jtext.setColumns(50);
    if (editovatelne) {
      Box box2 = Box.createHorizontalBox();
      box2.setAlignmentX(LEFT_ALIGNMENT);
      box2.add(jtext);
      box2.add(jbut);
      Box panel3 = Box.createHorizontalBox();
      if (FConst.JAR_DIR_EXISTUJE) {
        panel3.add(jRelativneKProgramu);
      }
      if (jActive.isEnabled()) {
        panel3.add(jActive);
      }
      if (panel3.getComponentCount() > 0) {
        add(panel3);
      }
      panel3.setAlignmentX(LEFT_ALIGNMENT);
      add(box2);
    }
    //panel.add(Box.createVerticalStrut(20));
    jCurrVal.setAlignmentX(LEFT_ALIGNMENT);
    add(jCurrVal);
    //    add(panel);
    //    add(Box.createVerticalStrut(20));
    jbut.addActionListener(new ActionListener() {

      private JFileChooser fc;

      @Override
      public void actionPerformed(ActionEvent ae) {
        if (fc == null) { // dlouho to trvá, tak vytvoříme vždy nový
          fc = new JFileChooser();
        }
        fc.setCurrentDirectory(new File(jtext.getText()));
        if (jenAdresare) {
          fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        int result = fc.showDialog(JJedenSouborPanel.this, "Vybrat");
        if (result == JFileChooser.APPROVE_OPTION) {
          jtext.setText(fc.getSelectedFile().getPath());
        }
      }
    });


    prepocitej();
    jtext.getDocument().addDocumentListener(this);

    jRelativneKProgramu.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        prepocitej();
      }
    });
    jActive.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        prepocitej();
      }
    });
  }

  public Filex vezmiSouborAProver() throws YNejdeTo {
    prepocitej();
    File dir = filex.getEffectiveFile();
    System.out.println("Prověřuji soubor: " + dir);
    if (!jenAdresare) {
      dir = dir.getParentFile();
    }
    if (dir.isDirectory() && dir.canRead()) return filex;
    boolean vysl = dir.mkdirs();
    if (!vysl)
      throw new JPrehledSouboru.YNejdeTo("Složku \"" + dir + "\" se nepodařilo stvořit pro \"" + label + "\"");
    return filex;
  }

  private void zmemniliNamTo() {
    prepocitej();
  }

  public void setFilex(Filex filex) {
    jtext.setText(filex.getFile().getPath());
    jRelativneKProgramu.setSelected(filex.isRelativeToProgram());
    jActive.setSelected(filex.isActive() || ! lzeDeaktivovat);
    prepocitej();
  }

  private void prepocitej() {
    filex = new Filex (new File(jtext.getText()), jRelativneKProgramu.isSelected(), jActive.isSelected());
    jCurrVal.setText(filex.getEffectiveFile().getPath());
    jtext.setEnabled(jActive.isSelected());
    jRelativneKProgramu.setEnabled(jActive.isSelected());
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    zmemniliNamTo();
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    zmemniliNamTo();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    zmemniliNamTo();
  }

  public void fokusniSe() {
    Container panel = null;
    for (Container comp = this; comp != null; comp = comp.getParent()) {
      if (comp instanceof JTabbedPane) {
        JTabbedPane tabpane = (JTabbedPane) comp;
        tabpane.setSelectedComponent(panel);
        break;
      }
      panel = comp;
    }
    jtext.requestFocus();
  }

  /**
   * @return the souborPanelName
   */
  public ESouborPanelName getSouborPanelName() {
    return souborPanelName;
  }

  //  @Override
  //  public void requestFocus() {
  //    super.requestFocus();
  //    jtext.requestFocus();
  //  }
}
