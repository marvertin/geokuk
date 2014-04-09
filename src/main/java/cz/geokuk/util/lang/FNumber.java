package cz.geokuk.util.lang;

/**Třída pracující s číselnými typy.
 * Unsigned převod mezi číselnými typy, bezznaménkový převod
 * Převod čísel do pole bytů, první byte je nejvyšší byte z původního čísla
 * @author Michal Polák
 * @version 1.01.02
 * created: 2000-02-21 20:35:00
 * modified¨: 2000-03-11 20:55:00
 */
public final class FNumber {

	/**Převede byte na short (bez znaménka)
	 * -32 převede na 224
	 */
	public static short unsignedByteToShort (byte base) {

		if (base < 0) {
            return (short) (0x100 + base);
        }
		else {
            return base;
        }
	}

	/**Převede byte na int (bez znaménka)
	 * -32 převede na 224
	 */
	public static int unsignedByteToInt (byte base) {

		if (base < 0) {
            return (0x100 + base);
        }
		else {
            return base;
        }
	}

	/**Převede short na int (bez znaménka)
	 */
	public static int unsignedShortToInt (short base) {

		if (base < 0) {
            return 0x10000 + base;
        }
		else {
            return base;
        }
	}

	/**Převede short na long (bez znaménka)
	 */
	public static long unsignedShortToLong (short base) {

		if (base < 0) {
            return (0x10000 + base);
        }
		else {
            return base;
        }
	}

	/**Převede int na long (bez znaménka)
	 */
	public static long unsignedIntToLong (int base) {

		if (base < 0) {
            return (0x100000000L + base);
        }
		else {
            return base;
        }
	}

	/**Převede short do pole bytů.
	 * @param short číslo k převedení
	 * @return byte[] Pole bytů čísla, první byte je nejvyšší byte původního čísla
	 */
	public static byte[] toByteArray (short n) {
		return new byte[] {(byte)(n >>> 8), (byte)(n & 0xFF)};
	}

	/**Převede pole bytů na short
	 * @param byte[] Pole bytů čísla, první byte je nejvyšší byte původního čísla
	 * @return short
	 */
	public static short shortFromByteArray (byte[] a) {

		return (short)((a[0] << 8) + unsignedByteToShort(a[1]));
	}

	/**Převede int do pole bytů.
	 * @param int číslo k převedení
	 * @return byte[] Pole bytů čísla, první byte je nejvyšší byte původního čísla
	 */
	public static byte[] toByteArray (int n) {

		return new byte[] {(byte)(n >>> 24), (byte)(n >>> 16), (byte)(n >>> 8), (byte)(n & 0xFF)};
	}

	/**Převede pole bytů na int
	 * @param byte[] Pole bytů čísla, první byte je nejvyšší byte původního čísla
	 * @return int
	 */
	public static int intFromByteArray (byte[] a) {

		return unsignedByteToInt(a[3]) + (a[2] << 8 & 0xFF00) + (a[1] << 16 & 0xFF0000) + (a[0] << 24 & 0xFF000000);
	}

	/**Převede long do pole bytů.
	 * @param long číslo k převedení
	 * @return byte[] Pole bytů čísla, první byte je nejvyšší byte původního čísla
	 */
	public static byte[] toByteArray (long n) {

		return new byte[] {(byte)(n >>> 56), (byte)(n >>> 48), (byte)(n >>> 40), (byte)(n >>> 32),
			(byte)(n >>> 24), (byte)(n >>> 16), (byte)(n >>> 8), (byte)(n & 0xFF)};
	}

	/**Převede pole bytů na long
	 * @param byte[] Pole bytů čísla, první byte je nejvyšší byte původního čísla
	 * @return long
	 */
	public static long longFromByteArray (byte[] a) {

		return (unsignedByteToInt(a[7]) + (a[6] << 8 & 0xFF00) +
		(a[5] << 16 & 0xFF0000) +				(a[4] << 24 & 0xFF000000) +
		((long)a[3] << 32 & 0xFF00000000L) +	((long)a[2] << 40 & 0xFF0000000000L) +
		((long)a[1] << 48 & 0xFF000000000000L) +((long)a[0] << 56 & 0xFF00000000000000L));
	}

	/**Zajistí, že hodnota bude mantinely.
	 * Pokud jsou mantinely přehozeny, je vyhozena výjimka.
	 * @param aCo Umísťovaná hodnota.
	 * @param aMin Minimální hodnota, kterou smívýsledek nabývat
	 * @param aMax Maximální hodntoa, kterou smí výsledek nabývat
	 * @return Hodnota aCo, pokud je v intervalu <aMin,aMax> nebo krajní mez intervalu, k níž je hodnota blíž.
	 */
	public static final long fit(long aCo, long aMin, long aMax)
	{
		if (aMax < aMin)
			throw new RuntimeException("Hodnotu " + aCo + " nelze umístit do intervalu <"+aMin+","+aMax+"> z důvodů překrývání mezí");
		return aCo < aMin ? aMin
						  : aCo > aMax ? aMax
									   : aCo;
	}

  /**
   * Bezpečně převede long na byte.
   * Pokud je předaný parameter mimo rozsah shortu, je vyhozena výjimka.
   * @param aCo Převáděné číslo
   * @return Číslo převedené na byte a pře vedené určitě správně.
   */
  public static final byte safeConvertToByte(long aCo) {
    if (aCo < Byte.MIN_VALUE || aCo > Byte.MAX_VALUE) throw new IllegalArgumentException("Value " + aCo + " is out of 'byte' baunderies");
    return (byte) aCo;
  }

  /**
   * Bezpečně převede int na byte.
   * Pokud je předaný parameter mimo rozsah shortu, je vyhozena výjimka.
   * @param aCo Převáděné číslo
   * @return Číslo převedené na byte a pře vedené určitě správně.
   */
  public static final byte safeConvertToByte(int aCo) {
    if (aCo < Byte.MIN_VALUE || aCo > Byte.MAX_VALUE) throw new IllegalArgumentException("Value " + aCo + " is out of 'byte' baunderies");
    return (byte) aCo;
  }

  /**
   * Bezpečně převede short na byte.
   * Pokud je předaný parameter mimo rozsah shortu, je vyhozena výjimka.
   * @param aCo Převáděné číslo
   * @return Číslo převedené na byte a pře vedené určitě správně.
   */
  public static final byte safeConvertToByte(short aCo) {
    if (aCo < Byte.MIN_VALUE || aCo > Byte.MAX_VALUE) throw new IllegalArgumentException("Value " + aCo + " is out of 'byte' baunderies");
    return (byte) aCo;
  }
  
  /**
   * Bezpečně převede integer na short.
   * Pokud je předaný parameter mimo rozsah shortu, je vyhozena výjimka.
   * @param aCo Převáděný integer
   * @return Číslo převedené na short a pře vedené určitě správně.
   */
  public static final short safeConvertToShort(int aCo) {
    if (aCo < Short.MIN_VALUE || aCo > Short.MAX_VALUE) throw new IllegalArgumentException("Value " + aCo + " is out of 'short' baunderies");
    return (short) aCo;
  }


  /**
   * Bezpečně převede long na short.
   * Pokud je předaný parameter mimo rozsah shortu, je vyhozena výjimka.
   * @param aCo Převáděný long
   * @return Číslo převedené na short a pře vedené určitě správně.
   */
  public static final short safeConvertToShort(long aCo) {
    if (aCo < Short.MIN_VALUE || aCo > Short.MAX_VALUE) throw new IllegalArgumentException("Value " + aCo + " is out of 'short' baunderies");
    return (short) aCo;
  }

  /**
   * Bezpečně převede long na int.
   * Pokud je předaný parameter mimo rozsah intu, je vyhozena výjimka.
   * @param aCo Převáděný long
   * @return Číslo převedené na int a pře vedené určitě správně.
   */
  public static final int safeConvertToInt(long aCo) {
    if (aCo < Integer.MIN_VALUE || aCo > Integer.MAX_VALUE) throw new IllegalArgumentException("Value " + aCo + " is out of 'int' baunderies");
    return (int) aCo;
  }

}
