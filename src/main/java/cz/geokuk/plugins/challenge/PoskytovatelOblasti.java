package cz.geokuk.plugins.challenge;

import java.util.Optional;

/** Dodavatel oblastí podle označení (stát, okres); oblast, kterou nezná, vrací prázdnou. */
public interface PoskytovatelOblasti {

	Optional<Oblast> najdi(String id);
}
