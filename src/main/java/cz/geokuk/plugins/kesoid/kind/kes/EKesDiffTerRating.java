package cz.geokuk.plugins.kesoid.kind.kes;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by dan on 16.7.14.
 */
@Slf4j
public enum EKesDiffTerRating {
	ONE, ONE_HALF, TWO, TWO_HALF, THREE, THREE_HALF, FOUR, FOUR_HALF, FIVE, UNKNOWN;



	public static EKesDiffTerRating parse(final String toParse) {
		if (toParse == null) {
			log.warn("Null passed as parameter to EKesDiffTerRating parser!");
			return UNKNOWN;
		}
		try {
			final float f = Float.parseFloat(toParse);
			final int ordinal = (int) (f * 2 - 2);
			final EKesDiffTerRating[] values = values();
			if (ordinal < values.length && ordinal >= 0) {
				return values[ordinal];
			} else {
				log.error("Ordinal out of range for string {}! Was {}, expected less than {}.", toParse, ordinal, values.length);
				return UNKNOWN;
			}
		} catch (final NumberFormatException e) {
			log.error("Unable to parse {} as float!", toParse);
			return UNKNOWN;
		}
	}

	public String to2DigitNumberString() {
		if (this == UNKNOWN) {
			return "0";
		} else {
			return Integer.toString(ordinal() * 5 + 10);
		}
	}

	public String toNumberString() {
		if (this == UNKNOWN) {
			return "0";
		} else {
			return String.valueOf(ordinal() / 2. + 1);
		}
	}

	public char toSingleChar() {
		if (this == EKesDiffTerRating.UNKNOWN) {
			return '?';
		} else {
			final int ordHalf = ordinal() / 2;
			final char base = (ordinal() & 1) == 0 ? '1' : 'A';
			return (char) (base + ordHalf);
		}
	}
}
