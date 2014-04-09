package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.core.coordinates.Mou;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.schema.SqlJetConflictAction;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of KachleManager that stores the data to SQLite database.
 *
 * @author Danstahr
 */
public class KachleDBManager implements KachleManager {

    private static final Logger log = LogManager.getLogger(KachleDBManager.class.getSimpleName());

    /**
     * The name of the SQLite file
     */
    private static final String FILE_NAME = "tiles.sqlite";

    /**
     * The name of the table with tiles
     */
    private static final String TABLE_NAME = "tiles";

    /**
     * A query to create the appropriate table
      */
    private static final String TABLE_CREATE_QUERY = String.format("CREATE TABLE %s (x int, y int, " +
            "z int, s varchar(10), image blob, PRIMARY KEY(x, y, z, s))", TABLE_NAME);

    /**
     * Since we've got many threads that can write to the database, using a single connection
     * is hardly possible (would require synchronization on code level, which is not the way
     * to go). We also want to avoid exposing the implementation details further. Since the
     * number of threads is small enough, we don't need a connection pool and instead we've got
     * a connection for each thread.
      */
    final Map<Map.Entry<Thread, File>, SqlJetDb> connections = new ConcurrentHashMap<>();

    /**
     * The DB file.
     */
    final KachleCacheFolderHolder folderHolder;

    /**
     * Get a connection to the database for the current thread. Also closes all invalid connections for the current
     * thread.
     * @return
     *      The connection or null if the connection couldn't be established.
     *
     * @see #connections
     */
    private SqlJetDb getDatabaseConnection() {
        Thread t = Thread.currentThread();
        File folder = folderHolder.getKachleCacheFolder().getEffectiveFile();
        File f = new File(folder, FILE_NAME);
        AbstractMap.SimpleImmutableEntry<Thread, File> mapKey = new AbstractMap.SimpleImmutableEntry<>(t, f);

        if (connections.containsKey(mapKey)) {
            // Got a valid connection
            return connections.get(mapKey);
        } else {
            try {
                // create a new connection
                SqlJetDb database = SqlJetDb.open(f, true);

                // Initialize the DB if needed
                if (!isDbInitialized(database)) {
                    initDb(database);
                }

                // Close all deprecated connections (should be exactly one or zero)
                // Done here for synchronization reasons. This way, we never close an active connection that's
                // needed elsewhere at the same moment and there's always at most one connection per thread.
                for (Map.Entry<Map.Entry<Thread, File>, SqlJetDb> conn : connections.entrySet()) {
                    if (conn.getKey().getKey().equals(t)) {
                        conn.getValue().close();
                        connections.remove(conn.getKey());
                    }
                }

                // Now, store the new connection and return it
                connections.put(mapKey, database);
                return database;
            } catch (SqlJetException e) {
                log.error("Unable to establish the DB connection!", e);
                return null;
            }
        }
    }

    /**
     * Checks whether the DB at the current location is initialized and rady for use.
     * @param connection
     *      A connection to the database.
     * @return
     *      True if its initialized, false otherwise
     */
    private boolean isDbInitialized(SqlJetDb connection) {
        try {
            return connection.getSchema().getTableNames().contains(TABLE_NAME);
        } catch (SqlJetException e) {
            log.error("A database error has occurred!", e);
            return false;
        }
    }

    /**
     * Initializes the database (if needed)
     * @param connection
     *      A connection to the database.
     */
    private synchronized void initDb(SqlJetDb connection) {
        // Another thread might have already done this, so check it once again
        if (!isDbInitialized(connection)) {
            // load and initialize the new DB
            try {
                connection.getOptions().setAutovacuum(true);
                connection.runWriteTransaction(new ISqlJetTransaction() {
                    @Override
                    public Object run(SqlJetDb sqlJetDb) throws SqlJetException {
                        sqlJetDb.createTable(TABLE_CREATE_QUERY);
                        return true;
                    }
                });
            } catch (SqlJetException e) {
                log.error("A database error has occurred!", e);
            } finally {
                try {
                    connection.commit();
                } catch (SqlJetException e) {
                    log.error("Couldn't commit to the database!", e);
                }
            }
        }
    }

    /**
     * Constructs a new instance of the DB Manager.
     * The constructor also verifies the database and creates the necessary tables if needed.
     * Creating multiple instances at the same location from multiple threads should be avoided
     * and a single instance should be used for all threads.
     */
    public KachleDBManager(KachleCacheFolderHolder holder) {
        log.trace("Constructor " + Thread.currentThread().getName());
        folderHolder = holder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final Ka0 ki) {
        // TODO : No need to load the whole image
        return load(ki) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image load(final Ka0 ki) {
        SqlJetDb database = getDatabaseConnection();
        Image img = null;
        ISqlJetCursor cursor = null;

        try {
            ISqlJetTable table = database.getTable(TABLE_NAME);
            database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
            cursor = table.lookup(table.getPrimaryKeyIndexName(), ki.getLoc().getMou().xx,
                    ki.getLoc().getMou().yy, ki.getLoc().getMoumer(), ki.typToString());
            if (cursor.eof()) {
                return null;
            }
            log.debug("{} : {} {} {} {} loading from DB", cursor.getRowId(), cursor.getInteger("x"),
                    cursor.getInteger("y"), cursor.getInteger("z"), cursor.getString("s"));
            img = ImageIO.read(cursor.getBlobAsStream("image"));
            if (img == null) {
                log.debug("Loaded DB image is null!");
            }
        } catch (SqlJetException | IOException e) {
            log.error("A database error has occurred!", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (SqlJetException e) {
                    log.error("Couldn't close the cursor!", e);
                }
            }
            try {
                database.commit();
            } catch (SqlJetException e) {
                log.error("Couldn't commit to the database!", e);
            }
        }
        return img;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean save(final Ka0 ki, ImageSaver dss) {
        SqlJetDb database = getDatabaseConnection();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(256*256);
        try {
            dss.save(bos);
        } catch (IOException e) {
            log.error("Uhm... Something's terribly wrong.", e);
            return false;
        }

        final byte[] dataToSave = bos.toByteArray();
        try {
            database.beginTransaction(SqlJetTransactionMode.WRITE);
            Mou mou = ki.getLoc().getMou();
            log.debug("Adding {} {} {} {}", mou.xx, mou.yy, ki.getLoc().getMoumer(), ki.typToString());
            database.getTable(TABLE_NAME).insertOr(SqlJetConflictAction.REPLACE, mou.xx, mou.yy, ki.getLoc().getMoumer(),
                    ki.typToString(), dataToSave);
            return true;
        } catch (SqlJetException e) {
            log.error("A database error has occurred!", e);
            return false;
        } finally {
            try {
                database.commit();
            } catch (SqlJetException e) {
                log.error("Couldn't commit to the database!", e);
            }
        }
    }
}
