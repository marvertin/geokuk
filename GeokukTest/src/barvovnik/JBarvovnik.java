package barvovnik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.util.gui.AlfaColorSelectionModel;
import cz.geokuk.util.gui.JAlfaColorChooser;

/**
 * 
 */

/**
 * @author veverka
 *
 */
public class JBarvovnik extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = -1527032129709847190L;
  private Box box;
  private final JButton northButton = new JButton("North");
  private final JButton southButton =  new JButton("South");
  private final JButton westButton =  new JButton("West");
  private final JButton eastButton  =  new JButton("East");

  private final Color color = new Color(200, 100, 30, 150);
  private final JAlfaColorChooser cch = new JAlfaColorChooser(Color.WHITE);
  /**
   * 
   */
  public JBarvovnik() {
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    initComponents();
    registerEvents();
    pack();
    System.out.println("Vybrana barva: " + cch.getColor()+ " = " + cch.getColor().getAlpha());
    //setSize(400, 300);

  }


  /**
   * 
   */
  private void registerEvents() {
    final AlfaColorSelectionModel model = cch.getSelectionModel();
    model.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent aE) {
        System.out.println("Vybrana barva: " + model.getSelectedColor() + " - " + model.getSelectedColor().getAlpha());
        northButton.setForeground(model.getSelectedColor());
        southButton.setBackground(model.getSelectedColor());
      }
    });
    model.setSelectedColorWithAlfa(color);
  }


  /**
   * 
   */
  private void initComponents() {
    //southButton.setOpaque(false);
    box = Box.createVerticalBox();
    //    box.add
    //    box.add(Box.createVerticalStrut(20));
    box.add(cch);
    add(box);
    add(northButton, BorderLayout.NORTH);
    add(southButton, BorderLayout.SOUTH);
    add(westButton, BorderLayout.WEST);
    add(eastButton, BorderLayout.EAST);
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

    final JBarvovnik hlavni = new JBarvovnik();

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        hlavni.setVisible(true);
      }
    });
  }
}
