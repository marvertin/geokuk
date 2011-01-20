package cz.geokuk.plugins.kesoid.mvc;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;

public class JVybiracBestOf extends JSpinner implements AfterEventReceiverRegistrationInit  {

  private static final long serialVersionUID = -484273090975902036L;
  
  public final SpinnerNumberModel iModel = new SpinnerNumberModel(0, 0, 100, 1);

  private KesoidModel kesoidModel;

  /**
   * 
   */
  public JVybiracBestOf() {
    setModel(iModel);
    setMaximumSize(new Dimension(35, 22));
  }

  public void onEvent(KeskyNactenyEvent aEvent) {
    int maximalniBestOf = aEvent.getVsechny().getMaximalniBestOf();
    iModel.setMinimum(0);
    iModel.setMaximum(maximalniBestOf);
    iModel.setValue(Math.min((Integer) iModel.getNumber(),
        maximalniBestOf));
    setEnabled(! iModel.getMinimum().equals(iModel.getMaximum()));
  }

  public void onEvent(FilterDefinitionChangedEvent event) {
    iModel.setValue(event.getFilterDefinition().getPrahBestOf());
  }

  @Override
  public void initAfterEventReceiverRegistration() {
    iModel.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        FilterDefinition definition = kesoidModel.getDefinition();
        definition.setPrahBestOf((Integer) iModel.getNumber());
        kesoidModel.setDefinition(definition);
      }
    });
  }
  
  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }

  
}