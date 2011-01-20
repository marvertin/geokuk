package cz.geokuk.util.gui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import cz.geokuk.util.gui.SelectionModel.Item;

public class JMvComboBox<T> extends JComboBox {

  private static final long serialVersionUID = 3831515220850009660L;

  private SelectionModel<T> model = new SelectionModel<T>();

  public void setSelectionModel(final SelectionModel<T> model) {
    this.model = model;
    final DefaultComboBoxModel defmod = new DefaultComboBoxModel();
    setModel(defmod);
    List<Item<T>> items = model.items;
    for (Item<T> item : items) {
      defmod.addElement(item);
    }
    defmod.addListDataListener(new ListDataListener() {

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
        @SuppressWarnings("unchecked")
        Item<T> item =((Item<T>) defmod.getSelectedItem());
        model.setSelectedItem(item);
      }
    });
    model.addListener(new SelectionListener<T>() {
      @Override
      public void selectionChanged(SelectionEvent<T> event) {
        defmod.setSelectedItem(event.item);
      }
    });

  }


  public SelectionModel<T> getSelectionModel() {
    return model;
  }


  @Override
  public Dimension getMaximumSize() {
    return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
  }
}
