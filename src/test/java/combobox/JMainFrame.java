package combobox;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
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

  private JComboBox cbb;
  private JTextField jtf;
  private final JSlider uhlovnik = new JSlider(-90 * 60, 90 * 60, 0);

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

    JPanel panel = new JPanel();
    cbb = new JComboBox(patternExamples);
    cbb.setEditable(true);
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

    cbb.addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          System.out.println("Vyselektovano: " +  (String)cbb.getSelectedItem()
              + " " + cbb.getSelectedIndex());
        }
      }
    });

    Component editorComponent = cbb.getEditor().getEditorComponent();
    System.out.println(editorComponent.getClass());
    //

    westButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int poz = Integer.parseInt(jtf.getText());
        String pridanost = "pridavame " + (citac+=100);
        cbb.insertItemAt(pridanost, poz);
        //        jtf.setText((String)cbb.getSelectedItem());
        //        System.out.println("VYBRANO: " + cbb.getSelectedItem());

      }
    });

    eastButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int poz = Integer.parseInt(jtf.getText());
        cbb.removeItemAt(poz);
      }
    });

    northButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        new Timer(3000, new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            int poz = Integer.parseInt(jtf.getText());
            String pridanost = "zamena " + (citac+=100);
            cbb.insertItemAt(pridanost, poz);
            cbb.removeItemAt(poz+1);

          }
        }).start();
      }
    });


  }


}
