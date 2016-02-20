package cz.geokuk.plugins.mapy;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class WinterTuristPodkladAction extends PodkladAction0 {

    private static final long serialVersionUID = 43564813465663197L;

    public WinterTuristPodkladAction() {
        super("Turistická zimní");
        putValue(SHORT_DESCRIPTION, "Zimní turistická mapa.");
        putValue(MNEMONIC_KEY, KeyEvent.VK_M);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('w'));
    }

    @Override
    public EMapPodklad getPodklad() {
        return EMapPodklad.TURIST_WINTER;
    }
}
