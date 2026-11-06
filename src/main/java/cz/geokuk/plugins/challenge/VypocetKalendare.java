package cz.geokuk.plugins.challenge;

import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Výpočet challenge kalendář: nález v každém dni roku, počítá se podle lokálního data nálezu. */
public final class VypocetKalendare {

	private static final MonthDay PRESTUPNY = MonthDay.of(2, 29);

	private VypocetKalendare() {}

	/** Lokální datum okamžiku nálezu v pásmu uživatele. */
	public static LocalDate lokalniDatum(final Instant okamzik, final ZoneId pasmo) {
		return okamzik.atZone(pasmo).toLocalDate();
	}

	/**
	 * @param dnes
	 *            dnešní datum v pásmu uživatele; podle něj se počítá „za kolik dní“ a které eventy ještě nenastaly
	 * @param lat
	 *            referenční bod (třeba střed mapy); při shodě data se vybírají kandidáti nejblíž němu a výsledek platí pro něj
	 * @param kese
	 *            prvky nesmí být null
	 */
	public static VysledekPolicek spocti(final PravidlaKalendare pravidla, final LocalDate dnes, final double lat, final double lon,
			final Iterable<? extends VstupKalendar> kese) {
		Objects.requireNonNull(pravidla, "pravidla");
		Objects.requireNonNull(dnes, "dnes");
		Objects.requireNonNull(kese, "kese");
		if (!VypocetSektoru.jePlatnyStred(lat, lon)) {
			throw new IllegalArgumentException("Bod mimo rozsah souřadnic: " + lat + ", " + lon);
		}
		final int max = pravidla.typy().maxKandidatu();
		final boolean vyroci = pravidla.kandidati() == PravidlaKalendare.Kandidati.EVENTY_A_VYROCI;
		final Set<MonthDay> nalezene = new HashSet<>();
		final Map<MonthDay, OmezenaFronta> kandidatiDne = new HashMap<>();
		for (final VstupKalendar k : kese) {
			Objects.requireNonNull(k, "keš");
			if (!pravidla.typy().lzePocitat(k)) {
				continue;
			}
			if (PravidlaTypu.platnyNalez(k)) {
				if (k.datumNalezu() != null) {
					nalezene.add(MonthDay.from(k.datumNalezu()));
				}
			} else if (PravidlaTypu.hledatelna(k) && (vyroci || k.event())) {
				pridejKandidata(pravidla, dnes, max, lat, lon, k, kandidatiDne);
			}
		}
		final List<MonthDay> dny = chybejiciDny(pravidla, dnes, nalezene);
		final List<String> stitky = new ArrayList<>();
		final List<KandidatPolicka> kandidati = new ArrayList<>();
		for (final MonthDay den : dny) {
			final String stitek = stitek(den, dniDoDalsiho(den, dnes));
			stitky.add(stitek);
			final OmezenaFronta fronta = kandidatiDne.get(den);
			if (fronta != null) {
				for (final OmezenaFronta.Kandidat c : fronta.serazene()) {
					kandidati.add(new KandidatPolicka(c.keska.id(), c.keska.lat(), c.keska.lon(), stitek, stitek(den, c.dni), c.dni, c.vzdalenost));
				}
			}
		}
		return new VysledekPolicek(pravidla.pozadovatPrestupny() ? 366 : 365, stitky, kandidati);
	}

	/** Do fronty dne se drží nejvýš {@code max} kandidátů s nejbližším datem, při shodě nejblíž bodu. */
	private static void pridejKandidata(final PravidlaKalendare pravidla, final LocalDate dnes, final int max, final double lat, final double lon, final VstupKalendar k,
			final Map<MonthDay, OmezenaFronta> kandidatiDne) {
		final LocalDate datum = k.datum();
		if (datum == null) {
			return;
		}
		final MonthDay den = MonthDay.from(datum);
		final int dni;
		if (k.event()) {
			if (datum.isBefore(dnes)) {
				return;
			}
			dni = (int) ChronoUnit.DAYS.between(dnes, datum);
		} else if (pravidla.kandidati() == PravidlaKalendare.Kandidati.EVENTY_A_VYROCI) {
			dni = dniDoDalsiho(den, dnes);
		} else {
			return;
		}
		OmezenaFronta fronta = kandidatiDne.get(den);
		if (fronta == null) {
			fronta = new OmezenaFronta(max);
			kandidatiDne.put(den, fronta);
		}
		fronta.pridej(k, dni, lat, lon);
	}

	private static List<MonthDay> chybejiciDny(final PravidlaKalendare pravidla, final LocalDate dnes, final Set<MonthDay> nalezene) {
		final List<MonthDay> dny = new ArrayList<>();
		for (LocalDate d = LocalDate.of(2025, 1, 1); d.getYear() == 2025; d = d.plusDays(1)) {
			final MonthDay md = MonthDay.from(d);
			if (!nalezene.contains(md)) {
				dny.add(md);
			}
		}
		if (pravidla.pozadovatPrestupny() && !nalezene.contains(PRESTUPNY)) {
			dny.add(PRESTUPNY);
		}
		Collections.sort(dny, Comparator.comparingInt((final MonthDay d) -> dniDoDalsiho(d, dnes)));
		return dny;
	}

	/** Za kolik dní nastane den poprvé (0 = dnes); 29. 2. až v nejbližším přestupném roce. */
	static int dniDoDalsiho(final MonthDay den, final LocalDate dnes) {
		for (int rok = dnes.getYear();; rok++) {
			if (den.isValidYear(rok)) {
				final LocalDate datum = den.atYear(rok);
				if (!datum.isBefore(dnes)) {
					return (int) ChronoUnit.DAYS.between(dnes, datum);
				}
			}
		}
	}

	/** Štítek dne, například „14. 3. (za 131 dní)“. */
	static String stitek(final MonthDay den, final int dni) {
		return den.getDayOfMonth() + ". " + den.getMonthValue() + ". (" + za(dni) + ")";
	}

	private static String za(final int dni) {
		if (dni == 0) {
			return "dnes";
		}
		return "za " + dni + (dni == 1 ? " den" : dni < 5 ? " dny" : " dní");
	}
}
