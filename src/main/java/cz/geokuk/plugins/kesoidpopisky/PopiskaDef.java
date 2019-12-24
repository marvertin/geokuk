package cz.geokuk.plugins.kesoidpopisky;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * Definice druhu popisky.
 * Každý takový druh má řádek v dialogu pro vzorky popisek.
 * S jedním kešoidovým pluginem může být svázáno více druhů.
 * Po určení druhu už zde vše je.
 * @author Martin
 *
 */
@Data
public class PopiskaDef {
	/** Jednoznáčný kód druhu popisky, je kíčem v preferences */
	private String code;

	/** Prompt, který se zobrazí vedle vzorku */
	private String prompt;

	/** Defaultní vzorek, pokud ho uživatel nepředefinoval */
	private String defaultVzorek;

	/**
	 * Nahrazovači zástupek
	 */
	private Map<String, PopiskyNahrazovac> nahrazovaci = new HashMap<>();

}
