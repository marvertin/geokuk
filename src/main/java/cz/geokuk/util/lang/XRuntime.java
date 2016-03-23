package cz.geokuk.util.lang;

/**
 * Předek všech technických výjimek. Bázová třída pro všechny aplikačně vyhazované technické výjimky. Neobsahuje žádné rošíření oproti svému předchůdci. Existuje pouze proto, aby se daly definovat metdody, které mohou technickou výjimku vyhazovat, zatímco aplikační nikoli.
 *
 * @author Martin Veverka
 */

public final class XRuntime extends XObject0 {

	static final long serialVersionUID = 2772768580961243187L;

	/**
	 * Vyhodí výjimku s tím, že přidá jméno třídy, ze které je vyhozena.
	 *
	 * @param trida
	 *            Třída, ze které je výjimka vyhazována. Volající metoda bezmyšlenkovitě uvádí vždy getClass(). Je to velmi užitečná informace v případě, že používáme následníky.
	 * @param s
	 *            Technicky orientovaná zpráva. Obsahuje informace o chybě určené technicky a systémově znalé osobě.
	 */
	public XRuntime(final Object trida, final String s) {
		super(trida, s);
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
	public XRuntime(final Object trida, final String s, final Exception e) {
		super(trida, s, e);
	}

	/**
	 * Vytvoří výjimku se zadano zprávou
	 *
	 * @param s
	 *            Technicky orientovaná zpráva. Obsahuje informace o chybě určené technicky a systémově znalé osobě.
	 */
	public XRuntime(final String s) {
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
	public XRuntime(final String s, final Exception e) {
		super(s, e);
	}

}
