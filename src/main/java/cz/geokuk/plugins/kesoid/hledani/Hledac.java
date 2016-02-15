/**
 * 
 */
package cz.geokuk.plugins.kesoid.hledani;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.geokuk.core.hledani.Hledac0;
import cz.geokuk.core.hledani.HledaciPodminka0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.util.lang.FUtil;



/**
 * @author veverka
 *
 */
public class Hledac extends Hledac0<Nalezenec> {

  private KesBag kesBag;
  /**
   * @param aKl
   */
  public Hledac(KesBag kesBag) {
    super();
    this.kesBag = kesBag;
  }

  public List<Nalezenec> hledej(HledaciPodminka0 podm) {
    System.out.println("Hledy, hledy, hledy: " + kesBag.getKesoidy().size() + " " + podm.getVzorek());
    Porovnavac poro = new Porovnavac(podm.getVzorek(), ((HledaciPodminka) podm).isRegularniVyraz());
    List<Nalezenec> list = new ArrayList<Nalezenec>();
    for (Kesoid kesoid : kesBag.getKesoidy()) {
      if (getFuture() != null && getFuture().isCancelled()) return null;
      String[] prohledavanci = kesoid.getProhledavanci();
      Nalezenec nal = null;
      for (String prohledavanec : prohledavanci) {
      	if (prohledavanec != null) {
	        nal = poro.porovnej(prohledavanec);
	        if (nal != null) break;
      	}
      }
      if (nal != null) {
        nal.setKes(kesoid);
        if (nal.getPoc() == nal.getKon()) {
          nal.setKdeNalezeno(null);
        }
        list.add(nal);
      }
    }
    return list;
  }

  private class Porovnavac {
    private final String vzorek;
    private final boolean regularniVyraz;

    private final Pattern pat;

    /**
     * @param aVzorek
     * @param aRegularniVyraz
     */
    public Porovnavac(String aVzorek, boolean aRegularniVyraz) {
      super();
      vzorek = FUtil.cestinuPryc(aVzorek.toLowerCase());
      regularniVyraz = aRegularniVyraz;
      pat = regularniVyraz ? Pattern.compile(vzorek, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : null;
    }

    public Nalezenec porovnej(String vCem) {
      if (regularniVyraz) {
        Matcher mat = pat.matcher(FUtil.cestinuPryc(vCem));
        boolean found = mat.find();
        if (found) {
          Nalezenec nal = new Nalezenec();
          nal.setKdeNalezeno(vCem);
          nal.setPoc(mat.start());
          nal.setKon(mat.end());
          return nal;
        } else {
          return null;
        }
      } else {
        int poz = FUtil.cestinuPryc(vCem.toLowerCase()).indexOf(vzorek);
        if (poz >= 0) {
          Nalezenec nal = new Nalezenec();
          nal.setKdeNalezeno(vCem);
          nal.setPoc(poz);
          nal.setKon(poz + vzorek.length());
          return nal;
        } else {
          return null;
        }
      }
    }

  }
  
//  public static void main(String[] args) {
//	  System.out.println(FUtil.cestinuPryc("Příliš žluťoučký kůň úpěl ďábelské ódy"));
//	  System.out.println(FUtil.cestinuPryc("Tady neni cestina"));
//	  System.out.println(FUtil.cestinuPryc("Tady je čeština"));
//  }
}
