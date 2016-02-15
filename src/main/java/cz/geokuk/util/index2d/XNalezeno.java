package cz.geokuk.util.index2d;


public class XNalezeno extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -1329207585553667294L;
  // TO není výjimka, ale my neumíme jinak končit
  Sheet<?> sheet;

  XNalezeno(Sheet<?> sheet) {
    super();
    this.sheet = sheet;
  }

}
