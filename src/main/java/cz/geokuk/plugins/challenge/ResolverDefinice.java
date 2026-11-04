package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.challenge.Challenge.DruhOblasti;

/** Z definice udělá to, co potřebuje výpočet: střed, oblast a pravidla; když něco chybí (domov, listing v datech, data oblasti), vrátí důvod místo výjimky. */
final class ResolverDefinice {

	/** Výsledek převodu definice. */
	static final class Vyreseno {
		final double lat;
		final double lon;
		/** Oblast; null = bez omezení. */
		final Oblast oblast;
		/** Proč definici nejde spočítat; null, když jde. */
		final String duvod;

		private Vyreseno(final double lat, final double lon, final Oblast oblast, final String duvod) {
			this.lat = lat;
			this.lon = lon;
			this.oblast = oblast;
			this.duvod = duvod;
		}

		boolean jeUplne() {
			return duvod == null;
		}

		static Vyreseno uplne(final double lat, final double lon, final Oblast oblast) {
			return new Vyreseno(lat, lon, oblast, null);
		}

		static Vyreseno neuplne(final String duvod) {
			return new Vyreseno(0, 0, null, duvod);
		}
	}

	private final PoskytovatelOblasti poskytovatel;
	private final Supplier<Wgs> domov;

	ResolverDefinice(final PoskytovatelOblasti poskytovatel, final Supplier<Wgs> domov) {
		this.poskytovatel = poskytovatel;
		this.domov = domov;
	}

	/**
	 * @param najdiListing
	 *            keš podle kódu v načtených datech (výchozí bod listingu je její souřadnice)
	 */
	Vyreseno vyres(final Challenge c, final Function<String, Optional<? extends VstupKes>> najdiListing) {
		final double lat;
		final double lon;
		switch (c.stred().druh()) {
		case DOMOV:
			final Wgs d = domov.get();
			if (d == null) {
				return Vyreseno.neuplne("Domovské souřadnice nejsou nastavené.");
			}
			lat = d.lat;
			lon = d.lon;
			break;
		case SOURADNICE:
			lat = c.stred().lat();
			lon = c.stred().lon();
			break;
		default:
			final Optional<? extends VstupKes> k = najdiListing.apply(c.stred().kodListingu());
			if (!k.isPresent()) {
				return Vyreseno.neuplne("Keš " + c.stred().kodListingu() + " (střed challenge) není v načtených datech.");
			}
			lat = k.get().lat();
			lon = k.get().lon();
			break;
		}
		if (!VypocetSektoru.jePlatnyStred(lat, lon)) {
			return Vyreseno.neuplne("Střed challenge má neplatné souřadnice.");
		}
		if (c.oblast().druh() == DruhOblasti.ZADNA) {
			return Vyreseno.uplne(lat, lon, null);
		}
		final List<Oblast> oblasti = new ArrayList<>();
		for (final String id : c.oblast().idOblasti()) {
			final Optional<Oblast> o = poskytovatel.najdi(id);
			if (!o.isPresent()) {
				return Vyreseno.neuplne("Data oblasti „" + id + "“ nejsou k dispozici.");
			}
			oblasti.add(o.get());
		}
		return Vyreseno.uplne(lat, lon, oblasti.size() == 1 ? oblasti.get(0) : new SjednoceniOblasti(oblasti));
	}
}
