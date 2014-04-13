package cz.geokuk.plugins.mapy;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Created by dan on 13.4.14.
 */
public class WinterTuristPodkladAction extends PodkladAction0 {
    private static final long serialVersionUID = -262970268937158619L;

    public WinterTuristPodkladAction() {
        super("Turistická zimní");
        putValue(SHORT_DESCRIPTION, "Zimní turistická mapa.");
        putValue(MNEMONIC_KEY, KeyEvent.VK_W);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('w'));
    }

    @Override
    public EMapPodklad getPodklad() {
        return EMapPodklad.TURIST_WINTER;
    }
}
