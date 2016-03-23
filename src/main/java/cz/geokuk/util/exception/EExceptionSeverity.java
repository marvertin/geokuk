package cz.geokuk.util.exception;

/**
 * Závažnost chyby. Určuje kam bude chyba dumpována.
 *
 * @author veverka
 */
public enum EExceptionSeverity {
    /**
     * Jsou dvě možnosti: 1. Identifikace výjimky je právě zobrazována koncovému uživateli 2. Identifikace předávána dál přes vzdálená volání, protože prohazovat výjimky vzdálenými voláními není moc dobrý nápad. Na klientské straně totiž nemusí být deseriaizovatelné. Oba případy jsou rovnocenné,
     * protože ani vdruhém případě nezvývá klientskému systému nic jiného, než zobrazit hlášku.
     */
	DISPLAY("a"),

	/**
	 * Výjimka je odchycena a zpracování je ošetřeno náhradním způsobem tak, aby aplikace ceká nespadla, přesto však aplikace není plně funkční, i výjimky této závažnosti by se měly odstranit. Příkladem je výjimka při zobrazení kontextové navigace, kdy kontextová navigace bude prázdná, pokud dojde
	 * během jejího zpracování k výjimce.
	 */
	WORKARROUND("b"),

	/**
	 * Došlo dle autora kódu k úplnému a vyčerpávajícímu ošetření výjimky. Přesto se autor domnívá, že výjimku je vhodné vydumpovat pro případné řešení potíží. Výjimka v žádném případě není vyhozena dál.
	 */
	CATCHE("c"),

	/**
	 * Výjimka je vyhozena dále. Protože však hrozí nebezpečí, že tam nahoře nebude výjimka výjimka řádně ošetřena, je zde projistotu vydumpována. Tyto stavy by měly být používány co možná nejméně, nejlépe vůbec.
	 */
	RETHROW("x");

	private final String iCode;

	/**
	 * @param aC
	 */
	EExceptionSeverity(final String aCode) {
		iCode = aCode;
	}

	/**
	 * @return Returns the code.
	 */
	public String getCode() {
		return iCode;
	}
}
