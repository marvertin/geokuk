package textarea;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * 
 */

/**
 * @author veverka
 *
 */
public class JMainFrame extends JFrame {

  static int citac;

  String[] patternExamples = {
      "aaaaaaaaa",
      "bbbbbbbbb",
  };

  /**
   * 
   */
  private static final long serialVersionUID = -1527032129709847190L;
  private Box box;
  private final JButton northButton = new JButton("North");
  private final JButton southButton =  new JButton("South");
  private final JButton westButton =  new JButton("West");
  private final JButton eastButton  =  new JButton("East");

  private JMenuItem menuF1;
  private JMenuItem menuF2;
  private JMenuItem menuF3;
  private JMenuItem menuF4;
  private JMenuItem menuF5;

  private JTextArea jTextArea;
  private final JSlider uhlovnik = new JSlider(-90 * 60, 90 * 60, 0);

  private JScrollPane jScrollPane;

  /**
   * 
   */
  public JMainFrame() {
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    initComponents();
    registerEvents();
    pack();
    setSize(400, 300);

  }



  /**
   * 
   */
  private void initComponents() {
    box = Box.createVerticalBox();
    add(box);
    add(northButton, BorderLayout.NORTH);
    add(southButton, BorderLayout.SOUTH);
    add(westButton, BorderLayout.WEST);
    add(eastButton, BorderLayout.EAST);

    JMenuBar menuBar = new JMenuBar();
    JMenu jMenu = new JMenu("Fn");
    menuBar.add(jMenu);
    menuF1 = createMenuItem("F1");
    jMenu.add(menuF1);
    menuF2 = createMenuItem("F2");
    jMenu.add(menuF2);
    menuF3 = createMenuItem("F3");
    jMenu.add(menuF3);
    menuF4 = createMenuItem("F4");
    jMenu.add(menuF4);
    menuF5 = createMenuItem("F5");
    jMenu.add(menuF5);

    setJMenuBar(menuBar);

    //uhlovnik = new JSlider();
    jTextArea = new JTextArea();
    jScrollPane = new JScrollPane(jTextArea);
    box.add(jScrollPane);

    add(uhlovnik, BorderLayout.SOUTH);
    //    uhlovnik.addChangeListener(new ChangeListener() {
    //      @Override
    //      public void stateChanged(ChangeEvent aE) {
    //        System.out.println(uhlovnik.getValue());
    //        job.uhel = uhlovnik.getValue() / 60.0 / 180.0 * Math.PI;
    //        job.repaint();
    //      }
    //    });
    pack();
  }

  private JMenuItem createMenuItem(final String fx) {
    JMenuItem jMenuItem = new JMenuItem(fx);
    jMenuItem.setAccelerator(KeyStroke.getKeyStroke(fx));
    jMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent aE) {
        System.out.println("Vybran menu item: " + fx);

      }
    });

    return jMenuItem;
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

    final JMainFrame hlavni = new JMainFrame();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        hlavni.setVisible(true);
      }
    });
  }

  /**
   * 
   */
  private void registerEvents() {
    //    cbb.addActionListener(new ActionListener() {
    //
    //      @Override
    //      public void actionPerformed(ActionEvent e) {
    //        System.out.println(e);
    //      }
    //
    //    });
    //


    westButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jTextArea.append("Tak to pridame. ");
      }
    });

    eastButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jTextArea.append("S odradkovani. \n");
        jScrollPane.setAutoscrolls(true);
      }
    });

    northButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        new Timer(3000, new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
          }
        }).start();
      }
    });


  }


}
