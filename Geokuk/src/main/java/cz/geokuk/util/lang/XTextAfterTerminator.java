package cz.geokuk.util.lang;

/**Za ukončovacím podřetězcem textu byl nalezen text.
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003 TurboConsult s.r.o.</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class XTextAfterTerminator extends XObject0 {

  /**
   * 
   */
  private static final long serialVersionUID = -4460304424159588081L;

  public XTextAfterTerminator(String s) {
    super(s);
  }

  public XTextAfterTerminator(String s, Exception e)
  {
      super(s,e);
  }

  public XTextAfterTerminator(Object trida, String s)
  {
      super(trida, s);
  }

  public XTextAfterTerminator(Object trida, String s, Exception e)
  {
      super(trida, s,e);
  }
}