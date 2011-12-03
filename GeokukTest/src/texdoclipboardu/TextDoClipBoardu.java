package texdoclipboardu;
import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * 
 */

/**
 * @author veverka
 *
 */
public class TextDoClipBoardu extends JFrame {

  private final String[] ssa = {"http://www.geocaching.com/seek/cache_details.aspx?guid=eb6ee1fc-44f3-4667-9711-2710d2c72d73",
      "http://www.geocaching.com/seek/cache_details.aspx?guid=9c3fced3-6daf-4f5a-a5fc-298993e739eb",
      "http://www.geocaching.com/seek/cache_details.aspx?guid=9373dfd4-42c3-4611-beb6-319fc97c43fa",
  "http://www.geocaching.com/seek/cache_details.aspx?guid=c8022719-eb5f-45ed-8545-2dbc2a87df91"};
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
  /**
   * 
   */
  public TextDoClipBoardu() {
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    initComponents();
    registerEvents();
    pack();
    setSize(400, 300);

  }


  /**
   * 
   */
  private void registerEvents() {
    northButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent aE) {
        Clipboard scl = getToolkit().getSystemClipboard();
        for (String s : ssa) {
          StringSelection ss = new StringSelection(s);
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
          }
          scl.setContents(ss, null);
        }
      }
    });
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

    final TextDoClipBoardu hlavni = new TextDoClipBoardu();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        hlavni.setVisible(true);
      }
    });
  }
}
