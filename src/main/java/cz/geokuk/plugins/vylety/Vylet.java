package cz.geokuk.plugins.vylety;

import java.util.*;

import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;

public class Vylet {

	private final Set<Kesoid> ano = new HashSet<>();
	private final Set<Kesoid> ne = new HashSet<>();

	public Set<Kesoid> get(final EVylet evyl) {
		Set<Kesoid> set;
		switch (evyl) {
		case ANO:
			set = ano;
			break;
		case NE:
			set = ne;
			break;
		default:
			throw new RuntimeException("Neznáme keše, které nevíme, zda jsou ve výletu");
		}
		return Collections.unmodifiableSet(set);
	}

	public EVylet get(final Kesoid kes) {
		if (ano.contains(kes)) {
			return EVylet.ANO;
		}
		if (ne.contains(kes)) {
			return EVylet.NE;
		}
		return EVylet.NEVIM;
	}

	EVylet add(final EVylet evyl, final Kesoid kes) {
		final EVylet evylPuvodni = get(kes);
		switch (evyl) {
		case ANO:
			ano.add(kes);
			ne.remove(kes);
			break;
		case NE:
			ne.add(kes);
			ano.remove(kes);
			break;
		case NEVIM:
			ano.remove(kes);
			ne.remove(kes);
			break;
		default:
			assert false;
		}
		return evylPuvodni;
	}

	void removeAll(final EVylet evyl) {
		switch (evyl) {
		case ANO:
			ano.clear();
			break;
		case NE:
			ne.clear();
			break;
		case NEVIM:
			break;
		default:
			assert false;
		}
	}

	public void add(final EVylet evyl, final Wpt wpt) {
		add(evyl, wpt.getKesoid());
	}

}
