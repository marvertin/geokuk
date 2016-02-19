package cz.geokuk.core.napoveda;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.Dlg;
import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.util.process.BrowserOpener;

public class ZkontrolovatAktualizaceSwingWorker extends MySwingWorker0<ZpravyAVerze, Void> {

  private static final Logger log = LogManager.getLogger(ZkontrolovatAktualizaceSwingWorker.class.getSimpleName());

  private final boolean zobrazitDialogPriPosledniVerzi;
  private final NapovedaModel napovedaModel;

  public ZkontrolovatAktualizaceSwingWorker(final boolean zobrazitDialogPriPosledniVerzi, final NapovedaModel napovedaModel) {
    this.zobrazitDialogPriPosledniVerzi = zobrazitDialogPriPosledniVerzi;
    this.napovedaModel = napovedaModel;
  }

  @Override
  protected ZpravyAVerze doInBackground() throws Exception {
    try {
      final int msgnad = napovedaModel.getLastViewedMsgNum();
      final URL url = new URL(FConst.WEB_PAGE_URL + "version.php?verze=" + FConst.VERSION + "&msgnad=" + msgnad);

      final URLConnection connection = url.openConnection();
      connection.setRequestProperty("User-Agent", "Geokuk/" + FConst.VERSION + " (" + FConst.WEB_PAGE_URL + ")");
      connection.setConnectTimeout(60000);
      final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
      final String lastVersion = readVersion(br);

      final List<ZpravaUzivateli> zpravy = nactiSeznamZprav(br);
      br.close();
      for (final ZpravaUzivateli zpravaUzivateli : zpravy) {
        log.debug(zpravaUzivateli);
      }
      log.info("Posledni verze: '" + lastVersion +
          "' ");
      return new ZpravyAVerze(zpravy, lastVersion);
    } catch (final IOException e) {
      log.error("An error has occurred while retrieving the info!", e);
      return new ZpravyAVerze(Collections.<ZpravaUzivateli>emptyList(), null);
    }
  }

  private String readVersion(final BufferedReader br) throws IOException {
    final Pattern pat = Pattern.compile("<h1>\\[\\[(.*)\\]\\]</h1>");
    String line;
    while ((line = br.readLine()) != null) {
      final Matcher matcher = pat.matcher(line);
      if (matcher.matches()) {
        return matcher.group(1);
      }
    }
    return null;
  }

  private List<ZpravaUzivateli> nactiSeznamZprav(final BufferedReader br) throws IOException {
    String line;
    final Pattern pat = Pattern.compile("<h1>==(.*)==</h1>");
    StringBuilder sb = null;
    int msgnum = 0;
    final List<ZpravaUzivateli> list = new ArrayList<>();
    while ((line = br.readLine()) != null) {
      final Matcher matcher = pat.matcher(line);
      if (matcher.matches()) {
        if (sb != null && sb.length() > 0) {
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
    if (sb != null && sb.length() > 0) {
      list.add(new ZpravaUzivateli(msgnum, sb.toString()));
    }
    return list;
  }


  @Override
  protected void donex() throws Exception {
    final ZpravyAVerze vysledek = get();
    if (FConst.I_AM_IN_DEVELOPMENT_ENVIRONMENT) {
      log.info("LAST VERSION: " + vysledek.lastVersion + " i have no version, i am in development environment");
    } else if (FConst.VERSION.equals(vysledek.lastVersion)) {
      if (zobrazitDialogPriPosledniVerzi) {
        Dlg.info("Používaná verze programu Geokuk " + FConst.VERSION + " je poslední distribuovanou verzí." , "Oznámení");
      }
    } else {
      final Object[] options = {"Zobrazit web",
          "Stáhnout nejnovější verzi",
      "Připomenout příště"};
      final int n = JOptionPane.showOptionDialog(Dlg.parentFrame(),
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
      case 0: zobrazitWeb();
      break;
      case 1: stahnoutJar();
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
      BrowserOpener.displayURL(new URL(FConst.WEB_PAGE_URL));
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private void stahnoutJar() {
    try {
      BrowserOpener.displayURL(new URL(FConst.LATEST_RELEASE_URL));
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private void spustitJavaWebStart() {
    try {
      BrowserOpener.displayURL(new URL(FConst.WEB_PAGE_URL + "geokuk.jnlp"));
      System.exit(0);
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

}
