package cz.geokuk.plugins.challenge;

import java.util.Set;

/** Úložiště nastavení challenge jako dvojic klíč a text; v programu uzel preferencí, v testu mapa. */
public interface ChallengeUloziste {

	/** Hodnota, nebo null. */
	String get(String klic);

	void put(String klic, String hodnota);

	void remove(String klic);

	Set<String> klice();
}
