package cz.geokuk.plugins.challenge;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * Oblasti ze zdrojů programu: {@code /cz/geokuk/plugins/challenge/oblasti/ID.obl} (viz {@link FormatOblasti}) a hranice z RÚIAN v souboru
 * {@code oblasti-ruian.txt} (viz {@link FormatRuian}); načtená oblast se pamatuje.
 */
@Slf4j
public final class ZdrojovyPoskytovatelOblasti implements PoskytovatelOblasti {

	private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9-]*");

	private static final Optional<Oblast> NENI = Optional.empty();

	private final String predpona;
	private final Map<String, Optional<Oblast>> nactene = new ConcurrentHashMap<>();

	/** Označení oblasti v definicích → kód v souboru RÚIAN. */
	private static final Map<String, String> KODY_RUIAN;
	static {
		final Map<String, String> m = new HashMap<>();
		m.put(Sablony.STAT_CESKO, "cr");
		m.put(Sablony.PRAHA, "praha");
		KODY_RUIAN = Collections.unmodifiableMap(m);
	}

	private Map<String, OblastPolygony> ruian;

	public ZdrojovyPoskytovatelOblasti() {
		this("/cz/geokuk/plugins/challenge/oblasti/");
	}

	ZdrojovyPoskytovatelOblasti(final String predpona) {
		this.predpona = predpona;
	}

	@Override
	public Optional<Oblast> najdi(final String id) {
		if (!ID.matcher(id).matches()) {
			return NENI;
		}
		return nactene.computeIfAbsent(id, this::nacti);
	}

	private synchronized Map<String, OblastPolygony> ruian() {
		if (ruian == null) {
			try (InputStream in = ZdrojovyPoskytovatelOblasti.class.getResourceAsStream(predpona + "oblasti-ruian.txt")) {
				if (in == null) {
					ruian = Collections.emptyMap();
				} else {
					try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
						ruian = FormatRuian.nacti(r);
					}
				}
			} catch (final IOException e) {
				log.warn("Hranice oblastí z RÚIAN nejdou načíst: {}", e.toString());
				ruian = Collections.emptyMap();
			}
		}
		return ruian;
	}

	private Optional<Oblast> nacti(final String id) {
		final String kodRuian = KODY_RUIAN.get(id);
		if (kodRuian != null) {
			final Oblast o = ruian().get(kodRuian);
			if (o != null) {
				return Optional.of(o);
			}
		}
		try (InputStream in = ZdrojovyPoskytovatelOblasti.class.getResourceAsStream(predpona + id + ".obl")) {
			if (in == null) {
				return NENI;
			}
			try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				return Optional.of(FormatOblasti.nacti(r));
			}
		} catch (final IOException e) {
			log.warn("Oblast {} nejde načíst: {}", id, e.toString());
			return NENI;
		}
	}
}
