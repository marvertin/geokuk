package cz.geokuk.plugins.mapy.kachle.gui;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.LinkedListMultimap;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.onoffline.OnofflineModelChangeEvent;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.*;
import cz.geokuk.plugins.mapy.kachle.data.*;
import cz.geokuk.plugins.mapy.kachle.podklady.Priority;
import cz.geokuk.util.pocitadla.*;

public abstract class JKachlovnik extends JSingleSlide0 implements AfterEventReceiverRegistrationInit {

	private static final Logger log = LogManager.getLogger(JKachlovnik.class.getSimpleName());

	private static final long serialVersionUID = -6300199882447791157L;

	private static Pocitadlo pocitZustalychKachli = new PocitadloRoste("Počet zůstalých kachlí",
	        "Počet kachlí jKachle, které jako kompoenty zůstaly a jen se změnila její lokace, protože po reinicializaci "
	                + "byly na svém místě (obvykle posun) a nebylo je tudíž nutné znovu vytvářet");

	private static Pocitadlo pocitReinicializaceKachlovniku = new PocitadloRoste("Kolikrát bylo nuceno reinicializovat " + "celý kachlovník",
	        "Říká, kolikrát byla zavolána metoda init pro reinicializaci celého kachlovníku v důsledku posunu, změnu " + "velikosti, zůůmování atd.");

	private static Pocitadlo pocitVynuceneNepouzitiExistujiciKachle = new PocitadloRoste("Vynucené použití neexistující " + "kachle",
	        "Kolikrát do JKachlovnik.ini() přišlo z venku, že JKachle komponenty nesmím použít");

	private static final Pocitadlo pocitKachliVKachlovniku2 = new PocitadloMalo("#kachlí v kachlovníku", "");

	private EKaType katype = null;

	private KachleModel kachleModel;

	// TODO musí to být private a musí být na tru nastaveno jen při rendrování
	protected boolean vykreslovatokamzite;

	/** Jen pro ladící účely */
	public final String nazevKachlovniku;

	private final Priority priorita;

	// je to jen kvuli garbage collectoru, aby nezrusil, NERUSIT PROMENNU i kdyz zdanlive je to na nic
	public JKachlovnik(final String nazevKachlovniku, final Priority priority) {
		this.nazevKachlovniku = nazevKachlovniku;
		priorita = priority;
		setLayout(null);
		setPreferredSize(new Dimension(800, 600));
		setBackground(Color.GREEN);
		setOpaque(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
	 */
	@Override
	public void initAfterEventReceiverRegistration() {
		registerEvents();
	}

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	public void onEvent(final OnofflineModelChangeEvent event) {
		if (event.isOnlineMOde()) {
			// Při zapnutí online módu e musí překlreslit. To se stane i když není online mód právě zapínán, ale to nevadí.
			init(false);
		}
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		setKachloType(event.getKatype());
	}

	@Override
	public void onVyrezChanged() {
		init(true);
	}

	/**
	 * @param aKachloTypes
	 *            the kachloTypes to set
	 */
	public void setKachloType(final EKaType katype) {
		if (this.katype == katype) {
			return;
		}
		this.katype = katype;
		init(false);
	}

	protected JKachle createJKachle(final Ka ka) {
		return new JKachle(this, ka);
	}

	protected void init(final boolean smimZnovuPouzitKachle) {
		if (!isSoordInitialized()) {
			return;
		}
		if (getWidth() == 0 || getHeight() == 0) {
			return; // nemá smysl rendrovat prázdný kachlovník
		}
		final Coord soord = getSoord();
		if (soord == null) {
			return;
		}
		assert kachleModel != null;
		if (katype == null) {
			return;
		}
		pocitReinicializaceKachlovniku.inc();
		if (!smimZnovuPouzitKachle) {
			pocitVynuceneNepouzitiExistujiciKachle.inc();
		}
		;

		// Musí to být multimapa, protože při malých měřítcích je jedna kachle vícekrát zorbazena,
		// takže pro jeden klíč máme hodně hodnot
		final LinkedListMultimap<KaLoc, JKachle> mapaKachli = LinkedListMultimap.create();
		Arrays.stream(getComponents()).map(jka -> (JKachle) jka).forEach(jka -> {
			mapaKachli.put(jka.getKaLoc(), jka);
		});
		;
		log.trace("Mapa {} kachlí {}", getComponents().length, mapaKachli);
		final Kaputer kaputer = new Kaputer(soord);
		if (log.isTraceEnabled()) {
			log.trace("Vykreslovani kachli od {} pro {} -- {}", kaputer.getKachlePoint(0, 0), kaputer.getKachleMou(0, 0), kaputer);
		}
		for (int yi = 0; yi < kaputer.getPocetKachliY(); yi++) {
			log.trace(" .... řádek", soord);
			for (int xi = 0; xi < kaputer.getPocetKachliX(); xi++) {
				final KaLoc kaloc = kaputer.getKaloc(xi, yi);
				final List<JKachle> seznamStejnychKachli = mapaKachli.get(kaloc);
				final boolean kachleSePouzije = smimZnovuPouzitKachle && seznamStejnychKachli.size() > 0;
				final JKachle jkachle;
				if (!kachleSePouzije) {
					log.trace("............... Vytváření JKachle" + kaloc);
					jkachle = createJKachle(new Ka(kaloc, katype));
					add(jkachle); // a přidat jako komponentu
					// kachle.ziskejObsah(priorita);
					jkachle.ziskejObsah(kachleModel, priorita);

				} else { // použije se původní kachle
					jkachle = seznamStejnychKachli.remove(0); // jednu z nich vezmeme, je jedno kterou, všechny mají stejný obsah
					pocitZustalychKachli.inc();
				}
				// napozicujeme každou kachli do správné podoby
				final Point p = kaputer.getKachlePoint(xi, yi);
				log.trace("....... vykresulji kachli {} na {}", kaloc, p);
				// System.out.println("KACHLE xx: " + new Point(x,y) + " ********* " + getSize());
				jkachle.setLocation(p.x, p.y);
			}
		}
		// System.out.println("mame komponent: " + super.getComponentCount());
		// System.out.println("Nahrazenych kachli: " + kachles.size());

		// Kachle, které zbyly v mapě jsou komponenty, jenž nebyly recyklovány, musí být odstraněny
		mapaKachli.values().forEach(jka -> {
			jka.uzTeNepotrebuju();
			remove(jka);
		});
		pocitKachliVKachlovniku2.set(getComponentCount());
		log.trace("Počet komponent (nejspíš kachlí) v kachlovníku: {}", getComponentCount());
	}

	void kachleZpracovana(final JKachle jKachle) {
	}

	private void registerEvents() {
	}

	// @Override
	// public void finalize() {
	// System.out.println("Kachlovník finalizován");
	// }
}
