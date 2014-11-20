/**
 * 
 */
package cz.geokuk.plugins.kesoid.mvc;


import java.io.File;

import cz.geokuk.core.program.FConst;
import cz.geokuk.core.program.UmisteniSouboru0;
import cz.geokuk.util.file.Filex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author veverka
 *
 */
public class KesoidUmisteniSouboru extends UmisteniSouboru0  {

  private static final Logger log = LogManager.getLogger(KesoidUmisteniSouboru.class.getSimpleName());

  public static final Filex GEOKUK_DATA_DIR = new Filex(new File(FConst.HOME_DIR, "geokuk"), false, true);
  public static final Filex CESTY_DIR  = new Filex(new File(FConst.HOME_DIR, "geokuk/cesty"), false, true);
  public static final Filex GEOGET_DATA_DIR = new Filex(new File("C:\\geoget\\data"), false, true);

  public static final Filex IMAGE_3RDPARTY_DIR = new Filex(new File(GEOKUK_DATA_DIR.getFile(), "images3rdParty"), false, false);
  public static final Filex IMAGE_MY_DIR = new Filex(new File(GEOKUK_DATA_DIR.getFile(), "imagesMy"), false, false);

  public static final File PRCHAVE_DIR = new File(GEOKUK_DATA_DIR.getFile(), "prchave"); // keš, kterou je vždymožno smazat

  //  public static final Filex KESOID_CACHE_DIR = new Filex(new File(PRCHAVE_DIR, "kesoids"), false, true);

  public static final Filex ANO_GGT = new Filex(new File(GEOKUK_DATA_DIR.getFile(), "lovim.ggt"), false, true);
  public static final Filex NE_GGT = new Filex(new File(GEOKUK_DATA_DIR.getFile(), "tedne.ggt"), false, true);

  private Filex kesDir;
  private Filex cestyDir;

  private Filex geogetDataDir;
  private Filex image3rdPartyDir;
  private Filex imageMyDir;

  private Filex neGgtFile;
  private Filex anoGgtFile;



  /**
   * @param aKesDir
   */
  private void check(Filex file) {
    if (file == null) throw new RuntimeException("Jmena souboru jeste nebyla inicializovana.");
  }

  /**
   * @param kesDir the kesDir to set
   */
  public void setKesDir(Filex kesDir) {
    this.kesDir = kesDir;
  }

  public void setCestyDir(Filex cestyDir) {
    this.cestyDir = cestyDir;
  }



  /**
   * @return the kesDir
   */
  public Filex getKesDir() {
    check(kesDir);
    return kesDir;
  }

  public Filex getCestyDir() {
    check(cestyDir);
    return cestyDir;
  }



  /**
   * @param neGgtFile the neGgtFile to set
   */
  public void setNeGgtFile(Filex neGgtFile) {
    this.neGgtFile = neGgtFile;
  }

  /**
   * @param neGgtFile the neGgtFile to set
   */
  public void setAnoGgtFile(Filex anoGgtFile) {
    this.anoGgtFile = anoGgtFile;
  }


  /**
   * @return the neGgtFile
   */
  public Filex getNeGgtFile() {
    check(neGgtFile);
    return neGgtFile;
  }

  /**
   * @return the neGgtFile
   */
  public Filex getAnoGgtFile() {
    check(anoGgtFile);
    return anoGgtFile;
  }


  public Filex getGeogetDataDir() {
    return geogetDataDir;
  }

  public void setGeogetDataDir(Filex geogetDir) {
    geogetDataDir = geogetDir;
  }

  public Filex getImage3rdPartyDir() {
    return image3rdPartyDir;
  }

  public void setImage3rdPartyDir(Filex image3rdPartyDir) {
    this.image3rdPartyDir = image3rdPartyDir;
  }

  public Filex getImageMyDir() {
    return imageMyDir;
  }

  public void setImageMyDir(Filex imageMyDir) {
    this.imageMyDir = imageMyDir;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((geogetDataDir == null) ? 0 : geogetDataDir.hashCode());
    result = prime * result + ((image3rdPartyDir == null) ? 0 : image3rdPartyDir.hashCode());
    result = prime * result + ((imageMyDir == null) ? 0 : imageMyDir.hashCode());
    result = prime * result + ((kesDir == null) ? 0 : kesDir.hashCode());
    result = prime * result + ((cestyDir == null) ? 0 : cestyDir.hashCode());
    result = prime * result + ((neGgtFile == null) ? 0 : neGgtFile.hashCode());
    result = prime * result + ((anoGgtFile == null) ? 0 : anoGgtFile.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KesoidUmisteniSouboru that = (KesoidUmisteniSouboru) o;

    if (anoGgtFile != null ? !anoGgtFile.equals(that.anoGgtFile) : that.anoGgtFile != null) return false;
    if (cestyDir != null ? !cestyDir.equals(that.cestyDir) : that.cestyDir != null) return false;
    if (geogetDataDir != null ? !geogetDataDir.equals(that.geogetDataDir) : that.geogetDataDir != null) return false;
    if (image3rdPartyDir != null ? !image3rdPartyDir.equals(that.image3rdPartyDir) : that.image3rdPartyDir != null)
      return false;
    if (imageMyDir != null ? !imageMyDir.equals(that.imageMyDir) : that.imageMyDir != null) return false;
    if (kesDir != null ? !kesDir.equals(that.kesDir) : that.kesDir != null) return false;
    if (neGgtFile != null ? !neGgtFile.equals(that.neGgtFile) : that.neGgtFile != null) return false;

    return true;
  }
}
