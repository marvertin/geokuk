/**
 * 
 */
package cz.geokuk.util.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;

import javax.swing.colorchooser.DefaultColorSelectionModel;

public class AlfaColorSelectionModel extends DefaultColorSelectionModel {

    private static final Logger log =
            LogManager.getLogger(AlfaColorSelectionModel.class.getSimpleName());
  private static final long serialVersionUID = -1718047742587104573L;
  private int alfa;

  @Override
  public void setSelectedColor(Color color) {
    // musíme pžidávat alfu zevnitř, protože takto nám volá model bez alfy
    // color chooser
    super.setSelectedColor(new Color((color.getRGB() & 0xFFFFFF) | (alfa << 24), true));
  }

  /* (non-Javadoc)
   * @see javax.swing.colorchooser.DefaultColorSelectionModel#getSelectedColor()
   */
  @Override
  public Color getSelectedColor() {
    //Color color = new Color(super.getSelectedColor().getRGB() & 0xFFFFFF | (alfa << 24));
    //    //color = super.getSelectedColor();
    Color color = getSelectedColorWithAlfa();
    return color;
  }

  public void setSelectedColorWithAlfa(Color color) {
    int alfaOld = alfa;
    alfa = color.getAlpha();
    log.debug("MODEL-setSelectedColorWithAlfa: " + alfa);
    Color colorBezAlfy = new Color(color.getRGB() & 0xFFFFFF);
    boolean barvaBezAlfyZustava = super.getSelectedColor().equals(colorBezAlfy);
    super.setSelectedColor(colorBezAlfy); // nastavit bez alfy, čímže jsou notifikovány lsitenery, pokud změna
    if (barvaBezAlfyZustava && alfaOld != alfa) {
      fireStateChanged();
    }
  }

  public Color getSelectedColorWithAlfa() {
      log.debug("MODEL-getSelectedColorWithAlfa1: " + alfa);
    Color color = new Color(super.getSelectedColor().getRGB() & 0xFFFFFF | (alfa << 24), true);
      log.debug("MODEL-getSelectedColorWithAlfa2: " + color + color.getAlpha() + " " + alfa);
    return color;
  }

  /**
   * @return the alfa
   */
  public int getAlfa() {
      log.debug("MODEL-getAlfa: " + alfa);
    return alfa;
  }

  public void setAlfa(int aAlfa) {
    if (alfa == aAlfa) return;
    alfa = aAlfa;
      log.debug("MODEL-setAlfa: " + alfa);
    fireStateChanged();
  }


  @Override
  public void fireStateChanged() {
    super.fireStateChanged();
  }




}