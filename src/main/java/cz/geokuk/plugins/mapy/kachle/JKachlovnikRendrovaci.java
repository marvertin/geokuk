package cz.geokuk.plugins.mapy.kachle;

import java.awt.Component;
import java.awt.Graphics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JKachlovnikRendrovaci extends JKachlovnik {

    private static final Logger log = LogManager.getLogger(JKachlovnikRendrovaci.class.getSimpleName());

    private static final long serialVersionUID = -3170605712662727739L;
    private Progressor progressor;
    private int citacZpracovanychKachli;
    private int celkovyPocetKachliKtereRendruejeme;


    /* (non-Javadoc)
     * @see cz.geokuk.core.coord.JSingleSlide0#render(java.awt.Graphics)
     */
    @Override
    public void render(Graphics g) throws InterruptedException {
        try {
            celkovyPocetKachliKtereRendruejeme = 0;
            citacZpracovanychKachli = 0;
            paintComponent(g);
            //if (true) return;
            vykreslovatokamzite = true;
            init(false, Priority.STAHOVANI);
            paint(g);
            Component[] components = getComponents();
            celkovyPocetKachliKtereRendruejeme = components.length;
            for (Component component : components) {
                if (component instanceof JKachle) {
                    JKachle kachle = (JKachle) component;
                    kachle.getKachle().waitNaDotazeniDlazdice();
                }
            }
        } finally {
            // KDyž končíme, třeba i výjimkou, rychle kachlím řekneme, že je nepotřebujeme
            // a ona se v mžiku vyprázdní front
            for (Component component : getComponents()) {
                if (component instanceof JKachle) {
                    JKachle kachle = (JKachle) component;
                    kachle.getKachle().uzTeNepotrebuju();
                }
            }
            log.trace("Opoustim cekani");
        }
        //paint(g);
    }

    public void setProgressor(Progressor progressor) {
        this.progressor = progressor;
    }

    public interface Progressor {
        void setProgress(int value, int maxlue);
    }

    @Override
    void kachleZpracovana(JKachle jKachle) {
        if (progressor != null) progressor.setProgress(++citacZpracovanychKachli, celkovyPocetKachliKtereRendruejeme);
    }

    @Override
    protected Kachle createKachle(KaAll plny, KachleModel kachleModel, boolean vykreslovatOkamzite, JKachlovnik jKachlovnik, JKachle jkachle) {
      return new KachleRendrovaci(plny, kachleModel, vykreslovatOkamzite, jkachle);
    }

    @Override
    protected JKachle createJKachle() {
      return new JKachleRendrovaci(this);
    }    
//    @Override
//    protected JKachle createKachle(KaAll plny, KachleModel kachleModel, boolean vykreslovatOkamzite, JKachlovnik jKachlovnik) {
//      JKachleRendrovaci jkachle = new JKachleRendrovaci(jKachlovnik);
//      jkachle.setKachle(new KachleRendrovaci(plny, kachleModel, vykreslovatOkamzite, jkachle));
//      return jkachle;
//    }

}
