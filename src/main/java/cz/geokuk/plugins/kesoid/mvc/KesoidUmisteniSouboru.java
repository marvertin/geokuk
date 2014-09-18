/**
 * 
 */
package cz.geokuk.plugins.kesoid.mvc;


import java.io.File;

import cz.geokuk.core.program.FConst;
import cz.geokuk.core.program.UmisteniSouboru0;
import cz.geokuk.util.file.Filex;

/**
 * @author veverka
 *
 */
public class KesoidUmisteniSouboru extends UmisteniSouboru0  {

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

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
      return super.equals(obj);
      // TODO : Fix this
    /*if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    KesoidUmisteniSouboru other = (KesoidUmisteniSouboru) obj;
    if (geogetDataDir == null) {
      if (other.geogetDataDir != null)
        return false;
    } else if (!geogetDataDir.equals(other.geogetDataDir))
      return false;
    if (image3rdPartyDir == null) {
      if (other.image3rdPartyDir != null)
        return false;
    } else if (!image3rdPartyDir.equals(other.image3rdPartyDir))
      return false;
    if (imageMyDir == null) {
      if (other.imageMyDir != null)
        return false;
    } else if (!imageMyDir.equals(other.imageMyDir))
      return false;
    if (kesDir == null) {
      if (other.kesDir != null)
        return false;
    } else if (!kesDir.equals(other.kesDir))
      return false;
    if (cestyDir == null) {
      if (other.cestyDir != null)
        return false;
    } else if (!cestyDir.equals(other.cestyDir))
      return false;
    if (neGgtFile == null) {
      if (other.neGgtFile != null)
        return false;
    } else if (!neGgtFile.equals(other.neGgtFile))
      return false;
    if (anoGgtFile == null) {
      if (other.anoGgtFile != null)
        return false;
    } else if (!anoGgtFile.equals(other.anoGgtFile))
      return false;
    return true;*/
  }


}
