package cz.geokuk.plugins.mrizky;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.MultiInjection;



/**
 * @author veverka
 *
 */
public class ZhasniVsechnyMrizkyAction extends Action0 {

  private static final long serialVersionUID = -8054017274338240706L;
  private List<MrizkaModel> mrizkaModels = new ArrayList<MrizkaModel>();

  /**
   * 
   */
  public ZhasniVsechnyMrizkyAction() {
    super("Zhasni mřížky");
    putValue(SHORT_DESCRIPTION, "Zhasni všechny rozsvícené mřížky.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  
  @Override
  public void actionPerformed(ActionEvent e) {
    for (MrizkaModel mrizkaModel : mrizkaModels) {
      mrizkaModel.setOnoff(false);
    }
  }

  @MultiInjection
  public void inject(MrizkaModel mrizkaModel) {
    if (! "Meritkovnik".equals(mrizkaModel.getSubType())) {
      this.mrizkaModels.add(mrizkaModel);  // jenom přížky se zhasínají
    }
  }
  

}
