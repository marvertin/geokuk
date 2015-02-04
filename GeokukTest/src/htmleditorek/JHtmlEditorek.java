package htmleditorek;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


/**
 * 
 */

/**
 * @author veverka
 *
 */
public class JHtmlEditorek extends JFrame {

  private static final long serialVersionUID = -5917151506774692763L;

  private boolean stisknut;

  public JHtmlEditorek() {
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);

    String url = "file:pokus.html";
    try {
      final JEditorPane htmlPane = new JEditorPane(url);
      htmlPane.setEditable(true);

      //EditorKit editorKit = htmlPane.getEditorKit();


      JMenuBar menuBar = new JMenuBar();
      JMenu menu = new JMenu("Edit");

      Object[] allKeys = htmlPane.getActionMap().allKeys();
      Collections.sort(Arrays.asList(allKeys), new Comparator<Object>() {
        @Override
        public int compare(Object aO1, Object aO2) {
          return aO1.toString().compareTo(aO2.toString());
        }
      });
      for (Object key : allKeys) {
        System.out.println(key);
        menu.add(htmlPane.getActionMap().get(key));
      }

      htmlPane.getActionMap().get("font-bold").putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl B"));
      htmlPane.getActionMap().get("font-bold").putValue(Action.NAME, "BBBBBBBBBBBBBBBBBBBBBBB");
      htmlPane.getActionMap().get("font-italic").putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl I"));
      htmlPane.getActionMap().get("font-underline").putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl U"));




      menuBar.add(menu);
      setJMenuBar(menuBar);
      Box box = Box.createVerticalBox();
      //box.add(new JTextField("tak co tady bude"));

      box.add(new JScrollPane(htmlPane));
      add(box);

      Box boxbut = Box.createHorizontalBox();
      boxbut.add(Box.createHorizontalGlue());
      boxbut.setAlignmentX(CENTER_ALIGNMENT);
      final JButton bubu = new JButton();
      ImageIcon ico = new ImageIcon("echo.png");
      bubu.setIcon(ico);
      //bubu.setPressedIcon(new ImageIcon("newspaper.png"));
      boxbut.add(bubu);
      Insets insets = bubu.getMargin();
      System.out.println(insets);
      insets.set(0, 0, 0, 0);
      bubu.setMargin(insets);
      bubu.setEnabled(true);
      //bubu.setModel(new ToggleButtonModel());
      bubu.setSelected(true);
      bubu.getModel().setSelected(true);
      bubu.getModel().setPressed(true);
      //bubu.setMargin(insets);
      JButton konec = new JButton("+");
      boxbut.add(Box.createHorizontalGlue());
      boxbut.add(konec);
      JTextField jf = new JTextField("chachacha");
      boxbut.add(jf);
      JLabel lejblik = new JLabel("lejblicek");
      boxbut.add(lejblik);
      konec.setMargin(insets);
      add(boxbut, BorderLayout.SOUTH);
      bubu.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent aE) {
          String text = htmlPane.getText();
          System.out.println(text);
          bubu.getModel().setPressed(stisknut);
          stisknut = ! stisknut;
        }
      });
      //boxbut.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      pack();
      System.out.println("BUBU: " +  bubu.getBounds());
      System.out.println("boxbut: " +  boxbut.getBounds());
    } catch(IOException ioe) {
      throw new RuntimeException(ioe);
    }

  }



  public static void main(String[] args) {
    final JHtmlEditorek hlavni = new JHtmlEditorek();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        hlavni.setVisible(true);
      }
    });
  }

}
