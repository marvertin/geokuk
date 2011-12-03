package componentresizing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;


/* ResizableComponent.java */

public class ResizovatelnaKomponentaMain extends JFrame {

  private static final long serialVersionUID = 1L;
  private JPanel panel = new JPanel(null);
  private Resizovatelnost resizer;


  public ResizovatelnaKomponentaMain() {

      add(panel);

      JPanel area = new JPanel(); 
      area.setBackground(Color.white);
      resizer = new Resizovatelnost(area);
      resizer.setBounds(50, 50, 200, 150);
      panel.add(resizer);

 
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(new Dimension(350, 300));
      setTitle("Resizable Component");
      setLocationRelativeTo(null);

      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent me) {

          requestFocus();
          resizer.repaint();
        }
      });
  }

  public static void main(String[] args) {
      ResizovatelnaKomponentaMain rc = new ResizovatelnaKomponentaMain();
      rc.setVisible(true);
  }
}

