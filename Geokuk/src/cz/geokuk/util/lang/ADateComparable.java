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

  public boolean isLess            (ADate b);
  public boolean isLessOrEqual     (ADate b);
  public boolean isGreater         (ADate b);
  public boolean isGreaterOrEqual  (ADate b);
  public boolean isEqual           (ADate b);
  public boolean isNotEqual        (ADate b);
}
