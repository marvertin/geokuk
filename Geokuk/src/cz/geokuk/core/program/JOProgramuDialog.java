package cz.geokuk.core.program;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;

import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.util.process.BrowserOpener;

public class JOProgramuDialog extends JMyDialog0 {

  private static final long serialVersionUID = 7180968190465321695L;

  public JOProgramuDialog() {
    setTitle("O programu");
    init();
  }

  @Override
  protected void initComponents() {
    Box box = Box.createVerticalBox();
    add(box);

    box.add(Box.createGlue());
    box.add(Box.createVerticalStrut(20));

    JLabel c1 = new JLabel("GeoKuk");
    c1.setAlignmentX(CENTER_ALIGNMENT);
    c1.setFont(new Font("Arial", Font.BOLD, 30));
    box.add(c1);

    JLabel c2 = new JLabel("Verze " + FConst.VERSION);
    c2.setAlignmentX(CENTER_ALIGNMENT);
    box.add(c2);

    box.add(Box.createVerticalStrut(10));
    JLabel c3 = new JLabel("(c) 2009 Matin Veverka");
    c3.setAlignmentX(CENTER_ALIGNMENT);
    box.add(c3);

    JLabel c4 = new JLabel("Profil na GC.COM a GC.CZ: \"rodinka veverek\"");

    c4.setAlignmentX(CENTER_ALIGNMENT);
    c4.setFont(new Font("Serif", Font.ITALIC, 12));
    box.add(c4);

    JButton bgccom = new JButton(ImageLoader.seekResIcon("gccom.png"));
    bgccom.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        String urls = "http://www.geocaching.com/profile/?guid=22cad0c7-59a3-417c-99d1-7c9079e9ae27";
        try {
          BrowserOpener.displayURL(new URL(urls));
        } catch (MalformedURLException e1) { // to půjde
        }
      }
    });
    bgccom.setAlignmentX(CENTER_ALIGNMENT);
    box.add(bgccom);

    box.add(Box.createVerticalStrut(10));
    JLabel zdarma1 = new JLabel("Program GeoKuk můžete zdarma používat");
    JLabel zdarma2 = new JLabel("pro nekomerční rekreační aktivity.");
    zdarma1.setFont(new Font("Serif", Font.ITALIC, 12));
    zdarma1.setAlignmentX(CENTER_ALIGNMENT);
    box.add(zdarma1);
    zdarma2.setFont(new Font("Serif", Font.ITALIC, 12));
    zdarma2.setAlignmentX(CENTER_ALIGNMENT);
    box.add(zdarma2);
    box.add(Box.createVerticalStrut(20));
    box.add(Box.createGlue());
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return null;
  }

}
