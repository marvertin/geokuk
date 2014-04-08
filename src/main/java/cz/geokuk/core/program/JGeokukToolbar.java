/**
 * 
 */
package cz.geokuk.core.program;

import java.awt.FlowLayout;

import javax.swing.JToolBar;

import cz.geokuk.framework.Factory;
import cz.geokuk.plugins.kesoid.mapicon.JToolbarOvladaceAlel;

/**
 * @author veverka
 *
 */
public class JGeokukToolbar extends JToolBar {


  private static final long serialVersionUID = -1467720799951484011L;
  private Factory factory;


  /**
   * 
   */
  public JGeokukToolbar() {
    setRollover(true);
  }

  public void addOvladaceAlel() {

    JToolbarOvladaceAlel jToolbarOvladaceAlel = factory.init(new JToolbarOvladaceAlel(this));
    jToolbarOvladaceAlel.setLayout(new FlowLayout(FlowLayout.LEFT ,0, 0));
    //jToolbarOvladaceAlel.setBackground(Color.MAGENTA);
    add(jToolbarOvladaceAlel);
  }


  public void inject(Factory factory) {
    this.factory = factory;
  }


}
