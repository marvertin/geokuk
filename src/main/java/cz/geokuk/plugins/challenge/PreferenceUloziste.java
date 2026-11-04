package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Úložiště nastavení challenge v uzlu preferencí. */
final class PreferenceUloziste implements ChallengeUloziste {

	private final Preferences uzel;

	PreferenceUloziste(final Preferences uzel) {
		this.uzel = uzel;
	}

	@Override
	public String get(final String klic) {
		return uzel.get(klic, null);
	}

	@Override
	public void put(final String klic, final String hodnota) {
		uzel.put(klic, hodnota);
	}

	@Override
	public void remove(final String klic) {
		uzel.remove(klic);
	}

	@Override
	public Set<String> klice() {
		try {
			return new LinkedHashSet<>(Arrays.asList(uzel.keys()));
		} catch (final BackingStoreException e) {
			return new LinkedHashSet<>();
		}
	}
}
