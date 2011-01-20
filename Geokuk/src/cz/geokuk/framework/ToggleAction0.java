package cz.geokuk.framework;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;


public abstract class ToggleAction0 extends Action0 {
  private static final long serialVersionUID = 3747754572841745541L;

  private ButtonModel bm; // = new DefaultButtonModel();
  private boolean iOnoff;

  public ToggleAction0(String name) {
    super(name);
  }

  /**
   * Přidá tuto modelo akci nějakému buttonu.
   * Z prvního buttonu bere model, do dalšího buttonu dává.
   * @param ab
   */
  public void join(AbstractButton ab) {
    if (bm == null) {
      bm = ab.getModel();
      nastavDoButtonu();
      registerEvents();
    } else {
      ab.setModel(bm);
    }
    ab.setAction(this);
    Icon icon = getIcon();
    if (icon != null) {
      ab.setIcon(icon);
    }
  }


  private void registerEvents() {
    bm.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        boolean onoff = event.getStateChange() == ItemEvent.SELECTED;
        if (iOnoff != onoff) {
          iOnoff = onoff;
          onSlectedChange(iOnoff);
        }
      }
    });
  }


  public void setSelected(boolean onoff) {
    this.iOnoff = onoff;
    nastavDoButtonu();
  }

  private void nastavDoButtonu() {
    if (bm != null) {
      bm.setSelected(iOnoff);
    }
  }
  
  public boolean isSelected() {
    return iOnoff;
  }

  protected abstract void onSlectedChange(boolean nastaveno);


  @Override
  public void actionPerformed(ActionEvent e) {
    // Neni potreba definovat, dela se to pres model 
  }


}
