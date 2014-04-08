package cz.geokuk.plugins.kesoid.mvc;

import java.awt.Dimension;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;
import cz.geokuk.plugins.vylety.EVylet;

public class JVybiracVyletu extends JComboBox implements AfterEventReceiverRegistrationInit {

  private static final long serialVersionUID = 1L;
  private final DefaultComboBoxModel model = new DefaultComboBoxModel(EVylet.values());
  private KesoidModel kesoidModel;

  public JVybiracVyletu() {
    setModel(model);
    setMaximumSize(new Dimension(100, 22));
    setToolTipText("Filtr dle zařazení keší do výletu. Vybírá se, zda se zobrazí všechny keše nebo všechny bez ignorovaných nebo jen vybrané");
  }

  public void onEvent(FilterDefinitionChangedEvent event) {
    model.setSelectedItem(event.getFilterDefinition().getPrahVyletu());
  }


  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    model.addListDataListener(new ListDataListener() {

      @Override
      public void intervalRemoved(ListDataEvent e) {
        nastavto();
      }

      @Override
      public void intervalAdded(ListDataEvent e) {
        nastavto();
      }

      @Override
      public void contentsChanged(ListDataEvent e) {
        nastavto();
      }

      private void nastavto() {
        FilterDefinition definition = kesoidModel.getDefinition();
        definition.setPrahVyletu((EVylet) model.getSelectedItem());
        kesoidModel.setDefinition(definition);
      }
    });
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }


}