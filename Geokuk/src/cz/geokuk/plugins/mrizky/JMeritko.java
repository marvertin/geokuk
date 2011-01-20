/**
 * 
 */
package cz.geokuk.plugins.mrizky;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JPanel;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.VyrezChangedEvent;


/**
 * @author veverka
 *
 */
public class JMeritko extends JPanel {

  private static final int MINIMALNI_SIRKA_DILKU = 20;
  private static final int MAXIMALNI_SIRKA_CELEHO_DILKU = 400;

  private static final long serialVersionUID = -4801191981059574701L;
  private Coord moord;

  public JMeritko() {
    setPreferredSize(new Dimension(1600, 40));
  }

  public void onEvent(VyrezChangedEvent event) {
    moord = event.getMoord();
  }

  @Override
  protected void paintComponent(Graphics g) {
    // nejdříve spočítat šíčku dílku
    double pixluNaMetr =  moord.getPixluNaMetr();
    if (pixluNaMetr <= 0 || Double.isNaN(pixluNaMetr)) return;
    double metruNaDilek = 1;
    int pixluNaDilek = 0;
    while(pixluNaDilek < MINIMALNI_SIRKA_DILKU) {
      metruNaDilek *= 10;
      pixluNaDilek = (int) (pixluNaMetr * metruNaDilek);
    }


    Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
    //		map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    map.put(TextAttribute.BACKGROUND, Color.WHITE);
    map.put(TextAttribute.SWAP_COLORS, TextAttribute.SWAP_COLORS_ON);
    map.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
    Font font = g.getFont().deriveFont( map );
    g.setFont( font );


    int pocetDilku = Math.min(8, MAXIMALNI_SIRKA_CELEHO_DILKU / pixluNaDilek);
    int pocatekY = 30;
    int tloustka = 6;
    int vyskaCarky = 6;
    int sirka = pixluNaDilek * pocetDilku;
    int offset = (getParent().getWidth() - sirka) / 2;
    for (int i = 0; i < pocetDilku; i ++) {
      double metruOdZacatku = i * metruNaDilek;
      int pixluOdZacatku =  offset + (int) (metruOdZacatku * pixluNaMetr);
      g.setColor(i % 2 == 0 ? Color.BLACK : Color.WHITE);
      g.fillRect(pixluOdZacatku, pocatekY, pixluNaDilek, tloustka);
      g.setColor(Color.BLACK);
      g.drawRect(pixluOdZacatku, pocatekY, pixluNaDilek, tloustka);
      g.setColor(Color.WHITE);
      g.drawLine(pixluOdZacatku, pocatekY-1, pixluOdZacatku + pixluNaDilek, pocatekY-1);
      g.setColor(Color.BLACK);
      g.fillRect(pixluOdZacatku, pocatekY-vyskaCarky, 2, vyskaCarky + tloustka);
      g.setColor(Color.WHITE);
      g.drawLine(pixluOdZacatku+2, pocatekY-1, pixluOdZacatku+2, pocatekY - vyskaCarky);

      g.setColor(i % 2 == 0 ? Color.WHITE : Color.BLACK);
      // teď písmenka
      g.setColor(Color.BLACK);

      g.drawString(popisek(metruOdZacatku), pixluOdZacatku, pocatekY - vyskaCarky - 3);
    }
    double metruOdZacatku = pocetDilku * metruNaDilek;
    int pixluOdZacatku =  offset +(int) (metruOdZacatku * pixluNaMetr);
    g.drawString(jednotka(metruOdZacatku), pixluOdZacatku, pocatekY - vyskaCarky - 3);

  }

  private String popisek(double d) {
    if (d >= 1000) d = d /1000;
    return Math.round(d) + "";
  }

  private String jednotka(double d) {
    if (d >= 1000) return "km"; else return "m";
  }

}
