package cz.geokuk.util.lang;

/**
 * Bázová třída pro všechny výjimky, které bude vyhazovat Tw nebo i TC aplikace. Jsou to kompilátorem nečekované výjimky. Tato třída se liší od předka Exception v těchto bodech: 1. Nemá konstruktor pr zřízení výjimky bez řetězce. 2. Uchovává odkaz na předchozí výjimku, čímž vytváří zřetězený seznam
 * výjimek a je možné při přehazování výjimek sledovat jejich stopu. metoda toString vrací informace o všech výjimkách v seznamu. 3. Nová metoda getStackTrace vracející nejúplnější informace o výjimkce. (ve statické i instanční verzo) 4. Nová metoda rethrow, která volající výjimku obalí výjimkou
 * stejného typu. (ve statické i instanční verzi)
 * 
 * @author Martin Veverka
 * @version $Revision: 17 $
 * @see "TW0139Util.vjp"
 * @see "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/exception/XObject0.java 17    15.03.00 7:57 Veverka $"
 */
public abstract class XObject0 extends RuntimeException {
	/**
	 * Odkaz na příští výjimku.
	 * 
	 * @serial
	 */
	// Exception next;

	/**
	 *
	 */
	private static final long serialVersionUID = -3973117135262826574L;

	/**
	 * Vytvoří výjimku se zadano zprávou
	 * 
	 * @param s
	 *            Technicky orientovaná zpráva. Obsahuje informace o chybě určené technicky a systémově znalé osobě.
	 */
	public XObject0(String s) {
		super(s);
	}

	/**
	 * Vytvoří výjimku a naváže ji na zadanou výjimku. Používá se v bloku catch v případě potřeby přehodit výjimku.
	 * 
	 * @param s
	 *            Technicky orientovaná zpráva. Obsahuje informace o chybě určené technicky a systémově znalé osobě.
	 * @param e
	 *            Přehazovaná výjimka.
	 */
	public XObject0(String s, Exception e) {
		super(s, e);
	}

	/**
	 * Vyhodí výjimku s tím, že přidá jméno třídy, ze které je vyhozena.
	 * 
	 * @param trida
	 *            Třída, ze které je výjimka vyhazována. Volající metoda bezmyšlenkovitě uvádí vždy getClass(). Je to velmi užitečná informace v případě, že používáme následníky.
	 * @param s
	 *            Technicky orientovaná zpráva. Obsahuje informace o chybě určené technicky a systémově znalé osobě.
	 */
	public XObject0(Object trida, String s) {
		this(FThrowable._constructClassString(trida, s));
	}

	/**
	 * Přehodí výjimku s tím, že přidá jméno třídy, ze které je přehozena.
	 * 
	 * @param trida
	 *            Třída, ze které je výjimka vyhazována. Volající metoda bezmyšlenkovitě uvádí vždy getClass(). Je to velmi užitečná informace v případě, že používáme následníky.
	 * @param s
	 *            Technicky orientovaná zpráva. Obsahuje informace o chybě určené technicky a systémově znalé osobě.
	 * @param e
	 *            Přehazovaná výjimka.
	 */
	public XObject0(Object trida, String s, Exception e) {
		this(FThrowable._constructClassString(trida, s), e);
	}

	/**
	 * Dá výjimku jako čitelný řetězec. Pokud je výjimka zřetězena s dalšími, vrací znakem nového řádku oddělené popisy jednotlivých výjimek.
	 * 
	 * @return Víceřádkový řetězec s informacemi o výjimce určeným pro programátora nebo správce systému.
	 * 
	 *         public final String toString() {
	 * 
	 *         if (next == null) return super.toString(); else return super.toString() + "\n" + next.toString();; }
	 */

	/**
	 * Dá veškeré informace o výjimce jako řetězec. Vrací stack trace jako řetězec pro tuto výjimku a všechny výjimky seznamu, součástí stacku je i výpis zpráv obsažených ve výjimce, z čehož vyplývá, že getStackTrace vrací nejúplnější informaci o výjimce, která je k dispozici.
	 * 
	 * @return Víceřádkový řetězec s informacemi o výjimce určeným pro programátora nebo správce systému. private final String _getStackTrace() { return FThrowable.getStackTrace(this); }
	 */

}
