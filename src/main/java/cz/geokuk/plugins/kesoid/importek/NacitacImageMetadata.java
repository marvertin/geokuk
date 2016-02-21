package cz.geokuk.plugins.kesoid.importek;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import cz.geokuk.core.coordinates.Wgs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NacitacImageMetadata extends Nacitac0 {

  private static final Logger log = LogManager.getLogger(NacitacImageMetadata.class.getSimpleName());

  private static final ImmutableSet<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("jpg", "raw", "tif");

  private void nacti(InputStream istm, String name, IImportBuilder builder, Future<?> future)
      throws IOException {
    if (future.isCancelled()) {
      return;
    }
    BufferedInputStream bis;
    if (istm instanceof BufferedInputStream) {
      bis = (BufferedInputStream) istm;
    } else {
      bis = new BufferedInputStream(istm);
    }
    Metadata imageMetadata;
    try {
      imageMetadata = ImageMetadataReader.readMetadata(bis, true);
    } catch (ImageProcessingException e) {
      log.error("The input stream for file " + name + "couldn't be loaded!", e);
      return;
    }
    GpsDirectory gpsDirectory = imageMetadata.getDirectory(GpsDirectory.class);
    if (gpsDirectory == null) {
      log.info("Image has no GPS metadata.");
      return;
    }
    GeoLocation exifLocation = gpsDirectory.getGeoLocation();
    if (exifLocation != null) {
      GpxWpt gpxWpt = new GpxWpt();
      gpxWpt.wgs = new Wgs(exifLocation.getLatitude(), exifLocation.getLongitude());
      gpxWpt.name = Iterables.getLast(Arrays.asList(name.split(Pattern.quote(File.separator))), "");
      gpxWpt.link.href = name;
      gpxWpt.desc = "EXIF coordinates";
      gpxWpt.type = "pic";
      gpxWpt.sym = "Photo";
      builder.addGpxWpt(gpxWpt);
    } else {
      log.info("Image {} has GPS metadata, but no lat/lon information.", name);
    }
  }

  @Override
  protected void nacti(File file, IImportBuilder builder, Future<?> future) throws IOException {
    if (!umiNacist(file)) {
      throw new IllegalArgumentException("Cannot load file " + file);
    }
    nacti(new BufferedInputStream(new FileInputStream(file)), file.getAbsolutePath(), builder, future);
  }

  @Override
  protected void nacti(ZipFile zipFile, ZipEntry zipEntry, IImportBuilder builder, Future<?> future)
      throws IOException {
    if (!umiNacist(zipEntry)) {
      throw new IllegalArgumentException("Cannot load zipped entry " + zipEntry + " from file " + zipFile);
    }
    nacti(new BufferedInputStream(
        zipFile.getInputStream(zipEntry)), zipFile.getName() + "/" + zipEntry.getName(), builder, future);
  }

  @Override
  boolean umiNacist(ZipEntry zipEntry) {
    return umiNacist(zipEntry.getName());
  }

  @Override
  boolean umiNacist(File file) {
    return umiNacist(file.getName());
  }

  private boolean umiNacist(String resourceName) {
    return SUPPORTED_FILE_EXTENSIONS.contains(Files.getFileExtension(resourceName.toLowerCase()));
  }
}
