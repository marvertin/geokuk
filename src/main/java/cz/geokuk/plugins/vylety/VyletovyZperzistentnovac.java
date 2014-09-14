package cz.geokuk.plugins.vylety;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cz.geokuk.core.program.FConst;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;



public class VyletovyZperzistentnovac {

  private int smimCist;

  private KesoidModel kesoidModel;

  private VyletPul loadGgt(File file) throws IOException {
    FileReader filere = null;
    try {
      filere = new FileReader(file);
      BufferedReader br = new BufferedReader(filere);
      return loadGgt(br);
    } catch (FileNotFoundException e) {
      //FExceptionDumper.dump(e, EExceptionSeverity.CATCHE, "Nacitani vyletu.");
      return new VyletPul(new HashSet<String>());
    } finally {
      if (filere != null) {
        filere.close();
      }
    }
  }

  public Vylet immediatlyNactiVylet(KesBag vsechny) {
    try {
      Vylet novyvylet = new Vylet();
      aktualizujVylet(novyvylet, loadGgt(kesoidModel.getUmisteniSouboru().getAnoGgtFile().getEffectiveFile()), EVylet.ANO, vsechny);
      aktualizujVylet(novyvylet, loadGgt(kesoidModel.getUmisteniSouboru().getNeGgtFile().getEffectiveFile()), EVylet.NE, vsechny);
      return novyvylet;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private VyletPul loadGgt(BufferedReader reader) throws IOException {
    String line;
    Set<String> set = new HashSet<>();
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.length() == 0) {
        continue;
      }
      set.add(line);
    }
    VyletPul vyletPul = new VyletPul(set);
    return vyletPul;
  }

  private void aktualizujVylet(Vylet novyvylet, VyletPul vyletPul, EVylet evyl, KesBag vsechny) {
    if (vsechny != null) {
      for (Kesoid kes : vsechny.getKesoidy()) {
        if (vyletPul.kesides.contains(kes.getCode())) {
          novyvylet.add(evyl, kes);
        }
      }
    }
  }


  public void immediatlyZapisVylet(Vylet vylet) {
    zapis(vylet, kesoidModel.getUmisteniSouboru().getAnoGgtFile().getEffectiveFile(), EVylet.ANO);
    zapis(vylet, kesoidModel.getUmisteniSouboru().getNeGgtFile().getEffectiveFile(),  EVylet.NE);
  }

  private void zapis(Vylet vylet, File file, EVylet evyl) {
    BufferedWriter wrt = null;
    smimCist ++;
    try {
      try {
        wrt = new BufferedWriter(new FileWriter(file));
        for (Kesoid kes : vylet.get(evyl)) {
          wrt.write(String.format("%s%s", kes.getCode(), FConst.NL));
        }
        wrt.close();
      } catch (IOException e) {
        if (wrt != null) {
          try {
            wrt.close();
          } catch (IOException e1) { // co s tím jiného
          }
        }
        throw new RuntimeException(e);
      }
    } finally {
      smimCist --;
    }
  }


  public boolean smimCist() {
    return smimCist == 0;
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }

}
