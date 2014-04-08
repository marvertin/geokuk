package cz.geokuk.plugins.kesoid.detail;


import javax.swing.Box;
import javax.swing.JLabel;

import cz.geokuk.plugins.kesoid.CzechGeodeticPoint;
import cz.geokuk.plugins.kesoid.Wpt;



/**
 * Detailní informace o vybrané keši.
 * @author Spikodrob
 *
 */
public class JCgpDetail extends JKesoidDetail0 {

  /**
   * 
   */
  private static final long serialVersionUID = -3323887260932949747L;

  private CzechGeodeticPoint cgp;

  private JLabel jXjtsk;
  private JLabel jYjtsk;
  
  public JCgpDetail() {
    initComponents();
  }


  private void initComponents() {
  	jXjtsk = new JLabel();
  	jYjtsk = new JLabel();

    Box hlav = Box.createVerticalBox();
    add(hlav);
    

    Box box2 = Box.createHorizontalBox();
    box2.add(new JLabel("y = "));
    box2.add(jYjtsk);
    box2.add(Box.createHorizontalStrut(20));
    box2.add(new JLabel("x = "));
    box2.add(jXjtsk);
    hlav.add(box2);
    
    
//
//
//    Box pan4b = Box.createVerticalBox();
//
//    box2.add(Box.createHorizontalGlue());
//
//    //  pan4.setAlignmentX(RIGHT_ALIGNMENT);
//    box2.add(pan4b);
//
//
//    hlav.add(box2);
//
//    Box box3 = Box.createHorizontalBox();
//    box3.add(Box.createGlue());
//    hlav.add(box3);
  }


  @Override
  public void napln(Wpt wpt) {
		cgp = (CzechGeodeticPoint)wpt.getKesoid();
		jXjtsk.setText(cgp.getXjtsk() + "");
		jYjtsk.setText(cgp.getYjtsk() + "");
  }
  

}
