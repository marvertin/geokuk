package swingworker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


/**
 * 
 */

/**
 * @author veverka
 *
 */
public class JHlavni extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1858517076938050238L;
  public static JLabel l;
  public static Bezic bezic;
  
  /**
   * 
   */
  public JHlavni() {
    Box p = Box.createVerticalBox();
    l = new JLabel("nic nespusteno");
    JButton bspust = new JButton("spust");
    JButton bvyjimkahned = new JButton("vyjimka hned");
    JButton bvyjimkatam = new JButton("vyjimka tam");
    p.add(l);
    p.add(bspust);
    p.add(bvyjimkahned);
    p.add(bvyjimkatam);
    
    add(p);
    pack();
    
    bspust.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent aE) {
        bezic = new Bezic();
        bezic.execute();
      }
    });
    
    bvyjimkahned.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent aE) {
        throw new RuntimeException("primo z vlakna prvni",
            new RuntimeException("primo z vlkna druhy"));
        
      }
    });

    bvyjimkatam.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent aE) {
        if (bezic != null) bezic.vyhodisx = true;
      }
    });

  }
  
  
  public static void main(String[] args) {
    
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      
      @Override
      public void uncaughtException(Thread aT, Throwable ee) {
        System.err.println("Osetrena nechycena vyjimka: ");
        for (Throwable e = ee; e != null; e = e.getCause()) {
          System.err.println("::" + e.toString() + "  " + e.getMessage());
        }
        ee.printStackTrace();
        
      }
    });
    
    final JHlavni hlavni = new JHlavni();
    
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        hlavni.setVisible(true);
      }
    });
  }
}
