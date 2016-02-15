package cz.geokuk.util.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * 
 */

/**
 * @author veverka
 *
 */
public class JAlfaColorChooser extends JColorChooser {

  /**
   * 
   */
  private static final long serialVersionUID = -1527032129709847190L;
  private JSlider slider;
  private JComponent original;


  /**
   * @param aInitialColor
   */
  public JAlfaColorChooser(Color aInitialColor) {
    super(new AlfaColorSelectionModel());
    getModel().setSelectedColorWithAlfa(aInitialColor);
    initComponents();
  }



  /**
   * @param aModel
   */
  public JAlfaColorChooser(AlfaColorSelectionModel aModel) {
    super(aModel);
    initComponents();
  }

  @Override
  public void setSelectionModel(ColorSelectionModel newModel) {
    AlfaColorSelectionModel model = (AlfaColorSelectionModel) newModel;
    super.setSelectionModel(model);
    registerListenerToModel();
    model.fireStateChanged();
  }

  @Override
  public AlfaColorSelectionModel getSelectionModel() {
    return (AlfaColorSelectionModel) super.getSelectionModel();
  }

  private AlfaColorSelectionModel getModel() {
    return getSelectionModel();
  }

  /**
   * 
   */
  private void initComponents() {
    //alfaModel.setAlfa(colorWithAlfa.getAlpha());
    original = getPreviewPanel();
    setPreviewPanel(new JPanel());
    slider = new JSlider(0, 255, getModel().getSelectedColor().getAlpha());
    slider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent aE) {
        int alfa = slider.getValue();
        System.out.println("SLIDER1: " + alfa);
        getModel().setAlfa(alfa);
        System.out.println("SLIDER2: " + alfa);
        original.setForeground(getModel().getSelectedColor());
        //System.out.println(slider.getValue());
      }
    });
    JPanel jPruh = new JPanel();
    jPruh.setLayout(new BoxLayout(jPruh, BoxLayout.LINE_AXIS));
    jPruh.add(new JLabel("Průhlednost: "));
    jPruh.add(slider);
    add(jPruh, BorderLayout.NORTH);
    registerListenerToModel();

  }



  private void registerListenerToModel() {
    getSelectionModel().addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent aE) {
        Color selectedColor = getModel().getSelectedColorWithAlfa();
        original.setForeground(selectedColor);
        int alpha = selectedColor.getAlpha();
        slider.setValue(alpha);
      }
    });
  }
}
