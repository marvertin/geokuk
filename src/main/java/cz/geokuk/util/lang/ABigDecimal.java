/*
 * Created on 13.7.2005
 */
package cz.geokuk.util.lang;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Základní datový typ pro množství (snadnější práce než s BigDecimal).
 * Dále v textu zmiňovaný pojem scale znamená počet desetinných míst.
 * 
 * Pozor, na rozdíl od BigDecimal, metoda equals vrátí 
 * true pro hodnoty '10.00' a '10.0000'.
 * 
 * Příklad zaokrouhlení na 10 nahoru: <PRE>
ABigDecimal vstup = ABigDecimal.from(13.65);
ABigDecimal vysledek = vstup.divide(10, 0, ABigDecimal.ERoundType.ROUND_UP).multiply(10);
</PRE>
 * 
 * @author polakm
 */
public class ABigDecimal extends AObject0 implements Serializable, IAtomBigDecimal, Comparable<ABigDecimal> {

  private static final long serialVersionUID = 8327121768181232919L;
  
  public enum ERoundType  {
 
    ROUND_CEILING (BigDecimal.ROUND_CEILING), //Rounding mode to round towards positive infinity.
    ROUND_DOWN (BigDecimal.ROUND_DOWN), //Rounding mode to round towards zero.
    ROUND_FLOOR (BigDecimal.ROUND_FLOOR), //Rounding mode to round towards negative infinity.
    ROUND_HALF_DOWN (BigDecimal.ROUND_HALF_DOWN),
    //Rounding mode to round towards "nearest neighbor" unless both neighbors are equidistant, in which case round down.
    ROUND_HALF_EVEN (BigDecimal.ROUND_HALF_EVEN),
    //Rounding mode to round towards the "nearest neighbor" unless both neighbors are equidistant, in which case, round towards the even neighbor
    ROUND_HALF_UP (BigDecimal.ROUND_HALF_UP),
    //Rounding mode to round towards "nearest neighbor" unless both neighbors are equidistant, in which case round up.
    ROUND_UNNECESSARY (BigDecimal.ROUND_UNNECESSARY),
    //Rounding mode to assert that the requested operation has an exact result, hence no rounding is necessary.
    ROUND_UP (BigDecimal.ROUND_UP); //Rounding mode to round away from zero.

    private final int iRoundType;

    ERoundType(int aRoundType) {
      iRoundType = aRoundType;
    }

  }

  private final BigDecimal iValue;

  private ABigDecimal(BigDecimal aValue) {

    if (aValue == null)
      throw new NullPointerException("new ABigDecimal(null) is prohibited !");
    iValue = aValue;
  }

  /**
   * Vytvoří instanci ABigDecimal z BigDecimal, nastaví drženému BigDecimalu scale na 2 (při nutnosti zaokrouhlení použito ROUND_HALF_UP).
   * @param BigDecimal 
   * @return ABigDecimal   
   */
  public static ABigDecimal from(BigDecimal aValue) {
    return fromBigDecimal(aValue, 2);
  }

  /**
     * Vytvoří instanci ABigDecimal z BigDecimal, drženému BigDecimalu scale nenastavuje.
     * @param BigDecimal 
     * @return ABigDecimal     
     */
  public static ABigDecimal fromBigDecimal(BigDecimal aValue) {
    if (aValue == null)
      return null;
    return new ABigDecimal(aValue);
  }

  /**
       * Vytvoří instanci ABigDecimal z BigDecimal, drženému BigDecimalu nastaví scale podle parametru (při nutnosti zaokrouhlení použito ROUND_HALF_UP).
       * @param BigDecimal
       * @param int scale 
       * @return ABigDecimal
       */
  public static ABigDecimal fromBigDecimal(BigDecimal aValue, int aScale) {
    return fromBigDecimal(aValue, aScale, ERoundType.ROUND_HALF_UP);
  }

  /**
       * Vytvoří instanci ABigDecimal z BigDecimal, drženému BigDecimalu nastaví scale podle parametru (při nutnosti zaokrouhlení použito ROUND_HALF_UP).
       * @param BigDecimal
       * @param int scale 
       * @return ABigDecimal
       */
  public static ABigDecimal fromABigDecimal(ABigDecimal aValue, int aScale) {

    if (aValue == null)
      return null;
    return fromBigDecimal(aValue.toBigDecimal(), aScale);
  }

  /**
         * Vytvoří instanci ABigDecimal z BigDecimal, drženému BigDecimalu nastaví scale podle parametru.
         * @param BigDecimal
         * @param int scale
         * @param ERoundType aRoundMethod
         * @return ABigDecimal         
         */
  public static ABigDecimal fromBigDecimal(BigDecimal aValue, int aScale, ERoundType aRoundMethod) {

    if (aValue == null)
      return null;
    return new ABigDecimal(aValue.setScale(aScale, aRoundMethod.iRoundType));
  }

  /**
           * Vytvoří instanci ABigDecimal z BigDecimal, drženému BigDecimalu nastaví scale podle parametru.
           * @param BigDecimal
           * @param int scale
           * @param ERoundType aRoundMethod
           * @return ABigDecimal         
           */
    public static ABigDecimal fromABigDecimal(ABigDecimal aValue, int aScale, ERoundType aRoundMethod) {

      if (aValue == null) return null;
      return fromBigDecimal(aValue.toBigDecimal(), aScale, aRoundMethod);
    }
    
  /**
   * Vytvoří instanci ABigDecimal z double, nastaví drženému BigDecimalu scale na 2 (při nutnosti zaokrouhlení použito ROUND_HALF_UP).
   * @param double 
   * @return ABigDecimal 
   */
  public static ABigDecimal from(double aValue) {
    return from(new BigDecimal(aValue));
  }

  /**
     * Vytvoří instanci ABigDecimal z double, drženému BigDecimalu scale nenastaví.
     * @param double 
     * @return ABigDecimal 
     */
  public static ABigDecimal fromDouble(double aValue) {
    return fromBigDecimal(new BigDecimal(aValue));
  }

  /**
     * Vytvoří instanci ABigDecimal z double, nastaví drženému BigDecimalu scale podle parametru (při nutnosti zaokrouhlení použito ROUND_HALF_UP).
     * @param double
     * @param int scale
     * @return ABigDecimal 
     */
  public static ABigDecimal fromDouble(double aValue, int aScale) {
    return fromBigDecimal(new BigDecimal(aValue), aScale);
  }

  /**
     * Vytvoří instanci ABigDecimal z double, nastaví drženému BigDecimalu scale podle parametru.
     * @param double
     * @param int scale
     * @param ERoundType aRoundMethod
     * @return ABigDecimal 
     */
  public static ABigDecimal fromDouble(double aValue, int aScale, ERoundType aRoundMethod) {
    return fromBigDecimal(new BigDecimal(aValue), aScale, aRoundMethod);
  }

  /**
   * Vrátí true, pokud lze sestrojit.
   * @param aValue
   * @return
   */
  public static boolean canFrom(BigDecimal aValue) {
    try {

      from(aValue);
      return true;
    } catch (Exception e) {

      return false;
    }
  }

  /**
   * Vrátí true, pokud lze sestrojit.
   * @param aValue
   * @return
   */
  public static boolean canFrom(double aValue) {
    try {

      from(aValue);
      return true;
    } catch (Exception e) {

      return false;
    }
  }

  /**
   * Vytvoří instanci ABigDecimal ze Stringu, nastaví drženému BigDecimalu scale na 2.
   * @param String 
   * @return ABigDecimal
   */
  public static ABigDecimal from(String aValue) {
    
    if (aValue == null) { return null; }
    
    // LATER Následující příkaz slouží k tomu, aby jako desetinný oddělovač fungovala
    // též čárka.
    // Nicméně toto není správné řešení. Obecně by totiž někde mělo být zajištěno,
    // aby se převod mezi stringem a "ABigDecimal" prováděl dle aktuálního locale.
    // Tento převod by možná měl být někde jinde, než v metodě "ABigDecimal.from"
    // - např. v nějakém formátovači.
    // Pokoušel jsem se nějaký formátovač najít v java-core-api.
    // Našel jsem např. formátovač "java.text.DecimalFormater",
    // ale nejsem si zcela jist, zda-li je to právě to, co by nám pomohlo.
    // Chtělo by to prozkoumat.
    // --kalendov, 2006-09-09
    aValue = aValue.replace(',','.');
    
    return from(new BigDecimal(aValue));
  }

  /**
   * Vytvoří instanci ABigDecimal z <code>aValue</code>. 
   * Scale se nastaví automaticky dle pozice desetinného oddělovače ve stringu <code>aValue</code>.
   */
  public static ABigDecimal fromWithAutomaticScaleSet(String aValue) {
    
    if (aValue == null) { return null; }
    
    // LATER Následující příkaz slouží k tomu, aby jako desetinný oddělovač fungovala
    // též čárka.
    // Nicméně toto není správné řešení. Obecně by totiž někde mělo být zajištěno,
    // aby se převod mezi stringem a "ABigDecimal" prováděl dle aktuálního locale.
    // Tento převod by možná měl být někde jinde, než v metodě "ABigDecimal.from"
    // - např. v nějakém formátovači.
    // Pokoušel jsem se nějaký formátovač najít v java-core-api.
    // Našel jsem např. formátovač "java.text.DecimalFormater",
    // ale nejsem si zcela jist, zda-li je to právě to, co by nám pomohlo.
    // Chtělo by to prozkoumat.
    // --kalendov, 2006-09-09
    aValue = aValue.replace(',','.');
    
    return fromBigDecimal(new BigDecimal(aValue));
  }
  

  /**
   * Zjistí, zda lze vytvořit z dané hodnoty.
   * @param aValue
   * @return
   */
  public static boolean canFrom(String aValue) {
    try {
      from(aValue);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Vrátí true pokud se jeho hodnota rovná hodnotě aObj.  
   */
  public boolean equals(Object aObj) {
    if (!(aObj instanceof ABigDecimal))
      return false;
    //return iValue.equals(((ABigDecimal) aObj).iValue);
    return iValue.compareTo(((ABigDecimal) aObj).iValue) == 0;
  }

  /** 
   * Vrátí jako řetězec.
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return iValue.toString();
  }

  /**
   * Vrátí jako double.
   * Použij tuto metodu s rozmyslem, pouze v odůvodněných případech. Většinou se k práci s částkou nehodí - raději použij toBigDecimal(). 
   * @return
   */
  public double toDouble() {
    return iValue.doubleValue();
  }

  /**
   * Vrátí jako BigDecimal.
   * @return
   */
  public final BigDecimal toBigDecimal() {
    return iValue;
  }

  /** 
   * Vrátí vlastní číslo jako heškód.
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return iValue.hashCode();
  }

  /**
   * 
   * @return
   * @deprecated Opravdu chceš k částce přistupovat jako k double ? Použij toBigDecimal(). Pokud skutečně chceš double, pak použij toDouble().
   */
  public double value() {
    return toDouble();
  }

  private void _checkArgument(Object o) {

    if (o == null)
      throw new IllegalArgumentException("Null value prohibited!");
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(ABigDecimal o) {

    if (o == null)
      throw new IllegalArgumentException("Comparing with null is prohibited!");
    ABigDecimal a = o;
    return iValue.compareTo(a.iValue);
  }

  public boolean isLess(ABigDecimal a) {

    return compareTo(a) < 0;
  }

  public boolean isLessOrEqual(ABigDecimal a) {

    return compareTo(a) <= 0;
  }

  public boolean isGreater(ABigDecimal a) {

    return compareTo(a) > 0;
  }

  public boolean isGreaterOrEqual(ABigDecimal a) {

    return compareTo(a) >= 0;
  }

  public static final ABigDecimal ZERO = ABigDecimal.from(0);

  public static boolean isLessThanZero(ABigDecimal ABigDecimal) {

    return ZERO.compareTo(ABigDecimal) > 0;
  }

  public static boolean isLessOrEqualToZero(ABigDecimal ABigDecimal) {

    return ZERO.compareTo(ABigDecimal) >= 0;
  }

  public static boolean isGreaterThanZero(ABigDecimal ABigDecimal) {

    return ZERO.compareTo(ABigDecimal) < 0;
  }

  public static boolean isGreaterOrEqualToZero(ABigDecimal ABigDecimal) {

    return ZERO.compareTo(ABigDecimal) <= 0;
  }

  public static boolean isEqualToZero(ABigDecimal ABigDecimal) {

    return ZERO.compareTo(ABigDecimal) == 0;
  }

  /**
   * Instanci podělí zadaným číslem a typ zaokrouhlení je BigDecimal.ROUND_FLOOR (tak jak známe ze školy, zaokrouhlení dolů, od pětky včetně nahoru), ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aDivider
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal divideRoundHalfUp(BigDecimal aDivider) {

    _checkArgument(aDivider);
    BigDecimal result = iValue.divide(aDivider, BigDecimal.ROUND_HALF_UP);
    return ABigDecimal.fromBigDecimal(result);
  }

  /**
   * Instanci podělí zadaným číslem a typ zaokrouhlení je BigDecimal.ROUND_FLOOR (tak jak známe ze školy, zaokrouhlení dolů, od pětky včetně nahoru), ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aDivider
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal divideRoundHalfUp(ABigDecimal aDivider) {
    
    _checkArgument(aDivider);
    return divideRoundHalfUp(aDivider.toBigDecimal());
  }
  
  /**
   * Instanci podělí zadaným číslem a typ zaokrouhlení je BigDecimal.ROUND_FLOOR (tak jak známe ze školy, zaokrouhlení dolů, od pětky včetně nahoru), ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * @param aDivider
   * @param aScale
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal divideRoundHalfUp(BigDecimal aDivider, int aScale) {

    _checkArgument(aDivider);
    BigDecimal result = iValue.divide(aDivider, aScale, BigDecimal.ROUND_HALF_UP);
    return ABigDecimal.fromBigDecimal(result);
  }

  /**
     * Instanci podělí zadaným číslem a typ zaokrouhlení je BigDecimal.ROUND_FLOOR (tak jak známe ze školy, zaokrouhlení dolů, od pětky včetně nahoru), ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
     * @param aDivider
     * @param aScale
     * @return nová instance ABigDecimal
     */
    public final ABigDecimal divideRoundHalfUp(ABigDecimal aDivider, int aScale) {

      _checkArgument(aDivider);
      return divideRoundHalfUp(aDivider.toBigDecimal(), aScale);
    }
    
  /**
   * Instanci podělí zadaným číslem a typ zaokrouhlení je BigDecimal.ROUND_FLOOR (tak jak známe ze školy, zaokrouhlení dolů, od pětky včetně nahoru), ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aDivider
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal divideRoundHalfUp(double aDivider) {

    return divideRoundHalfUp(new BigDecimal(aDivider));
  }

  /**
   * Instanci podělí zadaným číslem a typ zaokrouhlení je BigDecimal.ROUND_FLOOR (tak jak známe ze školy, zaokrouhlení dolů, od pětky včetně nahoru), ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * @param aDivider
   * @param aScale
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal divideRoundHalfUp(double aDivider, int aScale) {

    return divideRoundHalfUp(new BigDecimal(aDivider), aScale);
  }

  /**
     * Instanci podělí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
     * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
     * @param aDivider
     * @param ERoundType Typ zaokrouhlení (viz. BigDecimal)
     * @return nová instance ABigDecimal
     */
  public final ABigDecimal divide(BigDecimal aDivider, ERoundType aRoundType) {

    _checkArgument(aDivider);
    BigDecimal result = iValue.divide(aDivider, aRoundType.iRoundType);
    return ABigDecimal.fromBigDecimal(result);
  }

  /**
     * Instanci podělí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
     * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
     * @param aDivider
     * @param ERoundType Typ zaokrouhlení (viz. BigDecimal)
     * @return nová instance ABigDecimal
     */
  public final ABigDecimal divide(ABigDecimal aDivider, ERoundType aRoundType) {
    
    _checkArgument(aDivider);
    return divide(aDivider.toBigDecimal(), aRoundType);
  }
  
  /**
       * Instanci podělí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
       * @param aDivider
       * @param aScale
       * @param ERoundType Typ zaokrouhlení (viz. BigDecimal)
       * @return nová instance ABigDecimal
       */
  public final ABigDecimal divide(BigDecimal aDivider, int aScale, ERoundType aRoundType) {

    _checkArgument(aDivider);
    BigDecimal result = iValue.divide(aDivider, aScale, aRoundType.iRoundType);
    return ABigDecimal.fromBigDecimal(result);
  }

  /**
         * Instanci podělí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
         * @param aDivider
         * @param aScale
         * @param ERoundType Typ zaokrouhlení (viz. BigDecimal)
         * @return nová instance ABigDecimal
         */
    public final ABigDecimal divide(ABigDecimal aDivider, int aScale, ERoundType aRoundType) {

      _checkArgument(aDivider);
      return divide(aDivider.toBigDecimal(), aScale, aRoundType);
    }
    
  /**
     * Instanci podělí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
     * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
     * @param aDivider
     * @param ERoundType Typ zaokrouhlení (viz. BigDecimal)
     * @return nová instance ABigDecimal
     */
  public final ABigDecimal divide(double aDivider, ERoundType aRoundType) {

    return divide(new BigDecimal(aDivider), aRoundType);
  }

  /**
      * Instanci podělí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
      * @param aDivider
      * @param aScale
      * @param ERoundType Typ zaokrouhlení (viz. BigDecimal)
      * @return nová instance ABigDecimal
      */
  public final ABigDecimal divide(double aDivider, int aScale, ERoundType aRoundType) {

    return divide(new BigDecimal(aDivider), aScale, aRoundType);
  }

  /**
   * Instanci vynásobí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aDivider
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal multiply(BigDecimal aMultiplier) {

    _checkArgument(aMultiplier);
    BigDecimal result = iValue.multiply(aMultiplier);
    return ABigDecimal.fromBigDecimal(result);
  }

  /**
   * Instanci vynásobí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aDivider
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal multiply(ABigDecimal aMultiplier) {
    
    return multiply(aMultiplier.toBigDecimal());
  }
  
  /**
   * Instanci vynásobí zadaným číslem, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aDivider
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal multiply(double aMultiplier) {

    return multiply(new BigDecimal(aMultiplier));
  }

  /**
   * Instanci přičte zadanou částku, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aNumber
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal add(BigDecimal aNumber) {

    _checkArgument(aNumber);
    return _add(aNumber);
  }

  /**
   * Instanci přičte zadanou částku, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aNumber
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal add(ABigDecimal aNumber) {

    _checkArgument(aNumber);
    return _add(aNumber.toBigDecimal());
  }

  /**
   * Instanci přičte zadané číslo, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aNumber
   * @return nová instance ABigDecimal
   */
  private final ABigDecimal _add(BigDecimal aNumber) {
    BigDecimal result = iValue.add(aNumber);
    return ABigDecimal.fromBigDecimal(result);
  }

  /**
   * Instanci přičte zadané číslo, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aNumber
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal add(double aNumber) {
    return _add(new BigDecimal(aNumber));
  }
  
  /**
     * Instanci odečte zadanou částku, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
     * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
     * @param aNumber
     * @return nová instance ABigDecimal
     */
  public final ABigDecimal subtract(BigDecimal aNumber) {
    _checkArgument(aNumber);
    return _subtract(aNumber);
  }

  /**
     * Instanci odečte zadanou částku, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
     * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
     * @param aNumber
     * @return nová instance ABigDecimal
     */
  public final ABigDecimal subtract(ABigDecimal aNumber) {
    _checkArgument(aNumber);
    return _subtract(aNumber.toBigDecimal());
  }

  /**
   * Instanci odečte zadané číslo, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aNumber
   * @return nová instance ABigDecimal
   */
  private final ABigDecimal _subtract(BigDecimal aNumber) {
    BigDecimal result = iValue.subtract(aNumber);
    return ABigDecimal.fromBigDecimal(result);
  }

  /**
   * Instanci odečte zadané číslo, ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * Pozor, výsledek má stejný scale jako původní ABigDecimal ! Pokud by toto zaokrouhlování bylo nežádoucí, naprogramuj si to sám pomocí BigDecimal.
   * @param aNumber
   * @return nová instance ABigDecimal
   */
  public final ABigDecimal subtract(double aNumber) {
    return _subtract(new BigDecimal(aNumber));
  }

  /**
   * Vrátí číslo opačné (-this).
   * @return
   */
  public final ABigDecimal negate() {
    return ABigDecimal.fromBigDecimal(iValue.negate());
  }

  /**
   * Vrátí menší číslo.
   * @param a
   * @return
   */
  public final ABigDecimal min(ABigDecimal a) {

    return (compareTo(a) < 0) ? this : a;
  }

  /**
     * Vrátí větší číslo.
     * @param a
     * @return
     */
  public final ABigDecimal max(ABigDecimal a) {
    return (compareTo(a) > 0) ? this : a;
  }

  /**
   * Provede zaokrouhlení na daný počet desetinných míst (více podrobností viz. BigDecimal.setScale), ABigDecimal je immutable, takže se nezmění, ale je vytvořen nový ABigDecimal s výsledkem.
   * @param aScale Počet desetinných míst
   * @param ERoundType aRoundMode Zaokrouhlovací metoda (viz. BigDecimal konstanty)
   * @return
   */
  public final ABigDecimal round(int aScale, ERoundType aRoundMode) {
    return ABigDecimal.fromBigDecimal(iValue.setScale(aScale, aRoundMode.iRoundType));
  }

  /**
   * Vrátí absolutní hodnotu.
   * @return
   */
  public final ABigDecimal abs() {
    return (isLessThanZero(this)) ? this.negate() : this;
  }

  /**
   * @see BigDecimal#scale()
   */
  public int getScale() {
    return iValue.scale();
  }

  /**
   * @see BigDecimal#precision()
   */
  public int getPrecision() {
    return iValue.precision();
  }
  
  /**
   * Máme to zde, aby se atomický typ choval jako javabean a dal se použít v JSP
   * @return
   */
  public BigDecimal getValue() {
    return toBigDecimal();
  }
}
