package cz.geokuk.util.lang;

/** Doplňuje do Long užitečné metody, které by se daly čekat, ale nejsou tam.
 *
 * Bude-li kdy definována metoda, která má smysl i pro Integer,
 * pak nechť implementace v případné třídě {@link TwInteger} provolává implementaci odsud, z {@link TwLong},
 * a provádí patřičné doménové kontroly.
 *
 * @author  <a href="mailto:Bohuslav.Roztocil@turboconsult.cz">Bohuslav Roztočil</a>
 * @version $Revision: 3 $
 * @see     "TW0139Util.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/util/TwLong.java 3     23.10.00 14:23 Roztocil $"
 * @since   NQT 2.7
 */
public final class FLong
{

    /** Převede řetězec na hodnotu {@link Long}.
     *
     * Sama podle prefixu rozpozná oktalový, dekadický či hexadecimální zápis.
     *
     * @param   aValue  Převáděný řetězec - předpokládá se, že obsahuje číslo v oktalovém, dekadickém či hexadecimálním zápisu.
     * @return          Hodnota {@link Long} odpovídající zadanému parametru.
     *                  Je-li předána hodnota <b>null</b>, je výsledek také <b>null</b>.
     */
    public static Long decode(String aValue) throws NumberFormatException
    {
        String value = aValue;
        int    base  = 10;

        if (value == null) return null;

        if (value.toLowerCase().startsWith("0x")) {

            if (value.length() <= 2) throw new NumberFormatException("Hexadicimální zápis musí obsahovat za prefixem alespoň jeden znak.");
            base  = 16;
            value = value.substring(2);
        } else if (value.startsWith("0")) {
            base  = 8;
            if (value.length() > 1) value = value.substring(1);
        }

        return Long.parseLong(value, base);
    }
}
