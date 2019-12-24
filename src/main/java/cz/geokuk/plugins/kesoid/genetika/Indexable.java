package cz.geokuk.plugins.kesoid.genetika;

/**
 * Objekty, kterým jsou přidělovány indexy při jejich zřizování s tím, že objekty jsou vytvářeny v omezeném počtu.
 * @author veverka
 *
 */
@FunctionalInterface
public interface Indexable {
	/**
	 * Index přidělený objektu.
	 * @return Index nezáporný a malý. Očekává se, že bude indexem v poli pro zrychlení mapování.
	 */
	int getIndex();
}
