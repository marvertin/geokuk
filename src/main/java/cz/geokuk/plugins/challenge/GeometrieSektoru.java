package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;

/**
 * Geometrie challenge 360° pro kreslení na mapě: body, oblouky, klíny a prstenec sektorů kolem středu v zeměpisných souřadnicích. Jde o čisté funkce bez Swingu,
 * kreslení jen převede souřadnice na obrazovku. Směry se počítají stejným modelem jako výpočet sektorů ({@link Model}), takže hranice klínu leží přesně tam, kde se
 * sektor mění.
 */
public final class GeometrieSektoru {

	/** Model Země, po kterém se měří azimut a vzdálenost. */
	public enum Model {
		/** Koule (pravidla Project-GC). */
		KOULE,
		/** Elipsoid WGS84 (pravidla Stator). */
		ELIPSOID;

		public static Model zPravidel(final ZdrojPravidel zdroj) {
			return zdroj == ZdrojPravidel.STATOR ? ELIPSOID : KOULE;
		}
	}

	/** Stav úseku prstence. */
	public enum Stav {
		SPLNENY, CHYBI,
		/** Sektor je splněný jen nejistě nebo chybí těsně vedle nejistého (viz {@link Geodezie#NEJISTOTA_STUPNU}). */
		NEJISTY
	}

	/** Bod v zeměpisných souřadnicích. */
	public static final class Bod {
		public final double lat;
		public final double lon;

		Bod(final double lat, final double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		@Override
		public String toString() {
			return lat + ", " + lon;
		}
	}

	/** Souvislý úsek prstence se stejným stavem; {@code body} je jeho oblouk ve směru hodinových ručiček. */
	public static final class Usek {
		private final int zacatekSektor;
		private final int pocetSektoru;
		private final Stav stav;
		private final List<Bod> body;

		Usek(final int zacatekSektor, final int pocetSektoru, final Stav stav, final List<Bod> body) {
			this.zacatekSektor = zacatekSektor;
			this.pocetSektoru = pocetSektoru;
			this.stav = stav;
			this.body = Collections.unmodifiableList(body);
		}

		/** První sektor úseku; úsek pokračuje dál po směru hodinových ručiček a může přes 359 přejít na 0. */
		public int zacatekSektor() {
			return zacatekSektor;
		}

		public int pocetSektoru() {
			return pocetSektoru;
		}

		public Stav stav() {
			return stav;
		}

		public List<Bod> body() {
			return body;
		}
	}

	/** Tolerance porovnání vzdálenosti s hranicí poloměru v metrech. */
	public static final double TOLERANCE_POLOMERU = 1e-3;

	/** Největší úhlový krok oblouku ve stupních. */
	public static final double KROK_OBLOUKU = 1.0;

	private static final double A = 6378137.0;
	private static final double F = 1 / 298.257223563;

	private GeometrieSektoru() {}

	private static void zkontrolujStred(final double lat, final double lon) {
		if (!VypocetSektoru.jePlatnyStred(lat, lon)) {
			throw new IllegalArgumentException("Střed mimo rozsah souřadnic: " + lat + ", " + lon);
		}
	}

	/** Bod ve zadané vzdálenosti (m) a azimutu (stupně) od středu. */
	public static Bod bod(final double lat, final double lon, final double azimut, final double metru, final Model model) {
		zkontrolujStred(lat, lon);
		if (Double.isNaN(azimut) || Double.isInfinite(azimut)) {
			throw new IllegalArgumentException("Azimut není konečné číslo: " + azimut);
		}
		if (!(metru >= 0) || Double.isInfinite(metru)) {
			throw new IllegalArgumentException("Vzdálenost musí být nezáporná: " + metru);
		}
		return model == Model.ELIPSOID ? bodElipsoid(lat, lon, azimut, metru) : bodKoule(lat, lon, azimut, metru);
	}

	private static Bod bodKoule(final double lat, final double lon, final double azimut, final double metru) {
		final double d = metru / Geodezie.POLOMER_ZEME;
		final double a = Math.toRadians(azimut);
		final double p1 = Math.toRadians(lat);
		final double p2 = Math.asin(Math.sin(p1) * Math.cos(d) + Math.cos(p1) * Math.sin(d) * Math.cos(a));
		final double dl = Math.atan2(Math.sin(a) * Math.sin(d) * Math.cos(p1), Math.cos(d) - Math.sin(p1) * Math.sin(p2));
		return new Bod(Math.toDegrees(p2), normalizujDelku(lon + Math.toDegrees(dl)));
	}

	/** Přímá úloha podle Vincentyho. */
	private static Bod bodElipsoid(final double lat, final double lon, final double azimut, final double metru) {
		final double b = (1 - F) * A;
		final double alfa1 = Math.toRadians(azimut);
		final double sinAlfa1 = Math.sin(alfa1);
		final double cosAlfa1 = Math.cos(alfa1);
		final double tanU1 = (1 - F) * Math.tan(Math.toRadians(lat));
		final double cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1);
		final double sinU1 = tanU1 * cosU1;
		final double sigma1 = Math.atan2(tanU1, cosAlfa1);
		final double sinAlfa = cosU1 * sinAlfa1;
		final double cos2Alfa = 1 - sinAlfa * sinAlfa;
		final double u2 = cos2Alfa * (A * A - b * b) / (b * b);
		final double kA = 1 + u2 / 16384 * (4096 + u2 * (-768 + u2 * (320 - 175 * u2)));
		final double kB = u2 / 1024 * (256 + u2 * (-128 + u2 * (74 - 47 * u2)));
		double sigma = metru / (b * kA);
		double cos2SigmaM = 0;
		double sinSigma = 0;
		double cosSigma = 1;
		for (int i = 0; i < 100; i++) {
			cos2SigmaM = Math.cos(2 * sigma1 + sigma);
			sinSigma = Math.sin(sigma);
			cosSigma = Math.cos(sigma);
			final double deltaSigma = kB * sinSigma
					* (cos2SigmaM + kB / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - kB / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
			final double nova = metru / (b * kA) + deltaSigma;
			if (Math.abs(nova - sigma) < 1e-12) {
				sigma = nova;
				cos2SigmaM = Math.cos(2 * sigma1 + sigma);
				sinSigma = Math.sin(sigma);
				cosSigma = Math.cos(sigma);
				break;
			}
			sigma = nova;
		}
		final double tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlfa1;
		final double lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlfa1, (1 - F) * Math.sqrt(sinAlfa * sinAlfa + tmp * tmp));
		final double lambda = Math.atan2(sinSigma * sinAlfa1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlfa1);
		final double c = F / 16 * cos2Alfa * (4 + F * (4 - 3 * cos2Alfa));
		final double l = lambda - (1 - c) * F * sinAlfa * (sigma + c * sinSigma * (cos2SigmaM + c * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
		return new Bod(Math.toDegrees(lat2), normalizujDelku(lon + Math.toDegrees(l)));
	}

	private static double normalizujDelku(final double lon) {
		double l = (lon + 180) % 360;
		if (l < 0) {
			l += 360;
		}
		return l - 180;
	}

	/** Azimut převedený do rozsahu 0 až méně než 360. */
	private static double azimut360(final double a) {
		double x = a % 360;
		if (x < 0) {
			x += 360;
		}
		return x;
	}

	/**
	 * Oblouk kružnice o poloměru {@code metru} od azimutu {@code od} po azimut {@code doAz} ve směru hodinových ručiček; přes sever se přechází (od 359 do 1 má 2°).
	 * Rozdíl 0 dává jediný bod, nenulový násobek 360 celou kružnici (první a poslední bod jsou shodné), jinak se rozdíl bere v rozsahu od 0 do 360.
	 * <p>
	 * Délky bodů jsou v rozsahu -180 až 180, takže oblouk přes 180° (antimeridián) má mezi sousedními body skok téměř 360°; před kreslením ho vyrovná
	 * {@link #odvinuteDelky(List)}.
	 */
	public static List<Bod> oblouk(final double lat, final double lon, final double od, final double doAz, final double metru, final Model model) {
		final double rozdil = doAz - od;
		double sirka = azimut360(rozdil);
		if (rozdil != 0 && sirka == 0) {
			sirka = 360;
		}
		if (sirka == 0) {
			return new ArrayList<>(Collections.singletonList(bod(lat, lon, od, metru, model)));
		}
		final int useku = Math.max(1, (int) Math.ceil(sirka / KROK_OBLOUKU - 1e-9));
		final List<Bod> body = new ArrayList<>(useku + 1);
		for (int i = 0; i <= useku; i++) {
			body.add(bod(lat, lon, od + sirka * i / useku, metru, model));
		}
		return body;
	}

	/**
	 * Vyrovná skoky délky mezi sousedními body: každá další délka se posune o násobek 360°, aby ležela nejvýš 180° od předchozí. Výsledek může mít délky mimo -180 až 180
	 * (třeba 179,95 a potom 180,05), kreslení pak nevede čáru přes celou mapu; převod na obrazovku s tím musí počítat (mapa se opakuje nebo se bod přesune o 360°).
	 */
	public static List<Bod> odvinuteDelky(final List<Bod> body) {
		final List<Bod> vysledek = new ArrayList<>(body.size());
		double predchozi = 0;
		for (int i = 0; i < body.size(); i++) {
			double lon = body.get(i).lon;
			if (i > 0) {
				while (lon - predchozi > 180) {
					lon -= 360;
				}
				while (lon - predchozi < -180) {
					lon += 360;
				}
			}
			vysledek.add(new Bod(body.get(i).lat, lon));
			predchozi = lon;
		}
		return vysledek;
	}

	/**
	 * Mnohoúhelník sektoru (klín nebo jeho výřez mezi dvěma poloměry): vnější oblouk od {@code sektor} po {@code sektor + 1} a zpět vnitřní oblouk; při nulovém
	 * vnitřním poloměru se vrací do středu.
	 */
	public static List<Bod> klin(final double lat, final double lon, final int sektor, final double polomerMin, final double polomerMax, final Model model) {
		zkontrolujSektor(sektor);
		zkontrolujPolomery(polomerMin, polomerMax);
		final List<Bod> vnejsi = oblouk(lat, lon, sektor, sektor + 1, polomerMax, model);
		final List<Bod> mnohouhelnik = new ArrayList<>(vnejsi);
		if (polomerMin <= 0) {
			mnohouhelnik.add(bod(lat, lon, 0, 0, model));
		} else {
			final List<Bod> vnitrni = oblouk(lat, lon, sektor, sektor + 1, polomerMin, model);
			Collections.reverse(vnitrni);
			mnohouhelnik.addAll(vnitrni);
		}
		return mnohouhelnik;
	}

	/** Hraniční paprsek sektoru: úsečka od vnitřního po vnější poloměr při azimutu celého stupně. */
	public static List<Bod> paprsek(final double lat, final double lon, final double azimut, final double polomerMin, final double polomerMax, final Model model) {
		zkontrolujPolomery(polomerMin, polomerMax);
		final List<Bod> body = new ArrayList<>(2);
		body.add(bod(lat, lon, azimut, polomerMin, model));
		body.add(bod(lat, lon, azimut, polomerMax, model));
		return body;
	}

	/** Polohy popisků světových stran 0°, 90°, 180°, 270° na kružnici o daném poloměru. */
	public static List<Bod> svetoveStrany(final double lat, final double lon, final double metru, final Model model) {
		final List<Bod> body = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			body.add(bod(lat, lon, 90 * i, metru, model));
		}
		return body;
	}

	/**
	 * Prstenec: 360 sektorů sloučených do souvislých úseků stejného stavu, v pořadí od prvního sektoru, který začíná nový úsek. Úsek, který přechází přes sever, je jeden
	 * (z 359 do 0). Když mají všechny sektory stejný stav, vznikne jeden úsek s celou kružnicí.
	 */
	public static List<Usek> prstenec(final VysledekSektoru vysledek, final double lat, final double lon, final double polomer, final Model model) {
		final Stav[] stavy = new Stav[360];
		for (int s = 0; s < 360; s++) {
			stavy[s] = stavSektoru(vysledek, s);
		}
		int zacatek = -1;
		for (int s = 0; s < 360; s++) {
			if (stavy[s] != stavy[(s + 359) % 360]) {
				zacatek = s;
				break;
			}
		}
		final List<Usek> useky = new ArrayList<>();
		if (zacatek < 0) {
			useky.add(new Usek(0, 360, stavy[0], oblouk(lat, lon, 0, 360, polomer, model)));
			return useky;
		}
		int s = zacatek;
		int zpracovano = 0;
		while (zpracovano < 360) {
			final Stav stav = stavy[s % 360];
			int delka = 1;
			while (zpracovano + delka < 360 && stavy[(s + delka) % 360] == stav) {
				delka++;
			}
			useky.add(new Usek(s % 360, delka, stav, oblouk(lat, lon, s, s + delka, polomer, model)));
			s += delka;
			zpracovano += delka;
		}
		return useky;
	}

	/** Stav sektoru: splněný, chybí, nebo nejistý. */
	public static Stav stavSektoru(final VysledekSektoru vysledek, final int sektor) {
		zkontrolujSektor(sektor);
		if (vysledek.jeNejisty(sektor)) {
			return Stav.NEJISTY;
		}
		return vysledek.jeObsazeny(sektor) ? Stav.SPLNENY : Stav.CHYBI;
	}

	/**
	 * Sektor bodu podle jeho azimutu od středu, nebo -1, když je bod ve středu, blíž než {@code polomerMin} nebo dál než {@code polomerMax}. Hranice poloměrů patří
	 * dovnitř s přesností {@link #TOLERANCE_POLOMERU} (zaokrouhlení výpočtu bodu a vzdálenosti). Vzdálenost i azimut se měří v zadaném modelu (u elipsoidu po elipsoidu, u téměř protilehlých bodů po kouli), stejně jako v {@link #bod}.
	 */
	public static int sektorBodu(final double lat, final double lon, final double bodLat, final double bodLon, final double polomerMin, final double polomerMax, final Model model) {
		zkontrolujStred(lat, lon);
		zkontrolujPolomery(polomerMin, polomerMax);
		if (!VypocetSektoru.jePlatnyStred(bodLat, bodLon)) {
			return -1;
		}
		double vzdalenost = model == Model.ELIPSOID ? Geodezie.vzdalenostElipsoid(lat, lon, bodLat, bodLon) : Double.NaN;
		if (Double.isNaN(vzdalenost)) {
			vzdalenost = Geodezie.vzdalenostKoule(lat, lon, bodLat, bodLon);
		}
		if (vzdalenost < Geodezie.STEJNY_BOD_M || vzdalenost < polomerMin - TOLERANCE_POLOMERU || vzdalenost > polomerMax + TOLERANCE_POLOMERU) {
			return -1;
		}
		double azimut = model == Model.ELIPSOID ? Geodezie.azimutElipsoid(lat, lon, bodLat, bodLon) : Double.NaN;
		if (Double.isNaN(azimut)) {
			azimut = Geodezie.azimutKoule(lat, lon, bodLat, bodLon);
		}
		return Geodezie.sektor(azimut);
	}

	private static void zkontrolujSektor(final int sektor) {
		if (sektor < 0 || sektor > 359) {
			throw new IllegalArgumentException("Sektor mimo 0 až 359: " + sektor);
		}
	}

	private static void zkontrolujPolomery(final double min, final double max) {
		if (!(min >= 0) || !(max > min) || Double.isInfinite(max)) {
			throw new IllegalArgumentException("Poloměry musí splňovat 0 <= min < max: " + min + ", " + max);
		}
	}
}
