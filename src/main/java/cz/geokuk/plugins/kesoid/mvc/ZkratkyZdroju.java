package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.plugins.kesoid.importek.TypZdroje;

/**
 * Klávesové zkratky Ctrl+1 až Ctrl+9 pro zapnutí a vypnutí typů zdrojů. Číslo typu je pevné, určuje ho pořadí typu ve výčtu {@link TypZdroje} (GPX 1, GeoGet 2, GSAK 3, OpenSAK 4),
 * takže nezávisí na tom, které zdroje jsou právě vidět ani zapnuté. Typy za devátým místem zkratku nemají. Zkratky se vážou na kód klávesy {@code VK_1} až {@code VK_9}
 * (a číselnou klávesnici), ne na znak, takže fungují i na rozloženích, kde horní řada bez Shiftu píše jiné znaky.
 */
public final class ZkratkyZdroju {

	/** Nejvyšší číslo zkratky. */
	public static final int NEJVIC = 9;

	private ZkratkyZdroju() {}

	/** Číslo zkratky typu 1 až 9, nebo 0, když typ zkratku nemá. */
	public static int cislo(final TypZdroje typ) {
		return cislo(typ.ordinal());
	}

	static int cislo(final int poradi) {
		return poradi < NEJVIC ? poradi + 1 : 0;
	}

	/** Zkratka v menu: Ctrl a číslice horní řady; null, když typ zkratku nemá. */
	public static KeyStroke zkratka(final TypZdroje typ) {
		final int cislo = cislo(typ);
		return cislo == 0 ? null : KeyStroke.getKeyStroke(KeyEvent.VK_1 + cislo - 1, InputEvent.CTRL_DOWN_MASK);
	}

	/** Táž zkratka na číselné klávesnici; null, když typ zkratku nemá. */
	public static KeyStroke zkratkaNumpad(final TypZdroje typ) {
		final int cislo = cislo(typ);
		return cislo == 0 ? null : KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1 + cislo - 1, InputEvent.CTRL_DOWN_MASK);
	}

	/** Zkratka pro zobrazení, třeba „Ctrl+3“; null, když typ zkratku nemá. */
	public static String text(final TypZdroje typ) {
		final int cislo = cislo(typ);
		return cislo == 0 ? null : "Ctrl+" + cislo;
	}

	/** Název typu a v závorce jeho zkratka, třeba „GSAK (Ctrl+3)“. */
	public static String nazevSeZkratkou(final TypZdroje typ) {
		final String zkratka = text(typ);
		return zkratka == null ? typ.getNazev() : typ.getNazev() + " (" + zkratka + ")";
	}
}
