package tisk;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Tiskneme1 extends JFrame implements Printable {

  private static final long serialVersionUID = 6432419642521764443L;

  private final JButton jTiskovyDialog1 = new JButton("Tiskový dialog 1 ...");
  private final JButton jTiskovyDialog2 = new JButton("Tiskový dialog 2 ...");
  private final JButton jStrankovyDialog1 = new JButton("Stránkový dialog 1 ...");
  private final JButton jStrankovyDialog2 = new JButton("Stránkový dialog 2 ...");
  private final JButton jTisk = new JButton("Tisk");
  private final JButton jPreview = new JButton("Preview");
  private final JTextField jJmenoTiskarny = new JTextField(30);

  private final PrinterJob pj;

  private PageFormat pf;

  public Tiskneme1() {
    super("Pokusny tisknuc");
    Box box = Box.createVerticalBox();
    add(box);
    box.add(jTiskovyDialog1);
    box.add(jTiskovyDialog2);
    box.add(jStrankovyDialog1);
    box.add(jStrankovyDialog2);
    box.add(jJmenoTiskarny);
    box.add(jTisk);
    box.add(jPreview);
    pack();

    pj = PrinterJob.getPrinterJob();
    pf = pj.defaultPage();
    pj.setPrintable(this);
    vypis(pj.defaultPage());
    zverejniJmeno();

    jTiskovyDialog1.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean ok = pj.printDialog();
        jTiskovyDialog1.setText("Tiskový dialog 1 ... " + ok);
        zverejniJmeno();
        PrintService ps = pj.getPrintService();
        //pf = pj.getPageFormat(ps.getAttributes());
        System.out.println("------------");
        for (Attribute attr : ps.getAttributes().toArray()) {
          System.out.println("ATRIBUT: " + attr.getName() + " - " +attr.getCategory());
        }
        vypis(pf);
      }
    });
    jTiskovyDialog2.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        boolean ok = pj.printDialog(pras);
        pf = pj.getPageFormat(pras);
        jTiskovyDialog2.setText("Tiskový dialog 2 ... " + ok);
        zverejniJmeno();
        PrintService ps = pj.getPrintService();
        System.out.println("------------");
        for (Attribute attr : ps.getAttributes().toArray()) {
          System.out.println("ATRIBUT: " + attr.getName() + " - " +attr.getCategory());
        }
        vypis(pf);
      }
    });
    jStrankovyDialog1.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        pf = pj.pageDialog(pf);
        pf = pj.validatePage(pf);
        pj.setPrintable(Tiskneme1.this, pf);
        vypis(pf);
        //        jStrankovyDialog.setText("Stránkový dialog ..." + ok);
        zverejniJmeno();
      }
    });
    jStrankovyDialog2.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        pf = pj.pageDialog(new HashPrintRequestAttributeSet());
        pf = pj.validatePage(pf);
        pj.setPrintable(Tiskneme1.this, pf);
        vypis(pf);
        //        jStrankovyDialog.setText("Stránkový dialog ..." + ok);
        zverejniJmeno();
      }
    });

    jPreview.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        PrintPreview printPreview = new PrintPreview(Tiskneme1.this, pj, pf, "Náhled");
        printPreview.setVisible(true);
      }
    });

    jTisk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          pj.print();
        } catch (PrinterException e1) {
          throw new RuntimeException(e1);
        }
      }
    });
  }

  private void zverejniJmeno() {
    jJmenoTiskarny.setText(pj.getPrintService().getName());
  }

  @Override
  public int print(Graphics ag, PageFormat pf, int page) throws
  PrinterException {

    System.out.println("Printing page: " + page);
    //    if (true)
    //      return NO_SUCH_PAGE;

    /* User (0,0) is typically outside the imageable area, so we must
     * translate by the X and Y values in the PageFormat to avoid clipping
     */
    Graphics2D g = (Graphics2D)ag;
    g.translate(pf.getImageableX(), pf.getImageableY());

    int w = (int) pf.getImageableWidth();
    int h = (int) pf.getImageableHeight();
    g.setColor(Color.BLACK);
    if (page == 0) {
      g.scale(0.2, 0.2);
      /* Now we perform our rendering */
      g.drawString("Hello world!", 100, 100);
      g.drawString("Velikosti: " + g.getClipBounds(), 100, 200);
      g.drawString("Celkova:      " + pf.getWidth() + " * " + pf.getHeight(), 100, 300);
      g.drawString("ImagableSize: " + w + " * " + h, 100, 350);
      g.drawString("ImagableXY:   " + pf.getImageableX() + " * " + pf.getImageableY(), 100, 450);


      g.drawRect(0, 0, w-1, h-1);

      g.drawLine(0, h/2, w/2, 0);
      g.drawLine(w/2, 0, w, h/2);
      g.drawLine(w, h/2, w/2, h);
      g.drawLine(w/2, h, 0, h/2);

      g.drawLine(0, h/2, -w, h/2);
      g.drawLine(w/2, 0, w/2, -h);
      g.drawLine(w, h/2, 2*w, h/2);
      g.drawLine(w/2, h, w/2, 2*h);

      //    } else if (page == 1) {
      //      g.drawOval(0, 0, w, h);

    } else
      return NO_SUCH_PAGE;


    /* tell the caller that this page is part of the printed document */
    return PAGE_EXISTS;
  }



  public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

    System.out.println("JEDU");
    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    //UIManager.put("swing.boldMetal", Boolean.FALSE);
    //    f.addWindowListener(new WindowAdapter() {
    //      @Override
    //      public void windowClosing(WindowEvent e) {System.exit(0);}
    //    });
    Tiskneme1 tiskneme1 = new Tiskneme1();
    tiskneme1.setVisible(true);
  }

  private void vypis(PageFormat pf) {
    int w = (int) pf.getImageableWidth();
    int h = (int) pf.getImageableHeight();
    /* Now we perform our rendering */
    System.out.println("-----------");
    System.out.println("   Celkova:      " + pf.getWidth() + " * " + pf.getHeight());
    System.out.println("   ImagableSize: " + w + " * " + h);
    System.out.println("   ImagableXY:   " + pf.getImageableX() + " - " + pf.getImageableY());
    System.out.println("   Orientation:  " + pf.getOrientation());


  }
}
