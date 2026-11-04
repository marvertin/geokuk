package cz.geokuk.plugins.kesoidkruhy;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.DuvodNekresleni;
import cz.geokuk.plugins.kesoid.LimityKresleni;
import cz.geokuk.plugins.kesoid.StavVrstevModel;
import cz.geokuk.plugins.kesoid.ViditelnostSKesoidy;
import cz.geokuk.plugins.kesoid.VrstvaNadMapou;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.plugins.kesoid.mvc.LimityKresleniEvent;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

public class JZvyraznovaciKruhySlide extends JSingleSlide0 {
	private static final long serialVersionUID = -5858146658366237217L;
	private volatile int limitIkon = LimityKresleni.VYCHOZI_IKON;

	private Indexator<Wpt> iIndexator;

	private EKaType podklad;
	private KruhySettings kruhy;
	/** Poslední rozhodnutí o kreslení a počet vynechaných kruhů; z kreslení, pro vysvětlení, proč kruhy nejsou vidět. */
	private volatile DuvodNekresleni duvod = DuvodNekresleni.KRESLI;
	private volatile int vynechanychKruhu;
	private int hlaseneVynechane;
	private int hlaseneKreslene;
	/** Kolik kruhů s hranou ve výřezu se při posledním kreslení opravdu nakreslilo. */
	private volatile int kresleneKruhu;
	private final ViditelnostSKesoidy viditelnost = new ViditelnostSKesoidy(this);
	/** Null v kopii pro rendrování: ta o stavu nehlásí. */
	private StavVrstevModel stavModel;
	/** Počet bodů ve výřezu spočtený naposledy; počítá se jednou pro kreslení i pro stav. */
	private Object pocetKlic;
	private int pocetVeVyrezu;

	public JZvyraznovaciKruhySlide() {
		setOpaque(false);
		setCursor(null);
		initComponents();
	}

	@Override
	protected boolean jeNadMapouKNacitani() {
		return true;
	}

	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JZvyraznovaciKruhySlide();
	}

	public void inject(final StavVrstevModel stavModel) {
		this.stavModel = stavModel;
	}

	@Override
	protected void onVyrezChanged() {
		prepocitejStav();
	}

	public void onEvent(final LimityKresleniEvent event) {
		limitIkon = event.getLimity().getIkon();
		prepocitejStav();
		repaint();
	}

	public void onEvent(final KeskyVyfiltrovanyEvent event) {
		iIndexator = event.getFiltrovane().getIndexator();
		prepocitejStav();
		repaint();
	}

	public void onEvent(final KesoidOnoffEvent event) {
		viditelnost.setKesoidy(event.isOnoff());
		prepocitejStav();
	}

	public void onEvent(final KruhyPreferencesChangeEvent event) {
		viditelnost.setVlastni(event.kruhy.isOnoff());
		kruhy = event.kruhy;
		prepocitejStav();
		repaint();
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		podklad = event.getKatype();
		provazModely();
	}

	@Override
	public void paintComponent(final Graphics aG) {
		kresli(aG);
		hlasVynechane(aG);
	}

	/** Po kreslení celého výřezu oznámí, kolik kruhů se vynechalo; z částečného překreslení (dlaždice) se neoznamuje, počet by byl jen za část výřezu. */
	private void hlasVynechane(final Graphics g) {
		if (stavModel == null || !isSoordInitialized()) {
			return;
		}
		final Rectangle clip = g.getClipBounds();
		if (clip != null && !clip.contains(new Rectangle(getSoord().getDim()))) {
			return;
		}
		final int vynechano = duvod == DuvodNekresleni.KRESLI ? vynechanychKruhu : 0;
		final int kresleno = vynechano > 0 ? kresleneKruhu : 0;
		if (vynechano == hlaseneVynechane && kresleno == hlaseneKreslene) {
			return;
		}
		hlaseneVynechane = vynechano;
		hlaseneKreslene = kresleno;
		SwingUtilities.invokeLater(() -> stavModel.nahlasVynechane(vynechano, kresleno));
	}

	private void kresli(final Graphics aG) {
		if (iIndexator == null) {
			return;
		}
		final boolean jednotkove = kruhy.isJednotkovaVelikost();
		if (jednotkove) {
			kresliVMetrech((Graphics2D) aG);
			return;
		}
		final int r = kruhy.getVelikost();
		if (r == 0) {
			return;
		}
		final int pocet = pocetVeVyrezu();
		duvod = rozhodni(false, r, pocet, limitIkon, prilisHuste(pocet, Math.PI * r * r));
		vynechanychKruhu = 0;
		if (duvod != DuvodNekresleni.KRESLI) {
			return;
		}

		final Graphics2D g = (Graphics2D) aG;
		final int d = 2 * r;
		final Color barva = kruhy.getBarva();
		final Stroke prerus = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 5.0f, 5.0f }, 0);
		g.setColor(barva);
		iIndexator.bound(oblastKresleni(g, r + 1)).stream().forEach(wpt -> {
			final Mou mou = wpt.getMou();
			final Point p = getSoord().transform(mou);
			g.setStroke(prerus);
			g.setColor(barva);
			g.fillOval(p.x - r, p.y - r, d, d);
		});

	}

	/**
	 * Rozhodnutí o kreslení. Počet nad limitem kruhy skrývá vždy. V pixelech je skrývá i příliš velké pokrytí okna, v metrech ne: skutečný poloměr se kreslí bez ohledu na to,
	 * kolik obrazovky zakryje.
	 */
	static DuvodNekresleni rozhodni(final boolean vMetrech, final double polomerPx, final int pocet, final int limitIkon, final boolean prilisHuste) {
		if (polomerPx <= 0) {
			return DuvodNekresleni.POLOMER_PRILIS_MALY;
		}
		if (pocet > limitIkon) {
			return DuvodNekresleni.LIMIT_POCTU;
		}
		return !vMetrech && prilisHuste ? DuvodNekresleni.PRILIS_HUSTE : DuvodNekresleni.KRESLI;
	}

	/** Počet bodů ve výřezu; platí, dokud se nezmění výřez nebo data. */
	private int pocetVeVyrezu() {
		final BoundingRect vyrez = getSoord().getBoundingRect();
		final Object klic = Arrays.asList(vyrez.xx1, vyrez.yy1, vyrez.xx2, vyrez.yy2, System.identityHashCode(iIndexator));
		if (!klic.equals(pocetKlic)) {
			pocetVeVyrezu = iIndexator.count(vyrez);
			pocetKlic = klic;
		}
		return pocetVeVyrezu;
	}

	/** Pro stavový štítek: proč se zapnuté kruhy nekreslí; z výřezu, nastavení a dat, ne z kreslení. */
	DuvodNekresleni aktualniDuvod() {
		if (iIndexator == null || kruhy == null || !viditelnost.isViditelna() || !isSoordInitialized()) {
			return DuvodNekresleni.KRESLI;
		}
		final int pocet = pocetVeVyrezu();
		if (kruhy.isJednotkovaVelikost()) {
			return rozhodni(true, PolomerKruhu.pixlu(kruhy.getPolomerMetru(), getSoord().getPixluNaMetr()), pocet, limitIkon, false);
		}
		final int r = kruhy.getVelikost();
		return r == 0 ? DuvodNekresleni.KRESLI : rozhodni(false, r, pocet, limitIkon, prilisHuste(pocet, Math.PI * r * r));
	}

	private void prepocitejStav() {
		if (stavModel == null) {
			return;
		}
		final boolean jednotkove = kruhy != null && kruhy.isJednotkovaVelikost();
		final DuvodNekresleni d = aktualniDuvod();
		stavModel.nahlas(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY, jednotkove ? DuvodNekresleni.KRESLI : d, limitIkon);
		stavModel.nahlas(VrstvaNadMapou.JEDNOTKOVE_KRUHY, jednotkove ? d : DuvodNekresleni.KRESLI, limitIkon);
		if (!jednotkove || d != DuvodNekresleni.KRESLI) {
			hlaseneVynechane = 0;
			hlaseneKreslene = 0;
			stavModel.nahlasVynechane(0, 0);
		}
	}

	/** Důvod, proč se kruhy při posledním kreslení nekreslily; KRESLI, když se kreslily. */
	DuvodNekresleni getDuvod() {
		return duvod;
	}

	/** Kolik kruhů s hranou ve výřezu se při posledním kreslení vynechalo kvůli rozpočtu ploch {@link PolomerKruhu#ROZPOCET_PLOCHY}. */
	int getVynechanychKruhu() {
		return vynechanychKruhu;
	}

	/** Kruhy s poloměrem v metrech; okolo kruhů většího než výřez se kreslí jen to, co je ve výřezu. */
	private void kresliVMetrech(final Graphics2D g) {
		final double r = PolomerKruhu.pixlu(kruhy.getPolomerMetru(), getSoord().getPixluNaMetr());
		final int pocet = pocetVeVyrezu();
		duvod = rozhodni(true, r, pocet, limitIkon, false);
		vynechanychKruhu = 0;
		kresleneKruhu = 0;
		if (duvod != DuvodNekresleni.KRESLI) {
			return;
		}
		final Rectangle clip = g.getClipBounds();
		final Rectangle vyrez = clip != null ? clip : new Rectangle(getSoord().getDim());
		if (vyrez.isEmpty()) {
			return;
		}
		final Color barva = kruhy.getBarva();
		final Stroke prerus = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 5.0f, 5.0f }, 0);
		// Keš mimo výřez má kruh do výřezu zasahovat; okraj je omezen, aby se nepřetekly celá čísla.
		final int okraj = (int) Math.min(Integer.MAX_VALUE / 4, Math.ceil(r) + 1);
		final double x2 = vyrez.x + vyrez.width;
		final double y2 = vyrez.y + vyrez.height;
		// Kruhy, které výřez celý pokrývají, se spojí do jedné plochy se složeným krytím; po jednom by se vyplňovala celá obrazovka pro každou keš.
		final int[] pokryvajici = { 0 };
		final List<int[]> castecne = new ArrayList<>();
		iIndexator.bound(getSoord().transforToBounding(new Rectangle(vyrez.x - okraj, vyrez.y - okraj, vyrez.width + 2 * okraj, vyrez.height + 2 * okraj))).stream().forEach(wpt -> {
			final Point p = getSoord().transform(wpt.getMou());
			final PolomerKruhu.Kresleni jak = PolomerKruhu.kresleni(p.x, p.y, r, vyrez.x, vyrez.y, x2, y2);
			if (jak == PolomerKruhu.Kresleni.NIC) {
				return;
			}
			if (jak == PolomerKruhu.Kresleni.VYPLN_VYREZ) {
				pokryvajici[0]++;
				return;
			}
			castecne.add(new int[] { p.x, p.y });
		});
		// Kruhy s hranou ve výřezu se kreslí po jednom a cena roste s plochou: nejbližší ke středu výřezu se kreslí, dokud se nevyčerpá rozpočet ploch.
		final Dimension dim = getSoord().getDim();
		final int limit = PolomerKruhu.limitKruhu(r, (double) dim.width * dim.height);
		final List<int[]> kreslene = PolomerKruhu.nejblizsi(castecne, vyrez.x + vyrez.width / 2.0, vyrez.y + vyrez.height / 2.0, limit);
		vynechanychKruhu = castecne.size() - kreslene.size();
		kresleneKruhu = kreslene.size();
		g.setStroke(prerus);
		for (final int[] p : kreslene) {
			final java.awt.geom.Ellipse2D.Double kruh = new java.awt.geom.Ellipse2D.Double(p[0] - r, p[1] - r, 2 * r, 2 * r);
			g.setColor(barva);
			g.fill(kruh);
			g.setColor(Color.WHITE);
			g.draw(kruh);
		}
		if (pokryvajici[0] > 0) {
			g.setColor(new Color((barva.getRGB() & 0xFFFFFF) | PolomerKruhu.slozAlfu(barva.getAlpha(), pokryvajici[0]) << 24, true));
			g.fillRect(vyrez.x, vyrez.y, vyrez.width, vyrez.height);
		}
	}

	/**
	 *
	 */
	private void initComponents() {
		provazModely();
	}

	private void provazModely() {
		if (podklad != null) {
			// barvaAVElikostKruhuModel = Settings.barvyAVelikostiKruhu.get(podklad);
		}
	}

}
