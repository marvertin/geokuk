/**
 * 
 */
package cz.geokuk.core.profile;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.Preferences;

import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.MyPreferences;

/**
 * @author veverka
 *
 */
public final class FPreferencesInNearFile {

  private static boolean ukladatDoSouboru = false;

  private FPreferencesInNearFile() {}

  public static void saveNearToProgramIfShould() {
    if (ukladatDoSouboru) {
      saveNearToProgram();
    }
  }

  public static void deleteAndSwitchOff() {
    FConst.PREFERENCES_FILE.delete();
    ukladatDoSouboru = false;
  }

  public static File saveNearToProgramAndSwitchOn() {
    File file = saveNearToProgram();
    ukladatDoSouboru = true;
    return file;
  }

  private static File saveNearToProgram() {
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FConst.PREFERENCES_FILE))){
      MyPreferences.root().exportSubtree(bos);
      bos.close();
      updateLastModified();
      ukladatDoSouboru = true;
      System.out.printf("FPreferencesInNearFile: Ulozena vesera nastaveni do souboru \"%s\"\n", FConst.PREFERENCES_FILE);
    } catch (Exception e) {
      throw new RuntimeException("Problem while saving preferences to \"" + FConst.PREFERENCES_FILE + "\"", e);
    }
    return FConst.PREFERENCES_FILE;
  }


  /**
   * Nový soubor dotáhne do preferences, pokud
   */
  public static void loadNearToProgramIfNewer() {
    long lastModifiedSoubor = FConst.PREFERENCES_FILE.lastModified();
    long lastModifiedFromPreferences = MyPreferences.root().getLong("lastModified", 0);
    if (lastModifiedSoubor > 0) {
      if (lastModifiedSoubor > lastModifiedFromPreferences) {
        loadNearToProgram();
      }
      ukladatDoSouboru = true;
    }
  }

  private static void loadNearToProgram() {
    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FConst.PREFERENCES_FILE))){
      Preferences.importPreferences(bis);
      bis.close();
      updateLastModified();
      System.out.printf("FPreferencesInNearFile: Nactena vesera nastaveni do souboru \"%s\"\n", FConst.PREFERENCES_FILE);
    } catch (Exception e) {
      throw new RuntimeException("Problem while saving preferences to \"" + FConst.PREFERENCES_FILE + "\"", e);
    }
  }

  private static void updateLastModified() {
    long lastModified = FConst.PREFERENCES_FILE.lastModified();
    MyPreferences.root().putLong("lastModified", lastModified);
  }

  /**
   * @return the ukladatDoSouboru
   */
  public static boolean isUkladatDoSouboru() {
    return ukladatDoSouboru;
  }

}
