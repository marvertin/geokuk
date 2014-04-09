package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.plugins.mapy.KachleUmisteniSouboru;

/**
 * A factory for Kachle Managers.
 */
public class KachleManagerFactory {

    /**
     * Get instance of Kachle Manager.
     * @return
     *      The appropriate KachleManager instance.
     */
    public static KachleManager getInstance() {
        return new KachleDBManager();
    }
}
