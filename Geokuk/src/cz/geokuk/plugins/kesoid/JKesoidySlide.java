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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.core.coord.EJakOtacetPriRendrovani;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.PoziceSeMaMenitEvent;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.MouRect;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.FKurzory;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.MouseGestureContext;
import cz.geokuk.plugins.cesty.CestyModel;
import cz.geokuk.plugins.cesty.akce.OdebratZCestyAction;
import cz.geokuk.plugins.cesty.akce.PridatDoCestyAction;
import cz.geokuk.plugins.kesoid.mapicon.Alela;
import cz.geokuk.plugins.kesoid.mapicon.EAplikaceSkla;
import cz.geokuk.plugins.kesoid.mapicon.FenotypPreferencesChangedEvent;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;
import cz.geokuk.plugins.kesoid.mapicon.SkloAplikant;
import cz.geokuk.plugins.kesoid.mvc.CenterWaypointAction;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.plugins.kesoid.mvc.TiskniNaGcComAction;
import cz.geokuk.plugins.kesoid.mvc.UrlToClipboardForGeogetAction;
import cz.geokuk.plugins.kesoid.mvc.UrlToListingForGeogetAction;
import cz.geokuk.plugins.kesoid.mvc.ZhasniKeseUrciteAlelyAction;
import cz.geokuk.plugins.kesoid.mvc.ZobrazNaGcComAction;
import cz.geokuk.plugins.kesoid.mvc.ZoomKesAction;
import cz.geokuk.plugins.vylety.VyletAnoAction;
import cz.geokuk.plugins.vylety.VyletChangeEvent;
import cz.geokuk.plugins.vylety.VyletModel;
import cz.geokuk.plugins.vylety.VyletNeAction;
import cz.geokuk.plugins.vylety.VyletNevimAction;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.FlatVisitor;
import cz.geokuk.util.index2d.Indexator;
import cz.geokuk.util.index2d.Sheet;
import cz.geokuk.util.pocitadla.PocitadloNula;
import cz.geokuk.util.process.BrowserOpener;


public class JKesoidySlide extends JSingleSlide0 implements AfterEventReceiverRegistrationInit {

  //  static int POLOMER_KESE = 15; // je to v pixlech
  private static final int POLOMER_CITLIVOSTI = 10;

  private final BlockingQueue<WptPaintRequest> frontaWaypointu = new LinkedBlockingQueue<WptPaintRequest>();
  private final PocitadloNula pocitVelikostFrontyWaypointu = new PocitadloNula("Velikost vykreslovací waypointové fronty",
      "Kolik waypointů čeká na vykreslení.");

  private static final long serialVersionUID = -5858146658366237217L;


  private Indexator<Wpt> indexator;

  private CestyModel cestyModel;

  private Kesoid kesoidPodMysi;
  private Wpt wptPodMysi;

  private final JLabel jakoTooltip = new JLabel();

  //	private List<Rectangle> repaintovaneCtverce = new ArrayList<Rectangle>();
  //	private List<Rectangle> klipovanci = new ArrayList<Rectangle>();


  private IkonBag ikonBag;

  private Set<Alela> fenotypoveZakazaneAlely;

  private Set<String> iJmenaAlel;

  private Factory factory;

  private PoziceModel poziceModel;

  private KesoidModel kesoidModel;

  private final boolean vykreslovatOkamtiteAleDlouho;

  private final double scale = 1;

  private KesBag vsechny;

  private VyletModel vyletModel;


  public JKesoidySlide(boolean vykreslovatOkamtiteAleDlouho) {
    this.vykreslovatOkamtiteAleDlouho = vykreslovatOkamtiteAleDlouho;
    setLayout(null);
    setOpaque(false);

    jakoTooltip.setVisible(false);
    add(jakoTooltip);

    repaint();

    if (!vykreslovatOkamtiteAleDlouho) {
      new PaintovaciVlakno(this).start();
    }
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

  public void onEvent(KeskyNactenyEvent event) {
    vsechny = event.getVsechny();
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
    Graphics2D gg = (Graphics2D) ag;

    if (indexator == null) return;
    if (ikonBag == null) return;
    // Nevykresluju. kdyz je prekrocen limit, ale jen kdyz kreslim na obrazovku
    final boolean prekrocenLimit = ! vykreslovatOkamtiteAleDlouho && indexator.count(getSoord().getBoundingRect()) > FConst.MAX_POC_WPT_NA_MAPE;
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
      BoundingRect hranice = coVykreslovat(gg);
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
              paintWaypoint(gg, wpt, mou, i);
            }
          }
        }
      }
      // Vykreslení zvýrazněných
      if (kesoidPodMysi != null) {
        for (Wpt wpt : kesoidPodMysi.getWpts()) {
          paintWaypoint(gg, wpt, null, i);
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
    if (mou == null)
    {
      mou = wpt.getMou(); // z vykonnostnich duvodu se to vetsinou predava
    }
    Sklivec sklivec = wpt.getSklivec();

    //sklivec = null; // pro ucely testovani vzdfy vykreslujeme

    if (sklivec == null) { // nemame sklivce
      if (vykreslovatOkamtiteAleDlouho) {
        computeSklivec(wpt);
      } else {
        zaplanujNaplneniSklivce(wpt, mou);
      }
      sklivec = wpt.getSklivec(); // kdyby se náhodou už zaplnilo
      if (sklivec == null) return;
    }
    // Waypointů se může zobraztoat hodně, tak raději nekonvertujeme z Wgs
    Point p = getSoord().transform(mou);

    Imagant imagant = sklivec.imaganti.get(i);
    if (imagant == null) return;  // neni co vykreslovat
    //    if (vykreslovatOkamtiteAleDlouho) {
    //      System.out.println("Malujem kesika " + x + " " + y + " --- " +imagant.getImage().getWidth() + " " + imagant.getImage().getHeight());
    //    }

    // JE to zde proto, aby se zabyly warningy.
    double iscale = scale;
    if (iscale == 1) {
      int x = p.x + imagant.getXpos();
      int y = p.y + imagant.getYpos();
      g.drawImage(imagant.getImage(), x, y, null);
    } else {
      g = (Graphics2D) g.create();
      g.translate(p.x, p.y);
      g.scale(scale, scale);
      g.translate(imagant.getXpos(), imagant.getYpos());
      g.drawImage(imagant.getImage(), 0, 0, null);
    }
    //    BufferedImage bi = new BufferedImage(imagant.getImage().getWidth(), imagant.getImage().getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
    //    bi.getGraphics().drawImage(imagant.getImage(), 0, 0, null);
    //    //g.drawImage(imagant.getImage(), x, y, null);
    //    g.drawImage(bi, x, y, null);
    //    //g.fillRect(x, y, 50, 50);
  }

  private void zaplanujNaplneniSklivce(Wpt wpt, Mou mou) {

    frontaWaypointu.add(new WptPaintRequest(wpt, mou));
    pocitVelikostFrontyWaypointu.set(frontaWaypointu.size());
    //    spocitejSklivece(wpt);
  }

  private Genotyp computeGenotyp(Wpt wpt) {
    Genom genom = ikonBag.getGenom();
    Genotyp g = wpt.getGenotyp(genom);
    //    switch (cestyModel.get(wpt.getKesoid())) {
    //    //    case ANO: g.put(ikonBag.getGenom().ALELA_lovime); break;
    //    case NE:  g.put(ikonBag.getGenom().ALELA_ignoru); break;
    //    }
    switch (vyletModel.get(wpt.getKesoid())) {
    case ANO: g.put(genom.ALELA_lovime); break;
    case NE:  g.put(genom.ALELA_ignoru); break;
    }
    g.put(cestyModel.isOnVylet(wpt) ? genom.ALELA_nacestejsou : genom.ALELA_mimocesticu);

    if (wpt == wptPodMysi) {
      g.put(genom.ALELA_mouseon);
    }
    if (wpt.getKesoid() == kesoidPodMysi) {
      g.put(genom.ALELA_mousean);
    }
    g.removeAll(fenotypoveZakazaneAlely);
    return g;
  }


  private BoundingRect coVykreslovat(Graphics gg) {
    Insets bii = ikonBag.getSada().getBigiestIconInsets();
    Rectangle rect = gg.getClipBounds();
    if (vykreslovatOkamtiteAleDlouho) {
      System.out.println("Omezeni: " + rect);
    }
    if (rect == null) {
      rect = new Rectangle(getSize());
    }
    //  	klipovanci.add(rect);
    // to right a bottom je v poradku, protoze zvetsujeme obdelnik,
    // abychom chytli vsechy kese, ktere k nam mohou zapadnout
    Rectangle rect2 = new Rectangle(rect.x - bii.right, rect.y - bii.bottom,
        rect.width +  bii.left + bii.right ,
        rect.height + bii.top + bii.bottom);
    BoundingRect br = getSoord().transforToBounding(rect2);
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



  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e, MouseGestureContext ctx) {
    if (wptPodMysi != null) {
      if (SwingUtilities.isLeftMouseButton(e)) {
        poziceModel.setPozice(wptPodMysi);
      }
      Kesoid kesoid = wptPodMysi.getKesoid();
      if (e.getClickCount() >= 3 && kesoid.getUrlPrint() != null) {
        BrowserOpener.displayURL(kesoid.getUrlPrint());
      } else if (e.getClickCount() >= 2 && kesoid.getUrlShow() != null) {
        BrowserOpener.displayURL(kesoid.getUrlShow());
      }
      e.consume();
    } else {
      chain().mouseClicked(e, ctx);
    }
  }


  @Override
  public void addPopouItems(JPopupMenu popupMenu, MouseGestureContext ctx) {
    if (wptPodMysi != null) {
      initPopupMenuItems(popupMenu, wptPodMysi, vsechny);
    }
    chain().addPopouItems(popupMenu, ctx);
  }

  private void initPopupMenuItems(JPopupMenu p, Wpt mysNadWpt, KesBag vsechny) {
    // Přidat zhasínače
    JMenu zhasinace = new JMenu("Zhasni");
    p.add(zhasinace);
    Genotyp genotyp = mysNadWpt.getGenotyp(vsechny.getGenom());
    for (Alela alela : genotyp.getAlely()) {
      if (alela.getGen().isVypsatelnyVeZhasinaci() &&  !alela.isVychozi()) {
        zhasinace.add(factory.init(new ZhasniKeseUrciteAlelyAction(alela)));
      }
    }

    ///
    Kesoid kesoid = mysNadWpt.getKesoid();
    p.add(factory.init(new ZoomKesAction(kesoid)));
    JMenuItem item = new JMenuItem(factory.init(new CenterWaypointAction(mysNadWpt)));
    item.setText("Centruj");
    //TODO Dát ikonu středování
    item.setIcon(null);
    p.add(item);
    if (kesoid.getUrlShow() != null) {
      p.add(factory.init(new ZobrazNaGcComAction(kesoid)));
    }
    if (kesoid.getUrlPrint() != null) {
      p.add(factory.init(new TiskniNaGcComAction(kesoid)));
    }
    if (kesoid.getUrlShow() != null) {
      p.add(factory.init(new UrlToClipboardForGeogetAction(kesoid)));
    }
    if (kesoid.getUrlPrint() != null) {
      p.add(factory.init(new UrlToListingForGeogetAction(kesoid)));
    }
    p.addSeparator();
    p.add(factory.init(new PridatDoCestyAction(mysNadWpt)));
    p.add(factory.init(new OdebratZCestyAction(mysNadWpt)));
    p.addSeparator();
    p.add(factory.init(new VyletAnoAction(kesoid)));
    p.add(factory.init(new VyletNeAction(kesoid)));
    p.add(factory.init(new VyletNevimAction(kesoid)));
    p.addSeparator();
    for (Wpt wpt : kesoid.getWpts()) {
      p.add(factory.init(new CenterWaypointAction(wpt)));
    }
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(MouseEvent e, MouseGestureContext ctx) {
    Wpt wpt = najdiWptVBlizkosti(new Point(e.getX(), e.getY()));
    wptPodMysi = wpt;
    Kesoid kes = wpt == null ? null : wpt.getKesoid();

    if (kes != kesoidPodMysi) {
      Kesoid staraKes = kesoidPodMysi;
      kesoidPodMysi = kes;
      //repaint();
      repaintKes(staraKes);
      repaintKes(kesoidPodMysi);
    }

    if (wptPodMysi != null) {
      //      setCursor(cursor);
      jakoTooltip.setText(wptPodMysi.textToolTipu());
      jakoTooltip.setOpaque(true);
      jakoTooltip.setBackground(Color.WHITE);
      jakoTooltip.setSize(jakoTooltip.getPreferredSize());

      Point p = getSoord().transform(wptPodMysi.getWgs().toMou());
      p.y -= jakoTooltip.getHeight();
      jakoTooltip.setLocation(p);
      jakoTooltip.setVisible(true);
      //System.out.println("TEXTIK ZDE: " + p);
    } else {
      jakoTooltip.setVisible(false);
      //System.out.println("TEXTIK PRYC: ");
    }
    //setToolTipText();

  }

  @Override
  public Cursor getMouseCursor(boolean pressed) {
    if (wptPodMysi != null && ! pressed) {
      Cursor cursor = FKurzory.NAD_WAYPOINTEM;
      return cursor;
    } else
      return super.getMouseCursor(pressed);
  }


  private Wpt najdiWptVBlizkosti(Point p) {
    //System.out.println("POSUNOVACKA TO ASI BUDE: " + e);
    int polomerCitlivosi = POLOMER_CITLIVOSTI;
    Rectangle rect = new Rectangle(p.x - polomerCitlivosi, p.y - polomerCitlivosi,
        polomerCitlivosi * 2, polomerCitlivosi * 2);
    Sheet<Wpt> swpt = indexator == null ? null : indexator.locateAnyOne(getSoord().transforToBounding(rect));
    Wpt wpt = swpt == null ? null : swpt.get();
    return wpt;
  }


  public void onEvent(PoziceSeMaMenitEvent event) {
    // TODO-vylet Tato metoda by měla být na modelu, pak je otázkou s jakou citlivostí vybírat
    int polomerCitlivosi = POLOMER_CITLIVOSTI;
    Point p = getSoord().transform(event.mou);
    Rectangle rect = new Rectangle(p.x - polomerCitlivosi, p.y - polomerCitlivosi,
        polomerCitlivosi * 2, polomerCitlivosi * 2);
    Sheet<Wpt> swpt = indexator == null ? null : indexator.locateNearestOne(
        getSoord().transforToBounding(rect), event.mou.xx, event.mou.yy);
    Wpt wpt = swpt == null ? null : swpt.get();
    if (wpt != null && wpt.getMou().equals(event.mou)) { // je to přesně on
      int priorita = 40;
      if (wpt.isMainWpt()) {
        priorita = 50;
      }
      priorita -= wpt.getKesoid().getKesoidKind().ordinal();
      event.add(wpt, priorita);
    }
  }

  public void onEvent(VyletChangeEvent aEvent) {
    if (aEvent.isVelkaZmena()) {
      Wpt.invalidateAllSklivec();
      repaint();
    } else {
      repaintWpt(aEvent.getKes().getMainWpt());
    }
  }


  /**
   * Vrátí pozici nmyši, pokud je v blízkosti kříže
   * @return
   */
  @Override
  public Mouable getUpravenaMys() {
    if (wptPodMysi != null)
      return wptPodMysi;
    else
      return chain().getUpravenaMys();
  }

  @Override
  public Wpt getWptPodMysi() {
    return wptPodMysi;
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

    private final WeakReference<JKesoidySlide> wrKesoidy;

    public PaintovaciVlakno(JKesoidySlide jKesoidy) {
      super ("Paintovani");
      final ReferenceQueue<JKesoidySlide> refqueue = new ReferenceQueue<JKesoidySlide>();
      wrKesoidy = new WeakReference<JKesoidySlide>(jKesoidy, refqueue);
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
      JKesoidySlide jKesoidy = wrKesoidy.get();
      if (jKesoidy == null) return; // slide kešoidů už je pryč, takže ani nemá cenu dál něco řešit
      for (;;) {
        try {
          BlockingQueue<WptPaintRequest> frontaWaypointu2 = jKesoidy.frontaWaypointu;
          jKesoidy = null; // přičekání na požadavek ve froně nesmím držet slide waypointů, aby mohl být garbage collectorem sebrán
          WptPaintRequest wpr = frontaWaypointu2.poll(10000, TimeUnit.MILLISECONDS); // aby umělo vlákno skončit, musí být timeout
          jKesoidy = wrKesoidy.get();
          if (jKesoidy == null) return; // slide kešoidů už je pryč, takže ani nemá cenu dál něco řešit
          if (wpr == null) {
            continue;
          }
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
    private void spocitejSklivece(Wpt wpt, final JKesoidySlide jKesoidy) {
      jKesoidy.computeSklivec(wpt);
    }


    private void vyvolejVykresleni(final MouRect aMouRect, final JKesoidySlide jKesoidy) {
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
  public void inject(CestyModel cestyModel) {
    this.cestyModel = cestyModel;
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
    paintComponent(g);
  }


  /* (non-Javadoc)
   * @see cz.geokuk.core.coord.JSingleSlide0#createRenderableSlide()
   */
  @Override
  public JSingleSlide0 createRenderableSlide() {
    return new JKesoidySlide(true);
  }

  @Override
  public EJakOtacetPriRendrovani jakOtacetProRendrovani() {
    return EJakOtacetPriRendrovani.COORD;
  }

  //  @Override
  //  public void finalize() {
  //    System.out.println("Kesoidy finalizovány");
  //  }

  private void computeSklivec(Wpt wpt) {
    Sklivec sklivec = ikonBag.getSada().getSklivec(computeGenotyp(wpt));
    wpt.setSklivec(sklivec);
  }

  public void inject(VyletModel vyletModel) {
    this.vyletModel = vyletModel;
  }

}
