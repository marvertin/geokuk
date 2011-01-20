package cz.geokuk.util.lang;

import java.lang.reflect.Method;


/**
 * @author  <a href="mailto:?????.?????@turboconsult.cz">????? ?????</a>
 * @version $Revision: 3 $
 * @see     "TW####??????.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/ATwLongBase.java 3     26.05.00 16:00 Veverka $"
 */
public abstract class AInt0 extends AObject0 implements IAtomInt, Comparable<AInt0>
{
  private static final long serialVersionUID = -4485641313971435401L;

  /**Hodnota
   */
  private int iValue;

  /**Kontrola rozsahu.
   */
  protected AInt0(int aValue)
  {
    iValue = aValue;
    checkRange();
    validate();
  }

  /**
   * @param aValue
   */
  protected AInt0(String aValue) {
    try {
      iValue = Integer.parseInt(aValue.trim());
      checkRange();
      validate();
    } catch (Exception e) {
      throw new RuntimeException("Illegal value \"" + aValue + "\" for type \"" + getClass().getName() + "\"") ;
    }
  }

  /**Vrací se jako integer.
   */
  public int toInt()
  {
    return iValue;
  }

  /**Vrací se jako string.
   */
  public String toString()
  {
    return Integer.toString(iValue);
  }


  /**Minimální povolená hodnota. Následník může přepsat, pokud má být minimální hodnota jiná.
   * Předefinovaná metoda by měla pokud možno vracet konstantu.
   */
  protected int minValue()
  {
    return Integer.MIN_VALUE;
  }

  /**Maximální povolená hodnota. Následník může přepsat, pokud má být maximální hodnota jiná.
   * Předefinovaná metoda by měla pokud možno vracet konstantu.
   */
  protected int maxValue()
  {
    return Integer.MAX_VALUE;
  }

  /**Kontrola rozsahu. Metoda se volá z konstruktoru a kontroluje rozsah.
   */
  protected final void checkRange()
  {
    if (! (minValue() <= iValue && iValue <= maxValue()))
      throw new XCreateElement("Hodnota " + iValue + " není v požadovaném intervalu <"
          + minValue() + "," + maxValue() + ">");
  }

  /**Porovnání dvou elementárních typů.
   */
  public boolean equals(Object aObject)
  {
    if (!(aObject instanceof AInt0)) return false;
    AInt0 obj = (AInt0)aObject;
    return iValue == obj.iValue;
  }


  /**Heš kód.
   */
  public int hashCode()
  {
    return iValue;
  }


  /** Kontrola platnosti typu. Metoda je volána z kosntruktoru poté, co byla provedena validace rozsahu dély řetězce.
   * V okamžiku volání je zřejmé, že hodnota iValue je nastavena a je ve stanoveném rozsahu.
   * Metoda v případě nevalidity vyhazuje nečekovanou výjimku.
   * Neplést s metodou isValid(), to je jiná validita (menší váha).
   */
  protected void validate() {
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(AInt0 aO) {
    return iValue - aO.iValue;
  }

  /**
   * Továrna na atomické typy vracející typ, jenž byl zadaný
   * @param <T>
   * @param x
   * @param aClass
   * @return
   */
  public static <T extends AInt0> T from(Integer x, Class<T> aClass) {
    try {
      Method method = aClass.getMethod("from", Integer.class);
      if (method.getReturnType() != aClass) {
        throw new RuntimeException("Method 'from' must be declared to return " + aClass + ", but returns " + method.getReturnType());
      }
      Object resulto = method.invoke(null, x);
      @SuppressWarnings("unchecked")
      T result = (T) resulto;
      return result;
    } catch (Exception e) {
      throw new RuntimeException("Cannot create type " + aClass + " from constant value"  + x, e);
    }
  }
}
