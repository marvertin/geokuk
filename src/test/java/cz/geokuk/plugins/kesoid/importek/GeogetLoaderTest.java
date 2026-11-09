package cz.geokuk.plugins.kesoid.importek;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.framework.Progressor;

public class GeogetLoaderTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void loadsCachesWaypointsAndTagsWithAnEmptyDescription() throws Exception {
        assertImports(new byte[0], null);
    }

    @Test public void decompressesANonEmptyDescription() throws Exception {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(compressed)) {
            zip.write("Krátký popis".getBytes(StandardCharsets.UTF_8));
        }
        assertImports(compressed.toByteArray(), "Krátký popis");
    }

    private void assertImports(byte[] description, String expectedDescription) throws Exception {
        File database = temporary.newFile("geoget.db3");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             Statement sql = connection.createStatement()) {
            sql.execute("CREATE TABLE geocache (id TEXT, x REAL, y REAL, name TEXT, author TEXT, cachetype TEXT, cachesize TEXT, difficulty TEXT, terrain TEXT, cachestatus INTEGER, gs_ownerid INTEGER, dthidden INTEGER, country TEXT, state TEXT, dtfound INTEGER)");
            sql.execute("CREATE TABLE geolist (id TEXT, shortdesc BLOB, hint TEXT)");
            sql.execute("CREATE TABLE waypoint (id TEXT, x REAL, y REAL, prefixid TEXT, wpttype TEXT, name TEXT)");
            sql.execute("CREATE TABLE geotag (id TEXT, ptrkat INTEGER, ptrvalue INTEGER)");
            sql.execute("CREATE TABLE geotagcategory (key INTEGER, value TEXT)");
            sql.execute("CREATE TABLE geotagvalue (key INTEGER, value TEXT)");
            sql.execute("INSERT INTO geocache VALUES ('GC123', 50.0, 14.0, 'Test cache', 'Owner', 'Traditional Cache', 'Small', '1.5', '2', 0, 1, 20250101, 'Czech Republic', '', 0)");
            sql.execute("INSERT INTO waypoint VALUES ('GC123', 50.1, 14.1, 'FI', 'Final Location', 'Final')");
            sql.execute("INSERT INTO geotagcategory VALUES (1, 'favorites')");
            sql.execute("INSERT INTO geotagvalue VALUES (1, '7')");
            sql.execute("INSERT INTO geotag VALUES ('GC123', 1, 1)");
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO geolist (id, shortdesc) VALUES ('GC123', ?)")) {
                insert.setBytes(1, description);
                insert.executeUpdate();
            }
        }

        Map<String, GpxWpt> points = new HashMap<>();
        IImportBuilder builder = mock(IImportBuilder.class);
        doAnswer(invocation -> {
            GpxWpt point = (GpxWpt) invocation.getArguments()[0];
            points.put(point.name, point);
            return null;
        }).when(builder).addGpxWpt(any(GpxWpt.class));
        when(builder.get(anyString())).thenAnswer(invocation -> points.get(invocation.getArguments()[0]));
        ProgressModel progress = mock(ProgressModel.class);
        when(progress.start(anyInt(), anyString())).thenReturn(mock(Progressor.class));

        GeogetLoader loader = new GeogetLoader();
        assertTrue(loader.umiNacist(database));
        loader.nacti(database, builder, null, progress);

        assertEquals(2, points.size());
        assertEquals("Test cache", points.get("GC123").groundspeak.name);
        assertEquals(expectedDescription, points.get("GC123").groundspeak.shortDescription);
        assertEquals(7, points.get("GC123").gpxg.favorites);
        assertEquals("Final Location", points.get("FI123").sym);
    }
}
