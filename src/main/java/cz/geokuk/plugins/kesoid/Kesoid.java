package cz.geokuk.plugins.kesoid;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Icon;

import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.mapicon.Alela;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

public abstract class Kesoid extends Weikoid0 implements Cloneable {

  private static String[] urlPrefixes = new String[] {
    "http://www.geocaching.com/seek/cache_details.aspx?guid=",
    "http://www.waymarking.com/waymarks/",
    "http://dataz.cuzk.cz/gu/ztl",
  };
  //protected static String URL_PREFIX_SHOW = "http://www.geocaching.com/seek/cache_details.aspx?guid=";


  /** Jednoznacna identifikace jako GC124X4 nebo WM4587 */
  private String code;

  private EKesVztah vztah = EKesVztah.NORMAL;
  private EKesStatus status = EKesStatus.ACTIVE;

  private String author;
  private String hidden;

  private String zbytekUrl;

  private Set<Alela> userDefinedAlelas;

  /**
   * @return the firstWpt
   */
  public Wpt getFirstWpt() {
    if (next instanceof Wpt) {
      return (Wpt) next;
    } else {
      return null;
    }
  }

  public Wpt getMainWpt() {
    return getFirstWpt();
  }

  /**
   * @return the url
   */
  public String getUrl() {
    if (zbytekUrl == null || zbytekUrl.length() == 0) return zbytekUrl;
    char c = zbytekUrl.charAt(0);
    if (c == '-') return zbytekUrl.substring(1);
    int index = c - '0';
    return urlPrefixes[index] + zbytekUrl.substring(1);
  }

  public URL getUrlShow() {
    try {
      String url = getUrl();
      return new URL(url);
    } catch (MalformedURLException e) {
      return null;
    }
  }

  public URL getUrlPrint() {
    return null;
  }

  /**
   * @param aUrl the url to set
   */
  public void setUrl(String aUrl) {
    if (aUrl == null) {
      zbytekUrl = null;
      return;
    }
    // Řetězcová konkatenace zde provedená vytvoří nový řetězec, takže se nemusíme bát,
    // že podstringy všechno nesou
    char index = '0';
    for (String prefix : urlPrefixes) {
      if (aUrl.startsWith(prefix)) {
        zbytekUrl = index + aUrl.substring(prefix.length());
        return;
      }
      index ++;
    }
    zbytekUrl ='-' + aUrl;  // nemá žádný prefix
  }


  //////////////////////////////////////////////////////////////////
  public Iterable<Wpt> getWpts() {
    return new Iterable<Wpt>() {

      @Override
      public Iterator<Wpt> iterator() {
        return new Iterator<Wpt>() {

          private Weikoid0 curwk = Kesoid.this.next; // na první waypoint

          @Override
          public boolean hasNext() {
            return curwk instanceof Wpt;
          }

          @Override
          public Wpt next() {
            Wpt result = (Wpt) curwk;
            curwk = curwk.next;
            return result;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public int getWptsCount() {
    int count = 0;
    for (@SuppressWarnings("unused") Wpt wpt : getWpts()) {
      count ++;
    }
    return count;

  }

  public EKesVztah getVztah() {
    return vztah;
  }

  public void setVztahx(EKesVztah vztah) {
    this.vztah = vztah;
  }

  public void promoteVztah(EKesVztah vztah) {
    if (this.vztah == null) this.vztah = vztah;
    if (vztah.ordinal() > this.vztah.ordinal()) this.vztah = vztah;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void addWpt(Wpt wpt) {
    if (wpt == null) return;
    // najít konec
    Weikoid0 weik = this;
    while (weik.next instanceof Wpt) {
      weik = weik.next;
    }
    wpt.next = weik.next;
    weik.next = wpt;
  }

  public abstract void buildGenotyp(Genom genom, Genotyp g);


  public String getNazev() {
    return getFirstWpt().getNazev();
  }

  public String[] getProhledavanci() {
    return new String[] {getNazev(), getCode()};
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author.intern();
  }

  public String getHidden() {
    return hidden;
  }

  public void setHidden(String hidden) {
    this.hidden = hidden;
  }

  public EKesStatus getStatus() {
    return status;
  }

  public void setStatus(EKesStatus status) {
    this.status = status;
  }


  public abstract EKesoidKind getKesoidKind();

  public abstract void prispejDoTooltipu(StringBuilder sb, Wpt wpt);

  public abstract Icon getUrlIcon();

  public final void doBuildGenotyp(Genom genom, Genotyp g) {
    buildGenotyp(genom, g);
    if(userDefinedAlelas != null) {
      for (Alela alela : userDefinedAlelas) {
        assert alela != null;
        g.put(alela);
      }
    }

  }

  public void setUserDefinedAlelas(Set<Alela> userDefinedAlelas) {
    this.userDefinedAlelas = userDefinedAlelas;
  }

}
