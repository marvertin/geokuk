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
			prepocitej(moje, c, data);
		}
	}

	private void prepocitej(final long moje, final Challenge c, final Collection<? extends VstupKes> data) {
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
			model.nastavVysledek(c, vysledek);
		} finally {
			p.zavri();
			if (bezici == p) {
				bezici = null;
			}
		}
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

	/** Zastaví plánovač a uvolní paměť předpočtu. */
	public synchronized void zavri() {
		generace.incrementAndGet();
		final Predpocet p = bezici;
		if (p != null) {
			p.zrus();
		}
		planovac.shutdownNow();
		vykon.shutdownNow();
	}
}
