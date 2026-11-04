package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Oblast z jednoho nebo víc mnohoúhelníků (části) s dírami; okraj nesmí přecházet přes poledník 180° (takový se při vytvoření odmítne). Bod leží v oblasti, je-li v kterékoli části; v části platí pravidlo sudého počtu průsečíků přes
 * vnější okraj i díry. Hrany se pro rychlé hledání roztřídí do pásů podle zeměpisné šířky a obdélník části slouží jako předfiltr.
 */
public final class OblastPolygony implements Oblast {

	/** Jeden uzavřený okraj, souřadnice ve stupních; poslední bod se nemusí opakovat. */
	public static final class Okraj {
		final double[] lat;
		final double[] lon;

		public Okraj(final double[] lat, final double[] lon) {
			if (lat.length != lon.length || lat.length < 3) {
				throw new IllegalArgumentException("Okraj má mít aspoň 3 body se stejným počtem šířek a délek");
			}
			for (int i = 0; i < lat.length; i++) {
				if (!(lat[i] >= -90 && lat[i] <= 90 && lon[i] >= -180 && lon[i] <= 180)) {
					throw new IllegalArgumentException("Bod okraje mimo rozsah souřadnic: " + lat[i] + ", " + lon[i]);
				}
				if (Math.abs(lon[i] - lon[(i + 1) % lat.length]) > 180) {
					throw new IllegalArgumentException("Okraj přechází přes poledník 180°, to se nepodporuje");
				}
			}
			this.lat = lat.clone();
			this.lon = lon.clone();
		}

		public int pocetBodu() {
			return lat.length;
		}

		public double lat(final int i) {
			return lat[i];
		}

		public double lon(final int i) {
			return lon[i];
		}
	}

	/** Část oblasti: vnější okraj a díry. */
	public static final class Cast {
		final List<Okraj> okraje = new ArrayList<>();

		public Cast(final Okraj vnejsi, final List<Okraj> diry) {
			if (diry.size() + 1 > NEJVIC_OKRAJU) {
				throw new IllegalArgumentException("Část oblasti má víc než " + NEJVIC_OKRAJU + " okrajů (vnější okraj a díry)");
			}
			okraje.add(vnejsi);
			okraje.addAll(diry);
		}

		public List<Okraj> okraje() {
			return Collections.unmodifiableList(okraje);
		}
	}

	/** Nejvíc okrajů (vnější a díry) v jedné části; index okraje se v pásech hran kóduje do horního bajtu. */
	public static final int NEJVIC_OKRAJU = 256;

	private static final int PASU = 64;

	/** Část s předpočítanými pásy hran. */
	private static final class HledaneCast {
		final double minLat;
		final double maxLat;
		final double minLon;
		final double maxLon;
		final double vyskaPasu;
		/** Pro každý pás indexy hran: dvojice (okraj, hrana) zakódovaná jako okraj * 2^24 + hrana. */
		final int[][] hranyVPasu;
		final Okraj[] okraje;

		HledaneCast(final Cast cast) {
			okraje = cast.okraje.toArray(new Okraj[0]);
			double mnLat = Double.MAX_VALUE;
			double mxLat = -Double.MAX_VALUE;
			double mnLon = Double.MAX_VALUE;
			double mxLon = -Double.MAX_VALUE;
			for (final Okraj o : okraje) {
				for (int i = 0; i < o.lat.length; i++) {
					mnLat = Math.min(mnLat, o.lat[i]);
					mxLat = Math.max(mxLat, o.lat[i]);
					mnLon = Math.min(mnLon, o.lon[i]);
					mxLon = Math.max(mxLon, o.lon[i]);
				}
			}
			minLat = mnLat;
			maxLat = mxLat;
			minLon = mnLon;
			maxLon = mxLon;
			vyskaPasu = (maxLat - minLat) / PASU;
			final List<List<Integer>> pasy = new ArrayList<>();
			for (int p = 0; p < PASU; p++) {
				pasy.add(new ArrayList<>());
			}
			for (int oi = 0; oi < okraje.length; oi++) {
				final Okraj o = okraje[oi];
				final int n = o.lat.length;
				for (int i = 0; i < n; i++) {
					final double a = o.lat[i];
					final double b = o.lat[(i + 1) % n];
					if (a == b) {
						continue; // vodorovná hrana průsečík nedává
					}
					final int odPasu = pas(Math.min(a, b));
					final int doPasu = pas(Math.max(a, b));
					for (int p = odPasu; p <= doPasu; p++) {
						pasy.get(p).add(oi << 24 | i);
					}
				}
			}
			hranyVPasu = new int[PASU][];
			for (int p = 0; p < PASU; p++) {
				final List<Integer> v = pasy.get(p);
				hranyVPasu[p] = new int[v.size()];
				for (int i = 0; i < v.size(); i++) {
					hranyVPasu[p][i] = v.get(i);
				}
			}
		}

		private int pas(final double lat) {
			if (vyskaPasu == 0) {
				return 0;
			}
			return Math.max(0, Math.min(PASU - 1, (int) ((lat - minLat) / vyskaPasu)));
		}

		boolean obsahuje(final double lat, final double lon) {
			if (lat < minLat || lat > maxLat || lon < minLon || lon > maxLon) {
				return false;
			}
			boolean uvnitr = false;
			for (final int kod : hranyVPasu[pas(lat)]) {
				final Okraj o = okraje[kod >>> 24];
				final int i = kod & 0xFFFFFF;
				final int j = (i + 1) % o.lat.length;
				final double la = o.lat[i];
				final double lb = o.lat[j];
				if (la > lat != lb > lat) {
					final double lonPruseciku = o.lon[i] + (lat - la) / (lb - la) * (o.lon[j] - o.lon[i]);
					if (lon < lonPruseciku) {
						uvnitr = !uvnitr;
					}
				}
			}
			return uvnitr;
		}
	}

	private final String id;
	private final List<Cast> casti;
	private final List<HledaneCast> hledane = new ArrayList<>();
	private final String verze;

	/**
	 * @param id
	 *            označení oblasti
	 * @param verzeDat
	 *            označení verze zdrojových dat (třeba datum RÚIAN); do {@link #verze()} se přidá otisk geometrie
	 */
	public OblastPolygony(final String id, final String verzeDat, final List<Cast> casti) {
		if (casti.isEmpty()) {
			throw new IllegalArgumentException("Oblast " + id + " nemá žádnou část");
		}
		this.id = id;
		this.casti = new ArrayList<>(casti);
		final CRC32 crc = new CRC32();
		for (final Cast c : casti) {
			hledane.add(new HledaneCast(c));
			for (final Okraj o : c.okraje) {
				for (int i = 0; i < o.lat.length; i++) {
					crc.update(Long.toString(Math.round(o.lat[i] * 1e6)).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
					crc.update(',');
					crc.update(Long.toString(Math.round(o.lon[i] * 1e6)).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
					crc.update(';');
				}
			}
		}
		verze = verzeDat + "/" + String.format("%08x", crc.getValue());
	}

	public String id() {
		return id;
	}

	public List<Cast> casti() {
		return Collections.unmodifiableList(casti);
	}

	@Override
	public boolean obsahuje(final double lat, final double lon) {
		for (final HledaneCast c : hledane) {
			if (c.obsahuje(lat, lon)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String verze() {
		return verze;
	}
}
