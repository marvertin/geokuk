package cz.geokuk.plugins.kesoid.importek;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import cz.geokuk.core.coordinates.Wgs;

public class NacitacImageMetadata extends NacitacInputStream0 {

  private static final Logger log = LogManager.getLogger(NacitacImageMetadata.class.getSimpleName());

  private static final ImmutableSet<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("jpg", "raw", "tif");

  protected void nacti(InputStream istm, String name, IImportBuilder builder, Future<?> future)
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
