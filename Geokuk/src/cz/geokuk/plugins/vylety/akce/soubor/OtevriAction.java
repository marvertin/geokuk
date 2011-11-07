package cz.geokuk.plugins.vylety.akce.soubor;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import cz.geokuk.framework.Dlg;

public class OtevriAction extends SouboeCestaAction0 {

  private static final long serialVersionUID = 1L;
  private JFileChooser fc;

  public OtevriAction() {
    super("Otevři výlet (gpx)");
    putValue(SHORT_DESCRIPTION, "Otevře zadaný výlet v GPX a nahradí jim všechyn načtené cesty.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_V);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
    //putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!super.ulozitSDotazem()) return; // mělo se ukládat a řeklo se, že ne
    if (fc == null) { // dlouho to trvá, tak vytvoříme vždy nový
      fc = new JFileChooser();
      fc.addChoosableFileFilter(new GpxFilter());
    }
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    //fc.setCurrentDirectory(new File(jtext.getText()));
    fc.setSelectedFile(vyletModel.defaultAktualnihoVyletuFile());

    int result = fc.showDialog(Dlg.parentFrame(), "Otevřít");
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fc.getSelectedFile();
      if (! new GpxFilter().accept(selectedFile)) {
        Dlg.error("Soubor \"" + selectedFile + "\" nemá příponu GPX!");
      } else { // je to dobré, otvíráme
        vyletModel.otevri(selectedFile);
        System.out.println("Nactena cesta z: " + selectedFile);
      }
    }
  }


  private class GpxFilter extends FileFilter {

    @Override
    public boolean accept(File pathname) {
      if (pathname.isDirectory()) return true;
      if (pathname.getName().toLowerCase().endsWith(".gpx")) return true;
      return false;
    }

    @Override
    public String getDescription() {
      return "Soubory s cestami (*.gpx)";
    }

  }

}
