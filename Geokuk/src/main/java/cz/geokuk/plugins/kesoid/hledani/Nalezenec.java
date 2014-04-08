package cz.geokuk.plugins.kesoid.hledani;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.Nalezenec0;
import cz.geokuk.plugins.kesoid.Kesoid;

public class Nalezenec extends Nalezenec0 {
  private Kesoid kesoid;

  private String kdeNalezeno;
  // Indexy místa v řetězci, kde byl text nalezen
  private int poc;
  private int kon;

  public Kesoid getKes() {
    return kesoid;
  }

  public String getKdeNalezeno() {
    return kdeNalezeno;
  }

  public void setKdeNalezeno(String kdeNalezeno) {
    this.kdeNalezeno = kdeNalezeno;
  }

  public int getPoc() {
    return poc;
  }

  public void setPoc(int poc) {
    this.poc = poc;
  }

  public int getKon() {
    return kon;
  }

  public void setKon(int kon) {
    this.kon = kon;
  }

  public void setKes(Kesoid kes) {
    this.kesoid = kes;
  }

	@Override
  public Wgs getWgs() {
	  return kesoid.getMainWpt().getWgs();
  }
}
