package cz.geokuk.util.exception;

import cz.geokuk.util.lang.AString0;

/**
 * Identifiakce výjimky. Jen obaluje řetězec, kvůli typovým kontrolám.
 *
 * @author Martin Veverka
 *
 */
public class AExcId extends AString0 {

	static final long serialVersionUID = -248904485707733102L;

	public static AExcId from(final String s) {
		return s == null ? null : new AExcId(s);
	}

	protected AExcId(final String aValue) {
		super(aValue);
	}

}
