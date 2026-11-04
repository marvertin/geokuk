package cz.geokuk.plugins.kesoidkruhy;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.LimityKresleni;
import cz.geokuk.plugins.kesoid.ViditelnostSKesoidy;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.plugins.kesoid.mvc.LimityKresleniEvent;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.util.index2d.Indexator;

public class JZvyraznovaciKruhySlide extends JSingleSlide0 {
	private static final long serialVersionUID = -5858146658366237217L;
	private volatile int limitIkon = LimityKresleni.VYCHOZI_IKON;

	private Indexator<Wpt> iIndexator;

	private EKaType podklad;
	private KruhySettings kruhy;
	/** Poslední rozhodnutí o kreslení a počet vynechaných kruhů; z kreslení, pro vysvětlení, proč kruhy nejsou vidět. */
	private volatile Rozhodnuti duvod = Rozhodnuti.KRESLI;
	private volatile int vynechanychKruhu;
	private final ViditelnostSKesoidy viditelnost = new ViditelnostSKesoidy(this);

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

	public void onEvent(final LimityKresleniEvent event) {
		limitIkon = event.getLimity().getIkon();
		repaint();
	}

	public void onEvent(final KeskyVyfiltrovanyEvent event) {
		iIndexator = event.getFiltrovane().getIndexator();
		repaint();
	}

	public void onEvent(final KesoidOnoffEvent event) {
		viditelnost.setKesoidy(event.isOnoff());
	}

	public void onEvent(final KruhyPreferencesChangeEvent event) {
		viditelnost.setVlastni(event.kruhy.isOnoff());
		kruhy = event.kruhy;
		repaint();
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		podklad = event.getKatype();
		provazModely();
	}

	@Override
	public void paintComponent(final Graphics aG) {
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
		final int pocet = iIndexator.count(getSoord().getBoundingRect());
		duvod = rozhodni(false, r, pocet, limitIkon, prilisHuste(pocet, Math.PI * r * r));
		vynechanychKruhu = 0;
		if (duvod != Rozhodnuti.KRESLI) {
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

	/** Zda se kruhy kreslí, a když ne, proč. */
	enum Rozhodnuti {
		KRESLI, LIMIT_POCTU, PRILIS_HUSTE, POLOMER_PRILIS_MALY
	}

	/**
	 * Rozhodnutí o kreslení. Počet nad limitem kruhy skrývá vždy. V pixelech je skrývá i příliš velké pokrytí okna, v metrech ne: skutečný poloměr se kreslí bez ohledu na to,
	 * kolik obrazovky zakryje.
	 */
	static Rozhodnuti rozhodni(final boolean vMetrech, final double polomerPx, final int pocet, final int limitIkon, final boolean prilisHuste) {
		if (polomerPx <= 0) {
			return Rozhodnuti.POLOMER_PRILIS_MALY;
		}
		if (pocet > limitIkon) {
			return Rozhodnuti.LIMIT_POCTU;
		}
		return !vMetrech && prilisHuste ? Rozhodnuti.PRILIS_HUSTE : Rozhodnuti.KRESLI;
	}

	/** Důvod, proč se kruhy při posledním kreslení nekreslily; KRESLI, když se kreslily. */
	Rozhodnuti getDuvod() {
		return duvod;
	}

	/** Kolik kruhů s hranou ve výřezu se při posledním kreslení vynechalo kvůli {@link PolomerKruhu#MAX_CASTECNYCH_KRUHU}. */
	int getVynechanychKruhu() {
		return vynechanychKruhu;
	}

	/** Kruhy s poloměrem v metrech; okolo kruhů většího než výřez se kreslí jen to, co je ve výřezu. */
	private void kresliVMetrech(final Graphics2D g) {
		final double r = PolomerKruhu.pixlu(kruhy.getPolomerMetru(), getSoord().getPixluNaMetr());
		final int pocet = iIndexator.count(getSoord().getBoundingRect());
		duvod = rozhodni(true, r, pocet, limitIkon, false);
		vynechanychKruhu = 0;
		if (duvod != Rozhodnuti.KRESLI) {
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
		// Kruhy s hranou ve výřezu se kreslí po jednom a jeden velký kruh trvá milisekundy: nejbližších ke středu výřezu je nejvýš MAX_CASTECNYCH_KRUHU.
		final List<int[]> kreslene = PolomerKruhu.nejblizsi(castecne, vyrez.x + vyrez.width / 2.0, vyrez.y + vyrez.height / 2.0, PolomerKruhu.MAX_CASTECNYCH_KRUHU);
		vynechanychKruhu = castecne.size() - kreslene.size();
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
