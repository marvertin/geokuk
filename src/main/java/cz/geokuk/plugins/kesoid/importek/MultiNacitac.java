package cz.geokuk.plugins.kesoid.importek;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.DirScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author veverka
 */
public class MultiNacitac {

  private static final Logger log = LogManager.getLogger(MultiNacitac.class.getSimpleName());

  private final DirScanner ds;

  private final List<Nacitac0> nacitace = new ArrayList<>();

  private final KesoidModel kesoidModel;

  //private static final String CACHE_SUFFIX = ".cache.serialized";

  public MultiNacitac(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
    ds = new DirScanner();
    nacitace.add(new NacitacGeokuk());
    nacitace.add(new NacitacGpx());
    nacitace.add(new NacitacImageMetadata());
    nacitace.add(new GeogetLoader());
  }

  public void setDir(File dir, boolean prenacti) {
    ds.setDir(dir, prenacti);
  }

  public KesBag nacti(Future<?> future, Genom genom) throws IOException {
    List<File> list = ds.coMamNacist();
    if (list == null) {
      return null;
    }
    KesoidImportBuilder builder = new KesoidImportBuilder(kesoidModel.getGccomNick(), kesoidModel.getProgressModel());
    builder.init();
    for (File file : list) {
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
   * @param file
   * @param builder
   * @param future
   * @throws IOException
   */
  private void zpracujJedenFile(File file, KesoidImportBuilder builder, Future<?> future) throws IOException {
    if (isZipFile(file)) {
      try (ZipFile zipFile = new ZipFile(file)) {
        boolean nacitat = kesoidModel.maSeNacist(file);
        for (Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements(); ) {
          ZipEntry entry = en.nextElement();
          for (Nacitac0 nacitac : nacitace) {
            builder.setCurrentlyLoaded(file, nacitat);
            if (nacitat && nacitac.umiNacist(entry)) {
              nacitac.nactiBezVyjimky(zipFile, entry, builder, future);
            }
          }
        }
      }
    } else {
      for (Nacitac0 nacitac : nacitace) {
        boolean nacitat = kesoidModel.maSeNacist(file);
        builder.setCurrentlyLoaded(file, nacitat);
        if (nacitat && nacitac.umiNacist(file)) {
          nacitac.nactiBezVyjimky(file, builder, future);
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
