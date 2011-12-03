package combobox;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import cz.geokuk.core.render.JGeocodingComboBox;
import cz.geokuk.core.render.RenderSettings.Patterned;

/**
 * 
 */

/**
 * @author veverka
 *
 */
public class JGeoKombo extends JFrame {

  static int citac;


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

  private JGeocodingComboBox cbb;
  private JTextField jtf;
  private final JSlider uhlovnik = new JSlider(-90 * 60, 90 * 60, 0);

  /**
   * 
   */
  public JGeoKombo() {
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

    JPanel panel = new JPanel();
    cbb = new JGeocodingComboBox();
    panel.add(cbb);
    jtf = new JTextField();
    jtf.setColumns(10);
    panel.add(jtf);
    //uhlovnik = new JSlider();
    box.add(panel);

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

    final JGeoKombo hlavni = new JGeoKombo();

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

    cbb.addListener(new JGeocodingComboBox.Listener() {

      @Override
      public void patternChanged(Patterned patterned) {
        System.out.println(patterned);
      }

    });


    Component editorComponent = cbb.getEditor().getEditorComponent();
    System.out.println(editorComponent.getClass());
    //

    westButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        SortedMap<String,String> ss = new TreeMap<String, String>();
        citac += 100;
        ss.put("AA", "s-prvni AA " + (citac+1));
        ss.put("CC", "d-prvni CC " + (citac+2));
        ss.put("DD", "e-prvni DD " + (citac+3));
        ss.put("HH", "n-prvni HH " + (citac+4));
        cbb.addPatterns(ss, JGeocodingComboBox.PRAZDNE_GEOTAGGINGG_PATTERNS);
      }
    });

    eastButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        SortedMap<String,String> ss = new TreeMap<String, String>();
        citac += 100;
        ss.put("BB", "s-druha BB " + (citac+1));
        ss.put("FF", "x-druha FF " + (citac+2));
        ss.put("GG", "a-druha GG " + (citac+3));
        cbb.addPatterns(null, ss);
      }
    });

    northButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        for (char i=' '; i < 127; i++) {
          try {
            File.createTempFile("cccccccc", "soubor_" + i);
          } catch (IOException e1) {
            System.out.println("Nelze: " + i + "   " + e1.toString());
          }
        }
        System.out.println("konec");
      }
    });


  }


}
