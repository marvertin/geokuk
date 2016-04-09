package cz.geokuk.plugins.kesoid.importek;

import java.io.*;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.kesoid.EKesType;
import cz.geokuk.plugins.kesoid.mvc.GsakParametryNacitani;
import cz.geokuk.util.lang.ATimestamp;
import cz.geokuk.util.lang.StringUtils;

/**
 * Loads data from a GSAK database.
 * <p>
 * Information about GSAK DB schema - reverse engineered.
 *
 * @since ISSUE#48 [2016-04-01, Bohusz]
 */
public class GsakDbLoader extends Nacitac0 {
	private static final Logger log = LogManager.getLogger(GsakDbLoader.class.getSimpleName());

	private static int PROGRESS_VAHA_CACHES = 1;
	private static int PROGRESS_VAHA_WAYPOINTS = 16;
	private static int PROGRESS_VAHA_TAGS = 18;

	@SuppressWarnings("unused")
	private static final String ISO_DATE_FORMAT_TEMPLATE = "%d-%02d-%02dT00:00:00.000";
	private static final String ISO_TIME_FORMAT_REGEXP = "[0-2]?\\d:[0-5]\\d";

	private static final ImmutableSet<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("db3");
	private static final ImmutableSet<String> EXPECTED_TABLES = ImmutableSet.of("Attributes", "CacheImages", "CacheMemo", "Caches", "Corrected", "Custom", "Filter", "Ignore", "LogImages", "LogMemo",
	        "Logs");

	private static final ImmutableMap<String, String> ID_PREFIX_TO_SYM = ImmutableMap.of("GC", "Geocache", "WM", "Waymark", "MU", "Geocache");

	private final GsakParametryNacitani parametryNačítání;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////  Public  /////
	public GsakDbLoader(final GsakParametryNacitani aGsakParametryNacitani) {
		parametryNačítání = aGsakParametryNacitani;
	}

	@Override
	protected void nacti(final File aDbFile, final IImportBuilder aBuilder, final Future<?> aFuture, final ProgressModel aProgressModel) throws IOException {
		try (GsakDao dao = new GsakDao(aDbFile)) {
			if (!dao.schemaMatches()) {
				throw new IllegalArgumentException("DB schema doesn't match, cannot load from file " + aDbFile);
			}
			final int pocet = dao.cacheCount() * PROGRESS_VAHA_CACHES + dao.waypointCount() * PROGRESS_VAHA_WAYPOINTS + dao.tagCount() * PROGRESS_VAHA_TAGS;
			final Progressor progressor = aProgressModel.start(pocet, "Loading " + aDbFile.toString());
			loadCaches(dao, aBuilder, aFuture, progressor);
			loadWaypoints(dao, aBuilder, aFuture, progressor);
			loadCustomValues(dao, aBuilder, aFuture, progressor);
			progressor.finish();
		} catch (final SQLException e) {
			if (e.getMessage().contains("no such collation sequence:")) {
				// TODO: Nějak lépe zakomunikovat s uživatelem, nelíbí se mi, že že v BIZ třídě je interakce, ale nevím jak jinak. [ISSUE#48, 2016-04-09, Bohusz]
				Dlg.info("Databázový soubor \"" + aDbFile + "\" obsahuje nestandardní řazení.\n\nMělo by postačit databázi na chvíli vybrat jako aktivní, GSAK ji automaticky opraví.",
				        "GSAK soubor je zastaralý");
				return;
			}
			throw new IOException("Unable to load from " + aDbFile, e);
		}
	}

	@Override
	protected void nacti(final ZipFile zipFile, final ZipEntry zipEntry, final IImportBuilder builder, final Future<?> f, final ProgressModel aProgressModel) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	boolean umiNacist(final File file) {
		if (SUPPORTED_FILE_EXTENSIONS.contains(Files.getFileExtension(file.getAbsolutePath().toLowerCase()))) {
			return dbFileContains(file, EXPECTED_TABLES);
		}
		return false;
	}

	@Override
	boolean umiNacist(final ZipEntry zipEntry) {
		// JDBC can't access zipped files.
		return false;
	}

	//------------------------------------------------------------------------------------------------------  implementation  -----

	private void loadCaches(final GsakDao aDao, final IImportBuilder aBuilder, final Future<?> aFuture, final Progressor aProgressor) throws SQLException, IOException {
		final ATimestamp startTime = ATimestamp.now();
		final Counter čítač = new Counter();

		aDao.forEachCache(record -> {
			if (isCancelled(aFuture)) {
				return false;
			}
			aProgressor.addProgress(PROGRESS_VAHA_CACHES);
			//
			// Příprava dat:
			final Wgs coordinates = record.Latitude == 0.0 && record.Longitude == 0.0 ? null : new Wgs(record.Latitude, record.Longitude);
			final Wgs original = record.LatOriginal == 0.0 && record.LonOriginal == 0.0 ? null : new Wgs(record.LatOriginal, record.LonOriginal);
			//
			// Plnění dat:
			final GpxWpt cache = new GpxWpt();
			{
				cache.name = record.Code;
				if (cache.name != null && cache.name.length() > 1) {
					final String prefix = cache.name.substring(0, 2);
					final String sym = ID_PREFIX_TO_SYM.get(prefix);
					if (sym != null) {
						cache.sym = sym;
					}
				}
				cache.wgs = isCorrected(original, coordinates) ? original : coordinates;
				cache.time = record.PlacedDate;

				final Groundspeak groundspeak = new Groundspeak();
				{
					groundspeak.ownerid = record.OwnerId;
					groundspeak.name = record.Name;
					groundspeak.owner = intern(record.OwnerName);
					groundspeak.placedBy = intern(record.PlacedBy);
					groundspeak.type = GsakCacheType.fromGsakCode(record.CacheType).toGroundspeakName();
					groundspeak.container = intern(record.Container);
					groundspeak.difficulty = intern(record.Difficulty);
					groundspeak.terrain = intern(record.Terrain);
					groundspeak.country = intern(record.Country);
					groundspeak.state = intern(record.State);
					groundspeak.encodedHints = record.Hints;
					groundspeak.shortDescription = record.ShortDescription;
					groundspeak.archived = record.Archived;
					groundspeak.availaible = !record.TempDisabled;
				}
				cache.groundspeak = groundspeak;
				cache.desc = String.format("%s by %s (%s / %s)", cache.groundspeak.name, cache.groundspeak.placedBy, cache.groundspeak.difficulty, cache.groundspeak.terrain);
				cache.link.href = "http://coord.info/" + cache.name;
				cache.link.text = String.format("%s by %s", cache.groundspeak.name, cache.groundspeak.placedBy);

				if (!StringUtils.isBlank(record.FoundByMeDate)) {
					cache.sym = "Geocache Found";
					cache.gpxg.found = record.FoundByMeDate;
					final String time = _getFoundByMeTimeField(record.values);
					if (!StringUtils.isBlank(time)) {
						cache.gpxg.found += "T" + time;
					}
				}
				cache.gpxg.favorites = record.FavPoints;
				cache.gpxg.elevation = record.Elevation;
				cache.gpxg.czkraj = record.State;
				cache.gpxg.czokres = record.County;
				//                gpxWpt.gpxg.bestOf = ???;
				//                gpxWpt.gpxg.hodnoceni = ???;
				//                gpxWpt.gpxg.hodnoceniPocet = ???;
				//                gpxWpt.gpxg.znamka = ???;
			}
			aBuilder.addGpxWpt(cache);
			//
			// Corrected Coordinates:
			if (isCorrected(original, coordinates)) {
				final GpxWpt correctedCoordinateWaypoint = new GpxWpt();
				{
					correctedCoordinateWaypoint.wgs = coordinates;
					correctedCoordinateWaypoint.name = "##" + record.Code.substring(2);
					correctedCoordinateWaypoint.sym = "Final Location";
					correctedCoordinateWaypoint.desc = "Final (" + record.Name + ")";
				}
				aBuilder.addGpxWpt(correctedCoordinateWaypoint);
			}
			//
			čítač.inc();
			return true;
		});

		aProgressor.finish();
		logResult("Geocaches", startTime, čítač.getCount());
	}

	private void loadWaypoints(final GsakDao aDao, final IImportBuilder aBuilder, final Future<?> aFuture, final Progressor aProgressor) throws SQLException, IOException {
		final ATimestamp startTime = ATimestamp.now();
		final Counter čítač = new Counter();

		aDao.forEachWaypoint(record -> {
			if (isCancelled(aFuture)) {
				return false;
			}
			aProgressor.addProgress(PROGRESS_VAHA_WAYPOINTS);
			//
			final GpxWpt childWaypoint = new GpxWpt();
			{
				childWaypoint.wgs = new Wgs(record.cLat, record.cLon);
				childWaypoint.name = record.cCode;
				childWaypoint.sym = record.cType;
				childWaypoint.desc = record.cName;
			}
			aBuilder.addGpxWpt(childWaypoint);
			//
			čítač.inc();
			return true;
		});

		aProgressor.finish();
		logResult("Custom Values", startTime, čítač.getCount());
	}

	private void loadCustomValues(final GsakDao aDao, final IImportBuilder aBuilder, final Future<?> aFuture, final Progressor aProgressor) throws SQLException, IOException {
		final ATimestamp startTime = ATimestamp.now();
		final Counter čítač = new Counter();

		aDao.forEachCustomValue(record -> {
			if (isCancelled(aFuture)) {
				return false;
			}
			aProgressor.addProgress(PROGRESS_VAHA_TAGS);
			//
			final GpxWpt cache = aBuilder.get(record.get(GsakDao.CACHE_CODE_KEY));
			if (cache == null) {
				// Nějaká nekonzistence databáze, custom hodnoty existují, ale keš k nim ne. Asi se to někdy GSAK stane.
				return true;
			}
			record.entrySet().stream().forEach(e -> cache.gpxg.putUserTag(e.getKey(), Objects.toString(e.getValue())));
			//
			if (!StringUtils.isBlank(cache.gpxg.found) && !cache.gpxg.found.contains("T")) {
				final String time = _getFoundByMeTimeField(record);
				if (!StringUtils.isBlank(time)) {
					cache.gpxg.found += "T" + time;
				}
			}
			//
			čítač.inc();
			return true;
		});

		aProgressor.finish();
		logResult("Custom Values", startTime, čítač.getCount());
	}

	//-------------------------------------------------------------------------------------------------------------  utility  -----

	private static boolean isCancelled(final Future<?> aFuture) {
		return aFuture != null && aFuture.isCancelled();
	}

	private boolean isCorrected(final Wgs aOriginalCoordinates, final Wgs aCurrentCoordinates) {
		if (aOriginalCoordinates == null) {
			return false;
		}
		return !aCurrentCoordinates.equals(aOriginalCoordinates);
	}

	private String _getFoundByMeTimeField(final Map<String, ?> values) {
		final Pattern time = Pattern.compile(ISO_TIME_FORMAT_REGEXP);
		for (final String name : parametryNačítání.getCasNalezu()) {
			final Object value = values.get(name);
			final Matcher matcher = time.matcher(Objects.toString(value, ""));
			if (matcher.find()) {
				return matcher.group(0);
			}
		}
		return null;
	}

//	private String formatDateTime(final int yyyymmddDate) {
//		final int day = yyyymmddDate % 100;
//		final int month = yyyymmddDate / 100 % 100;
//		final int year = yyyymmddDate / 10000;
//		return String.format(DATE_FORMAT_TEMPLATE, year, month, day);
//	}
//
	private void logResult(final String nazev, final ATimestamp startTime, final int pocet) {
		final double trvani = ATimestamp.now().diff(startTime);
		log.info("{} {} loaded in {} s, it is {} items/s. ", pocet, nazev, trvani / 1000.0, pocet * 1000 / trvani);
	}

	// Friendly, aby mohlo být použito i v GeogetLoader.
	static boolean dbFileContains(final File aFile, final Set<String> aExpectedTables) {
		try (GsakDao dao = new GsakDao(aFile)) {
			return dao.containsTables(aExpectedTables);
		} catch (IOException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Counter {
		private int iCount;

		public int inc() {
			return ++iCount;
		}

		public int getCount() {
			return iCount;
		}
	}

	private static final EKesType OTHER = EKesType.LOCATIONLESS_REVERSE;
	private static final EKesType EKESTYPE_WAYMARK = OTHER;
	private static final EKesType EKESTYPE_MAZE_EXHIBIT = OTHER;
	private static final EKesType EKESTYPE_LAB_CACHE = OTHER;
	private static final EKesType EKESTYPE_BLOCK_PARTY = OTHER;
	private static final EKesType EKESTYPE_OTHER = OTHER;
	private static final EKesType EKESTYPE_GIGA_EVENT = OTHER;
	private static final EKesType EKESTYPE_GROUNDSPEAK_HQ = OTHER;
	private static final EKesType EKESTYPE_BENCHMARK = OTHER;
	private static final EKesType EKESTYPE_LF_EVENT = OTHER;
	private static final EKesType EKESTYPE_LF_CELEBRATION = OTHER;
	private static final EKesType EKESTYPE_PROJECT_APE = OTHER;

	private enum GsakCacheType {
		// @formatter:off
		A(EKESTYPE_PROJECT_APE,					"Project APE Cache"),
		B(EKesType.LETTERBOX_HYBRID,			"Letterbox Hybrid"),
		C(EKesType.CACHE_IN_TRASH_OUT_EVENT,	"Cache In Trash Out Event"),
		D(EKESTYPE_LF_CELEBRATION,				"Groundspeak Lost and Found Celebration"),
		E(EKesType.EVENT,						"Event Cache"),
		F(EKESTYPE_LF_EVENT,					"Lost and Found Event Caches"),
		G(EKESTYPE_BENCHMARK,					"Benchmark"),
		H(EKESTYPE_GROUNDSPEAK_HQ,				"Groundspeak HQ Cache"),
		I(EKesType.WHERIGO,						"Wherigo Cache"),
		J(EKESTYPE_GIGA_EVENT,					"Giga-Event Cache"),
		L(EKesType.LOCATIONLESS_REVERSE,		"Locationless (Reverse) Cache"),
		M(EKesType.MULTI,						"Multi-cache"),
		O(EKESTYPE_OTHER,						"Other"),
		P(EKESTYPE_BLOCK_PARTY,					"Groundspeak Block Party"),
		Q(EKESTYPE_LAB_CACHE,					"Lab Cache"),
		R(EKesType.EARTHCACHE,					"Earthcache"),
		T(EKesType.TRADITIONAL,					"Traditional Cache"),
		U(EKesType.UNKNOWN,						"Unknown Cache"),
		V(EKesType.VIRTUAL,						"Virtual Cache"),
		W(EKesType.WEBCAM,						"Webcam Cache"),
		X(EKESTYPE_MAZE_EXHIBIT,				"GPS Adventures Exhibit"),
		Y(EKESTYPE_WAYMARK,						"Waymark"),
		Z(EKesType.MEGA_EVENT,					"Mega-Event Cache"),
		;

		private static final GsakCacheType UNRECOGNIZED = L;

		/*
		   <groundspeak:type>Traditional Cache</groundspeak:type>
		   <groundspeak:type>Multi-cache</groundspeak:type>
		   <groundspeak:type>Letterbox Hybrid</groundspeak:type>
		   <groundspeak:type>Cache In Trash Out Event</groundspeak:type>
		   <groundspeak:type>Event Cache</groundspeak:type>
		   <groundspeak:type>Locationless (Reverse) Cache</groundspeak:type>
		   <groundspeak:type>Virtual Cache</groundspeak:type>
		   <groundspeak:type>Webcam Cache</groundspeak:type>
		   <groundspeak:type>Unknown Cache</groundspeak:type>
		   <groundspeak:type>Benchmark</groundspeak:type>
		   <groundspeak:type>Other</groundspeak:type>
		   <groundspeak:type>Earthcache</groundspeak:type>
		   <groundspeak:type>Project APE Cache</groundspeak:type>
		   <groundspeak:type>Mega-Event Cache</groundspeak:type>
		   <groundspeak:type>GPS Adventures Exhibit</groundspeak:type>
		   <groundspeak:type>Wherigo Cache</groundspeak:type>
		   <groundspeak:type>Waymark</groundspeak:type>
		   <groundspeak:type>Lost and Found Event Caches</groundspeak:type>
		   <groundspeak:type>Groundspeak HQ Cache</groundspeak:type>
		   <groundspeak:type>Groundspeak Lost and Found Celebration</groundspeak:type>
		   <groundspeak:type>Groundspeak Block Party</groundspeak:type>
		   <groundspeak:type>Giga-Event Cache</groundspeak:type>
		   <groundspeak:type>Lab Cache</groundspeak:type>
		   */
		// @formatter:off

		private final EKesType iGeokukCacheType;
		private final String iGroundspeakName;

		private GsakCacheType(final EKesType aGeokukCacheType, final String aGroundspeakName) {
			iGeokukCacheType = aGeokukCacheType;
			iGroundspeakName = aGroundspeakName;
		}
		public static GsakCacheType fromGsakCode(final String aGsakCode) {
			return Arrays.stream(GsakCacheType.values())//
			.filter(v -> v.name().equalsIgnoreCase(aGsakCode))//
			.findFirst()
			.orElse(UNRECOGNIZED)
			;
		}
		@SuppressWarnings("unused")
		public EKesType toGeokukType() {
			return iGeokukCacheType;
		}
		public String toGroundspeakName() {
			return iGroundspeakName;
		}
	}

	//---------------------------------------------------------------------------------------------------------  data access  -----

	private static class GsakDao implements Closeable {
		public static final String CACHE_CODE_KEY = "Code";

		private static final String CACHE_COUNT = "SELECT COUNT(*) FROM Caches";
		private static final String WAYPOINT_COUNT = "SELECT COUNT(*) FROM Waypoints";
		private static final String TAG_COUNT = "SELECT COUNT(*) FROM Caches";
		private static final String SELECT_CACHES = "SELECT c.*, m.ShortDescription, m.Hints FROM Caches c JOIN CacheMemo m ON m.Code = c.Code";
		private static final String SELECT_WAYPOINTS = "SELECT * FROM Waypoints";
		private static final String SELECT_CUSTOMVALUES = "SELECT * FROM Custom";

		private final Connection iConnection;
		private final Statement iStatement;

		public GsakDao(final File aSqliteDatabaseFile) throws SQLException {
			final String url = "jdbc:sqlite:" + aSqliteDatabaseFile.getAbsolutePath();
			iConnection = DriverManager.getConnection(url);
			iStatement = iConnection.createStatement();
		}

		@Override
		public void close() throws IOException {
			try {
				try {
					iStatement.close();
				} finally {
					iConnection.close();
				}
			} catch (final SQLException e) {
				throw new IOException("Closing DAO failure.", e);
			}
		}

		public int cacheCount() throws SQLException {
			return count(CACHE_COUNT);
		}

		public int waypointCount() throws SQLException {
			return count(WAYPOINT_COUNT);
		}

		public int tagCount() throws SQLException {
			return count(TAG_COUNT);
		}

		public boolean forEachCache(final Function<GsakCache, Boolean> aAction) throws SQLException {
			return forEach(SELECT_CACHES, GsakCache::new, aAction);
		}

		public boolean forEachWaypoint(final Function<GsakWaypoint, Boolean> aAction) throws SQLException {
			return forEach(SELECT_WAYPOINTS, GsakWaypoint::new, aAction);
		}

		public boolean forEachCustomValue(final Function<Map<String, String>, Boolean> aAction) throws SQLException {
			return forEach(SELECT_CUSTOMVALUES, LinkedHashMap::new, aAction);
		}

		public boolean schemaMatches() throws SQLException {
			return containsTables(EXPECTED_TABLES);
		}

		public boolean containsTables(final Set<String> aExpectedTables) throws SQLException {
			final Set<String> tables = new HashSet<>();
			try (ResultSet rs = iStatement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
				while (rs.next()) {
					tables.add(rs.getString(1));
				}
			}
			final boolean tablesMissing = aExpectedTables.stream()//
			        .filter(expected -> !tables.contains(expected))//
			        .findFirst()//
			        .isPresent()//
			;
			return !tablesMissing;
		}

		//--------------------------------------------------------------------------------------

		private int count(final String aSqlStatement) throws SQLException {
			try (ResultSet rs = iStatement.executeQuery(aSqlStatement)) {
				return rs.getInt(1);
			}
		}

		private <T> boolean forEach(final String aSelectStatement, final Supplier<T> aRecordFactory, final Function<T, Boolean> aAction) throws SQLException {
			try (ResultSet rs = iStatement.executeQuery(aSelectStatement)) {
				while (rs.next()) {
					final T record = loadRecord(rs, aRecordFactory);
					if (!aAction.apply(record)) {
						return false;
					}
				}
			}
			return true;
		}

		private <T> T loadRecord(final ResultSet aResultSet, final Supplier<T> aRecordFactory) throws SQLException {
			final T record = aRecordFactory.get();
			if (record instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>) record;
				loadMap(aResultSet, map);
			} else {
				loadPojo(aResultSet, record);
				if (record instanceof AllValues) {
					loadMap(aResultSet, ((AllValues)record).values);
				}
			}
			return record;
		}

		private <T extends Map<String, Object>> void loadMap(final ResultSet aResultSet, final T aMap) throws SQLException {
			for (final String columnName : columnNames(aResultSet)) {
				final String key = columnName.equalsIgnoreCase("cCode") ? CACHE_CODE_KEY : columnName;
				final Object value = aResultSet.getObject(columnName);
				if (value != null || value instanceof String && !StringUtils.isBlank((String)value)) {
					aMap.put(key, value);
				}
			}
		}

		private <T> void loadPojo(final ResultSet aResultSet, final T aPojo) throws SQLException {
			//System.out.println("***** " + columnNames(aResultSet));
			Arrays.stream(aPojo.getClass().getDeclaredFields()) //
			        .filter(f -> Modifier.isPublic(f.getModifiers())) //
			        .filter(f -> !Modifier.isStatic(f.getModifiers())) //
			        .forEach(f -> {
				        try {
					        final String name = f.getName();
					        final Class<?> type = f.getType();

					        if (type == String.class) {
						        f.set(aPojo, aResultSet.getString(name));
					        } else if (type == boolean.class) {
						        f.set(aPojo, aResultSet.getInt(name) == 1);
					        } else if (type == int.class) {
						        f.set(aPojo, aResultSet.getInt(name));
					        } else if (type == double.class) {
						        f.set(aPojo, aResultSet.getDouble(name));
					        } else if (type == BigDecimal.class) {
						        f.set(aPojo, aResultSet.getBigDecimal(name));
					        } else {
						        throw new IllegalStateException("Unknon type " + type.getSimpleName() + " of field " + name);
					        }
				        } catch (final IllegalAccessException | SQLException e) {
					        throw new RuntimeException("Error reading " + aPojo.getClass().getSimpleName(), e);
				        }
			        });
		}

		private List<String> columnNames(final ResultSet aResultSet) throws SQLException {
			final List<String> result = new LinkedList<>();
			final ResultSetMetaData md = aResultSet.getMetaData();
			final int počet = md.getColumnCount();
			for (int i = 1; i <= počet; i++) {
				result.add(md.getColumnName(i));
			}
			return result;
		}

	}

	private static class GsakCache extends AllValues {
		public String Code;
		public String Name;
		//          public double Distance;
		public String PlacedBy;
		public boolean Archived;
		//          public String Bearing;
		//          public String CacheId;
		public String CacheType;
		//          public String Changed;
		public String Container;
		         public String County;
		public String Country;
		//          public double Degrees;
		public String Difficulty;
		//          public boolean DNF;
		//          public String DNFDate;
		//          public int Found;
		//          public int FoundCount;
		public String FoundByMeDate;
		//          public boolean FTF;
		//          public boolean HasCorrected;
		//          public boolean HasTravelBug;
		//          public boolean HasUserNote;
		//          public String LastFoundDate;
		//          public String LastGPXDate;
		//          public String LastLog;
		//          public String LastUserDate;
		public double Latitude;
		//          public String Lock;
		//          public String LongHtm;
		public double Longitude;
		//          public int MacroFlag;
		//          public String MacroSort;
		//          public int NumberOfLogs;
		public int OwnerId;
		public String OwnerName;
		public String PlacedDate;
		//          public boolean ShortHtm;
		//          public String SmartName;
		//          public boolean SmartOverride;
		//          public String Source;
		public String State;
		//          public String Symbol;
		public boolean TempDisabled;
		public String Terrain;
		//          public String UserData;
		//          public String User2;
		//          public String User3;
		//          public String User4;
		//          public boolean UserFlag;
		//          public String UserNoteDate;
		//          public int UserSort;
		//          public boolean Watch;
		//          public boolean IsOwner;
		          public double LatOriginal;
		          public double LonOriginal;
		//          public String Created;
		//          public String Status;
		//          public String Color;
		//          public boolean ChildLoad;
		//          public String LinkedTo;
		//          public boolean GetPolyFlag;
		public int Elevation;
		//          public String Resolution;
		//          public String GcNote;
		//          public boolean IsPremium;
		//          public String Guid;
		public int FavPoints;

		// TABLE CacheMemo
		//          public String LongDescription;
		public String ShortDescription;
		//          public String Url;
		public String Hints;
		//          public String UserNote;
		//          public String TravelBugs;
		//
	}

	public static class GsakWaypoint extends AllValues {
		public String cParent;
		public String cCode;
		public String cPrefix;
		public String cName;
		public String cType;
		public double cLat;
		public double cLon;
		public boolean cByuser;
		public String cDate;
		public boolean cFlag;
		public boolean sB1;
	}

	public static abstract class AllValues {
		public final Map<String, Object> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}
}
