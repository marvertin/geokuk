package cz.geokuk.plugins.challenge;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.challenge.ResolverDefinice.Vyreseno;
import lombok.extern.slf4j.Slf4j;

/**
 * Plánovač challenge: při zapnutí, změně definic, aktivních, domova nebo načtených keší (s odkladem, ať se změny sloučí) spočítá na pozadí výsledky aktivních
 * challenge a uloží je do {@link ChallengeModel}. Challenge se počítají postupně jedna po druhé (předpočet každé zabírá paměť), běžící výpočet se novou změnou ruší a po
 * dokončení challenge se předpočet zahodí. Výsledky pro střed mapy a „další chybějící“ viz {@link #dalsiChybejici(Challenge)}.
 * Zatím se nepředává množina ignorovaných keší pro zdroj Stator (je prázdná).
 */
@Slf4j
public final class PlanovacChallenge {

	/** Jak dlouho se čeká na další změnu, než se začne počítat. */
	static final long ODKLAD_MS = 500;

	/** Klíč zdroje dat: poloha keše vůči středu závisí jen na souřadnicích, které předpočet kontroluje u každé keše, takže zdroj do klíče nepatří. */
	private static final String KLIC_ZDROJE = "kese";

	private final ChallengeModel model;
	private final ResolverDefinice resolver;
	private final Supplier<Path> adresar;
	private final Supplier<Wgs> stredMapy;
	private final Consumer<VybranyKandidat> vystredit;
	private final long odklad;
	/** Pro test: zavolá se s každým vytvořeným předpočtem. */
	Consumer<Predpocet> poVytvoreni = p -> {};
	private final ScheduledExecutorService planovac = Executors.newSingleThreadScheduledExecutor(r -> demon(r, "Odklad plánovače challenge"));
	private final ExecutorService vykon = Executors.newSingleThreadExecutor(r -> demon(r, "Plánovač challenge"));
	private final Map<String, DalsiChybejici> dalsi = new HashMap<>();
	private final Map<String, DalsiPolicko> dalsiPolicko = new HashMap<>();
	/** Vstup výsledku D/T a kalendáře podle challenge; mění se při plném přepočtu, ne při přepočtu podle jiného středu mapy. */
	private final Map<String, Object> vstupyPolicek = new HashMap<>();

	/** O kolik metrů se musí střed mapy posunout, aby se D/T a kalendář přepočítaly podle nového středu. */
	static final double PRAH_POSUNU_M = 2000;

	/** Dnešní datum pro kalendář; pro test jde nahradit. */
	Supplier<java.time.LocalDate> dnes = java.time.LocalDate::now;

	/** Střed mapy, podle kterého se naposledy začaly počítat D/T a kalendář (i když výpočet ještě běží); podle něj se rozhoduje, zda je potřeba další přepočet. */
	private volatile Wgs stredPolicek;
	private final java.util.concurrent.atomic.AtomicLong generacePolicek = new java.util.concurrent.atomic.AtomicLong();
	private ScheduledFuture<?> naplanovanoPolicek;

	private volatile Collection<? extends VstupKes> kese;
	private final java.util.concurrent.atomic.AtomicLong generace = new java.util.concurrent.atomic.AtomicLong();
	private volatile Predpocet bezici;
	private ScheduledFuture<?> naplanovano;

	private static Thread demon(final Runnable r, final String jmeno) {
		final Thread t = new Thread(r, jmeno);
		t.setDaemon(true);
		t.setPriority(Thread.NORM_PRIORITY - 2);
		return t;
	}

	/**
	 * @param adresar
	 *            složka pro soubory předpočtu
	 * @param stredMapy
	 *            střed viditelného výřezu mapy
	 * @param vystredit
	 *            vystředí mapu na vybranou keš
	 */
	public PlanovacChallenge(final ChallengeModel model, final PoskytovatelOblasti oblasti, final Supplier<Wgs> domov, final Supplier<Path> adresar, final Supplier<Wgs> stredMapy,
			final Consumer<VybranyKandidat> vystredit) {
		this(model, oblasti, domov, adresar, stredMapy, vystredit, ODKLAD_MS);
	}

	PlanovacChallenge(final ChallengeModel model, final PoskytovatelOblasti oblasti, final Supplier<Wgs> domov, final Supplier<Path> adresar, final Supplier<Wgs> stredMapy,
			final Consumer<VybranyKandidat> vystredit, final long odklad) {
		this.model = model;
		resolver = new ResolverDefinice(oblasti, domov);
		this.adresar = adresar;
		this.stredMapy = stredMapy;
		this.vystredit = vystredit;
		this.odklad = odklad;
	}

	/** Načtené keše se změnily (nový výčet, jiné nálezy). */
	public void keseNacteny(final Collection<? extends VstupKes> kese) {
		this.kese = kese;
		naplanujPrepocet();
	}

	/** Změnil se domov (u challenge se středem v domově se musí přepočítat). */
	public void domovZmenen() {
		naplanujPrepocet();
	}

	/** Změna stavu modelu; přepočet vyvolají jen změny, které ovlivní výsledek. */
	public void modelZmenen(final ChallengeZmenenyEvent event) {
		switch (event.druh()) {
		case NACTENO:
		case ZAPNUTO:
		case DEFINICE:
		case AKTIVNI:
			naplanujPrepocet();
			break;
		default:
			break;
		}
	}

	/** Sloučí změny: nový přepočet začne, až půl sekundy nepřijde další, a běžící se ruší. */
	public synchronized void naplanujPrepocet() {
		if (planovac.isShutdown()) {
			return;
		}
		generace.incrementAndGet();
		final Predpocet p = bezici;
		if (p != null) {
			p.zrus();
		}
		if (naplanovano != null) {
			naplanovano.cancel(false);
		}
		naplanovano = planovac.schedule(() -> SwingUtilities.invokeLater(this::zacniPrepocet), odklad, TimeUnit.MILLISECONDS);
	}

	/** Ve vlákně Swingu se přečte stav modelu a výpočet jde na pozadí. */
	private void zacniPrepocet() {
		final long moje;
		synchronized (this) {
			moje = generace.get();
		}
		final boolean zapnuto = model.jeZapnuto();
		final List<Challenge> aktivni = model.aktivni();
		final Collection<? extends VstupKes> data = kese;
		vykon.execute(() -> {
			try {
				prepocitej(moje, zapnuto, aktivni, data);
			} catch (final RuntimeException e) {
				log.warn("Přepočet challenge selhal: {}", e.toString());
			}
		});
	}

	private boolean zastaralo(final long moje) {
		return generace.get() != moje;
	}

	private void prepocitej(final long moje, final boolean zapnuto, final List<Challenge> aktivni, final Collection<? extends VstupKes> data) {
		if (!zapnuto || aktivni.isEmpty() || data == null) {
			return;
		}
		for (final Challenge c : aktivni) {
			if (zastaralo(moje)) {
				return;
			}
			if (model.jeVUi(c)) {
				prepocitej(moje, c, data);
			}
		}
	}

	private void prepocitej(final long moje, final Challenge c, final Collection<? extends VstupKes> data) {
		if (c.typ() != Challenge.Typ.SEKTORY_360) {
			prepocitejPolicka(() -> zastaralo(moje), c, data, true);
			return;
		}
		final Vyreseno v = resolver.vyres(c, kod -> najdi(data, kod));
		if (!v.jeUplne()) {
			model.nastavStavPredpoctu(c, new StavPredpoctu(StavPredpoctu.Faze.CHYBA, 0, 0, v.duvod));
			return;
		}
		final Predpocet p = new Predpocet(adresar.get(), KLIC_ZDROJE, c.zdroj(), v.lat, v.lon, v.oblast);
		bezici = p;
		try {
			poVytvoreni.accept(p);
			p.pridejPosluchace(s -> {
				if (!zastaralo(moje)) {
					model.nastavStavPredpoctu(c, s);
				}
			});
			final Future<?> f = p.spust(data);
			try {
				f.get();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				p.zrus();
				return;
			} catch (final ExecutionException e) {
				log.warn("Předpočet challenge {} selhal: {}", c.nazev(), e.toString());
				return;
			}
			if (p.stav().faze() != StavPredpoctu.Faze.HOTOVO || zastaralo(moje)) {
				return;
			}
			final VysledekSektoru vysledek = VypocetSektoru.spoctiSKlasifikaci(c.zdroj(), v.lat, v.lon, p.klasifikator(), Collections.<String> emptySet(), data, () -> zastaralo(moje));
			if (vysledek == null || zastaralo(moje)) {
				return;
			}
			synchronized (this) {
				dalsi.remove(c.nazev());
			}
			model.nastavVysledek(c, vysledek, v.lat, v.lon);
		} finally {
			p.zavri();
			if (bezici == p) {
				bezici = null;
			}
		}
	}

	/** Pohled na keše, které umějí zadaný vstup; ostatní se přeskočí. */
	private static <T> Iterable<T> jen(final Collection<? extends VstupKes> data, final Class<T> typ) {
		return () -> new java.util.Iterator<T>() {
			private final java.util.Iterator<? extends VstupKes> it = data.iterator();
			private T dalsi;

			@Override
			public boolean hasNext() {
				while (dalsi == null && it.hasNext()) {
					final VstupKes k = it.next();
					if (typ.isInstance(k)) {
						dalsi = typ.cast(k);
					}
				}
				return dalsi != null;
			}

			@Override
			public T next() {
				if (!hasNext()) {
					throw new java.util.NoSuchElementException();
				}
				final T v = dalsi;
				dalsi = null;
				return v;
			}
		};
	}

	/**
	 * D/T matice a kalendář: čistá funkce nad kešemi, počítá se podle středu mapy.
	 *
	 * @param novyVstup
	 *            plný přepočet (jiné keše nebo definice); výběr „dalšího“ pak začne od začátku, při přepočtu jen podle jiného středu pokračuje
	 */
	private void prepocitejPolicka(final java.util.function.BooleanSupplier zastarale, final Challenge c, final Collection<? extends VstupKes> data, final boolean novyVstup) {
		final Wgs stred = stredMapy.get();
		if (stred == null) {
			model.nastavStavPredpoctu(c, new StavPredpoctu(StavPredpoctu.Faze.CHYBA, 0, 0, "Střed mapy není znám."));
			return;
		}
		stredPolicek = stred;
		model.nastavStavPredpoctu(c, new StavPredpoctu(StavPredpoctu.Faze.POCITA, 0, data.size(), null));
		final VysledekPolicek vysledek;
		if (c.typ() == Challenge.Typ.DT_MATICE) {
			vysledek = VypocetMatice.spocti(PravidlaTypu.proMatici(), stred.lat, stred.lon, jen(data, VstupDT.class));
		} else {
			vysledek = VypocetKalendare.spocti(PravidlaKalendare.vychozi(), dnes.get(), stred.lat, stred.lon, jen(data, VstupKalendar.class));
		}
		if (zastarale.getAsBoolean()) {
			return;
		}
		final Object klic;
		synchronized (this) {
			if (novyVstup || !vstupyPolicek.containsKey(c.nazev())) {
				vstupyPolicek.put(c.nazev(), new Object());
			}
			klic = vstupyPolicek.get(c.nazev());
		}
		model.nastavVysledekPolicek(c, vysledek.sVstupem(klic), stred.lat, stred.lon);
		model.nastavStavPredpoctu(c, new StavPredpoctu(StavPredpoctu.Faze.HOTOVO, data.size(), data.size(), null));
	}

	/**
	 * Střed mapy se změnil: D/T a kalendář se přepočítají podle nového středu, když se střed posunul aspoň o {@link #PRAH_POSUNU_M} od středu posledního výpočtu.
	 * Sektory se nepřepočítávají (nezávisí na středu mapy). Volat ve vlákně Swingu.
	 */
	public void stredMapyZmenen() {
		final Wgs ted = stredMapy.get();
		final Wgs pred = stredPolicek;
		if (ted == null || (pred != null && Geodezie.vzdalenostKoule(pred.lat, pred.lon, ted.lat, ted.lon) < PRAH_POSUNU_M)) {
			return;
		}
		for (final Challenge c : model.aktivni()) {
			if (c.typ() != Challenge.Typ.SEKTORY_360) {
				naplanujPolicka();
				return;
			}
		}
	}

	private synchronized void naplanujPolicka() {
		if (planovac.isShutdown()) {
			return;
		}
		generacePolicek.incrementAndGet();
		if (naplanovanoPolicek != null) {
			naplanovanoPolicek.cancel(false);
		}
		naplanovanoPolicek = planovac.schedule(() -> SwingUtilities.invokeLater(this::zacniPolicka), odklad, TimeUnit.MILLISECONDS);
	}

	private void zacniPolicka() {
		final long moje = generacePolicek.get();
		final boolean zapnuto = model.jeZapnuto();
		final List<Challenge> aktivni = model.aktivni();
		final Collection<? extends VstupKes> data = kese;
		if (!zapnuto || data == null) {
			return;
		}
		vykon.execute(() -> {
			try {
				for (final Challenge c : aktivni) {
					if (c.typ() != Challenge.Typ.SEKTORY_360 && model.jeVUi(c) && generacePolicek.get() == moje) {
						prepocitejPolicka(() -> generacePolicek.get() != moje, c, data, false);
					}
				}
			} catch (final RuntimeException e) {
				stredPolicek = null;
				log.warn("Přepočet D/T a kalendáře selhal: {}", e.toString());
			}
		});
	}

	private static Optional<VstupKes> najdi(final Collection<? extends VstupKes> data, final String kod) {
		for (final VstupKes k : data) {
			if (kod.equalsIgnoreCase(k.id())) {
				return Optional.of(k);
			}
		}
		return Optional.empty();
	}

	/**
	 * „Další chybějící“: vybere kandidáta nejbližšího středu mapy (cyklicky, už vybrané přeskakuje), uloží ho do modelu a vystředí na něj mapu. Volat ve vlákně
	 * Swingu. Vrací vybraného, nebo null, když výsledek nebo kandidáti nejsou.
	 */
	public VybranyKandidat dalsiChybejici(final Challenge c) {
		if (c.typ() != Challenge.Typ.SEKTORY_360) {
			return dalsiPolicko(c);
		}
		final VysledekSektoru v = model.vysledek(c);
		if (v == null) {
			return null;
		}
		final Wgs stred = stredMapy.get();
		if (stred == null) {
			return null;
		}
		final DalsiChybejici d;
		synchronized (this) {
			d = dalsi.computeIfAbsent(c.nazev(), k -> new DalsiChybejici());
		}
		final VybranyKandidat k = d.dalsi(v, stred.lat, stred.lon);
		if (k != null) {
			model.nastavVybranyKandidat(c, k);
			vystredit.accept(k);
		}
		return k;
	}

	private VybranyKandidat dalsiPolicko(final Challenge c) {
		final VysledekPolicek v = model.vysledekPolicek(c);
		final Wgs stred = stredMapy.get();
		if (v == null || stred == null) {
			return null;
		}
		final DalsiPolicko d;
		synchronized (this) {
			d = dalsiPolicko.computeIfAbsent(c.nazev(), k -> new DalsiPolicko());
		}
		final KandidatPolicka k = d.dalsi(v, stred.lat, stred.lon);
		if (k == null) {
			return null;
		}
		final VybranyKandidat vk = new VybranyKandidat(k.id(), k.lat(), k.lon(), -1, Geodezie.vzdalenostKoule(stred.lat, stred.lon, k.lat(), k.lon()), k.popis());
		model.nastavVybranyKandidat(c, vk);
		vystredit.accept(vk);
		return vk;
	}

	/**
	 * „Předchozí chybějící“: vrátí se k předchozímu vybranému kandidátovi a vystředí na něj mapu; null, když není kam. Ve vlákně Swingu.
	 */
	public VybranyKandidat predchoziChybejici(final Challenge c) {
		if (c.typ() != Challenge.Typ.SEKTORY_360) {
			return predchoziPolicko(c);
		}
		final VysledekSektoru v = model.vysledek(c);
		final Wgs stred = stredMapy.get();
		if (v == null || stred == null) {
			return null;
		}
		final DalsiChybejici d;
		synchronized (this) {
			d = dalsi.get(c.nazev());
		}
		final VybranyKandidat k = d == null ? null : d.predchozi(v, stred.lat, stred.lon);
		if (k != null) {
			model.nastavVybranyKandidat(c, k);
			vystredit.accept(k);
		}
		return k;
	}

	private VybranyKandidat predchoziPolicko(final Challenge c) {
		final VysledekPolicek v = model.vysledekPolicek(c);
		final Wgs stred = stredMapy.get();
		if (v == null || stred == null) {
			return null;
		}
		final DalsiPolicko d;
		synchronized (this) {
			d = dalsiPolicko.get(c.nazev());
		}
		final KandidatPolicka k = d == null ? null : d.predchozi(v);
		if (k == null) {
			return null;
		}
		final VybranyKandidat vk = new VybranyKandidat(k.id(), k.lat(), k.lon(), -1, Geodezie.vzdalenostKoule(stred.lat, stred.lon, k.lat(), k.lon()), k.popis());
		model.nastavVybranyKandidat(c, vk);
		vystredit.accept(vk);
		return vk;
	}

	/** Zastaví plánovač a uvolní paměť předpočtu. */
	public synchronized void zavri() {
		generace.incrementAndGet();
		generacePolicek.incrementAndGet();
		final Predpocet p = bezici;
		if (p != null) {
			p.zrus();
		}
		planovac.shutdownNow();
		vykon.shutdownNow();
	}
}
