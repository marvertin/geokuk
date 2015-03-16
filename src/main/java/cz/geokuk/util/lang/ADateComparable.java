/*
 * Created on 17.5.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cz.geokuk.util.lang;

/**
 * @author polakm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface ADateComparable {

  boolean isLess            (ADate b);
  boolean isLessOrEqual     (ADate b);
  boolean isGreater         (ADate b);
  boolean isGreaterOrEqual  (ADate b);
}
