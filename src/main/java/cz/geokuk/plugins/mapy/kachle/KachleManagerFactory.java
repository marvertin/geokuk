package cz.geokuk.plugins.mapy.kachle;

/**
 * A factory for Kachle Managers.
 */
public class KachleManagerFactory {

    /**
     * Get instance of Kachle Manager.
     * @return
     *      The appropriate KachleManager instance.
     */
    public static KachleManager getInstance(KachleCacheFolderHolder folderHolder) {
        return new KachleDBManager(folderHolder);
    }
}
