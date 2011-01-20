package cz.geokuk.core.napoveda;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;


import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.Dlg;
import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.util.process.BrowserOpener;

public class ZkontrolovatAktualizaceSwingWorker extends MySwingWorker0<String, Void> {

  private final boolean zobrazitDialogPriPosledniVerzi;

  public ZkontrolovatAktualizaceSwingWorker(boolean zobrazitDialogPriPosledniVerzi) {
    this.zobrazitDialogPriPosledniVerzi = zobrazitDialogPriPosledniVerzi;
  }

  @Override
  protected String doInBackground() throws Exception {
    try {
      URL url = new URL(FConst.WEB_PAGE_URL + "version.txt");

      URLConnection connection = url.openConnection();
      connection.setConnectTimeout(60000);
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String lastVersion = br.readLine().trim();
      br.close();
      System.out.println("Posledni verze: '" + lastVersion +
      "' ");
      return lastVersion;
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void donex() throws Exception {
    String lastVersion = get();
    if (FConst.VERSION.equals(lastVersion)) {
      if (zobrazitDialogPriPosledniVerzi) {
        Dlg.info("Používaná verze programu Geokuk " + FConst.VERSION + " je poslední distribuovanou verzí." , "Oznámení");
      }
    } else {
      Object[] options = {"Spustit novou verzi pomocí Java WebStart",
          "Zobrazit web",
      "Nedělat nic"};
      int n = JOptionPane.showOptionDialog(Dlg.parentFrame(),
          "<html></b>Používaná verze programu Geokuk <b>" + FConst.VERSION + "</b> " +
          "není poslední distribuovanou verzí. Poslední distribuovaná verze je " + lastVersion
          + ".",
          "Spuštění nové verze",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          options,
          options[2]);
      switch (n) {
      case 0: spustitJavaWebStart();
      break;
      case 1: zobrazitWeb();
      break;
      default:
        break;
      }
      System.out.println(n);

    }

    super.donex();
  }

  private void zobrazitWeb() {
    try {
      BrowserOpener.displayURL(new URL(FConst.WEB_PAGE_URL + "spust.html"));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private void spustitJavaWebStart() {
    try {
      BrowserOpener.displayURL(new URL(FConst.WEB_PAGE_URL + "geokuk.jnlp"));
      System.exit(0);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

}
