package cz.geokuk.util.procak;

/**
 * Výsleek zpracování objektu procakem.
 * @author veverka
 *
 */
public enum EProcakResult {

	/** Zpracoval jsem ho, už ho nikdo jiný nesmí dostat, takže jen jeden procak smí řící DONE */
	DONE,

	/** Už ho nechci vidět, neni můj, ať se o něho postará někdo jinej. */
	NEVER,

	/** Zatím ho neumím zpracovat, ale chci ho vidět ještě v dalším kole, pokud ho nikdo jiný nezpracuje po mně nebo přede mnou v dalším kole. Ať ho vidí všichni
	 * a když ho ebudou chtít, nechť se dostane zpět ke mně. */
	NEXT_ROUND,

	/** Zatím ho nemohu zpracovat, protože nevím, ale nesmí propadnout dolů, aby ho nikdo nesežral. */
	PROBABLY_MY_BUT_NEXT_ROUND,
	;
}
