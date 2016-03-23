package cz.geokuk.plugins.kesoid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by dan on 16.7.14.
 */
public enum EKesDiffTerRating {
	ONE, ONE_HALF, TWO, TWO_HALF, THREE, THREE_HALF, FOUR, FOUR_HALF, FIVE, UNKNOWN;

	private static final Logger log = LogManager.getLogger(EKesDiffTerRating.class.getSimpleName());

	public String toNumberString() {
		if (this == UNKNOWN) {
			return "0";
		} else {
			return String.valueOf(this.ordinal() / 2. + 1);
		}
	}

	public String to2DigitNumberString() {
		if (this == UNKNOWN) {
			return "0";
		} else {
			return Integer.toString(this.ordinal() * 5 + 10);
		}
	}

	public char toSingleChar() {
		if (this == EKesDiffTerRating.UNKNOWN) {
			return '?';
		} else {
			int ordHalf = ordinal() / 2;
			char base = (ordinal() & 1) == 0 ? '1' : 'A';
			return (char) (base + ordHalf);
		}
	}

	public static EKesDiffTerRating parse(String toParse) {
		if (toParse == null) {
			log.warn("Null passed as parameter to EKesDiffTerRating parser!");
			return UNKNOWN;
		}
		try {
			float f = Float.parseFloat(toParse);
			int ordinal = (int) (f * 2 - 2);
			EKesDiffTerRating[] values = values();
			if (ordinal < values.length && ordinal >= 0) {
				return values[ordinal];
			} else {
				log.error("Ordinal out of range for string {}! Was {}, expected less than {}.", toParse, ordinal, values.length);
				return UNKNOWN;
			}
		} catch (NumberFormatException e) {
			log.error("Unable to parse {} as float!", toParse);
			return UNKNOWN;
		}
	}
}
