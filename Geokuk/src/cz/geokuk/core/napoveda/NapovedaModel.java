package cz.geokuk.core.napoveda;

import java.net.MalformedURLException;
import java.net.URL;

import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.Model0;
import cz.geokuk.util.process.BrowserOpener;

public class NapovedaModel extends Model0 {

  @Override
  protected void initAndFire() {
    fire(new NapovedaModelChangedEvent());
  }

  public void zkontrolujNoveAktualizace(boolean zobrazovatInfoPriSpravneVerzi) {
    if (isOnlineMode()) {
      new ZkontrolovatAktualizaceSwingWorker(zobrazovatInfoPriSpravneVerzi).execute();
    }

  }

  /**
   * @param onlineMode the onlineMode to set
   */
  public void setOnlineMode(boolean onlineMode) {
    if (onlineMode == isOnlineMode()) return;
    currPrefe().putBoolean("onlineMode", onlineMode);
    fire(new NapovedaModelChangedEvent());
  }


  /**
   * @return the onlineMode
   */
  public boolean isOnlineMode() {
    //    return Settings.vseobecne.onlineMode.isSelected();
    boolean b = currPrefe().getBoolean("onlineMode", true);
    return b;
  }


  public void zobrazNapovedu(String tema) {
    try {
      BrowserOpener.displayURL(new URL(tema == null ? FConst.WEB_PAGE_WIKI : FConst.WEB_PAGE_WIKI + "/" + tema));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }

  }

}
