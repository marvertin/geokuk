package cz.geokuk.plugins.kesoid.importek;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import cz.geokuk.core.coordinates.Wgs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.concurrent.Future;
import java.util.zip.*;

/**
 * Loads data from a GeoGet database.
 *
 * <p>Information about GeoGet DB schema: http://geoget.ararat.cz/doku.php/user:databaze
 */
public class GeogetLoader extends Nacitac0 {
  private static final String GEOGET_CACHES_QUERY =
      Joiner.on('\n').join(
          "SELECT",
          "  geocache.id as id,",
          "  geocache.x as lat,",
          "  geocache.y as lon,",
          "  geocache.name as name,",
          "  geocache.author as author,",
          "  geocache.cachetype as cachetype,",
          "  geocache.cachesize as cachesize,",
          "  geocache.difficulty as difficulty,",
          "  geocache.terrain as terrain,",
          "  geocache.cachestatus as cachestatus,",
          "  geocache.gs_ownerid as gs_ownerid,",
          "  geocache.dthidden as dthidden,",
          "  geocache.country as country,",
          "  geocache.state as state,",
          "  geolist.shortdesc as shortdesc,",
          "  geolist.hint as hint",
          "FROM geocache",
          "LEFT JOIN geolist",
          "  ON geocache.id = geolist.id");

  private static final String GEOGET_WAYPOINTS_QUERY = "SELECT id, x as lat, y as lon, prefixid, wpttype, name" +
      " FROM waypoint";

  private static final String DATE_FORMAT_TEMPLATE = "%d-%02d-%02dT00:00:00.000";

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
         Statement statement = c.createStatement()) {
        loadCaches(statement, builder, future);
        loadWaypoints(statement, builder, future);
      }
    catch (SQLException e) {
      throw new IOException("Unable to load from " + file, e);
    }
  }

  private void loadCaches(Statement statement, IImportBuilder builder, Future<?> future)
      throws SQLException, IOException {
    try (ResultSet rs = statement.executeQuery(GEOGET_CACHES_QUERY)) {
      while (rs.next()) {
        if (future != null && future.isCancelled()) {
          return;
        }
        GpxWpt gpxWpt = new GpxWpt();
        gpxWpt.wgs = new Wgs(rs.getDouble("lat"), rs.getDouble("lon"));
        gpxWpt.name = rs.getString("id");
        if (gpxWpt.name != null && gpxWpt.name.length() > 1) {
          String prefix = gpxWpt.name.substring(0, 2);
          String sym = ID_PREFIX_TO_SYM.get(prefix);
          if (sym != null) {
            gpxWpt.sym = sym;
          }
        }

        gpxWpt.time = formatDateTime(rs.getInt("dthidden"));

        Groundspeak groundspeak = new Groundspeak();
        groundspeak.ownerid = rs.getInt("gs_ownerid");
        groundspeak.name = rs.getString("name");
        groundspeak.placedBy = rs.getString("author");
        groundspeak.owner = groundspeak.placedBy;
        groundspeak.type = rs.getString("cachetype");
        groundspeak.container = rs.getString("cachesize");
        groundspeak.difficulty = rs.getString("difficulty");
        groundspeak.terrain = rs.getString("terrain");
        groundspeak.country = rs.getString("country");
        groundspeak.state = rs.getString("state");

        InputStream shortDescBlob = rs.getBinaryStream("shortdesc");
        InflaterInputStream inflaterStream = new InflaterInputStream(shortDescBlob);
        groundspeak.shortDescription = CharStreams.toString(new InputStreamReader(inflaterStream, Charsets.UTF_8));
        Closeables.closeQuietly(inflaterStream);
        groundspeak.encodedHints = rs.getString("hint");

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
    }
  }

  private void loadWaypoints(Statement statement, IImportBuilder builder, Future<?> future) throws SQLException {
    try (ResultSet rs = statement.executeQuery(GEOGET_WAYPOINTS_QUERY)) {
      while (rs.next()) {
        if (future != null && future.isCancelled()) {
          return;
        }
        GpxWpt gpxWpt = new GpxWpt();
        gpxWpt.wgs = new Wgs(rs.getDouble("lat"), rs.getDouble("lon"));
        String parentId = rs.getString("id");
        if (parentId != null && parentId.length() > 1) {
          String suffix = parentId.substring(2);
          gpxWpt.name = rs.getString("prefixid") + suffix;
        }
        gpxWpt.sym = rs.getString("wpttype");
        gpxWpt.desc = rs.getString("name");
        builder.addGpxWpt(gpxWpt);
      }
    }
  }

  private String formatDateTime(int yyyymmddDate) {
    int day = yyyymmddDate % 100;
    int month = (yyyymmddDate / 100) % 100;
    int year = (yyyymmddDate / 10000);
    return String.format(DATE_FORMAT_TEMPLATE, year, month, day);
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
