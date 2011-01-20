package cz.geokuk.util.lang;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class Interval0 implements java.io.Serializable {
  private static final long serialVersionUID = -2036649164930416712L;
  private final boolean includeBounds;

  protected Interval0(boolean aIncludeBounds) {

    includeBounds = aIncludeBounds;
  }

  protected boolean includeBounds() {return includeBounds;}
}
