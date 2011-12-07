package cz.geokuk.plugins.vylety.akce.soubor;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;

import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.vylety.VyletChangedEvent;
import cz.geokuk.plugins.vylety.data.Doc;

public class UlozJakoAction extends SouboeCestaAction0 {

  private static final long serialVersionUID = 1L;
  private Doc doc;


  public UlozJakoAction() {
    super("Ulož výlet (gpx) jako ...");
    putValue(SHORT_DESCRIPTION, "Uloží zadaný výlet do jiného souboru GPX");
    putValue(MNEMONIC_KEY, KeyEvent.VK_V);
    //putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    JFileChooser fc = new JFileChooser();
    fc.addChoosableFileFilter(new GpxFilter());
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fc.setSelectedFile(vyletModel.getImplicitniVyletSaveAsNovyFile());
    int result = fc.showDialog(Dlg.parentFrame(), "Uložit jako");
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = doplnGpxPriponuProUkladani(fc.getSelectedFile());
      if (selectedFile.exists()) { // dtaz na přepsání
        if (! Dlg.prepsatSoubor(selectedFile)) return;
      }
      doc.setFile(selectedFile);
      vyletModel.uloz(doc.getFile(), doc, true);
      System.out.println("Uložena cesta do: " + doc.getFile());
    }
    // TODO ukládat na pozadí
  }

  public void onEvent(VyletChangedEvent event) {
    doc = event.getDoc();
    setEnabled(doc != null && (! doc.isEmpty()));
  }


}
