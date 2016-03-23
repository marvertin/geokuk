package cz.geokuk.plugins.cesty.data;

import java.awt.Color;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.coordinates.*;
import cz.geokuk.plugins.cesty.FBarvy;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.util.lang.FUtil;

public class Cesta implements Iterable<Bousek0> {

	public static class SearchResult {
		public Bousek0	bousek;
		public long		kvadradVzdalenosti	= Long.MAX_VALUE;
	}

	private static final Logger	log	= LogManager.getLogger(Cesta.class.getSimpleName());
	/// prazdna cesta ma prazdny start i cil, ale snad by ani nemala byt
	private Bod					start;
	private Bod					cil;
	private Doc					doc;

	private String				nazev;

	public static Cesta create() {
		return new Cesta();
	}

	/**
	 * Dálku v km v zadané barvě
	 */
	public static String dalkaHtml(final double dalka, final Color color) {
		return String.format("<font color=%s><i>%.1f km</i></font>", FUtil.getHtmlColor(color), dalka / 1000);
	}

	private Cesta() {}

	public double dalka() {
		double suma = 0;
		for (final Usek usek : getUseky()) {
			suma += usek.dalka();
		}
		return suma;
	}

	public String dalkaHtml() {
		return dalkaHtml(dalka(), FBarvy.CURTA_NORMALNE);
	}

	public double dalkaStartuACile() {
		// System.out.println(System.identityHashCode(this) + ": dalkaStartuACile "+ start + " " + cil);
		kontrolaKonzistence();
		return FGeoKonvertor.dalka(start, cil);
	}

	public String dalkaStartuACileHtml() {
		return dalkaHtml(dalkaStartuACile(), Color.BLACK);
	}

	/**
	 * Vrací seznam všech bodů cesty.
	 *
	 * @return
	 */
	public Iterable<Bod> getBody() {
		return () -> new Iterator<Bod>() {

			private Bod nextBod = start;

			@Override
			public boolean hasNext() {
				return nextBod != null;
			}

			@Override
			public Bod next() {
				final Bod result = nextBod;
				nextBod = nextBod.getUvpred() == null ? null : nextBod.getUvpred().getBvpred();
				return result;
			}

			@Override
			public void remove() {

				throw new RuntimeException("Bod nelze odstranit, protože to není implementováno");
			}
		};
	}

	/**
	 * Vrací seznam všech bodů cesty.
	 *
	 * @return
	 */
	public Iterable<Bousek0> getBousky() {
		return () -> new Iterator<Bousek0>() {

			private Bousek0 nextBousek = start;

			@Override
			public boolean hasNext() {
				return nextBousek != null;
			}

			@Override
			public Bousek0 next() {
				final Bousek0 result = nextBousek;
				nextBousek = nextBousek.getBousekVpred() == null ? null : nextBousek.getBousekVpred();
				return result;
			}

			@Override
			public void remove() {

				throw new RuntimeException("Bod nelze odstranit, protože to není implementováno");
			}
		};
	}

	public Bod getCil() {
		return cil;
	}

	public String getNazev() {
		return nazev;
	}

	public String getNazevADalkaHtml() {
		return getNazevHtml() + " " + dalkaHtml();
	}

	public String getNazevHtml() {
		if (nazev != null) {
			// Ta mezera je tam kvůli bezejmenným cestám
			return " <i>\"" + nazev + "\"</i>";
		} else {
			return "";
		}
	}

	/**
	 * Vrátí kolik podcest se souvislými úseky v cestě existuje.
	 *
	 * @return Nemůže být větší než počet vzdušných úseků a nemůže být menší než 1, pokud nějaký vzdušný úsek existuje.
	 */
	public int getPocetPodcestVzdusnychUseku() {
		int pocet = 0;
		boolean minuleBylVzdusny = false;
		for (final Usek usek : getUseky()) {
			if (usek.isVzdusny() && !minuleBylVzdusny) {
				pocet++;
			}
			minuleBylVzdusny = usek.isVzdusny();
		}
		return pocet;
	}

	public int getPocetVzdusnychUseku() {
		int pocet = 0;
		for (final Usek usek : getUseky()) {
			if (usek.isVzdusny()) {
				pocet++;
			}
		}
		return pocet;
	}

	public int getPocetWaypointu() {
		// System.out.println("Volá se getPocetWaypointu");
		// new Throwable().printStackTrace();
		// TODO-vylet Optimalizovat, volá se často.
		int sum = 0;
		for (final Bod bod : getBody()) {
			if (bod.mouable instanceof Wpt) {
				sum++;
			}
		}
		return sum;
	}

	public Bod getStart() {
		return start;
	}

	/**
	 * Vrací seznam všechj úseků cesty.
	 *
	 * @return
	 */
	public Iterable<Usek> getUseky() {
		return () -> new Iterator<Usek>() {

			private Usek currUsek = start == null ? null : start.getUvpred();

			@Override
			public boolean hasNext() {
				return currUsek != null;
			}

			@Override
			public Usek next() {
				final Usek result = currUsek;
				currUsek = currUsek.getBvpred().getUvpred();
				return result;
			}

			@Override
			public void remove() {
				throw new RuntimeException("Usek nelze odstranit, co se temi body");
			}
		};
	}

	/**
	 * Vrátí množinu waypointů, které jsou v cestě.
	 *
	 * @return
	 */
	public Set<Wpt> getWpts() {
		final Set<Wpt> wpts = new HashSet<>();
		for (final Bod bod : getBody()) {
			if (bod.mouable instanceof Wpt) {
				wpts.add((Wpt) bod.mouable);
			}
		}
		return wpts;
	}

	public boolean hasBod(final Mou mou) {
		for (final Bod bod : getBody()) {
			if (bod.getMou().equals(mou)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasWpt(final Wpt wpt) {
		// System.out.println("Volá se hasWpt");
		// TODO-vylet Optimalizovat, volá se často.
		for (final Bod bod : getBody()) {
			if (bod.mouable == wpt) {
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty() {
		return start == null;
	}

	public boolean isJednobodova() {
		return start.equals(cil);
	}

	public boolean isKruh() {
		if (isEmpty()) {
			return false; // prázdná není kruh
		}
		if (isJednobodova()) {
			return false; // jednobodová není kruh
		}
		final boolean result = start.getMouable().equals(cil.getMouable());
		return result;
	}

	@Override
	public Iterator<Bousek0> iterator() {
		return getBousky().iterator();
	}

	public void kontrolaKonzistence() {
		boolean assertsEnabled = false;
		assert assertsEnabled = true;
		if (!assertsEnabled) {
			return;
		}

		kon(doc != null);
		doc.kontrolaZeJeTady(this);

		kon(start == null && cil == null || start != null && cil != null);
		if (start == null) {
			return;
		}
		kon(start.getUvzad() == null);
		kon(cil.getUvpred() == null);

		for (Bousek0 bousek = start; bousek != null; bousek = bousek.getBousekVpred()) {
			kon(bousek.getCesta() == this);
			bousek.kontrolaKonzistence();
			if (bousek.getBousekVpred() == null) {
				kon(bousek.equals(cil));
			}
		}

		for (Bousek0 bousek = cil; bousek != null; bousek = bousek.getBousekVzad()) {
			kon(bousek.getCesta() == this);
			bousek.kontrolaKonzistence();
			if (bousek.getBousekVzad() == null) {
				kon(bousek.equals(start));
			}
		}

		// System.out.println(System.identityHashCode(this) + ": uspela kontrola existence: " + pocetBousku + " bousku " + ATimestamp.now());
	}

	/**
	 * Najde úsek, do kterého je nejlepší daný bod vložit. Pokud vrátí bod, je to bod krajový a je nejlepší vložit za něj.
	 *
	 * @param mou
	 *            Null vrací jen pokud je cesta úplně prázdná.
	 * @return
	 */
	public Bousek0 locateBousekKamNejlepeVlozit(final Mou mou) {
		final Bousek0 bousek = locateNejblizsi(mou);
		if (bousek instanceof Usek) {
			final Usek usek = (Usek) bousek;
			return usek;
		}
		if (bousek instanceof Bod) {
			final Bod bod = (Bod) bousek;
			final Usek usek = bod.getBlizsiUsek(mou);
			return usek == null ? bod : usek;
		}
		return null; // cesta je úplně prázdná
	}

	/**
	 * Vrací nejbližší objekt. Dá se vždy přednost bodu před úsekem. Vrací null, pokud nic není do zadané vzdálenosti
	 *
	 * @param mou
	 * @param maximalniVzdalenost
	 * @return
	 */
	public Bousek0 locateNejblizsi(final Mou mou) {
		long minKvadrat = Long.MAX_VALUE;
		Bousek0 neblizsiUsta = null;
		for (final Bod bod : getBody()) {
			final long kvadratVzdalenosti = bod.computeKvadratVzdalenosti(mou);
			if (kvadratVzdalenosti < minKvadrat) {
				minKvadrat = kvadratVzdalenosti;
				neblizsiUsta = bod;
			}
		}
		for (final Usek usek : getUseky()) {
			final long kvadratVzdalenosti = usek.computeKvadratVzdalenosti(mou);
			if (kvadratVzdalenosti < minKvadrat) {
				minKvadrat = kvadratVzdalenosti;
				neblizsiUsta = usek;
			}
		}
		return neblizsiUsta;
	}

	/**
	 * Vrací nejbližší objekt, ale jen když je do zadané vzálenosti. Dá se vždy přednost bodu před úsekem. Vrací null, pokud nic není do zadané vzdálenosti
	 *
	 * @param mou
	 * @param maximalniVzdalenost
	 * @return
	 */
	public SearchResult locateNejblizsiDoKvadratuVzdalenosi(final Mou mou, final long kvadratMaximalniVzdalenosti, final boolean aDatPrednostBoduPredUsekem) {
		long minKvadrat = Long.MAX_VALUE;
		Bousek0 neblizsiBousek = null;
		for (final Bod bod : getBody()) {
			final long kvadratVzdalenosti = bod.computeKvadratVzdalenosti(mou);
			if (kvadratVzdalenosti <= kvadratMaximalniVzdalenosti) {
				if (kvadratVzdalenosti <= minKvadrat) {
					minKvadrat = kvadratVzdalenosti;
					neblizsiBousek = bod;
				}
			}
		}
		if (aDatPrednostBoduPredUsekem && neblizsiBousek != null) {
			final SearchResult sr = new SearchResult();
			sr.bousek = neblizsiBousek;
			sr.kvadradVzdalenosti = minKvadrat;
			return sr;
		}
		for (final Usek usek : getUseky()) {
			final long kvadratVzdalenosti = usek.computeKvadratVzdalenostiBoduKUsecce(mou);
			if (kvadratVzdalenosti <= kvadratMaximalniVzdalenosti) {
				if (kvadratVzdalenosti < minKvadrat) {
					minKvadrat = kvadratVzdalenosti;
					neblizsiBousek = usek;
				}
			}
		}
		final SearchResult sr = new SearchResult();
		sr.bousek = neblizsiBousek;
		sr.kvadradVzdalenosti = minKvadrat;
		return sr;
	}

	public void napojBouskyNaTutoCestu() {
		for (final Bousek0 bousek : this) {
			bousek.cesta = this;
		}
	}

	public void pospojujVzdusneUseky() {
		final List<Bod> keSmazani = new ArrayList<>();
		for (final Bod bod : getBody()) {
			if (bod.isMeziVzdusnymiUseky()) {
				keSmazani.add(bod);
			}
		}
		for (final Bod bod : keSmazani) {
			bod.remove();
		}
	}

	void clear() {
		// System.out.println(System.identityHashCode(this) + ": clear()");
		// new Throwable().printStackTrace(System.out);
		start = null;
		cil = null;
	}

	Bod createBod(final Mouable mouable) {
		return new Bod(this, mouable);
	}

	Usek createUsek() {
		return new Usek(this);
	}

	Doc getDoc() {
		return doc;
	}

	Usek odeberBodNechceme(final Mouable mouable) {
		if (mouable instanceof Bod) {
			final Bod bod = (Bod) mouable;
			final Usek usek = bod.remove();
			return usek;
		} else {
			for (final Bod bod : getBody()) {
				if (bod.mouable == mouable) {
					final Usek usek = bod.remove();
					return usek;
				}
			}
		}
		return null;
	}

	Bod pridejNaKonec(final Mouable mouable) {
		if (isEmpty()) {
			return zacni(mouable);
		} else if (jsouMocBlizko(cil, mouable)) {
			return cil;
		} else {
			final Bod bod = createBod(mouable);
			final Usek usek = createUsek();
			usek.spoj(cil, bod);
			cil = bod;
			return bod;
		}
	}

	Bod pridejNaMisto(final Mouable mouable) {
		final Bousek0 usbod = locateBousekKamNejlepeVlozit(mouable.getMou());
		if (usbod instanceof Usek) {
			final Usek usek = (Usek) usbod;
			final Bod bod = usek.rozdelAZanikni(mouable);
			return bod;
		} else if (usbod instanceof Bod) {
			final Bod bod = (Bod) usbod;
			Bod b = null;
			if (bod.equals(start)) {
				b = pridejNaZacatek(mouable);
			} else {
				b = pridejNaKonec(mouable);
			}
			assert b != null;
			return b;
		} else {
			return zacni(mouable);
		}
	}

	Bod pridejNaZacatek(final Mouable mouable) {
		if (isEmpty()) {
			return zacni(mouable);
		} else {
			final Bod bod = createBod(mouable);
			if (jsouMocBlizko(bod, start)) {
				return start;
			}
			final Usek usek = createUsek();
			usek.spoj(bod, start);
			start = bod;
			return bod;
		}
	}

	/**
	 * Přijme body z jiné cesty a tuto cestu vyprázdní
	 *
	 * @param cesta
	 */
	void prijmyBodyZJine(final Cesta cesta) {
		assert cesta != this;
		setStart(cesta.getStart());
		setCil(cesta.getCil());
		cesta.clear();
		log.debug(System.identityHashCode(this) + ": prijmyBodyZJine() " + System.identityHashCode(cesta) + start + " " + cil);
		napojBouskyNaTutoCestu();
	}

	void pripojitPred(final Cesta cesta1) {
		final Cesta cesta = Doc.propojCestyDoJine(cesta1, this);
		prijmyBodyZJine(cesta);
		doc.removex(cesta1);
	}

	void pripojitZa(final Cesta cesta2) {
		final Cesta cesta = Doc.propojCestyDoJine(this, cesta2);
		prijmyBodyZJine(cesta);
		doc.removex(cesta2);
	}

	void remove() {
		if (doc == null) {
			return;
		}
		doc.removex(this);
		doc = null;
	}

	void reverse() {
		final ArrayList<Usek> useky = new ArrayList<>();
		for (final Usek usek : getUseky()) {
			useky.add(usek);
		}
		for (final Usek usek : useky) {
			final Bod bod = usek.getBvpred();
			usek.setBvpred(usek.getBvzad());
			usek.setBvzad(bod);
		}
		for (final Usek usek : useky) {
			usek.getBvpred().setUvzad(usek);
			usek.getBvzad().setUvpred(usek);
		}
		final Bod bod = start;
		start = cil;
		cil = bod;
		start.setUvzad(null);
		cil.setUvpred(null);
		setChanged();
	}

	void setCil(final Bod cil) {
		if (cil == null) {
			clear();
		} else {
			this.cil = cil;
			cil.setUvpred(null); // odříznout zbytek ke konci
		}
	}

	void setDoc(final Doc doc2) {
		doc = doc2;
	}

	void setChanged() {
		if (doc != null) {
			doc.setChanged();
		}
	}

	void setNazev(final String nazev) {
		this.nazev = nazev;
	}

	void setStart(final Bod start) {
		if (start == null) {
			clear();
		} else {
			this.start = start;
			start.setUvzad(null); // odříznout zbytek k začátku
		}
	}

	/**
	 * Zahájí novou cestu, zruší vše, co zde bylo.
	 */
	Bod zacni(final Mouable mouable) {
		start = createBod(mouable);
		cil = start;
		return start;
	}

	private boolean jsouMocBlizko(final Mouable b1, final Mouable b2) {
		final long kvadratVzdalenosti = FUtil.soucetKvadratu(b1.getMou().xx - b2.getMou().xx, b1.getMou().yy - b2.getMou().yy);
		return kvadratVzdalenosti < 1000;
	}

	private void kon(final boolean podm) {
		if (!podm) {
			throw new RuntimeException("Selhala kontrola konzistence cesty");
		}
	}

}
