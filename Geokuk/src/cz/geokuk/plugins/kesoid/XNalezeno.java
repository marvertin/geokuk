package cz.geokuk.plugins.kesoid;

import cz.geokuk.util.index2d.Sheet;

public class XNalezeno extends RuntimeException {

	/**
   * 
   */
  private static final long serialVersionUID = -1329207585553667294L;
	// TO není výjimka, ale my neumíme jinak končit
	Sheet<Wpt> swpt;

	XNalezeno(Sheet<Wpt> swpt) {
	  super();
	  this.swpt = swpt;
  }
	
}
