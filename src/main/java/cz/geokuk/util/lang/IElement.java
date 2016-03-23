package cz.geokuk.util.lang;

/**
 * Příznak, že třída představuje elementární typ - TC rozšíření konceptu primitivních typů.
 *
 * <P>
 * Elementární typy jsou chápány jako dále nestrukturované a veškerý TW kód se s nimi takto snaží zacházet.
 * To platí především pro serializaci a já příbuzné procesy (XMLizace atd.).
 * Na rozdíl od primitivních typů (a jejich obálek - sic!) je možné vytvářet následníky elementárních typů.
 *
 * @author   Martin Veverka
 * @version $Revision: 4 $
 * @see     "TW0139Util.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/IElement.java 4     15.03.00 10:55 Veverka $"
 */
public interface IElement extends java.io.Serializable
{
	public String asString();
}
