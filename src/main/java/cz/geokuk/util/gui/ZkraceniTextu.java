package cz.geokuk.util.gui;

import java.awt.FontMetrics;

/** Zkrácení textu s koncovkou „…“, aby se vešel do dané šířky. */
public final class ZkraceniTextu {

	private ZkraceniTextu() {}

	/** Text zkrácený o konec s „…“, aby se vešel do {@code sirka}; text, který se vejde, beze změny; vždy zůstane aspoň jeden znak. */
	public static String zkrat(final String text, final FontMetrics fm, final int sirka) {
		if (fm.stringWidth(text) <= sirka) {
			return text;
		}
		int n = text.length();
		while (n > 1 && fm.stringWidth(text.substring(0, n) + "…") > sirka) {
			n--;
		}
		return text.substring(0, n) + "…";
	}
}
