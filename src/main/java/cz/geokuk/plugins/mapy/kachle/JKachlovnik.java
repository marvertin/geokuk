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
import cz.geokuk.core.coordinates.Mou;
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

        //System.out.println(System.identityHashCode(this) + " kachlovnik se inicializuje: " + getWidth() + " " + getHeight() );
        int xn = getWidth();
        int yn = getHeight();

        // kraj krajove kachle vlevo dole, ktera se jeste musi zobrazit
        //int moukrok = soord.getMoukrok();
        //int moumaska = ~(moukrok - 1);
        //Mou mou0 = new Mou(soord.getMoupoc().xx & moumaska, soord.getMoupoc().yy & moumaska);

        //System.out.println(xn + " --- " + yn);

        Mou moustred = soord.getMoustred();
        int moumer = soord.getMoumer();
        int moukrok = soord.getMoukrok(); // o kolik mou je to od kachle ke kachli (pro moumer=0 je to 2^32, tedy v integeru 0, což odpovídá, že se stále zobrazuje stejná kachle)
        int maskaHorni =  ~ (moukrok - 1);
        int xx0 = moustred.xx &  maskaHorni; // posuneme od středu nalevo na nejbližší hranici kachlí
        int yy0 = moustred.yy &  maskaHorni; // posuneme od středu dolů na nejbližší hranici kachlí
        int xd = (moustred.xx >> (KaLoc.MAX_MOUMER - moumer))  & KaLoc.KACHLE_MASKA; // o tolik pixlů nalevo od středu bude svislá hranice kachlí
        int yd = (moustred.yy >> (KaLoc.MAX_MOUMER - moumer))  & KaLoc.KACHLE_MASKA; // o tolik pixklů dolů od středu bude vodorovná hranice kachlí
        assert xd >=0 && xd < KaLoc.KACHLE_PIXELS;
        assert yd >=0 && yd < KaLoc.KACHLE_PIXELS;
        log.trace("moukrok ={} moumer={}  - [{},{}] maskaHorni={}", Integer.toHexString(moukrok), moumer, Integer.toHexString(xx0), Integer.toHexString(yy0), Integer.toHexString(maskaHorni));
        int x0, y0;
        for (x0 = xn / 2 - xd; x0 > 0; x0 -= KaLoc.KACHLE_PIXELS, xx0-=moukrok); // nastavit x0 i xx zleva před kreslenou plochu (- je zde, protože nalevo od středu)
        for (y0 = yn / 2 + yd; y0 > 0; y0 -= KaLoc.KACHLE_PIXELS, yy0+=moukrok); // nastavit x0 i yy shora před kreslenou plochu (+ je zde, protože dolů od středu, druhé plus, ptotož mouy jde sdola nahoru)
        assert x0 <=0 && y0 <= 0;
        // nyní máme [x0,y0] a [xx0, yy0] souřadnice styku čtyř kachlí. S tím, že kachle, která od tohoto bodu jde
        //  dolů (jižně) a vpravo (východně) zasáhne nejméně jedním pixlem do levého horního roku okna.
        // a teď jedeme
        Map<KaLoc, Kachle> newKachles = new HashMap<>(200);

        log.trace("Vykreslovani kachli od [{},{}] pro mou[{},{}] {}", x0, y0, Integer.toHexString(xx0), Integer.toHexString(yy0), soord);
        final Point p = new Point();
        int xx, yy;
        int indexComponenty = 0;
        for (yy = yy0, p.y = y0; p.y < yn; yy-=moukrok, p.y += KaLoc.KACHLE_PIXELS) {
            log.trace(" .... řádek", soord);
            for (xx = xx0, p.x = x0; p.x < xn; xx+=moukrok, p.x += KaLoc.KACHLE_PIXELS) {
                Mou mou = new Mou(xx, yy); // toto jsou vždy souřadnice levého horního rohu kachle, "p" jsou souřadnice levého horního rohu v okně
                KaLoc lokace = KaLoc.ofSZ (new Mou(mou), soord.getMoumer());
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
                    kachle.setVzdalenostOdStredu(soord.getVzdalenostKachleOdStredu(lokace.getMouSZ()));
                    kachle.ziskejObsah(priorita);
                } else {  // použije se původní kachle
                    pocitZustalychKachli.inc();
                }
                jkachle.setKachle(kachle);
                // napozicujeme každou kachli do správné podoby
                log.trace("....... vykresulji kachli {} na {}", lokace, p);
                //System.out.println("KACHLE xx: " + new Point(x,y) + " ********* " + getSize());
                jkachle.setLocation(p.x, p.y);
                kachle.setPoziceJenProVypsani(lokace);
                newKachles.put(lokace, kachle);  // děláme vždy novou mapu kachlí i když je znovu používáme
                mou = mou.add(0, moukrok);
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

//    protected JKachle createKachle(KaAll plny, KachleModel kachleModel, boolean vykreslovatOkamzite, JKachlovnik jKachlovnik) {
//        JKachle jkachle = new JKachle(jKachlovnik);
//        jkachle.setKachle(new Kachle(plny, kachleModel, vykreslovatOkamzite, jkachle));
//        return jkachle;
//    }

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
