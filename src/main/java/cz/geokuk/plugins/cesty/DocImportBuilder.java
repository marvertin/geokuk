package cz.geokuk.plugins.cesty;

import java.util.ArrayList;
import java.util.List;

import cz.geokuk.plugins.cesty.data.Bod;
import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.cesty.data.Updator;
import cz.geokuk.plugins.cesty.data.Usek;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.importek.IImportBuilder;

public class DocImportBuilder implements IImportBuilder {


  private Cesta cesta;
  private List<Cesta> cesty = new ArrayList<>();

  private boolean zacatekSegmentu;

  private final Updator updator  = new Updator();

  public DocImportBuilder() {
  }

  @Override
  public void addGpxWpt(GpxWpt gpxwpt) {
  }

  @Override
  public void addTrackWpt(GpxWpt wpt) {
    Bod bod = updator.pridejNaKonec(cesta, wpt.wgs.toMou());
    Usek usek = bod.getUvzad(); // právě přidaný úsek, pokud nějaký
    if (usek != null && zacatekSegmentu) {
      updator.setVzdusny(usek, true);
    }
    zacatekSegmentu = false;
  }

  @Override
  public void begTrackSegment() {
    zacatekSegmentu = true;
  }

  @Override
  public void endTrackSegment() {
  }

  @Override
  public void begTrack() {
    cesta = Cesta.create();
  }

  @Override
  public void endTrack() {
    cesty.add(cesta);
    cesta = null;
  }

  List<Cesta> getCesty() {
    return cesty;
  }

  void setCesty(List<Cesta> cesty) {
    this.cesty = cesty;
  }

  @Override
  public void setTrackName(String aTrackName) {
    updator.setNazev(cesta, aTrackName);
  }

  @Override
  public GpxWpt get(String aName) {
    throw new RuntimeException("Neimplementovano");
  }

}
