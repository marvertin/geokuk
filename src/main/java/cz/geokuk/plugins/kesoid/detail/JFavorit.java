package cz.geokuk.plugins.kesoid.detail;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import cz.geokuk.img.ImageLoader;

public class JFavorit extends JComponent {

  private static final long serialVersionUID = 8991499244324360406L;
  private int kolik = Integer.MIN_VALUE;
  private static final Font sFont = new Font("SansSerif", Font.BOLD, 18);

  public JFavorit(int kolik) {
    setKolik(kolik);
  }



  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    int w = getWidth();
    int h = getHeight();
    int zaobleni = 20;

    // fill Ellipse2D.Double
    GradientPaint redtowhite = new GradientPaint(w/2, 0, new Color(242,222,158), w/2, h, new Color(251,251,243));
    g2.setPaint(redtowhite);
    g2.fillRoundRect(0, 0, w, h, zaobleni, zaobleni);

    //g2.setStroke(new BasicStroke(2.0f));
    g2.setColor(Color.decode("#E9A24C"));
    g2.drawRoundRect(0, 0, w -1, h- 1, zaobleni, zaobleni);

    g2.drawImage(ImageLoader.seekResImage("icon_fav.png"), 8, 7, null);

    String ss = new Integer(kolik).toString();
    g2.setFont(sFont);
    g2.setColor(Color.BLACK);
    g2.drawString(ss, 25, 22);
  }

  public void setKolik(int kolik) {
    if (kolik == this.kolik) return;
    this.kolik = kolik;
    String ss = new Integer(kolik).toString();
    int stringWidth = super.getFontMetrics(sFont).stringWidth(ss);
    Dimension newPrefferedSize = new Dimension(33 + stringWidth ,30);
    setPreferredSize(newPrefferedSize);
    revalidate();
    repaint();
  }

  public static void main(String[] args) {
    JFrame frm = new JFrame();
    JPanel p = new JPanel();
    final JFavorit jFavorit = new JFavorit(-1);
    p.add(jFavorit);
    frm.add(p);
    frm.pack();
    frm.setVisible(true);


    jFavorit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jFavorit.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent mouseevent) {
        System.out.println("Prasteno do mysi " + jFavorit.kolik);
      };
    });

    new Timer(1000, new ActionListener() {
      int kolik = 0;

      @Override
      public void actionPerformed(ActionEvent actionevent) {
        kolik ++;
        jFavorit.setKolik(kolik);
      }
    }).start();
  }
}
