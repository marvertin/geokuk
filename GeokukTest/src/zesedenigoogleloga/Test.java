package zesedenigoogleloga;

import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;
public class Test
{
  public static void main(String[] args) throws Exception
  {
     JFrame frame = new JFrame();
     Image logo = Toolkit.getDefaultToolkit().getImage(new URL("http://www.google.com/images/logo.gif"));
     MediaTracker tracker = new MediaTracker(frame);
     tracker.addImage(logo,0);
     tracker.waitForAll();
     
     BufferedImage logoCopy = new BufferedImage(logo.getWidth(null),logo.getHeight(null),BufferedImage.TYPE_USHORT_GRAY);
     logoCopy.createGraphics().drawImage(logo,0,0,null);
     JLabel logoLabel = new JLabel(new ImageIcon(logoCopy));
     frame.setSize(300,200);
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.getContentPane().add(logoLabel);
     frame.setVisible(true);
  }
} 