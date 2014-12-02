package cz.geokuk.plugins.kesoid.importek;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import cz.geokuk.core.coordinates.Wgs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

public class NacitacImageMetadata extends Nacitac0 {

    private static final Logger log = LogManager.getLogger(NacitacImageMetadata.class.getSimpleName());

    @Override
    protected void nactiKdyzUmis(InputStream istm, String jmeno, IImportBuilder builder, Future<?> future)
            throws IOException {
        // TODO : code cleanup, other images than JPG
        if (!jmeno.toLowerCase().endsWith("jpg")) {
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
            log.error("The input stream with name " + jmeno + " couldn't be loaded!", e);
            return;
        }
        GpsDirectory gpsDirectory = imageMetadata.getDirectory(GpsDirectory.class);
        if (gpsDirectory == null) {
            log.info("Image {} has no GPS metadata.", jmeno);
            return;
        }
        GeoLocation exifLocation = gpsDirectory.getGeoLocation();
        if (exifLocation != null) {
            GpxWpt gpxWpt = new GpxWpt();
            gpxWpt.wgs = new Wgs(exifLocation.getLatitude(), exifLocation.getLongitude());
            gpxWpt.name = jmeno;
            gpxWpt.desc = "PHOTO";
            // TODO : set the correct type, add icons
            gpxWpt.type = "wm";
            gpxWpt.sym = "Photos Then and Now";
            // gpxWpt.type = "pic";
            // gpxWpt.sym = "photo";
            builder.addGpxWpt(gpxWpt);
        } else {
            log.info("Image {} has GPS metadata, but no lat/lon information.", jmeno);
        }
    }
}
