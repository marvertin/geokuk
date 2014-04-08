package cz.geokuk.util.lang;

  /**
   * <p>Title: </p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2003</p>
   * <p>Company: </p>
   * @author unascribed
   * @version 1.0
   */

  public class ADateInterval extends Interval0 implements java.io.Serializable {

    private  static final long serialVersionUID = 1261494997489495034L;

    private final ADateComparable lowerLimit;
    private final ADateComparable upperLimit;

    private ADateInterval(ADateComparable aLowerLimit, ADateComparable aUpperLimit, boolean aIncludeBounds) {

      super(aIncludeBounds);
      lowerLimit = (aLowerLimit != null) ? aLowerLimit : new ADateComparable(){

        public boolean isLess            (ADate b) {return false;}
        public boolean isLessOrEqual     (ADate b) {return false;}
        public boolean isGreater         (ADate b) {return true;}
        public boolean isGreaterOrEqual  (ADate b) {return true;}
        public boolean isEqual           (ADate b) {return false;}
        public boolean isNotEqual        (ADate b) {return true;}
      };
      upperLimit = (aUpperLimit != null) ? aUpperLimit : new ADateComparable(){

      public boolean isLess            (ADate b) {return true;}
      public boolean isLessOrEqual     (ADate b) {return true;}
      public boolean isGreater         (ADate b) {return false;}
      public boolean isGreaterOrEqual  (ADate b) {return false;}
      public boolean isEqual           (ADate b) {return false;}
      public boolean isNotEqual        (ADate b) {return true;}
    };
    }

    public static ADateInterval create(ADateComparable aLowerLimit, ADateComparable aUpperLimit) {

      return create(aLowerLimit, aUpperLimit, true);
    }

    public static ADateInterval create(ADateComparable aLowerLimit, ADateComparable aUpperLimit, boolean aIncludeBounds) {

      return new ADateInterval(aLowerLimit, aUpperLimit, aIncludeBounds);
    }

    public boolean isInInterval(ADate aValue) {

      if (includeBounds()) {

        return lowerLimit.isLessOrEqual(aValue) && upperLimit.isGreaterOrEqual(aValue);
      }
      else {

        return lowerLimit.isLess(aValue) && upperLimit.isGreater(aValue);
      }
    }

    public ADateComparable getLowerLimit() {return lowerLimit;}
    public ADateComparable getUpperLimit() {return upperLimit;}
}
