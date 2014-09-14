package cz.geokuk.plugins.mapy.kachle;

import java.awt.*;
import java.util.Collection;

/**
 * Common interface for the tiles storage.
 *
 * @author Danstahr
 */
public interface KachleManager {

    /**
     * Checks whether the requested tile exists in the storage.
     * @param ki
     *      The identifier of the tile.
     * @return
     *      True if it can be retrieved from the storage, false otherwise
     */
    public boolean exists(Ka0 ki);

    /**
     * Loads the requested tile from the storage.
     * @param ki
     *     The identifier of the tile.
     * @return
     *     The requested tile as Image or null if the tile couldn't be loaded or isn't present
     *     in the storage.
     */
    public Image load(Ka0 ki);

    /**
     * Saves a tile to the storage.
     * @param ki
     *     The identifier of the tile.
     * @param dss
     *     The image dump that's to be saved.
     * @return
     *     True if the save was successful, false otherwise.
     * @deprecated
     *     Use the batch version {@link #save(java.util.Collection)}
     */
    @Deprecated
    public boolean save(Ka0 ki, ImageSaver dss);

    /**
     * Save many tiles to the storage. Should be atomic ("all or nothing")
     * @param itemsToSave
     *     Collection of items (identifier of the tile -> image dump)
     * @return
     *     true if the saving of ALL items was successful, false otherwise
     */
    public boolean save(Collection<DiskSaveRequest> itemsToSave);
}
