package busycursortest;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BusyCursorTest extends JFrame {
    /**
   * 
   */
  private static final long serialVersionUID = 1L;
    JPanel panel = new JPanel();
    JButton wait1 = new JButton("Wait 1/3 of a second");
    JButton wait2 = new JButton("Wait 2/3 of a second");
    JButton wait3 = new JButton("Wait 1 second");

    public BusyCursorTest() {
        setTitle("Busy Cursor Test");
        setSize(400,400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridLayout layout = new GridLayout(3,1);
        panel.setLayout(layout);
        panel.add(wait1);
        panel.add(wait2);
        panel.add(wait3);
        getContentPane().add(panel);
        
        ActionListener wait1ActionListener = delayActionListener(333);
        ActionListener wait2ActionListener = delayActionListener(666);
        ActionListener wait3ActionListener = delayActionListener(5000);
        
        // Add in the busy cursor
        ActionListener busy1ActionListener = CursorController.createListener(this, wait1ActionListener);
        ActionListener busy2ActionListener = CursorController.createListener(this, wait2ActionListener);
        ActionListener busy3ActionListener = CursorController.createListener(this, wait3ActionListener);
        
        wait1.addActionListener(busy1ActionListener);
        wait2.addActionListener(busy2ActionListener);
        wait3.addActionListener(busy3ActionListener);
        
        setVisible(true);
    }
    
    /**
     * Creates an actionListener that waits for the specified number of milliseconds.
     */
    private ActionListener delayActionListener(final int delay) {
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    System.out.printf("Waiting for %d milliseconds\n", new Integer(delay));
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        };
        return listener;
    }
 
    public static void main(String[] args) {
        new BusyCursorTest();
    }
    
    public static class CursorController {
      public static final Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
      public static final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
      public static final int delay = 500; // in milliseconds

      private CursorController() {}
      
      public static ActionListener createListener(final Component component, final ActionListener mainActionListener) {
          ActionListener actionListener = new ActionListener() {
              public void actionPerformed(final ActionEvent ae) {
                  
                  TimerTask timerTask = new TimerTask() {
                      public void run() {
                          component.setCursor(busyCursor);
                      }
                  };
                  Timer timer = new Timer(); 
                  
                  try {   
                      timer.schedule(timerTask, delay);
                      mainActionListener.actionPerformed(ae);
                  } finally {
                      timer.cancel();
                      component.setCursor(defaultCursor);
                  }
              }
          };
          return actionListener;
      }
  }
    
}
