package cz.geokuk.plugins.vylety;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.vylety.cesty.Bod;
import cz.geokuk.plugins.vylety.cesty.Cesta;
import cz.geokuk.plugins.vylety.cesty.Doc;

public class Ukladac {

  private PrintWriter pwrt;

  public void uloz(File f, Doc doc) {

    try {
      pwrt = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    p("<?xml version=\"1.0\" encoding=\"UTF-8\"  ?>");
    p("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" >");
    uloz(doc);

    p("</gpx>");
    pwrt.close();
  }


  private void uloz(Doc doc) {
    for (Cesta cesta : doc.getCesty()) {
      uloz(cesta);
    }
  }

  private void uloz(Cesta cesta) {
    p("<trk>");
    if (cesta.getNazev() != null) {
      p("  <name>" + cesta.getNazev() + "</name>");
    }
    p("  <trkseg>");
    for (Bod b : cesta.getBody()) {
      uloz(b.getMouable().getMou());
      if (b.getUvpred() != null && b.getUvpred().isVzdusny()) {
        p("  </trkseg>");
        p("  <trkseg>");
      }
    }
    p("  </trkseg>");
    p("</trk>");

  }



  private void uloz(Mou mou) {
    Wgs wgs = mou.toWgs();
    p("    <trkpt lat=\"%f\" lon=\"%f\" />", wgs.lat, wgs.lon);
  }



  private void p(String format, Object ... p) {
    pwrt.printf(Locale.US, format, p);
    pwrt.println();
  }




}
