package cz.geokuk.plugins.mapy.kachle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.napoveda.NapovedaModelChangedEvent;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;
import cz.geokuk.util.pocitadla.PocitadloRoste;

public abstract class JKachlovnik extends JSingleSlide0 implements AfterEventReceiverRegistrationInit {

    private static final Logger log = LogManager.getLogger(JKachlovnik.class.getSimpleName());

    private static final long serialVersionUID = -6300199882447791157L;

    private static Pocitadlo pocitZustalychKachli = new PocitadloRoste("Počet zůstalých kachlí",
            "Počet kachlí jKachle, které jako kompoenty zůstaly a jen se změnila její lokace, protože po reinicializaci " +
                    "byly na svém místě (obvykle posun) a nebylo je tudíž nutné znovu vytvářet");

    private static Pocitadlo pocitReinicializaceKachlovniku = new PocitadloRoste("Kolikrát bylo nuceno reinicializovat " +
            "celý kachlovník",
            "Říká, kolikrát byla zavolána metoda init pro reinicializaci celého kachlovníku v důsledku posunu, změnu " +
                    "velikosti, zůůmování atd.");

    private static Pocitadlo pocitVynuceneNepouzitiExistujiciKachle = new PocitadloRoste("Vynucené použití neexistující " +
            "kachle", "Kolikrát do JKachlovnik.ini() přišlo z venku, že JKachle komponenty nesmím použít");

    private final Pocitadlo pocitKachliVKachlovniku1 = new PocitadloMalo("Počet kachlí v kachlovníku 1", "");
    private final Pocitadlo pocitKachliVKachlovniku2 = new PocitadloMalo("Počet kachlí v kachlovníku 2", "");

    private Map<KaLoc, Kachle> kachles = new HashMap<>(200);


    private KaSet kachloTypesSet = new KaSet(EnumSet.noneOf(EKaType.class));

    private KachleModel kachleModel;


    // FIXME musí to být private a musí být na tru nastaveno jen při rendrování
    protected boolean vykreslovatokamzite;


    // je to jen kvuli garbage collectoru, aby nezrusil, NERUSIT PROMENNU i kdyz zdanlive je to na nic
    public JKachlovnik() {
        setLayout(null);
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.GREEN);
        setOpaque(false);
    }


    @Override
    public void onVyrezChanged() {
        init(true, Priority.KACHLE);
    }

    private void registerEvents() {
    }


    public void onEvent(ZmenaMapNastalaEvent event) {
        setKachloTypes(event.getKaSet());
    }

    protected void init(boolean smimZnovuPouzitKachle, Priority priorita) {
        if (!isSoordInitialized()) return;
        if (getWidth() == 0 || getHeight() == 0) return; // nemá smysl rendrovat prázdný kachlovník
        Coord soord = getSoord();
        if (soord == null) return;
        assert kachleModel != null;
        if (kachloTypesSet == null) return;
        pocitReinicializaceKachlovniku.inc();
        if (!smimZnovuPouzitKachle) {
            pocitVynuceneNepouzitiExistujiciKachle.inc();
        }

        Kaputer kaputer = new Kaputer(soord);
        Map<KaLoc, Kachle> newKachles = new HashMap<>(200);
        if (log.isTraceEnabled()) {
          log.trace("Vykreslovani kachli od {} pro {} -- {}", kaputer.getKachlePoint(0, 0), kaputer.getKachleMou(0, 0), kaputer);
        }
        int indexComponenty = 0;
        for (int yi = 0; yi < kaputer.getPocetKachliY(); yi++) {
            log.trace(" .... řádek", soord);
            for (int xi = 0; xi < kaputer.getPocetKachliX(); xi++) {
                KaLoc lokace = kaputer.getKaloc(xi, yi);
                Kachle kachle = kachles.remove(lokace);
                boolean kachleSePouzije = smimZnovuPouzitKachle && kachle != null;
                final JKachle jkachle;
                if (indexComponenty >= getComponentCount()) {
                  jkachle = createJKachle(); 
                  add(jkachle);          // a přidat jako komponentu
                } else {
                  jkachle = (JKachle) getComponent(indexComponenty);
                }
                indexComponenty ++;
                if (!kachleSePouzije) {
                    kachle = createKachle(new KaAll(lokace, kachloTypesSet), kachleModel, vykreslovatokamzite, this, jkachle); // když se nepoužije, musí se stvořit nová
                    kachle.setVzdalenostOdStredu(kaputer.getVzdalenostKachleOdStredu(lokace.getMouSZ()));
                    kachle.ziskejObsah(priorita);
                } else {  // použije se původní kachle
                    pocitZustalychKachli.inc();
                }
                jkachle.setKachle(kachle);
                // napozicujeme každou kachli do správné podoby
                Point p = kaputer.getKachlePoint(xi,  yi);
                log.trace("....... vykresulji kachli {} na {}", lokace, p);
                //System.out.println("KACHLE xx: " + new Point(x,y) + " ********* " + getSize());
                jkachle.setLocation(p.x, p.y);
                kachle.setPoziceJenProVypsani(lokace);
                newKachles.put(lokace, kachle);  // děláme vždy novou mapu kachlí i když je znovu používáme
            }
        }
        //System.out.println("mame komponent: " + super.getComponentCount());
        //System.out.println("Nahrazenych kachli: " + kachles.size());
        for (Kachle kachle : kachles.values()) {
            kachle.uzTeNepotrebuju();
        }
        while (indexComponenty < getComponentCount()) {
          remove(indexComponenty); // zbytek vymazat
        }
        kachles = newKachles; // a nově získané kachle sem schovat
        pocitKachliVKachlovniku1.set(kachles.size());
        pocitKachliVKachlovniku2.set(getComponentCount());
        log.trace("Počet komponent (nejspíš kachlí) v kachlovníku: {}", getComponentCount());
    }

    protected Kachle createKachle(KaAll plny, KachleModel kachleModel, boolean vykreslovatOkamzite, JKachlovnik jKachlovnik, JKachle jkachle) {
      return new Kachle(plny, kachleModel, vykreslovatOkamzite, jkachle);
    }

    protected JKachle createJKachle() {
      return new JKachle(this);
    }



    /**
     * @return the kachloTypes
     */
    public KaSet getKachloTypes() {
        return kachloTypesSet;
    }

    public void onEvent(NapovedaModelChangedEvent event) {
        if (event.getModel().isOnlineMode()) {
            init(false, Priority.KACHLE);
        }
    }

    /**
     * @param aKachloTypes the kachloTypes to set
     */
    public void setKachloTypes(KaSet aKachloSet) {
        if (kachloTypesSet.equals(aKachloSet)) return;
        kachloTypesSet = aKachloSet;
        init(false, Priority.KACHLE);
    }

    public void inject(KachleModel kachleModel) {
        this.kachleModel = kachleModel;
    }

    /* (non-Javadoc)
     * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
     */
    @Override
    public void initAfterEventReceiverRegistration() {
        registerEvents();
    }

    void kachleZpracovana(JKachle jKachle) {
    }


    //  @Override
    //  public void finalize() {
    //    System.out.println("Kachlovník finalizován");
    //  }
}
