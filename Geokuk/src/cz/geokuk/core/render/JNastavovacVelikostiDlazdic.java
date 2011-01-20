package cz.geokuk.core.render;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.EKaType;

public class JNastavovacVelikostiDlazdic extends JPanel
implements AfterEventReceiverRegistrationInit  {

  private static final long serialVersionUID = -484273090975902036L;

  public final SpinnerNumberModel iModel = new SpinnerNumberModel(10, 10, 10000, 1);

  private RenderModel renderModel;


  private JSpinner jMaximalniVelikost;
  private JTextField jSkutecnaVelikost;
  private JTextField jPocetDlazdic;

  /**
   * 
   */
  public JNastavovacVelikostiDlazdic(String smer) {
    initComponents(smer);

  }

  /**
   * 
   */
  private void initComponents(String smer) {
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    jMaximalniVelikost = new JSpinner();
    jMaximalniVelikost.setModel(iModel);
    jSkutecnaVelikost = new JTextField();
    jSkutecnaVelikost.setColumns(5);
    jSkutecnaVelikost.setEditable(false);
    jPocetDlazdic = new JTextField();
    jPocetDlazdic.setColumns(5);
    jPocetDlazdic.setEditable(false);

    add(jMaximalniVelikost);
    add(Box.createHorizontalStrut(5));
    add(jSkutecnaVelikost);
    add(Box.createHorizontalStrut(5));
    add(jPocetDlazdic);
    add(Box.createHorizontalStrut(5));
    add(new JLabel("(maximum / aktuálně / počet)"));

    jMaximalniVelikost.setToolTipText("Maximální veliksot dlaždice v pixlech ve směru " + smer + ".");
    jSkutecnaVelikost.setToolTipText("Skutečně rendrovaná velikost dlaždice ve směru " + smer + ", není větší než velikost aktuální.");
    jPocetDlazdic.setToolTipText("Počet rendrovaných dlaždic ve směru " + smer + ".");

  }

  public void setMetrika(DlazdicovaMetrika metrika) {
    jSkutecnaVelikost.setText(metrika.dlaSize + "");
    jPocetDlazdic.setText(metrika.dlaPocet + "");
  }

  public void onEvent(ZmenaMapNastalaEvent event) {
    EKaType podklad = event.getKaSet().getPodklad();
    iModel.setMinimum(podklad.getMinMoumer());
    iModel.setMaximum(podklad.getMaxMoumer());
    iModel.setValue(podklad.fitMoumer((Integer)iModel.getValue()));
  }

  public void onEvent(PripravaRendrovaniEvent event) {
    iModel.setValue(event.getRenderSettings().getRenderedMoumer());
  }


  @Override
  public void initAfterEventReceiverRegistration() {
    registerEvents();
  }

  public void addChangeListener(ChangeListener listener) {
    jMaximalniVelikost.addChangeListener(listener);
  }

  /**
   * 
   */
  private void registerEvents() {
    iModel.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        RenderSettings settings = renderModel.getRenderSettings();
        settings.setKmzMaxDlazdiceX((Integer) iModel.getNumber());
        renderModel.setRenderSettings(settings);
      }
    });
  }

  public void inject(RenderModel renderModel) {
    this.renderModel = renderModel;

  }

  /**
   * @return
   */
  public Integer getMaximalniVelikost() {
    return (Integer) iModel.getValue();
  }

  /**
   * @param kmzMaxDlazdiceX
   */
  public void setMaximalniVelikost(int kmzMaxDlazdice) {
    iModel.setValue(kmzMaxDlazdice);
  }


}