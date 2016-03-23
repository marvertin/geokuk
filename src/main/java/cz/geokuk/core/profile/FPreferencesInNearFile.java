/**
 *
 */
package cz.geokuk.core.profile;

import java.io.*;
import java.util.prefs.Preferences;

import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.MyPreferences;

/**
 * @author veverka
 *
 */
public final class FPreferencesInNearFile {

	private static boolean ukladatDoSouboru = false;

	public static void deleteAndSwitchOff() {
		FConst.PREFERENCES_FILE.delete();
		ukladatDoSouboru = false;
	}

	/**
	 * @return the ukladatDoSouboru
	 */
	public static boolean isUkladatDoSouboru() {
		return ukladatDoSouboru;
	}

	/**
	 * Nový soubor dotáhne do preferences, pokud
	 */
	public static void loadNearToProgramIfNewer() {
		final long lastModifiedSoubor = FConst.PREFERENCES_FILE.lastModified();
		final long lastModifiedFromPreferences = MyPreferences.root().getLong("lastModified", 0);
		if (lastModifiedSoubor > 0) {
			if (lastModifiedSoubor > lastModifiedFromPreferences) {
				loadNearToProgram();
			}
			ukladatDoSouboru = true;
		}
	}

	public static File saveNearToProgramAndSwitchOn() {
		final File file = saveNearToProgram();
		ukladatDoSouboru = true;
		return file;
	}

	public static void saveNearToProgramIfShould() {
		if (ukladatDoSouboru) {
			saveNearToProgram();
		}
	}

	private static void loadNearToProgram() {
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FConst.PREFERENCES_FILE))) {
			Preferences.importPreferences(bis);
			bis.close();
			updateLastModified();
			System.out.printf("FPreferencesInNearFile: Nactena vesera nastaveni do souboru \"%s\"\n", FConst.PREFERENCES_FILE);
		} catch (final Exception e) {
			throw new RuntimeException("Problem while saving preferences to \"" + FConst.PREFERENCES_FILE + "\"", e);
		}
	}

	private static File saveNearToProgram() {
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FConst.PREFERENCES_FILE))) {
			MyPreferences.root().exportSubtree(bos);
			bos.close();
			updateLastModified();
			ukladatDoSouboru = true;
			System.out.printf("FPreferencesInNearFile: Ulozena vesera nastaveni do souboru \"%s\"\n", FConst.PREFERENCES_FILE);
		} catch (final Exception e) {
			throw new RuntimeException("Problem while saving preferences to \"" + FConst.PREFERENCES_FILE + "\"", e);
		}
		return FConst.PREFERENCES_FILE;
	}

	private static void updateLastModified() {
		final long lastModified = FConst.PREFERENCES_FILE.lastModified();
		MyPreferences.root().putLong("lastModified", lastModified);
	}

	private FPreferencesInNearFile() {}

}
