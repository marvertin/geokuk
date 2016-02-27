package cz.geokuk.plugins.kesoid.importek;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.DirScanner;
import cz.geokuk.util.file.KeFile;
import cz.geokuk.util.file.Root;

/**
 * @author veverka
 */
public class MultiNacitac {

  private static final Logger log = LogManager.getLogger(MultiNacitac.class.getSimpleName());

  private final DirScanner ds;

  private final List<Nacitac0> nacitace = new ArrayList<>();

  private final KesoidModel kesoidModel;
  
  private static final Pattern FILE_NAME_REGEX_GEOKUK_DIR = Pattern.compile("(?i).*\\.(geokuk|gpx|zip|jpg|raw|tif)");
  private static final Pattern FILE_NAME_REGEX_GEOGET_DIR = Pattern.compile("geoget.db3");


  //private static final String CACHE_SUFFIX = ".cache.serialized";

  public MultiNacitac(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
    ds = new DirScanner();
    nacitace.add(new NacitacGeokuk());
    nacitace.add(new NacitacGpx());
    nacitace.add(new NacitacImageMetadata());
    nacitace.add(new GeogetLoader());
  }

  public void setRootDirs(boolean prenacti, File kesDir, File geogetDir) {
    ds.seRootDirs(prenacti, new Root(kesDir, FILE_NAME_REGEX_GEOKUK_DIR), 
        new Root(geogetDir, FILE_NAME_REGEX_GEOGET_DIR));
  }

  public void setGeogetDataDir(File aEffectiveFile, boolean aPrenacti) {
    // TODO Auto-generated method stub
  }

  public KesBag nacti(Future<?> future, Genom genom) throws IOException {
    List<KeFile> list = ds.coMamNacist();
    if (list == null) {
      return null;
    }
    KesoidImportBuilder builder = new KesoidImportBuilder(kesoidModel.getGccomNick(), kesoidModel.getProgressModel());
    builder.init();
    for (KeFile file : list) {
      log.debug("Nacitam: " + file);
      try {
        zpracujJedenFile(file, builder, future);
      } catch (Exception e) {
        FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Problem pri cteni souboru " + file);
        ds.nulujLastScaned(); // ať se načte znovu
      }
    }

    builder.done(genom);

    return builder.getKesBag();
  }

  /**
   * @param kefile
   * @param builder
   * @param future
   * @throws IOException
   */
  private void zpracujJedenFile(KeFile kefile, KesoidImportBuilder builder, Future<?> future) throws IOException {
    File file = kefile.getFile();
    if (isZipFile(file)) {
      try (ZipFile zipFile = new ZipFile(file)) {
        boolean nacitat = kesoidModel.maSeNacist(kefile);
        for (Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements(); ) {
          ZipEntry entry = en.nextElement();
          for (Nacitac0 nacitac : nacitace) {
            builder.setCurrentlyLoading(kefile, nacitat);
            if (nacitat && nacitac.umiNacist(entry)) {
              nacitac.nactiBezVyjimky(zipFile, entry, builder, future, kesoidModel.getProgressModel());
            }
          }
        }
      }
    } else {
      for (Nacitac0 nacitac : nacitace) {
        boolean nacitat = kesoidModel.maSeNacist(kefile);
        builder.setCurrentlyLoading(kefile, nacitat);
        if (nacitat && nacitac.umiNacist(file)) {
          nacitac.nactiBezVyjimky(file, builder, future, kesoidModel.getProgressModel());
        }
      }
    }
  }

  /**
   * Checks whether the given file is a ZIP file.
   * Copied from
   * http://www.java2s.com/Code/Java/File-Input-Output/DeterminewhetherafileisaZIPFile.htm
   */
  private static boolean isZipFile(File fileToTest) {
    if (fileToTest.isDirectory()) {
      return false;
    }
    if (fileToTest.length() < 4) {
      return false;
    }
    try (DataInputStream in = new DataInputStream(new BufferedInputStream(
        new FileInputStream(fileToTest)))) {
      int test = in.readInt();
      return test == 0x504b0304;
    } catch (IOException e) {
      throw new IllegalArgumentException("The file " + fileToTest + " cannot be checked!",
          e);
    }
  }

}
