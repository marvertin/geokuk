package cz.geokuk.plugins.mapy;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class GeographyPodkladAction extends PodkladAction0 {

    private static final long serialVersionUID = 43564813465663197L;

    public GeographyPodkladAction() {
        super("Zeměpisná");
        putValue(SHORT_DESCRIPTION, "Zeměpisná mapa");
        putValue(MNEMONIC_KEY, KeyEvent.VK_G);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('g'));
    }

    @Override
    public EMapPodklad getPodklad() {
        return EMapPodklad.ZEMEPIS;
    }
}
