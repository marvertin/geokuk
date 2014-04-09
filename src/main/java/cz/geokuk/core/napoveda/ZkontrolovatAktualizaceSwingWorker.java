package cz.geokuk.core.napoveda;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.Dlg;
import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.util.process.BrowserOpener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZkontrolovatAktualizaceSwingWorker extends MySwingWorker0<ZpravyAVerze, Void> {

    private static final Logger log = LogManager.getLogger(ZkontrolovatAktualizaceSwingWorker.class.getSimpleName());

  private final boolean zobrazitDialogPriPosledniVerzi;
  private final NapovedaModel napovedaModel;

  public ZkontrolovatAktualizaceSwingWorker(boolean zobrazitDialogPriPosledniVerzi, NapovedaModel napovedaModel) {
    this.zobrazitDialogPriPosledniVerzi = zobrazitDialogPriPosledniVerzi;
    this.napovedaModel = napovedaModel;
  }

  @Override
  protected ZpravyAVerze doInBackground() throws Exception {
    try {
      int msgnad = napovedaModel.getLastViewedMsgNum();
      URL url = new URL(FConst.WEB_PAGE_URL + "version.php?verze=" + FConst.VERSION + "&msgnad=" + msgnad);

      URLConnection connection = url.openConnection();
      connection.setConnectTimeout(60000);
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
      String lastVersion = readVersion(br);

      List<ZpravaUzivateli> zpravy = nactiSeznamZprav(br);
      br.close();
      for (ZpravaUzivateli zpravaUzivateli : zpravy) {
        log.debug(zpravaUzivateli);
      }
      log.info("Posledni verze: '" + lastVersion +
          "' ");
      return new ZpravyAVerze(zpravy, lastVersion);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String readVersion(BufferedReader br) throws IOException {
    Pattern pat = Pattern.compile("<h1>\\[\\[(.*)\\]\\]</h1>");
    String line;
    while ((line = br.readLine()) != null) {
      Matcher matcher = pat.matcher(line);
      if (matcher.matches())
        return  matcher.group(1);
    }
    return null;
  }

  private List<ZpravaUzivateli> nactiSeznamZprav(BufferedReader br) throws IOException {
    String line;
    Pattern pat = Pattern.compile("<h1>==(.*)==</h1>");
    StringBuilder sb = null;
    int msgnum = 0;
    List<ZpravaUzivateli> list = new ArrayList<ZpravaUzivateli>();
    while ((line = br.readLine()) != null) {
      Matcher matcher = pat.matcher(line);
      if (matcher.matches()) {
        if (sb != null  && sb.length() > 0) {
          list.add(new ZpravaUzivateli(msgnum, sb.toString()));
        }
        sb = new StringBuilder();
        msgnum = Integer.parseInt(matcher.group(1));
      } else {
        if (sb != null) {
          sb.append(line);
          sb.append("\n");
        }
      }

    }
    if (sb != null  && sb.length() > 0) {
      list.add(new ZpravaUzivateli(msgnum, sb.toString()));
    }
    return list;
  }


  @Override
  protected void donex() throws Exception {
    ZpravyAVerze vysledek = get();
    if (FConst.VERSION.equals(vysledek.lastVersion)) {
      if (zobrazitDialogPriPosledniVerzi) {
        Dlg.info("Používaná verze programu Geokuk " + FConst.VERSION + " je poslední distribuovanou verzí." , "Oznámení");
      }
    } else {
      Object[] options = {"Spustit novou",
          "Zobrazit web",
          "Stáhnout jar",
      "Nedělat nic"};
      int n = JOptionPane.showOptionDialog(Dlg.parentFrame(),
          "<html></b>Používaná verze programu Geokuk <b>" + FConst.VERSION + "</b> " +
              "není poslední distribuovanou verzí. Poslední distribuovaná verze je " + vysledek.lastVersion
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
      case 2: stahnoutJar();
      break;
      default:
        break;
      }
      System.out.println(n);

      //http://geokuk.cz/geokuk.jar
    }
    napovedaModel.setZpravyUzivatelum(vysledek.zpravy);

    super.donex();
  }

  private void zobrazitWeb() {
    try {
      BrowserOpener.displayURL(new URL(FConst.WEB_PAGE_URL + "spust.html"));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private void stahnoutJar() {
    try {
      BrowserOpener.displayURL(new URL(FConst.WEB_PAGE_URL + "geokuk.jar"));
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
