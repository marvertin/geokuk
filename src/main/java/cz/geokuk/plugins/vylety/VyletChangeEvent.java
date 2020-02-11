package cz.geokuk.plugins.vylety;

import java.util.Optional;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.Wpt;
import lombok.*;

@Getter @RequiredArgsConstructor @ToString
public class VyletChangeEvent extends Event0<VyletModel> {

	private final VyletModel vyletModel;

	private final Wpt wpt;
	private final EVylet evyl;
	private final EVylet evylPuvodni;



	public Optional<Wpt> getAno() {
		return evyl == EVylet.ANO ? Optional.of(wpt) : Optional.empty();
	}

	public Optional<Wpt> getNe() {
		return evyl == EVylet.NE ? Optional.of(wpt) : Optional.empty();
	}

	/**
	 * Zda se změnil celý výlet, pokud ano, tak se nsmí brát jednotlivé změny Většinout to bude znamenat načtení výletu.
	 *
	 * @return
	 */
	public boolean isVelkaZmena() {
		return evyl == null || wpt == null;
	}

}
