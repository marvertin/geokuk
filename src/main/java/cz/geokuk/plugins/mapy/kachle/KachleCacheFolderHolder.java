/**
 * 
 */
package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.util.file.Filex;

/**
 * @author veverka
 *
 */
public class KachleCacheFolderHolder {

  private Filex kachleCacheDir;

  public Filex getKachleCacheFolder() {
    return kachleCacheDir;
  }

  /**
   * @param kachleCacheDir
   */
  public void setKachleCacheDir(Filex kachleCacheDir) {
    this.kachleCacheDir = kachleCacheDir;
  }
}
