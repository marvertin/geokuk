package cz.geokuk.plugins.kesoid.mvc;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;

public class JVybiracHodnoceni extends JSpinner implements AfterEventReceiverRegistrationInit {

  private static final long serialVersionUID = 2417664157609045262L;

  public final SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 100, 1);
  private KesoidModel kesoidModel;


  public JVybiracHodnoceni() {
    setModel(model);
    setMaximumSize(new Dimension(35, 22));
    setToolTipText("Filte dlr prahu hodnocení keší na geocaching.cz, zobrazí se jen kedše mající hodnocení větší nebo rovné prahu.");
  }

  public void onEvent(KeskyNactenyEvent aEvent) {
    int maximalniHodnoceni = aEvent.getVsechny().getMaximalniHodnoceni();
    model.setMinimum(0);
    model.setMaximum(maximalniHodnoceni);
    model.setValue(Math.min((Integer) model.getNumber(), (Integer) model.getMaximum()));
    setEnabled(! model.getMinimum().equals(model.getMaximum()));
  }

  public void onEvent(FilterDefinitionChangedEvent event) {
    model.setValue(event.getFilterDefinition().getPrahHodnoceni());
  }

  @Override
  public void initAfterEventReceiverRegistration() {
    model.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        FilterDefinition definition = kesoidModel.getDefinition();
        definition.setPrahHodnoceni((Integer) model.getNumber());
        kesoidModel.setDefinition(definition);
      }
    });
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }


}