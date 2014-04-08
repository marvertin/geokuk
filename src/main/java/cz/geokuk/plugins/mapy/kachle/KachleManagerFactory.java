package cz.geokuk.plugins.mapy.kachle;

/**
 * Created by dan on 7.4.14.
 */
public class KachleManagerFactory {
    public static KachleManager getInstance() {
        return new KachleDBManager();
    }
}
