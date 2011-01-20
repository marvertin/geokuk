package cz.geokuk.plugins.kesoid;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.core.coord.EJakOtacetPriRendrovani;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.MouRect;
import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.Factory;
import cz.geokuk.plugins.kesoid.mapicon.Alela;
import cz.geokuk.plugins.kesoid.mapicon.EAplikaceSkla;
import cz.geokuk.plugins.kesoid.mapicon.FenotypPreferencesChangedEvent;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;
import cz.geokuk.plugins.kesoid.mapicon.SkloAplikant;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.JKesPopup;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.plugins.vylety.VyletChangeEvent;
import cz.geokuk.plugins.vylety.VyletModel;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.FlatVisitor;
import cz.geokuk.util.index2d.Indexator;
import cz.geokuk.util.index2d.Sheet;
import cz.geokuk.util.pocitadla.PocitadloNula;
import cz.geokuk.util.process.BrowserOpener;


public class JKesoidy extends JSingleSlide0 implements MouseInputListener, AfterEventReceiverRegistrationInit {

  //  static int POLOMER_KESE = 15; // je to v pixlech
  private static final int POLOMER_CITLIVOSTI = 10;

  private final BlockingQueue<WptPaintRequest> frontaWaypointu = new LinkedBlockingQueue<WptPaintRequest>();
  private final PocitadloNula pocitVelikostFrontyWaypointu = new PocitadloNula("Velikost vykreslovací waypointové fronty",
  "Kolik waypointů čeká na vykreslení.");

  private static final long serialVersionUID = -5858146658366237217L;


  private Indexator<Wpt> indexator;

  private VyletModel vyletModel;

  private Kesoid mysNadKesi;
  private Wpt mysNadWpt;

  private final JLabel jakoTooltip = new JLabel();

  //	private List<Rectangle> repaintovaneCtverce = new ArrayList<Rectangle>();
  //	private List<Rectangle> klipovanci = new ArrayList<Rectangle>();


  private IkonBag ikonBag;

  private Set<Alela> fenotypoveZakazaneAlely;

  private Set<String> iJmenaAlel;

  private Factory factory;

  private PoziceModel poziceModel;

  private KesoidModel kesoidModel;

  private RendrovaciPesek rendrovaciPesek;

  //  private VyrezModel vyrezModel;

  public JKesoidy() {
    setLayout(null);
    setOpaque(false);

    jakoTooltip.setVisible(false);
    add(jakoTooltip);

    repaint();

    new PaintovaciVlakno(this).start();
  }


  /**
   * 
   */
  private void registerEvents() {
  }


  public void onEvent(KeskyVyfiltrovanyEvent aEvent) {
    indexator = aEvent.getFiltrovane().getIndexator();
    repaint();
  }

  public void onEvent(VyletChangeEvent aEvent) {
    if (aEvent.isVelkaZmena()) {
      Wpt.invalidateAllSklivec();
      repaint();
    } else {
      repaintWpt(aEvent.getKes().getMainWpt());
    }
  }

  public void onEvent(KesoidOnoffEvent event) {
    setVisible(event.isOnoff());
  }

  public void onEvent(IkonyNactenyEvent aEvent) {
    ikonBag = aEvent.getBag();
    repaintIfVse();
  }

  public void onEvent(FenotypPreferencesChangedEvent aEvent) {
    iJmenaAlel = aEvent.getJmenaNefenotypovanychAlel();
    repaintIfVse();
  }

  private void repaintIfVse() {
    if (iJmenaAlel == null) return;
    if (ikonBag == null) return;
    fenotypoveZakazaneAlely = ikonBag.getGenom().namesToAlely(iJmenaAlel);
    Wpt.invalidateAllSklivec();
    repaint();
  }


  @Override
  protected void paintComponent(Graphics ag) {
    Graphics2D g = (Graphics2D) ag;

    if (indexator == null) return;
    if (ikonBag == null) return;
    // Nevykresluju. kdyz je prekrocen limit, ale jen kdyz kreslim na obrazovku
    final boolean prekrocenLimit = rendrovaciPesek == null && indexator.count(getSoord().getBoundingRect()) > FConst.MAX_POC_WPT_NA_MAPE;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        kesoidModel.setPrekrocenLimitWaypointuVeVyrezu(prekrocenLimit);
      }
    });

    // vytvoření prázdných seznamů
    final EnumMap<Wpt.EZOrder, List<Sheet<Wpt>>> mapa = new EnumMap<Wpt.EZOrder, List<Sheet<Wpt>>>(Wpt.EZOrder.class);
    for (Wpt.EZOrder zorder : Wpt.EZOrder.values()) {
      mapa.put(zorder, new ArrayList<Sheet<Wpt>>(10000));
    }

    // Roztřídit waypointy podle pořadí vykreslování
    if (! prekrocenLimit) {
      BoundingRect hranice = coVykreslovat(g);
      indexator.visit(hranice, new FlatVisitor<Wpt>() {
        @Override
        public void visit(Sheet<Wpt> aSheet) {
          Wpt wpt = aSheet.get();
          mapa.get(wpt.getZorder()).add(aSheet);
        }
      });
    }

    List<SkloAplikant> skloAplikanti = ikonBag.getSada().getSkloAplikanti();
    for (int i=0; i< skloAplikanti.size(); i++) {
      final SkloAplikant skloAplikant = skloAplikanti.get(i);
      if (skloAplikant.aplikaceSkla == EAplikaceSkla.VSE) {  // jen na skla, na kterych je vsechno
        if (! prekrocenLimit) {
          for (List<Sheet<Wpt>> list : mapa.values()) {
            for (Sheet<Wpt> swpt : list) {
              Wpt wpt = swpt.get();
              Mou mou = new Mou(swpt.getXx(), swpt.getYy());
              paintWaypoint(g, wpt, mou, i);
            }
          }
        }
      }
      // Vykreslení zvýrazněných
      if (mysNadKesi != null) {
        for (Wpt wpt : mysNadKesi.getWpts()) {
          paintWaypoint(g, wpt, null, i);
        }
      }
    }
  }

  /**
   * @param g
   * @param wpt
   * @param mou
   * @param i
   */
  private void paintWaypoint(Graphics2D g, Wpt wpt, Mou mou, int i) {
    if (mou == null) mou = wpt.getMou(); // z vykonnostnich duvodu se to vetsinou predava
    Sklivec sklivec = wpt.getSklivec();

    //sklivec = null; // pro ucely testovani vzdfy vykreslujeme

    if (sklivec == null) { // nemame sklivce
      if (rendrovaciPesek != null) rendrovaciPesek.necoSeNevyKreslilo();
      zaplanujNaplneniSklivce(wpt, mou);
      sklivec = wpt.getSklivec(); // kdyby se náhodou už zaplnilo
      if (sklivec == null) return;
    }
    // Waypointů se může zobraztoat hodně, tak raději nekonvertujeme z Wgs
    Point p = getSoord().transform(mou);

    Imagant imagant = sklivec.imaganti.get(i);
    if (imagant == null) return;  // neni co vykreslovat
    int x = p.x + imagant.getXpos();
    int y = p.y + imagant.getYpos();
    g.drawImage(imagant.getImage(), x, y, null);
  }

  private void zaplanujNaplneniSklivce(Wpt wpt, Mou mou) {

    frontaWaypointu.add(new WptPaintRequest(wpt, mou));
    pocitVelikostFrontyWaypointu.set(frontaWaypointu.size());
    //    spocitejSklivece(wpt);
  }

  private Genotyp computeGenotyp(Wpt wpt) {
    Genotyp g = wpt.getGenotyp(ikonBag.getGenom());
    switch (vyletModel.get(wpt.getKesoid())) {
    case ANO: g.put(ikonBag.getGenom().ALELA_lovime); break;
    case NE:  g.put(ikonBag.getGenom().ALELA_ignoru); break;
    }

    if (wpt == mysNadWpt) g.put(ikonBag.getGenom().ALELA_mouseon);
    if (wpt.getKesoid() == mysNadKesi) g.put(ikonBag.getGenom().ALELA_mousean);
    g.removeAll(fenotypoveZakazaneAlely);
    return g;
  }


  private BoundingRect coVykreslovat(Graphics g) {
    Insets bii = ikonBag.getSada().getBigiestIconInsets();
    Rectangle rect = g.getClipBounds();
    if (rect == null) rect = new Rectangle(getSize());
    //  	klipovanci.add(rect);
    // to right a bottom je v poradku, protoze zvetsujeme obdelnik,
    // abychom chytli vsechy kese, ktere k nam mohou zapadnout
    Rectangle rect2 = new Rectangle(rect.x - bii.right, rect.y - bii.bottom,
        rect.width +  bii.left + bii.right ,
        rect.height + bii.top + bii.bottom);
    BoundingRect br = preved(rect2);
    return br;
  }

  private BoundingRect preved(Rectangle rect) {
    Point p1 = new Point(rect.x, rect.y);
    Point p2 = new Point(rect.x + rect.width, rect.y + rect.height);
    Mou mou1 = getSoord().transform(p1);
    Mou mou2 = getSoord().transform(p2);
    // To je spravne, protoze souradnice jdou opacne
    BoundingRect br = new BoundingRect(mou1.xx, mou2.yy, mou2.xx, mou1.yy);
    return br;
  }




  private void repaintKes(Kesoid kes) {
    if (kes == null) return;
    for (Wpt wpt : kes.getWpts()) {
      repaintWpt(wpt);
    }
  }

  private void repaintWpt(Wpt wpt) {
    wpt.invalidate();

    //    Point p = coord.transform(wpt.getWgs().toMou());
    //    Rectangle rect1 = new Rectangle(p.x - POLOMER_KESE, p.y - POLOMER_KESE,
    //        POLOMER_KESE * 2, POLOMER_KESE * 2);

    frontaWaypointu.add(new WptPaintRequest(wpt, null));
    //    Repaintanger repaintanger = new Repaintanger();
    //    repaintanger.includeInsetsOnly(wpt);
    //    repaintanger.include(wpt.getWgs().toMou());
    //
    //    Rectangle rect2 = repaintanger.computeRectangle(coord);
    //    //    	new Rectangle(p.x - POLOMER_KESE, p.y - POLOMER_KESE,
    //    //        POLOMER_KESE * 2, POLOMER_KESE * 2);
    //    //	  repaintovaneCtverce .add(rect);
    //    //    System.out.println("REKTANGLE-1: " + rect1 + " -- " + p);
    //    //    System.out.println("REKTANGLE-2: " + rect2);
    //    repaint(rect2);
  }


  private Sheet<Wpt> najdiNejakyWpt(BoundingRect br) {
    if (indexator == null) return null;
    try {
      indexator.visit(br, new FlatVisitor<Wpt>() {
        @Override
        public void visit(Sheet<Wpt> sheet) {
          throw new XNalezeno(sheet);
        }
      });
      return null; // nevypadla výjimka, nebylo nalezeno
    } catch (XNalezeno e) { // bylo něco nalezeno
      return e.swpt;
    }

  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    if (mysNadWpt != null) {
      if (SwingUtilities.isLeftMouseButton(e)) {
        poziceModel.setPozice(mysNadWpt);
      }
      Kesoid kesoid = mysNadWpt.getKesoid();
      if (e.getClickCount() >= 3 && kesoid.getUrlPrint() != null) {
        BrowserOpener.displayURL(kesoid.getUrlPrint());
      } else if (e.getClickCount() >= 2 && kesoid.getUrlShow() != null) {
        BrowserOpener.displayURL(kesoid.getUrlShow());
      }
      e.consume();
    } else {
      chain().mouseClicked(e);
    }
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    boolean jepopup = maybeShowPopup(e);
    if (!jepopup) chain().mousePressed(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    boolean jepopup = maybeShowPopup(e);
    if (!jepopup) chain().mouseReleased(e);
  }


  /**
   * @param aE
   */
  private boolean maybeShowPopup(MouseEvent e) {
    if (e.isPopupTrigger() && mysNadWpt != null) {

      System.out.println("BYL BY POPUP " + mysNadWpt);
      JPopupMenu popup = sestavPopup(mysNadWpt);
      if (popup != null) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param aMysNadWpt
   * @return
   */
  private JPopupMenu sestavPopup(Wpt aMysNadWpt) {
    return factory.init(new JKesPopup(aMysNadWpt));
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    //System.out.println("POSUNOVACKA TO ASI BUDE: " + e);
    int polomerCitlivosi = POLOMER_CITLIVOSTI;
    Rectangle rect = new Rectangle(e.getX() - polomerCitlivosi, e.getY() - polomerCitlivosi,
        polomerCitlivosi * 2, polomerCitlivosi * 2);
    Sheet<Wpt> swpt = najdiNejakyWpt(preved(rect));
    Wpt wpt = swpt == null ? null : swpt.get();
    mysNadWpt = wpt;
    Kesoid kes = wpt == null ? null : wpt.getKesoid();

    if (kes != mysNadKesi) {
      Kesoid staraKes = mysNadKesi;
      mysNadKesi = kes;
      //repaint();
      repaintKes(staraKes);
      repaintKes(mysNadKesi);
    }

    if (mysNadWpt != null) {
      Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      setMouseCursor(cursor);
      //      setCursor(cursor);
      jakoTooltip.setText(mysNadWpt.textToolTipu());
      jakoTooltip.setOpaque(true);
      jakoTooltip.setBackground(Color.WHITE);
      jakoTooltip.setSize(jakoTooltip.getPreferredSize());

      Point p = getSoord().transform(mysNadWpt.getWgs().toMou());
      p.y -= jakoTooltip.getHeight();
      jakoTooltip.setLocation(p);
      jakoTooltip.setVisible(true);
      //System.out.println("TEXTIK ZDE: " + p);
    } else {
      setMouseCursor(null);
      //setCursor(null);
      jakoTooltip.setVisible(false);
      //System.out.println("TEXTIK PRYC: ");
    }
    //setToolTipText();

  }


  /**
   * Vrátí pozici nmyši, pokud je v blízkosti kříže
   * @return
   */
  @Override
  public Mou getUpravenaMys() {
    if (mysNadWpt != null) {
      return mysNadWpt.getWgs().toMou();
    } else {
      return chain().getUpravenaMys();
    }
  }

  private static class WptPaintRequest {

    private final Wpt wpt;
    private final Mou mou;

    public WptPaintRequest(Wpt wpt, Mou mou) {
      this.wpt = wpt;
      this.mou = mou == null ? wpt.getWgs().toMou() : mou;
    }

  }

  private static class PaintovaciVlakno extends Thread {

    private final WeakReference<JKesoidy> wrKesoidy;

    public PaintovaciVlakno(JKesoidy jKesoidy) {
      super ("Paintovani");
      final ReferenceQueue<JKesoidy> refqueue = new ReferenceQueue<JKesoidy>();
      wrKesoidy = new WeakReference<JKesoidy>(jKesoidy, refqueue);
      new Thread("Zabijec paintovaciho vlakna") {
        @Override
        public void run() {
          try {
            refqueue.remove();
          } catch (InterruptedException e) { // když přeruší, tak také přeruším
          }
          PaintovaciVlakno.this.interrupt();
        };
      }.start();
    }

    @Override
    public void run() {
      JKesoidy jKesoidy = wrKesoidy.get();
      if (jKesoidy == null) return; // slide kešoidů už je pryč, takže ani nemá cenu dál něco řešit
      for (;;) {
        try {
          BlockingQueue<WptPaintRequest> frontaWaypointu2 = jKesoidy.frontaWaypointu;
          jKesoidy = null; // přičekání na požadavek ve froně nesmím držet slide waypointů, aby mohl být garbage collectorem sebrán
          WptPaintRequest wpr = frontaWaypointu2.poll(10000, TimeUnit.MILLISECONDS); // aby umělo vlákno skončit, musí být timeout
          jKesoidy = wrKesoidy.get();
          if (jKesoidy == null) return; // slide kešoidů už je pryč, takže ani nemá cenu dál něco řešit
          if (wpr == null) continue;
          MouRect mouRect = new MouRect();
          while (wpr != null) {
            if (wpr.wpt.getSklivec() == null) {
              spocitejSklivece(wpr.wpt, jKesoidy);
            }
            mouRect.add(wpr.mou);
            wpr = jKesoidy.frontaWaypointu.poll();
            jKesoidy.pocitVelikostFrontyWaypointu.set(jKesoidy.frontaWaypointu.size());
          }
          vyvolejVykresleni(mouRect, jKesoidy);
          if (jKesoidy.rendrovaciPesek != null) jKesoidy.rendrovaciPesek.vykreslovaciFrontaBylaVyprazdnena();
        } catch (InterruptedException e) {
          //          System.out.println("Paintovaci vlakno konci diky intrerupci.");
          return;
        }
      }
    }

    /**
     * Asynchronně volaná metoda provýpočet sklivce
     * @param wpt
     */
    private void spocitejSklivece(Wpt wpt, final JKesoidy jKesoidy) {
      Sklivec sklivec = jKesoidy.ikonBag.getSada().getSklivec(jKesoidy.computeGenotyp(wpt));
      wpt.setSklivec(sklivec);
    }


    private void vyvolejVykresleni(final MouRect aMouRect, final JKesoidy jKesoidy) {
      if (aMouRect.isEmpty()) return;
      SwingUtilities.invokeLater(
          new Runnable() {
            @Override
            public void run() {
              Insets bigiestIconInsets = jKesoidy.ikonBag.getSada().getBigiestIconInsets();
              Rectangle rect = jKesoidy.getSoord().transform(aMouRect, bigiestIconInsets);
              jKesoidy.repaint(rect);
            }
          });
    }

    //    @Override
    //    public void finalize() {
    //      System.out.println("Paintovací vlákno fanalizováno");
    //    }
  }

  @Override
  public void inject(Factory factory) {
    this.factory = factory;
  }
  public void inject(PoziceModel poziceModel) {
    this.poziceModel = poziceModel;
  }
  public void inject(VyletModel vyletModel) {
    this.vyletModel = vyletModel;
  }
  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }
  //  public void inject(VyrezModel vyrezModel) {
  //    this.vyrezModel = vyrezModel;
  //  }


  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    registerEvents();
  }

  /* (non-Javadoc)
   * @see cz.geokuk.core.coord.JSingleSlide0#render(java.awt.Graphics)
   */
  @Override
  public void render(Graphics g) {
    try {
      do {
        rendrovaciPesek = new RendrovaciPesek();
        paintComponent(g);
        rendrovaciPesek.cekejNaKonec();
      } while (! rendrovaciPesek.mameVseVykresleno());
    } finally {
      rendrovaciPesek = null;
    }

  }

  private  class RendrovaciPesek {
    private boolean necoSeNevykreslilo;
    private boolean vykreslovaciFrontaBylaVyprazdnena;

    synchronized void necoSeNevyKreslilo() {
      necoSeNevykreslilo = true;
      vykreslovaciFrontaBylaVyprazdnena = false;
    }

    synchronized void vykreslovaciFrontaBylaVyprazdnena() {
      vykreslovaciFrontaBylaVyprazdnena = true;
      notify(); // a upozornit, že by se mohlo jet dál
    }

    synchronized void cekejNaKonec() {
      if (! necoSeNevykreslilo) return; // pokud už nemám nevykreslence, není nač čekat
      while (! vykreslovaciFrontaBylaVyprazdnena) { // ček, dokud se frontan nevykreslí
        // dokud se nevyprazdni fronta, musime cekat
        try {
          wait();
        } catch (InterruptedException e) { // no tak nic
        }
      }
    }

    synchronized boolean mameVseVykresleno() {
      return ! necoSeNevykreslilo;
    }

  }

  /* (non-Javadoc)
   * @see cz.geokuk.core.coord.JSingleSlide0#createRenderableSlide()
   */
  @Override
  public JSingleSlide0 createRenderableSlide() {
    return new JKesoidy();
  }

  @Override
  public EJakOtacetPriRendrovani jakOtacetProRendrovani() {
    return EJakOtacetPriRendrovani.COORD;
  }

  //  @Override
  //  public void finalize() {
  //    System.out.println("Kesoidy finalizovány");
  //  }

}
