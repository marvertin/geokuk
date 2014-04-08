package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.core.coordinates.Mou;
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
 * Created by dan on 7.4.14.
 */
public class KachleDBManager implements KachleManager {
    // TODO : improve the hardcoded values

    final Map<Thread, SqlJetDb> connections = new ConcurrentHashMap<>();

    final File databaseFile = new File("/tmp/database.sqlite");

    private SqlJetDb getDatabaseConnection() {
        // Don't need to check the multi-thread stuff, it's one per thread
        Thread t = Thread.currentThread();
        if (connections.containsKey(t)) {
            return connections.get(t);
        } else {
            try {
                SqlJetDb database = SqlJetDb.open(databaseFile, true);
                connections.put(t, database);
                return database;
            } catch (SqlJetException e) {
                System.err.println("Unable to establish the DB connection!");
                return null;
            }
        }
    }

    public KachleDBManager() {
        System.out.println("Constructor " + Thread.currentThread().getName());
        SqlJetDb database = getDatabaseConnection();
        try {
            Set<String> tables = database.getSchema().getTableNames();
            if (!tables.contains("tiles")) {
                database.getOptions().setAutovacuum(true);
                database.runWriteTransaction(new ISqlJetTransaction() {
                    @Override
                    public Object run(SqlJetDb sqlJetDb) throws SqlJetException {
                        sqlJetDb.createTable("CREATE TABLE tiles(x int, y int, " +
                                "z int, s varchar(10), image blob, PRIMARY KEY(x, y, z, s))");
                        return true;
                    }
                });
            }
        } catch (SqlJetException e) {
            e.printStackTrace();
        } finally {
            try {
                database.commit();
            } catch (SqlJetException e) {
                System.err.println("Couldn't commit to the database!");
            }
        }
    }

    @Override
    public boolean exists(final Ka0 ki) {
        // TODO : No need to lad the whole image
        return load(ki) != null;
    }

    @Override
    public Image load(final Ka0 ki) {
        SqlJetDb database = getDatabaseConnection();
        Image img = null;
        ISqlJetCursor cursor = null;

        try {
            ISqlJetTable table = database.getTable("tiles");
            database.beginTransaction(SqlJetTransactionMode.READ_ONLY);
            cursor = table.lookup(table.getPrimaryKeyIndexName(), ki.getLoc().getMou().xx,
                    ki.getLoc().getMou().yy, ki.getLoc().getMoumer(), ki.typToString());
            if (cursor.eof()) {
                return null;
            }
            System.out.println(cursor.getRowId() + " : " +
                    cursor.getInteger("x") + " " +
                    cursor.getInteger("y") + " " +
                    cursor.getInteger("z") + " " +
                    cursor.getString("s") + " loading from DB!");
            img = ImageIO.read(cursor.getBlobAsStream("image"));
            if (img == null) {
                System.out.println("null");
            }
        } catch (SqlJetException | IOException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (SqlJetException e) {
                    System.err.println("Couldn't close the cursor!");
                }
            }
            try {
                database.commit();
            } catch (SqlJetException e) {
                System.err.println("Couldn't commit to the database!");
            }
        }
        return img;
    }

    @Override
    public boolean save(final Ka0 ki, ImageSaver dss) {
        SqlJetDb database = getDatabaseConnection();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(256*256);
        try {
            dss.save(bos);
        } catch (IOException e) {
            System.err.println("Uhm... Something's terribly wrong");
            return false;
        }

        final byte[] dataToSave = bos.toByteArray();
        try {
            database.beginTransaction(SqlJetTransactionMode.WRITE);
            Mou mou = ki.getLoc().getMou();
            System.out.println("Adding " + mou.xx + " " + mou.yy + " " + ki.getLoc().getMoumer() + " " +
                        ki.typToString());
            database.getTable("tiles").insertOr(SqlJetConflictAction.REPLACE, mou.xx, mou.yy, ki.getLoc().getMoumer(),
                    ki.typToString(), dataToSave);
            return true;
        } catch (SqlJetException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                database.commit();
            } catch (SqlJetException e) {
                System.out.println("Unable to commit the changes!");
            }
        }
    }
}
