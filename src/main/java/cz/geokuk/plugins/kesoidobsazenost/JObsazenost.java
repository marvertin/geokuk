package cz.geokuk.plugins.kesoidobsazenost;


import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.MouseGestureContext;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.util.gui.JBarvovyDvojSlider;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.FlatVisitor;
import cz.geokuk.util.index2d.Indexator;
import cz.geokuk.util.index2d.Sheet;


public class JObsazenost extends JSingleSlide0 implements AfterEventReceiverRegistrationInit {

  private static final int POLOMER_OBSAZENOSTI = 161;

  private static final long serialVersionUID = -5858146658366237217L;

  private ObsazenostSettings obsazenost;

  private Indexator<Wpt> iIndexator;

  private JBarvovyDvojSlider iSlidovnik;


  private ObsazenostModel obsazenostModel;

  public JObsazenost() {
    setOpaque(false);
    setCursor(null);
    initComponents();
  }

  /**
   * 
   */
  private void initComponents() {
    setLayout(new BorderLayout());
    iSlidovnik = new JBarvovyDvojSlider();
    add(iSlidovnik, BorderLayout.EAST);
    iSlidovnik.getBarvovnik().setToolTipText("Nastavení stupně šedi kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
    iSlidovnik.getPruhlednik().setToolTipText("Nastavení průhlednosti kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
  }

  /**
   * 
   */
  private void registerEvents() {
    iSlidovnik.addChangeListener(new ChangeListenerImplementation());
  }

  public void onEvent(KeskyNactenyEvent event) {
    iIndexator = event.getVsechny().getIndexator();
    repaint();
  }

  public void onEvent(ObsazenostPreferencesChangeEvent event) {
    obsazenost = event.obsazenost;
    iSlidovnik.setColor(obsazenost.getColor());
    repaint();
  }

  public void onEvent(ObsazenostOnoffEvent event) {
    setVisible(event.isOnoff());
    repaint();
  }



  @Override
  public void paintComponent(Graphics aG) {
    if (iIndexator == null) return;
    final Graphics2D g = (Graphics2D) aG;
    final int r = polomerObsazenosti();
    final int d = 2 * r;
    if (d < 4) return; // nemá smysl kreslit malé kroužky
    //obsazenost.setColor(new Color(128,128,128,128));
    g.setColor(obsazenost.getColor());
    int mouokraj = (int) (getSoord().getMouboduNaMetr() * POLOMER_OBSAZENOSTI);
    BoundingRect boundingRect = getSoord().getBoundingRect().rozsir(mouokraj);
    //final Area area = new Area();
    iIndexator.visit(boundingRect, new FlatVisitor<Wpt>() {

      @Override
      public void visit(Sheet<Wpt> aSheet) {
        Wpt wpt = aSheet.get();
        if (! wpt.obsazujeOblast()) return;
        Mou mou = new Mou(aSheet.getXx(), aSheet.getYy());
        Point p = getSoord().transform(mou);
        //Ellipse2D kruh = new Ellipse2D.Float(p.x -r, p.y - r, d, d);
        //Area areakruh = new Area(kruh);
        //area.add(areakruh);
        g.fillOval(p.x -r, p.y - r, d, d);
      }
    });

    //g.fill(area);
  }

  /**
   * @return
   */
  private int polomerObsazenosti() {
    return (int) (getSoord().getPixluNaMetr() * POLOMER_OBSAZENOSTI);
  }

  //g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
  /**
   * @author veverka
   *
   */
  private final class ChangeListenerImplementation implements ChangeListener {
    @Override
    public void stateChanged(ChangeEvent aArg0) {
      ObsazenostSettings data = obsazenostModel.getData();
      data.setColor(iSlidovnik.getColor());
      obsazenostModel.setData(data);
    }
  }

  @Override
  public void initAfterEventReceiverRegistration() {
    registerEvents();
  }


  public void inject(ObsazenostModel obsazenostModel) {
    this.obsazenostModel = obsazenostModel;
  }

  @Override
  public JSingleSlide0 createRenderableSlide() {
    return new JObsazenost();
  }

  @Override
  public void mouseDragged(MouseEvent e, MouseGestureContext ctx) {
    //ctx.setMouseCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    super.mouseDragged(e, ctx);
  }
}
