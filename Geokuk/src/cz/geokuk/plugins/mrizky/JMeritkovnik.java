package cz.geokuk.plugins.mrizky;


import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.BeanSubtype;


public class JMeritkovnik extends JSingleSlide0 implements AfterInjectInit {


  private static final long serialVersionUID = -5858146658366237217L;
  private JMeritko meritko;

  public JMeritkovnik() {
    setOpaque(false);
    setCursor(null);
    initComponents();
  }

  /**
   * 
   */
  private void initComponents() {
    setLayout(new BorderLayout());
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    meritko = new JMeritko();
    panel.add(meritko);
    add(meritko, BorderLayout.SOUTH);
  }


  @SuppressWarnings("unused")
  private void drawLine(Graphics g, int xx1, int yy1, int xx2, int yy2) {
    Point p1 = getSoord().transform(new Mou(xx1, yy1));
    Point p2 = getSoord().transform(new Mou(xx2, yy2));
    g.drawLine(p1.x, p1.y, p2.x, p2.y);

  }

  @BeanSubtype ("Meritkovnik")
  public void onEvent(MrizkaEvent event) {
    setVisible(event.onoff);
  }

  @Override
  public void initAfterInject() {
    factory.init(meritko);
  }

  @Override
  public JSingleSlide0 createRenderableSlide() {
    return new JMeritkovnik();
  }

}
