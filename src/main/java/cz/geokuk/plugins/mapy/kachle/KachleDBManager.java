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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of KachleManager that stores the data to SQLite database.
 *
 * @author Danstahr
 */
// TODO : remove hardcoded values and magic constants
public class KachleDBManager implements KachleManager {

    private static final Logger log = LogManager.getLogger(KachleDBManager.class.getSimpleName());

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
    final Map<Thread, SqlJetDb> connections = new ConcurrentHashMap<>();

    /**
     * The DB file.
     */
    final File databaseFile = new File("/tmp/database.sqlite");

    /**
     * Get a connection to the database for the current thread.
     * @return
     *      The connection or null if the connection couldn't be established.
     *
     * @see #connections
     */
    private SqlJetDb getDatabaseConnection() {
        Thread t = Thread.currentThread();
        if (connections.containsKey(t)) {
            return connections.get(t);
        } else {
            try {
                SqlJetDb database = SqlJetDb.open(databaseFile, true);
                connections.put(t, database);
                return database;
            } catch (SqlJetException e) {
                log.error("Unable to establish the DB connection!", e);
                return null;
            }
        }
    }

    /**
     * Constructs a new instance of the DB Manager.
     * The constructor also verifies the database and creates the necessary tables if needed.
     * Creating multiple instances at the same location from multiple threads should be avoided
     * and a single instance should be used for all threads.
     */
    public KachleDBManager() {
        log.trace("Constructor " + Thread.currentThread().getName());
        SqlJetDb database = getDatabaseConnection();
        try {
            Set<String> tables = database.getSchema().getTableNames();
            if (!tables.contains(TABLE_NAME)) {
                database.getOptions().setAutovacuum(true);
                database.runWriteTransaction(new ISqlJetTransaction() {
                    @Override
                    public Object run(SqlJetDb sqlJetDb) throws SqlJetException {
                        sqlJetDb.createTable(TABLE_CREATE_QUERY);
                        return true;
                    }
                });
            }
        } catch (SqlJetException e) {
            log.error("A database error has occurred!", e);
        } finally {
            try {
                database.commit();
            } catch (SqlJetException e) {
                log.error("Couldn't commit to the database!", e);
            }
        }
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
            log.error("Uhm... Something's terribly wrong", e);
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
