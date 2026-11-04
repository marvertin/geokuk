package cz.geokuk.plugins.kesoid;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Které zapnuté vrstvy kruhů se právě nekreslí a proč; z toho vzniká štítek ve stavovém řádku. Neměnný. */
public final class StavVrstev {

	public static final String TEXT_STITKU = "Kruhy skryté";
	public static final String TEXT_STITKU_OMEZENO = "Kruhy omezené";

	/** Skrytá vrstva: důvod a limit, o který šlo (u limitu počtu). */
	public static final class Skryta {
		private final DuvodNekresleni duvod;
		private final int limit;

		Skryta(final DuvodNekresleni duvod, final int limit) {
			this.duvod = duvod;
			this.limit = limit;
		}

		public DuvodNekresleni getDuvod() {
			return duvod;
		}

		public int getLimit() {
			return limit;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Skryta)) {
				return false;
			}
			final Skryta s = (Skryta) o;
			return duvod == s.duvod && limit == s.limit;
		}

		@Override
		public int hashCode() {
			return Objects.hash(duvod, limit);
		}
	}

	private static final StavVrstev PRAZDNY = new StavVrstev(Collections.emptyMap(), 0, 0);

	private final Map<VrstvaNadMapou, Skryta> skryte;
	/** Kolik kruhů s hranou ve výřezu se nekreslí kvůli omezení jejich počtu, a kolik se jich kreslí. */
	private final int vynechanychKruhu;
	private final int kreslenychKruhu;

	private StavVrstev(final Map<VrstvaNadMapou, Skryta> skryte, final int vynechanychKruhu, final int kreslenychKruhu) {
		this.skryte = skryte;
		this.vynechanychKruhu = vynechanychKruhu;
		this.kreslenychKruhu = kreslenychKruhu;
	}

	public static StavVrstev prazdny() {
		return PRAZDNY;
	}

	/** Nový stav, ve kterém vrstva má daný důvod; {@code KRESLI} nebo null ji ze skrytých vyřadí. */
	public StavVrstev s(final VrstvaNadMapou vrstva, final DuvodNekresleni duvod, final int limit) {
		final Map<VrstvaNadMapou, Skryta> m = new EnumMap<>(VrstvaNadMapou.class);
		m.putAll(skryte);
		if (duvod == null || duvod == DuvodNekresleni.KRESLI) {
			m.remove(vrstva);
		} else {
			m.put(vrstva, new Skryta(duvod, duvod == DuvodNekresleni.LIMIT_POCTU ? limit : 0));
		}
		return nova(Collections.unmodifiableMap(m), vynechanychKruhu, kreslenychKruhu);
	}

	/** Nový stav s počtem vynechaných a kreslených kruhů; bez vynechaných se kreslené nepamatují. */
	public StavVrstev sVynechanymi(final int vynechano, final int kresleno) {
		return vynechano > 0 ? nova(skryte, vynechano, kresleno) : nova(skryte, 0, 0);
	}

	private static StavVrstev nova(final Map<VrstvaNadMapou, Skryta> skryte, final int vynechano, final int kresleno) {
		return skryte.isEmpty() && vynechano == 0 ? PRAZDNY : new StavVrstev(skryte, vynechano, kresleno);
	}

	public int getKreslenychKruhu() {
		return kreslenychKruhu;
	}

	public int getVynechanychKruhu() {
		return vynechanychKruhu;
	}

	/** Má se ukázat štítek: nějaká zapnutá vrstva se nekreslí, nebo se kruhy kreslí jen částečně. */
	public boolean jeKVideni() {
		return maSkryte() || vynechanychKruhu > 0;
	}

	/** Je aspoň jedna zapnutá vrstva skrytá? */
	public boolean maSkryte() {
		return !skryte.isEmpty();
	}

	public Skryta get(final VrstvaNadMapou vrstva) {
		return skryte.get(vrstva);
	}

	/** Skrytá vrstva zvýrazňovacích nebo jednotkových kruhů (nikdy obě), nebo null. */
	public Skryta getSkrytyKruh() {
		final Skryta s = skryte.get(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY);
		return s != null ? s : skryte.get(VrstvaNadMapou.JEDNOTKOVE_KRUHY);
	}

	public Map<VrstvaNadMapou, Skryta> getSkryte() {
		return skryte;
	}

	/** Text štítku ve stavovém řádku. */
	public String text() {
		return maSkryte() ? TEXT_STITKU : TEXT_STITKU_OMEZENO;
	}

	/** Tooltip štítku: řádek pro každou skrytou vrstvu s důvodem. */
	public String tooltip() {
		final StringBuilder sb = new StringBuilder("<html>");
		boolean prvni = true;
		for (final Map.Entry<VrstvaNadMapou, Skryta> e : skryte.entrySet()) {
			if (!prvni) {
				sb.append("<br/>");
			}
			prvni = false;
			sb.append(e.getKey().getNazev()).append(": ").append(textDuvodu(e.getKey(), e.getValue().getDuvod(), e.getValue().getLimit()));
		}
		if (vynechanychKruhu > 0) {
			if (!prvni) {
				sb.append("<br/>");
			}
			sb.append("Kruhy v metrech: kresleno jen ").append(kreslenychKruhu).append(" nejbližších ke středu, přibližte mapu");
		}
		return sb.append("</html>").toString();
	}

	/** Důvod česky pro danou vrstvu. */
	public static String textDuvodu(final VrstvaNadMapou vrstva, final DuvodNekresleni duvod, final int limit) {
		switch (duvod) {
		case LIMIT_POCTU:
			return String.format(new Locale("cs"), "příliš mnoho bodů ve výřezu (limit %,d), přibližte mapu", limit);
		case PRILIS_HUSTE:
			return "příliš husté, přibližte mapu";
		case POLOMER_PRILIS_MALY:
			return vrstva == VrstvaNadMapou.OBSAZENOST ? "nezobrazuje se v tak malém měřítku, přibližte mapu" : "poloměr pod 3 px, přibližte mapu";
		default:
			return "";
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof StavVrstev)) {
			return false;
		}
		final StavVrstev s = (StavVrstev) o;
		return skryte.equals(s.skryte) && vynechanychKruhu == s.vynechanychKruhu && kreslenychKruhu == s.kreslenychKruhu;
	}

	@Override
	public int hashCode() {
		return Objects.hash(skryte, vynechanychKruhu, kreslenychKruhu);
	}

	@Override
	public String toString() {
		return "StavVrstev" + skryte.keySet();
	}
}
