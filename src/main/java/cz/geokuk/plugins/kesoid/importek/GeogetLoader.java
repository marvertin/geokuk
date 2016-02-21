package cz.geokuk.plugins.kesoid.importek;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import cz.geokuk.core.coordinates.Wgs;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads data from a GeoGet database.
 *
 * <p>Information about GeoGet DB schema: http://geoget.ararat.cz/doku.php/user:databaze
 */
public class GeogetLoader extends Nacitac0 {
  private static final String GEOGET_DB_QUERY =
      "SELECT id, x as lat, y as lon, name, author, cachetype, cachesize, difficulty, terrain, cachestatus " +
          "FROM geocache";

  private static final ImmutableSet<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("db3");

  private static final ImmutableMap<String, String> ID_PREFIX_TO_SYM = ImmutableMap.of(
      "GC", "Geocache",
      "WM", "Waymark",
      "MU", "Geocache");

  @Override
  protected void nacti(File file, IImportBuilder builder, Future<?> future) throws IOException {
    if (!umiNacist(file)) {
      throw new IllegalArgumentException("Cannot load from file " + file);
    }
    try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
         Statement statement = c.createStatement();
         ResultSet rs = statement.executeQuery(GEOGET_DB_QUERY)) {
      while (rs.next()) {
        if (future != null && future.isCancelled()) {
          return;
        }
        GpxWpt gpxWpt = new GpxWpt();
        gpxWpt.wgs = new Wgs(rs.getDouble("lat"), rs.getDouble("lon"));
        gpxWpt.name = rs.getString("id");
        if (gpxWpt.name.length() > 1) {
          String prefix = gpxWpt.name.substring(0, 2);
          String sym = ID_PREFIX_TO_SYM.get(prefix);
          if (sym != null) {
            gpxWpt.sym = sym;
          }
        }

        Groundspeak groundspeak = new Groundspeak();
        groundspeak.name = rs.getString("name");
        groundspeak.placedBy = rs.getString("author");
        groundspeak.type = rs.getString("cachetype");
        groundspeak.container = rs.getString("cachesize");
        groundspeak.difficulty = rs.getString("difficulty");
        groundspeak.terrain = rs.getString("terrain");

        int cacheStatus = rs.getInt("cachestatus");
        switch (cacheStatus) {
          case 0:
            groundspeak.archived = false;
            groundspeak.availaible = true;
            break;
          case 1:
            groundspeak.availaible = false;
            groundspeak.archived = false;
            break;
          case 2:
            groundspeak.archived = true;
            groundspeak.availaible = false;
            break;
        }

        gpxWpt.groundspeak = groundspeak;
        gpxWpt.desc = String.format("%s by %s (%s / %s)",
            gpxWpt.groundspeak.name, gpxWpt.groundspeak.placedBy,
            gpxWpt.groundspeak.difficulty, gpxWpt.groundspeak.terrain);
        builder.addGpxWpt(gpxWpt);
      }
    } catch (SQLException e) {
      throw new IOException("Unable to load from " + file, e);
    }
  }

  @Override
  protected void nacti(ZipFile zipFile, ZipEntry zipEntry, IImportBuilder builder, Future<?> f) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  boolean umiNacist(ZipEntry zipEntry) {
    // JDBC can't access zipped files.
    return false;
  }

  @Override
  boolean umiNacist(File file) {
    return SUPPORTED_FILE_EXTENSIONS.contains(Files.getFileExtension(file.getAbsolutePath().toLowerCase()));
  }
}
