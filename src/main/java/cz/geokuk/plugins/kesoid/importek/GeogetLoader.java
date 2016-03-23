package cz.geokuk.plugins.kesoid.importek;

import java.io.*;
import java.sql.*;
import java.util.concurrent.Future;
import java.util.zip.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.framework.Progressor;
import cz.geokuk.util.lang.ATimestamp;

/**
 * Loads data from a GeoGet database.
 *
 * <p>
 * Information about GeoGet DB schema: http://geoget.ararat.cz/doku.php/user:databaze
 */
public class GeogetLoader extends Nacitac0 {

	private static final Logger							log							= LogManager.getLogger(GeogetLoader.class.getSimpleName());

	private static int									PROGRESS_VAHA_CACHES		= 1;
	private static int									PROGRESS_VAHA_WAYPOINTS		= 16;
	private static int									PROGRESS_VAHA_TAGS			= 18;

	private static final String							GEOGET_CACHES_QUERY			= Joiner.on('\n').join("SELECT", "  geocache.id as id,", "  geocache.x as lat,", "  geocache.y as lon,",
			"  geocache.name as name,", "  geocache.author as author,", "  geocache.cachetype as cachetype,", "  geocache.cachesize as cachesize,", "  geocache.difficulty as difficulty,",
			"  geocache.terrain as terrain,", "  geocache.cachestatus as cachestatus,", "  geocache.gs_ownerid as gs_ownerid,", "  geocache.dthidden as dthidden,", "  geocache.country as country,",
			"  geocache.state as state,", "  geocache.dtfound as dtfound,", "  geolist.shortdesc as shortdesc,", "  geolist.hint as hint", "FROM geocache", "LEFT JOIN geolist",
			"  ON geocache.id = geolist.id");

	private static final String							GEOGET_CACHES_COUNT			= "SELECT count(*) FROM geocache";

	private static final String							GEOGET_WAYPOINTS_QUERY		= "SELECT id, x as lat, y as lon, prefixid, wpttype, name" + " FROM waypoint";

	private static final String							GEOGET_WAYPOINTS_COUNT		= "SELECT count(*) FROM waypoint";

	private static final String							DATE_FORMAT_TEMPLATE		= "%d-%02d-%02dT00:00:00.000";

	private static final ImmutableSet<String>			SUPPORTED_FILE_EXTENSIONS	= ImmutableSet.of("db3");

	private static final ImmutableMap<String, String>	ID_PREFIX_TO_SYM			= ImmutableMap.of("GC", "Geocache", "WM", "Waymark", "MU", "Geocache");

	private static final String							GEOGET_TAGS_QUERY_FRAGMENT	= Joiner.on('\n').join("FROM geotag t ", "LEFT JOIN geotagcategory c ", "  ON t.ptrkat = c.key ",
			"LEFT JOIN geotagvalue v ", "  ON t.ptrvalue = v.key ",
			"WHERE (c.value IN ('favorites', 'Elevation', 'Hodnoceni-Pocet', 'Hodnoceni', 'BestOf', 'Znamka') or c.value like '" + PREFIX_USERDEFINOANYCH_GENU + "%')");

	private static final String							GEOGET_TAGS_COUNT			= "SELECT count(*) " + GEOGET_TAGS_QUERY_FRAGMENT;
	private static final String							GEOGET_TAGS_QUERY			= Joiner.on('\n').join("SELECT", "  t.id as id,", "  c.value as category,", "  v.value as value ")
			+ GEOGET_TAGS_QUERY_FRAGMENT;

	@Override
	protected void nacti(File file, IImportBuilder builder, Future<?> future, ProgressModel aProgressModel) throws IOException {
		if (!umiNacist(file)) {
			throw new IllegalArgumentException("Cannot load from file " + file);
		}
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath()); Statement statement = c.createStatement()) {
			int pocet = count(statement, GEOGET_CACHES_COUNT) * PROGRESS_VAHA_CACHES + count(statement, GEOGET_WAYPOINTS_COUNT) * PROGRESS_VAHA_WAYPOINTS
					+ count(statement, GEOGET_TAGS_COUNT) * PROGRESS_VAHA_TAGS;
			Progressor progressor = aProgressModel.start(pocet, "Loading " + file.toString());
			loadCaches(statement, builder, future, progressor);
			loadWaypoints(statement, builder, future, progressor);
			loadTags(statement, builder, future, progressor);
			progressor.finish();
		} catch (SQLException e) {
			throw new IOException("Unable to load from " + file, e);
		}
	}

	private int count(Statement statement, String countQuery) throws SQLException {
		try (ResultSet rs = statement.executeQuery(countQuery)) {
			return rs.getInt(1);
		}
	}

	private void logResult(String nazev, ATimestamp startTime, int pocet) {
		double trvani = ATimestamp.now().diff(startTime);
		log.info("{} {} loaded in {} s, it is {} items/s. ", pocet, nazev, trvani / 1000.0, pocet * 1000 / trvani);
	}

	private void loadCaches(Statement statement, IImportBuilder builder, Future<?> future, Progressor progressor) throws SQLException, IOException {
		ATimestamp startTime = ATimestamp.now();
		int citac = 0;
		try (ResultSet rs = statement.executeQuery(GEOGET_CACHES_QUERY)) {
			while (rs.next()) {
				if (future != null && future.isCancelled()) {
					return;
				}
				progressor.addProgress(PROGRESS_VAHA_CACHES);
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
				groundspeak.placedBy = intern(rs.getString("author"));
				groundspeak.owner = intern(groundspeak.placedBy);
				groundspeak.type = intern(rs.getString("cachetype"));
				groundspeak.container = intern(rs.getString("cachesize"));
				groundspeak.difficulty = intern(rs.getString("difficulty"));
				groundspeak.terrain = intern(rs.getString("terrain"));
				groundspeak.country = intern(rs.getString("country"));
				groundspeak.state = intern(rs.getString("state"));
				groundspeak.encodedHints = rs.getString("hint");

				byte[] shortDescBytes = rs.getBytes("shortdesc");
				if (shortDescBytes != null) {
					try (InputStream is = new InflaterInputStream(new ByteArrayInputStream(shortDescBytes))) {
						groundspeak.shortDescription = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
					}
				}

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
				gpxWpt.desc = String.format("%s by %s (%s / %s)", gpxWpt.groundspeak.name, gpxWpt.groundspeak.placedBy, gpxWpt.groundspeak.difficulty, gpxWpt.groundspeak.terrain);

				gpxWpt.link.href = "http://coord.info/" + gpxWpt.name;
				gpxWpt.link.text = String.format("%s by %s", gpxWpt.groundspeak.name, gpxWpt.groundspeak.placedBy);

				long dtfound = rs.getLong("dtfound");
				if (dtfound != 0) {
					gpxWpt.sym = "Geocache Found";
					gpxWpt.gpxg.found = Long.toString(dtfound);
				}

				builder.addGpxWpt(gpxWpt);
				citac++;

			}
		} finally {
			progressor.finish();
			logResult("Geocaches", startTime, citac);
		}
	}

	private void loadWaypoints(Statement statement, IImportBuilder builder, Future<?> future, Progressor progressor) throws SQLException {
		ATimestamp startTime = ATimestamp.now();
		int citac = 0;
		try (ResultSet rs = statement.executeQuery(GEOGET_WAYPOINTS_QUERY)) {
			while (rs.next()) {
				if (future != null && future.isCancelled()) {
					return;
				}
				progressor.addProgress(PROGRESS_VAHA_WAYPOINTS);
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
				citac++;
			}
		} finally {
			progressor.finish();
			logResult("Waypoints", startTime, citac);
		}
	}

	private void loadTags(Statement statement, IImportBuilder builder, Future<?> future, Progressor progressor) throws SQLException {
		ATimestamp startTime = ATimestamp.now();
		int citac = 0;
		try (ResultSet rs = statement.executeQuery(GEOGET_TAGS_QUERY)) {
			while (rs.next()) {
				if (future != null && future.isCancelled()) {
					return;
				}
				progressor.addProgress(PROGRESS_VAHA_TAGS);

				String name = rs.getString("id");
				String category = rs.getString("category");
				String value = rs.getString("value");
				GpxWpt gpxWpt = builder.get(name);

				if (name == null || category == null || value == null || gpxWpt == null) {
					continue;
				}

				try {
					if (category.startsWith(PREFIX_USERDEFINOANYCH_GENU)) {
						gpxWpt.gpxg.putUserTag(category.substring(PREFIX_USERDEFINOANYCH_GENU.length()), value);
					} else {
						switch (category) {
						case "favorites":
							gpxWpt.gpxg.favorites = Integer.parseInt(value);
							break;
						case "Elevation":
							gpxWpt.gpxg.elevation = Integer.parseInt(value);
							break;
						case "BestOf":
							gpxWpt.gpxg.bestOf = Integer.parseInt(value);
							break;
						case "Hodnoceni":
							if (!value.isEmpty()) {
								gpxWpt.gpxg.hodnoceni = Integer.parseInt(value.substring(0, value.length() - 1)); // odříznout procenta
							}
							break;
						case "Hodnoceni-Pocet":
							if (!value.isEmpty()) {
								gpxWpt.gpxg.hodnoceniPocet = Integer.parseInt(value.substring(0, value.length() - 1)); // odříznout x
							}
							break;
						case "Znamka":
							gpxWpt.gpxg.znamka = Integer.parseInt(value);
							break;
						default:
							log.warn("Unknown tag category: {}", category);
						}
					}
				} catch (NumberFormatException e) {
					log.warn("Unable to parse number!", e);
				}
				citac++;
			}
		} finally {
			progressor.finish();
			logResult("Tags", startTime, citac);
		}
	}

	private String formatDateTime(int yyyymmddDate) {
		int day = yyyymmddDate % 100;
		int month = (yyyymmddDate / 100) % 100;
		int year = (yyyymmddDate / 10000);
		return String.format(DATE_FORMAT_TEMPLATE, year, month, day);
	}

	@Override
	protected void nacti(ZipFile zipFile, ZipEntry zipEntry, IImportBuilder builder, Future<?> f, ProgressModel aProgressModel) throws IOException {
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
