/**
 *
 */
package cz.geokuk.core.coord;

import java.awt.Dimension;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.MouRect;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.EKaType;
import cz.geokuk.util.lang.FMath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author veverka
 */
public class VyrezModel extends Model0 {

    private static final Logger log = LogManager.getLogger(VyrezModel.class.getSimpleName());

    public static final Wgs DEFAULTNI_DOMACI_SOURADNICE = new Wgs(49.8, 15.5);

    //private EventFirer ef;

    private PoziceModel poziceModel;

    private EKaType podkladMap;

    private Coord moord = Coord.prozatimniInicializacni();

    /**
     * @return the moord
     */
    public Coord getMoord() {
        return moord;
    }

    public void vystredovatNaPozici() {
        Mou mou = poziceModel.getPoziceq().getPoziceMou();
        if (mou != null) {
            presunMapuNaMoustred(mou);
        }
    }

    public boolean isPoziceUprostred() {
        Mou mouPozice = poziceModel.getPoziceq().getPoziceMou();
        Mou mouStred = moord.getMoustred();
        boolean b = mouPozice.equals(mouStred);

        log.debug(mouPozice);
        log.debug(mouStred);
        return b;
    }

    public void zoomTo(MouRect mourect) {
        Mou moustred = mourect.getStred();
        poziceModel.setPozice(moustred.toWgs());
        vystredovatNaPozici();
        //coord.setMoustred(moustred);
        //setMoucur(moustred);
        // odspodu hledáme měřítko, které tam vleze
        int mer = 20;
        for (; ; mer--) {
            int pom = 1 << (20 - mer); // pomer pro toto meritko
            if (mourect.getMouWidth() / pom <= moord.getDim().getWidth()
                    && mourect.getMouHeight() / pom <= moord.getDim().getHeight()) {
                break; // hledáme nejbližší nejlepší
            }
        }
        setMeritkoMapyAutomaticky(mer);
    }


    public void setMeritkoMapy(int moumer) {
        setMoumer(nastavitelneMeritkoZChteneho(moumer, false));
    }

    public void zoomByGivenPoint(int moumer, Mou zoomMouStred) {
        int skutecnyMoumer = nastavitelneMeritkoZChteneho(moumer, false);
        setMoord(moord.derive(skutecnyMoumer, moord.computeZoom(skutecnyMoumer, zoomMouStred)));
    }

    /**
     * Jen nedovolí turistické mapě úplné přiblížení
     *
     * @param moumer
     */
    public void setMeritkoMapyAutomaticky(int moumer) {
        setMoumer(nastavitelneMeritkoZChteneho(moumer, true));
    }

    public int omezMeritko(int moumer) {
        return nastavitelneMeritkoZChteneho(moumer, false);
    }

    private int nastavitelneMeritkoZChteneho(int moumer, boolean autoMeritko) {
        moumer = FMath.fit(moumer, podkladMap.getMinMoumer(),
                autoMeritko ? podkladMap.getMaxAutoMoumer() : podkladMap.getMaxMoumer());
        return moumer;
    }

    public int nejblizsiMeritko() {
        return podkladMap.getMaxMoumer();
    }

    public int nejvzdalenejsiMeritko() {
        return podkladMap.getMinMoumer();
    }

    public boolean jeNejblizsiMeritko() {
        return nastavitelneMeritkoZChteneho(moord.getMoumer() + 1, false) == moord.getMoumer();
    }

    public boolean jeNejvzdaLenejsiMeritko() {
        return nastavitelneMeritkoZChteneho(moord.getMoumer() - 1, false) == moord.getMoumer();
    }


    public void inject(PoziceModel poziceModel) {
        this.poziceModel = poziceModel;
    }


    /* (non-Javadoc)
     * @see cz.geokuk.program.Model0#initAndFire()
     */
    @Override
    protected void initAndFire() {
        int moumer = currPrefe().node(FPref.UVODNI_SOURADNICE_node).getInt(FPref.MOUMER_value, 6);
        Mou moustred = currPrefe().node(FPref.UVODNI_SOURADNICE_node).getMou(FPref.MOUSTRED_value, DEFAULTNI_DOMACI_SOURADNICE.toMou());
        setMoord(moord.derive(moumer, moustred));
    }

    public void setMoumer(int moumer) {
        setMoord(moord.derive(moumer));
    }

    public void presunMapuNaMoustred(Mou moustred) {
        setMoord(moord.derive(moustred));
    }


    public void onEvent(ZmenaMapNastalaEvent event) {
        podkladMap = event.getKaSet().getPodklad();
        setMoumer(FMath.fit(moord.getMoumer(), podkladMap.getMinMoumer(), podkladMap.getMaxMoumer()));
    }

    public void setVelikost(int width, int height) {
        setMoord(moord.derive(new Dimension(width, height)));
    }

    public void setMoord(Coord moord) {
        if (moord.equals(this.moord)) return;
        this.moord = moord;
        currPrefe().node(FPref.UVODNI_SOURADNICE_node).putInt(FPref.MOUMER_value, moord.getMoumer());
        currPrefe().node(FPref.UVODNI_SOURADNICE_node).putMou(FPref.MOUSTRED_value, moord.getMoustred());

        fire(new VyrezChangedEvent(moord));
    }
}
