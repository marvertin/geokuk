package cz.geokuk.plugins.mapy.kachle;

import java.awt.*;

/**
 * Created by dan on 7.4.14.
 */
public interface KachleManager {
    public boolean exists(Ka0 ki);

    public Image load(Ka0 ki);

    public boolean save(Ka0 ki, ImageSaver dss);
}
