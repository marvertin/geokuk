package cz.geokuk.util.lang;

/**Rozhraní, které implementují všechny objekty jenž umí vrátit
 * svoji hodnotu jako int. Činnost je podobná jakou u IElementLong
 * s tím rozdílem, že pracují s intem. Dvě implementace existují
 * především proto, že v databázi je nutné použít různý typ.
 *
 * @author  <a href="mailto:?????.?????@turboconsult.cz">????? ?????</a>
 * @version $Revision: 2 $
 * @see     "TW####??????.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/IElementInt.java 2     23.05.00 12:01 Veverka $"
 */
public interface IElementInt
extends IElement
{
	public int asInt();
}
