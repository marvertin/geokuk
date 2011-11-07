package cz.geokuk.plugins.vylety.akce.soubor;


import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.vylety.VyletModel;
import cz.geokuk.plugins.vylety.cesty.Doc;



public abstract class SouboeCestaAction0 extends Action0 {

  private static final long serialVersionUID = -2637836928166450446L;

  protected VyletModel vyletModel;

  public SouboeCestaAction0(String string) {
    super(string);
  }

  public void inject(VyletModel vyletModel) {
    this.vyletModel = vyletModel;

  }

  protected boolean ulozit() {
    Doc xdoc = vyletModel.getDoc();
    if (xdoc.getFile() == null) { // ještě nebyl určen soubor, musíme se zeptat
      JFileChooser fc = new JFileChooser();
      fc.addChoosableFileFilter(new GpxFilter());
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fc.setSelectedFile(vyletModel.getImplicitniVyletNovyFile());
      int result = fc.showDialog(Dlg.parentFrame(), "Uložit");
      if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = doplnGpxPriponuProUkladani(fc.getSelectedFile());
        if (selectedFile.exists()) { // dtaz na přepsání
          if (! Dlg.prepsatSoubor(selectedFile)) return false;
        }
        xdoc.setFile(selectedFile);
      } else
        return false;
    }
    // TODO ukládat na pozadí a také mít jinde ukládací dialog
    vyletModel.uloz(xdoc.getFile(), xdoc, true);
    System.out.println("Uložena cesta do: " + xdoc.getFile());
    return true;
  }

  public boolean ulozitSDotazem() {
    if (! vyletModel.getDoc().isChanged()) return true; // nezměna znamená uloženo
    Object[] options =
    {"Uložit změny",
        "Zahodit změny",
    "Zrušit"};
    String hlaska = vyletModel.getDoc().getFile() != null
    ? "<html>Soubor s výletem byl změněn <b>" + vyletModel.getDoc().getFile() + "</b> "
        : "Byl vytvořen nový výlet, ale nebyl doposud uložen do souboru."
          + ".";
    int n = JOptionPane.showOptionDialog(Dlg.parentFrame(),
        hlaska,
        "Uložení změn ve výletu",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[2]);
    System.out.println(n);
    if (n == 0) {
      boolean result = ulozit();
      return result;
    } else if (n ==1)
      return true;
    else
      return false; // končit nebudeme

  }

  File doplnGpxPriponuProUkladani(File file) {
    if (file == null) return null;
    if (file.getName().toLowerCase().endsWith(".gpx")) return file;
    return new File(file.getPath() + ".gpx");
  }

  File doplnGgtPriponuProUkladani(File file) {
    if (file == null) return null;
    if (file.getName().toLowerCase().endsWith(".ggt")) return file;
    return new File(file.getPath() + ".ggt");
  }


}
