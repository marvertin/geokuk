/**
 * 
 */
package index2dguitest;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

/**
 * @author veverka
 *
 */
public class Main  {

  /**
   * @author veverka
   *
   */



  private JFrame vytvorFrame() {
    JFrame frame = new JFrame();
    frame.setSize(1000,800);
    frame.setTitle("Index2d GUI test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    final Toolkit toolkit = frame.getToolkit();
    Dimension size = toolkit.getScreenSize();
    System.out.println(size);
    frame.setLocation(size.width/2 - frame.getWidth()/2,
        size.height/2 - frame.getHeight()/2);

    JBody jbody = new JBody();
    frame.getContentPane().add(jbody);

    jbody.setLayout(null);

    //    Mysovani mysovani = new Mysovani();
    //    panel.addMouseListener(mysovani);
    //    panel.addMouseMotionListener(mysovani);

    return frame;

  }

  public static void main(String[] args)  {
    new Main().vytvorFrame().setVisible(true);
  }
}
