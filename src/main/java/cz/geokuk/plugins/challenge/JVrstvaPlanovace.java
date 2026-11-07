package cz.geokuk.plugins.challenge;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import cz.geokuk.core.coord.JSingleSlide0;

/** Společný předek vrstev plánovače na mapě: čte přehled aktivní challenge z modelu a drží podklad ke kreslení, dokud se výsledek nebo výběr nezmění. */
abstract class JVrstvaPlanovace extends JSingleSlide0 {

	private static final long serialVersionUID = 1L;

	/** Popisek „0“ na začátku stupnice měřítka vyčnívá přes okraj jeho oblasti; o tolik se překážka rozšíří na obě strany. */
	static final int PRESAH_MERITKA = 16;

	protected transient ChallengeModel model;
	private transient java.util.concurrent.Executor stavba;
	private final transient Runnable poHotovo = this::repaint;
	private transient TvarPlanovace tvar;
	private transient Supplier<? extends Collection<Rectangle>> prekazky = Collections::emptyList;

	JVrstvaPlanovace() {
		setOpaque(false);
		setCursor(null);
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	/** Překážky, kterým se značky vyhýbají: panely v rozích a legenda (viz {@link cz.geokuk.framework.RozmisteniPrekryvu#prekazky}); volá se při kreslení. */
	public void setPrekazky(final Supplier<? extends Collection<Rectangle>> prekazky) {
		this.prekazky = prekazky;
	}

	/** Překážky na mapě dané velikosti: panely a legenda od správce rozmístění, měřítko, pruh plánovače a ukazatel výkonu. */
	List<Rectangle> prekazky(final int sirka, final int vyska) {
		final Collection<Rectangle> panely = prekazky.get();
		final List<Rectangle> l = new ArrayList<>(panely);
		final Rectangle meritko = cz.geokuk.framework.RozmisteniPrekryvu.meritko(new Dimension(sirka, vyska));
		meritko.grow(PRESAH_MERITKA, 0);
		l.add(meritko);
		l.add(cz.geokuk.framework.RozmisteniPrekryvu.pruhPlanovace(cz.geokuk.framework.RozmisteniPrekryvu.volnaSirkaPruhu(sirka, panely)));
		l.add(cz.geokuk.framework.RozmisteniPrekryvu.UKAZATEL_VYKONU);
		return l;
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		repaint();
	}

	@Override
	protected boolean jeNadMapouKNacitani() {
		return true;
	}

	@Override
	public abstract void paintComponent(java.awt.Graphics g);

	/** Přepínač „Sektory“ v liště; prstenec, klín a čára jsou jen u 360°, jinak se nekreslí vůbec. */
	boolean kresliSektory() {
		return model == null || model.isKresliSektory();
	}

	/** Přepínač „Štítky“ v liště. */
	boolean kresliStitky() {
		return model == null || model.isKresliStitky();
	}

	/** Vlákno pro stavbu podkladu mimo EDT; bez něj (výchozí, testy) se staví hned. */
	public void setStavba(final java.util.concurrent.Executor stavba) {
		this.stavba = stavba;
	}

	/** Podklad kreslení pro zobrazenou challenge, nebo null, když se nic nekreslí. Dokud se nový staví, kreslí se předchozí. */
	TvarPlanovace tvar() {
		final PrehledChallenge p = model == null || !model.jeZapnuto() ? null : model.prehled();
		if (p == null) {
			tvar = null;
			TvarCache.vymaz();
			return null;
		}
		final TvarPlanovace t = TvarCache.dej(p, model.vybranyKandidat(p.challenge()), stavba, poHotovo);
		if (t != null) {
			tvar = t;
		} else if (TvarCache.selhalo()) {
			tvar = null;
		}
		return tvar;
	}
}
