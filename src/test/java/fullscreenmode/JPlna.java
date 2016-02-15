package fullscreenmode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * 
 */

/**
 * @author veverka
 *
 */
public class JPlna extends JFrame {
  
  private static final long serialVersionUID = -3233381588100388300L;
  public JZavirakFullScreen zavirak;
  private JToolBar iTb;

  /**
   * 
   */
  public JPlna() {
    
    FullScreenAction.mainFrame = this;
    
    Box p = Box.createVerticalBox();
    //setUndecorated(true);
    
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    
    FullScreenAction ac = new FullScreenAction();
    
    JMenuBar jMenuBar = new JMenuBar();
    JMenu jMenu = new JMenu("Cosi");
    JMenuItem jMenuItem = new JMenuItem();
    jMenuItem.setAction(ac);
    jMenu.add(jMenuItem);
    jMenuBar.add(jMenu);
    setJMenuBar(jMenuBar);
    
//    JComponent component = new JFrame();
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F11"),
    "fullscreen");
    getRootPane().getActionMap().put("fullscreen", ac);

    zavirak = new JZavirakFullScreen();
    zavirak.setVisible(false);
    p.add(zavirak);
    zavirak.setAction(ac);
    
    JTextField jtf = new JTextField("ddddddddddddddddddddd");
    p.add(jtf);
    
    iTb = new JToolBar();
    iTb.add(ac);
    add(iTb, BorderLayout.PAGE_START);
    JLabel lejblik = new JLabel("ddddddddddd");
    p.setOpaque(true);
    p.setBackground(Color.GREEN);
    p.add(lejblik);
    
    add(p);
    pack();
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
    
    final JPlna hlavni = new JPlna();
    
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        hlavni.setVisible(true);
      }
    });
  }
  
  
  @SuppressWarnings("serial")
  private static class FullScreenAction extends AbstractAction {

    public static JPlna mainFrame;
    
    /**
     * 
     */
    public FullScreenAction() {
      super("Celá obrazovka");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      final GraphicsDevice gs = ge.getDefaultScreenDevice();
      setEnabled(gs.isFullScreenSupported());
    }
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent aE) {
      
      System.out.println("Stisknouto F11");
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      final GraphicsDevice gs = ge.getDefaultScreenDevice();
      boolean jsmeVeFulu = gs.getFullScreenWindow() != null;
      
      
      mainFrame.setVisible(false);
      mainFrame.dispose();
      if (jsmeVeFulu) { // ta kdeme pryc
        System.out.println("DO OKNA");
        mainFrame.getJMenuBar().setVisible(true);
        mainFrame.iTb.setVisible(true);
        mainFrame.setUndecorated(false);
        mainFrame.zavirak.setVisible(false);
        gs.setFullScreenWindow(null);
      } else {
        System.out.println("DO FULU");
        mainFrame.getJMenuBar().setVisible(false);
        mainFrame.iTb.setVisible(false);
        mainFrame.setUndecorated(true);
        mainFrame.zavirak.setVisible(true);
        gs.setFullScreenWindow(mainFrame);
      }
      mainFrame.setVisible(true);
      
    }
    
  }
  
  
  private static class JZavirakFullScreen extends JButton {

    /**
     * 
     */
    private static final long serialVersionUID = -974399150182077553L;
    
    /**
     * 
     */
    public JZavirakFullScreen() {
      super("ukonèi full screen");
      setForeground(Color.RED);
      setBackground(new Color(0,0,0,128));
    }
  }
}
