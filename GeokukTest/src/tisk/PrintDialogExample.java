package tisk;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.UIManager;

public class PrintDialogExample implements Printable {


  @Override
  public int print(Graphics g, PageFormat pf, int page) throws
  PrinterException {

    if (page > 0)
      return NO_SUCH_PAGE;

    /* User (0,0) is typically outside the imageable area, so we must
     * translate by the X and Y values in the PageFormat to avoid clipping
     */
    Graphics2D g2d = (Graphics2D)g;
    g2d.translate(pf.getImageableX(), pf.getImageableY());

    /* Now we perform our rendering */
    g.drawString("Test the print dialog!", 100, 100);

    /* tell the caller that this page is part of the printed document */
    return PAGE_EXISTS;
  }

  public static void main(String args[]) {

    try {
      String cn = UIManager.getSystemLookAndFeelClassName();
      UIManager.setLookAndFeel(cn); // Use the native L&F
    } catch (Exception cnf) {
    }
    PrinterJob job = PrinterJob.getPrinterJob();
    PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
    PageFormat pf = job.pageDialog(aset);
    job.setPrintable(new PrintDialogExample(), pf);
    boolean ok = job.printDialog(aset);
    if (ok) {
      try {
        job.print(aset);
      } catch (PrinterException ex) {
        /* The job did not successfully complete */
      }
    }
    System.exit(0);
  }
}
