package cz.geokuk.plugins.mapy.kachle.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.swing.SwingUtilities;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.napoveda.CoJeNoveho;
import cz.geokuk.core.napoveda.JCoJeNoveho;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.podklady.Priority;
import cz.geokuk.plugins.mapy.pruvodce.PlanPruvodce;
import cz.geokuk.plugins.mapy.pruvodce.PruvodceKontext;
import cz.geokuk.plugins.mapy.pruvodce.ProgramPruvodceKontext;
import cz.geokuk.plugins.mapy.pruvodce.StahnoutMapuAVrstvy;
import cz.geokuk.plugins.mapy.sprava.KatalogovaMapa;
import cz.geokuk.plugins.mapy.sprava.MatriceMap;
import cz.geokuk.plugins.mapy.sprava.NabidkaRadku;
import cz.geokuk.plugins.mapy.sprava.KatalogovaVrstva;
import cz.geokuk.plugins.mapy.sprava.SpravaMapEvent;
import cz.geokuk.plugins.mapy.sprava.StavVrstvy;
import cz.geokuk.plugins.mapy.sprava.SpravaMapModel;
import cz.geokuk.plugins.mapy.sprava.StavMapy;
import cz.geokuk.plugins.mapy.sprava.gui.RadaOfflineMapy;

public class JKachlovnikPresCele extends JKachlovnik {

	private static final long serialVersionUID = -3170605712662727739L;
	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.core.coord.JSingleSlide0#render(java.awt.Graphics)
	 */

	public JKachlovnikPresCele() {
		super("Hlavní kachlovník", Priority.KACHLE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.core.coord.JSingleSlide0#createRenderableSlide()
	 */
	@Override
	protected void init(final boolean smimZnovuPouzitKachle) {
		// Okno mohlo přejít na displej s jiným měřítkem.
		final GraphicsConfiguration gc = getGraphicsConfiguration();
		if (gc != null && getKachleModel() != null) {
			getKachleModel().setMeritkoDispleje(gc.getDefaultTransform().getScaleX());
		}
		super.init(smimZnovuPouzitKachle);
	}

	@Override
	protected boolean kreslitLogoNacitani() {
		return getKachloType() != null && !getKachloType().isPrekryv();
	}

	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JKachlovnikRendrovaci();
	}


	private static final Color ODKAZ = new Color(0x1A5FB4);

	private transient SpravaMapModel spravaMap;
	/** Je vidět pruh plánovače vlevo nahoře? Rada se pak vyhýbá jeho oblasti. */
	private boolean pruhPlanovace;

	/** Kde jsou v kachlovníku odkazy rady a mapy, které stahují; prázdné, když se nekreslí. */
	private final transient List<Rectangle> odkazyRady = new ArrayList<>();
	private final transient List<KatalogovaMapa> mapyRady = new ArrayList<>();
	/** Pravidlo plánu u odkazů „a vrstvy“, null u odkazu jen na mapu; souběžně s {@link #odkazyRady}. */
	private final transient List<PlanPruvodce.Rezim> rezimyRady = new ArrayList<>();
	private transient RadaOfflineMapy rada;
	/** Kde je v kachlovníku ikona ⓘ atribuce; null, když se nekreslí. */
	private transient Rectangle ikonaAtribuce;
	/** Rada a atribuce, jak se naposledy kreslily nebo zkontrolovaly; null, když se nekreslí. */
	private transient RozlozeniRady posledniRada;
	private transient AtribuceMapy.Rozlozeni posledniAtribuce;

	public void inject(final cz.geokuk.plugins.challenge.ChallengeModel challengeModel) {
		pruhPlanovace = challengeModel.jePruhViditelny();
	}

	public void onEvent(final cz.geokuk.plugins.challenge.ChallengeZmenenyEvent event) {
		final boolean pruh = event.getModel().jePruhViditelny();
		if (pruh != pruhPlanovace) {
			pruhPlanovace = pruh;
			repaint();
		}
	}

	public void inject(final SpravaMapModel spravaMap) {
		this.spravaMap = spravaMap;
	}

	public void onEvent(final SpravaMapEvent event) {
		if (!odkazyRady.isEmpty() || rada != null && rada.mapa != null && rada.mapa.getId().equals(event.getMapa().getId())) {
			repaint();
		}
	}

	public void onEvent(final cz.geokuk.plugins.mapy.sprava.SpravaVrstevEvent event) {
		prekresliRaduAAtribuci();
	}

	public void onEvent(final cz.geokuk.plugins.mapy.sprava.KatalogVrstevEvent event) {
		prekresliRaduAAtribuci();
	}

	public void onEvent(final cz.geokuk.plugins.mapy.sprava.StavyMapEvent event) {
		prekresliRaduAAtribuci();
	}

	/** Překreslí rozhraní rady a atribuce, jen když se jejich obsah od posledního kreslení změnil; mapa pod nimi se nepřekresluje. */
	private void prekresliRaduAAtribuci() {
		final RozlozeniRady novaRada = maAtribuci() && jeRada() ? sestavRadu(getFontMetrics(getFont().deriveFont(Font.BOLD, 12f)), getFontMetrics(getFont().deriveFont(Font.PLAIN, 12f))) : null;
		if (!Objects.equals(podpis(novaRada), podpis(posledniRada))) {
			prekresli(sjednot(posledniRada == null ? null : posledniRada.oblast(), novaRada == null ? null : novaRada.oblast()));
		}
		posledniRada = novaRada;
		final AtribuceMapy.Rozlozeni novaAtribuce = rozlozAtribuci(getFontMetrics(getFont().deriveFont(Font.PLAIN, 10f)));
		if (!Objects.equals(podpis(novaAtribuce), podpis(posledniAtribuce))) {
			prekresli(sjednot(posledniAtribuce == null ? null : posledniAtribuce.pole, novaAtribuce == null ? null : novaAtribuce.pole));
		}
		posledniAtribuce = novaAtribuce;
	}

	private void prekresli(final Rectangle oblast) {
		if (oblast != null) {
			repaint(oblast.x - 2, oblast.y - 2, oblast.width + 4, oblast.height + 4);
		}
	}

	private static Rectangle sjednot(final Rectangle a, final Rectangle b) {
		return a == null ? b : b == null ? a : a.union(b);
	}

	private static List<Object> podpis(final RozlozeniRady r) {
		return r == null ? null : Arrays.asList(r.oblast(), r.radky, r.podpisTabulky());
	}

	private static List<Object> podpis(final AtribuceMapy.Rozlozeni r) {
		return r == null ? null : Arrays.asList(r.text, r.pole);
	}

	public void onEvent(final cz.geokuk.plugins.mapy.kachle.ReliefCuzkEvent event) {
		reliefCuzk = event.isZapnuto();
		repaint();
	}

	/** Klik na odkaz rady stáhne nabízenou mapu; vrací, jestli klik patřil odkazu. */
	public boolean klikNaRadu(final Point bod) {
		if (ikonaAtribuce != null && ikonaAtribuce.contains(bod)) {
			ukazPlnouAtribuci();
			return true;
		}
		if (spravaMap == null) {
			return false;
		}
		for (int i = 0; i < odkazyRady.size(); i++) {
			if (odkazyRady.get(i).contains(bod)) {
				if (rezimyRady.get(i) == null) {
					spravaMap.stahni(mapyRady.get(i));
				} else {
					stahniMapuAVrstvy(mapyRady.get(i), rezimyRady.get(i));
				}
				return true;
			}
		}
		return false;
	}

	/** Mapa a všechny vrstvy; u celé země s regiony i jejich vrstvy, hned bez dialogu, dotaz jen nad mezí průvodce. */
	private void stahniMapuAVrstvy(final KatalogovaMapa mapa, final PlanPruvodce.Rezim rezim) {
		if (getKachleModel() == null) {
			return;
		}
		final PruvodceKontext kontext = new ProgramPruvodceKontext(spravaMap, null, getKachleModel());
		final List<KatalogovaMapa> regiony = spravaMap.getKatalog().regiony(mapa);
		final List<String> klice = new ArrayList<>();
		klice.add(mapa.getId());
		regiony.forEach(r -> klice.add(r.getId()));
		final PlanPruvodce plan = StahnoutMapuAVrstvy.plan(kontext, regiony.isEmpty() ? null : mapa.getId(), klice, rezim);
		StahnoutMapuAVrstvy.spust(kontext, plan, StahnoutMapuAVrstvy.dialog(this));
	}

	public boolean jeNadOdkazemRady(final Point bod) {
		return odkazyRady.stream().anyMatch(r -> r.contains(bod)) || ikonaAtribuce != null && ikonaAtribuce.contains(bod);
	}

	/** Zeměpisný obdélník, do kterého se vejdou všechny čtyři rohy výřezu. */
	private RadaOfflineMapy.Vyrez vyrez() {
		final Wgs[] rohy = { getSoord().getMouSZ().toWgs(), getSoord().getMouSV().toWgs(), getSoord().getMouJV().toWgs(), getSoord().getMouJZ().toWgs() };
		double jih = 90;
		double zapad = 180;
		double sever = -90;
		double vychod = -180;
		for (final Wgs r : rohy) {
			jih = Math.min(jih, r.lat);
			sever = Math.max(sever, r.lat);
			zapad = Math.min(zapad, r.lon);
			vychod = Math.max(vychod, r.lon);
		}
		return new RadaOfflineMapy.Vyrez(jih, zapad, sever, vychod);
	}

	static final String RADA_BEZ_SPRAVY = "Správce offline map najdete v menu Mapy a v bloku Mapy vpravo dole ve stavovém řádku.";

	/** Radu, kde vzít offline mapu, kreslí přes kachle nahoře uprostřed. */
	@Override
	protected void paintChildren(final Graphics g) {
		super.paintChildren(g);
		odkazyRady.clear();
		mapyRady.clear();
		rezimyRady.clear();
		rada = null;
		posledniRada = null;
		if (maAtribuci() && jeRada()) {
			kresliRadu((Graphics2D) g);
		}
	}

	private boolean maAtribuci() {
		return getKachloType() != null && !getKachloType().getAtribuce().isEmpty();
	}

	/**
	 * Atribuce: jeden zkrácený řádek vpravo dole v pravé čtvrtině šířky, prostředek dole patří měřítku; plné znění po kliku na ikonu ⓘ. Kreslí ji vrstva {@link JOdkazRadySlide} nad
	 * všemi ostatními vrstvami mapy, aby ji nezakryly.
	 */
	void kresliAtribuci(final Graphics2D g) {
		ikonaAtribuce = null;
		posledniAtribuce = null;
		if (!maAtribuci()) {
			return;
		}
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setFont(getFont().deriveFont(Font.PLAIN, 10f));
			final FontMetrics fm = g2.getFontMetrics();
			final AtribuceMapy.Rozlozeni r = rozlozAtribuci(fm);
			AtribuceMapy.kresli(g2, r, fm);
			ikonaAtribuce = r.ikona;
			posledniAtribuce = r;
		} finally {
			g2.dispose();
		}
	}

	private AtribuceMapy.Rozlozeni rozlozAtribuci(final FontMetrics fm) {
		return maAtribuci() ? AtribuceMapy.rozloz(zkratkyAtribuce(), fm, getWidth(), getHeight(), getWidth() / 4) : null;
	}

	/** Zkrácené zdroje atribuce: podklad, data offline mapy, vrstvy a překryvy. */
	List<String> zkratkyAtribuce() {
		final List<String> v = new ArrayList<>();
		v.addAll(AtribuceMapy.zkrat(textAtribuce()));
		for (final KatalogovaVrstva k : vrstvyAtribuce()) {
			v.addAll(AtribuceMapy.zkrat(k.getAtribuce()));
		}
		if (prekryvTrasPotrebujeAtribuci()) {
			v.add(AtribuceMapy.OSM);
		}
		if (reliefCuzk) {
			v.addAll(AtribuceMapy.zkrat(EKaType.RELIEF_CUZK.getAtribuce()));
		}
		return AtribuceMapy.slouc(v);
	}

	/** Plné znění atribuce: jeden zdroj na řádek. */
	List<String> plnaAtribuce() {
		final List<String> v = new ArrayList<>();
		v.add(textAtribuce());
		if (getKachloType() != null && getKachloType().isOffline() && getKachleModel() != null && getKachleModel().getTemaOfflineMapy().getAtribuce() != null) {
			v.add(getKachleModel().getTemaOfflineMapy().getAtribuce());
		}
		if (prekryvTrasPotrebujeAtribuci()) {
			v.add(JKachlovnikTras.ATRIBUCE);
		}
		if (reliefCuzk && !uzJeVeZneni(v, EKaType.RELIEF_CUZK.getAtribuce())) {
			v.add(ATRIBUCE_RELIEFU);
		}
		for (final KatalogovaVrstva k : vrstvyAtribuce()) {
			final String t = k.getAtribuceDlouha() != null && !k.getAtribuceDlouha().trim().isEmpty() ? k.getAtribuceDlouha().trim() : k.getAtribuce();
			if (t != null && !t.trim().isEmpty() && !v.contains(t.trim()) && !uzJeVeZneni(v, t)) {
				v.add(t.trim());
			}
		}
		return v;
	}

	/** Všechny zdroje z textu už jsou ve znění; jinak by se třeba OpenStreetMap opakovalo. */
	private static boolean uzJeVeZneni(final List<String> zneni, final String text) {
		final List<String> mam = new ArrayList<>();
		zneni.forEach(z -> mam.addAll(AtribuceMapy.zkrat(z)));
		return mam.containsAll(AtribuceMapy.zkrat(text));
	}

	private String textAtribuce() {
		if (getKachloType() == null) {
			return "";
		}
		return getKachloType().isOffline() && getKachleModel() != null ? getKachleModel().getAtribuceOfflineMapy() : getKachloType().getAtribuce();
	}

	/** Přes mapu se kreslí stínovaný reliéf ČÚZK. */
	private boolean reliefCuzk;

	static final String ATRIBUCE_RELIEFU = "Stínovaný reliéf " + EKaType.RELIEF_CUZK.getAtribuce();

	private boolean prekryvTrasPotrebujeAtribuci() {
		return JKachlovnikTras.potrebujeAtribuci(getKachloType()) && getKachleModel() != null && getKachleModel().isPrekryvTras();
	}

	private List<KatalogovaVrstva> vrstvyAtribuce() {
		final List<KatalogovaVrstva> v = new ArrayList<>();
		if (spravaMap == null || getKachloType() == null || !getKachloType().isOffline()) {
			return v;
		}
		for (final KatalogovaVrstva k : spravaMap.getKatalogVrstev().getVrstvy()) {
			if (k.getAtribuce() != null && !k.getAtribuce().trim().isEmpty() && spravaMap.getStav(k).getMistni() == StavVrstvy.Mistni.MAME) {
				v.add(k);
			}
		}
		return v;
	}

	/** Ukáže plné znění atribuce v okně jako Co je nového. */
	void ukazPlnouAtribuci() {
		final JCoJeNoveho obsah = new JCoJeNoveho();
		obsah.setHtml(htmlAtribuce(plnaAtribuce()));
		JCoJeNoveho.dialog(SwingUtilities.getWindowAncestor(this), "Atribuce mapy", obsah).setVisible(true);
	}

	/** Plné znění jako HTML; texty atribuce přicházejí i ze staženého katalogu, proto se escapují. */
	static String htmlAtribuce(final List<String> texty) {
		final StringBuilder html = new StringBuilder("<html><body>");
		for (final String t : texty) {
			if (!t.trim().isEmpty()) {
				html.append("<p>").append(CoJeNoveho.escape(t)).append("</p>");
			}
		}
		return html.append("<p><a href=\"https://www.openstreetmap.org/copyright\">Autorská práva OpenStreetMap</a></p></body></html>").toString();
	}

	/** Offline mapa a střed výřezu nepokrývá žádná podrobná mapa. */
	boolean jeRada() {
		return getKachloType() != null && getKachloType().isOffline() && getKachleModel() != null && isSoordInitialized() && getSoord() != null
				&& !getKachleModel().jePodrobnaOfflineMapa(getSoord().getMoustred().toWgs(), getSoord().getMoumer());
	}

	/** Rada rozložená do kachlovníku: text, tabulka map s odkazy a obdélník, který zabírá. */
	private static final class RozlozeniRady {
		final RadaOfflineMapy rada;
		final List<String> radky;
		final List<RadaOfflineMapy.RadekRady> tabulka;
		/** Šířky sloupců tabulky: název mapy, Stáhnout mapu, Stáhnout mapu a vrstvy. */
		final int[] sloupce;
		final int x;
		final int y;
		final int w;
		final int h;

		RozlozeniRady(final RadaOfflineMapy rada, final List<String> radky, final List<RadaOfflineMapy.RadekRady> tabulka, final int[] sloupce, final int x, final int y, final int w,
				final int h) {
			this.rada = rada;
			this.radky = radky;
			this.tabulka = tabulka;
			this.sloupce = sloupce;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		Rectangle oblast() {
			return new Rectangle(x, y, w, h);
		}

		List<String> podpisTabulky() {
			final List<String> p = new ArrayList<>();
			for (final RadaOfflineMapy.RadekRady r : tabulka) {
				p.add(r.nazev + "|" + r.region + "|" + r.stitek + "|" + (r.rada == null ? "" : r.rada.mapa.getId() + "|" + r.rada.odkaz + "|" + r.rada.odkaz2));
			}
			return p;
		}
	}

	private static final int ODSAZENI_REGIONU = 14;
	private static final int MEZERA_SLOUPCU = 16;

	private RozlozeniRady sestavRadu(final FontMetrics fmTucne, final FontMetrics fm) {
		final RadaOfflineMapy novaRada = spravaMap == null ? null : RadaOfflineMapy.pro(spravaMap.getKatalog(), spravaMap::getStav, getSoord().getMoustred().toWgs(),
				spravaMap.getBeziciStahovani().map(StavMapy::getMapa).orElse(null), vyrez(), nabidkyMap());
		final String text = novaRada == null ? RADA_BEZ_SPRAVY : novaRada.text;
		final List<String> radky = text == null ? new ArrayList<>() : zalom(text, fm, Math.max(200, getWidth() - 40));
		final List<RadaOfflineMapy.RadekRady> tabulka = novaRada == null ? new ArrayList<>() : novaRada.tabulka();
		final int[] sloupce = new int[3];
		for (final RadaOfflineMapy.RadekRady r : tabulka) {
			if (!r.stitek) {
				sloupce[0] = Math.max(sloupce[0], fm.stringWidth(r.nazev) + (r.region ? ODSAZENI_REGIONU : 0));
			}
			if (r.rada != null) {
				sloupce[1] = Math.max(sloupce[1], fm.stringWidth(r.rada.odkaz));
				if (r.rada.odkaz2 != null) {
					sloupce[2] = Math.max(sloupce[2], fm.stringWidth(r.rada.odkaz2));
				}
			}
		}
		int w = fmTucne.stringWidth(RadaOfflineMapy.NADPIS);
		if (!tabulka.isEmpty()) {
			w = Math.max(w, sloupce[0] + MEZERA_SLOUPCU + sloupce[1] + (sloupce[2] > 0 ? MEZERA_SLOUPCU + sloupce[2] : 0));
			for (final RadaOfflineMapy.RadekRady r : tabulka) {
				if (r.stitek) {
					w = Math.max(w, fm.stringWidth(r.nazev));
				}
			}
		}
		for (final String r : radky) {
			w = Math.max(w, fm.stringWidth(r));
		}
		w += 16;
		final int h = fmTucne.getHeight() + (radky.size() + tabulka.size()) * fm.getHeight() + 8;
		final int xRady = (getWidth() - w) / 2;
		return new RozlozeniRady(novaRada, radky, tabulka, sloupce, xRady, cz.geokuk.framework.RozmisteniPrekryvu.yRady(xRady, pruhPlanovace), w, h);
	}

	/** Stavy a velikosti map a vrstev pro dvě tlačítka rady; matice se sestaví až při prvním dotazu. */
	private java.util.function.Function<KatalogovaMapa, NabidkaRadku> nabidkyMap() {
		if (spravaMap == null || spravaMap.getKatalogVrstev() == null) {
			return null;
		}
		final List<MatriceMap.Radek> matice = new ArrayList<>();
		final boolean[] sestavena = { false };
		return m -> {
			if (!sestavena[0]) {
				sestavena[0] = true;
				matice.addAll(MatriceMap.sestav(spravaMap.getKatalog(), spravaMap::getStav, spravaMap.getKatalogVrstev(), spravaMap::getStav));
			}
			return matice.stream().filter(r -> r.getKlic().equals(m.getId())).findFirst().map(r -> NabidkaRadku.pro(r, new NabidkaRadku.Okolnosti(-1, null))).orElse(null);
		};
	}

	private void kresliRadu(final Graphics2D g) {
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			final Font tucne = getFont().deriveFont(Font.BOLD, 12f);
			final Font obycejne = getFont().deriveFont(Font.PLAIN, 12f);
			final FontMetrics fmTucne = g2.getFontMetrics(tucne);
			final FontMetrics fm = g2.getFontMetrics(obycejne);
			final RozlozeniRady rr = sestavRadu(fmTucne, fm);
			posledniRada = rr;
			rada = rr.rada;
			final int x = rr.x;
			g2.setColor(new Color(255, 255, 255, 215));
			g2.fillRect(x, rr.y, rr.w, rr.h);
			g2.setColor(Color.DARK_GRAY);
			int yy = rr.y + 4 + fmTucne.getAscent();
			g2.setFont(tucne);
			g2.drawString(RadaOfflineMapy.NADPIS, x + 8, yy);
			g2.setFont(obycejne);
			yy += fmTucne.getDescent() + fm.getAscent() + fmTucne.getLeading();
			for (final String r : rr.radky) {
				g2.drawString(r, x + 8, yy);
				yy += fm.getHeight();
			}
			final int x1 = x + 8 + rr.sloupce[0] + MEZERA_SLOUPCU;
			final int x2 = x1 + rr.sloupce[1] + MEZERA_SLOUPCU;
			int pruh = 0;
			for (final RadaOfflineMapy.RadekRady r : rr.tabulka) {
				if (!r.stitek && pruh++ % 2 != 0) {
					g2.setColor(new Color(0, 0, 0, 14));
					g2.fillRect(x + 4, yy - fm.getAscent(), rr.w - 8, fm.getHeight());
				}
				g2.setColor(Color.DARK_GRAY);
				g2.drawString(r.nazev, x + 8 + (r.region ? ODSAZENI_REGIONU : 0), yy);
				if (r.rada != null) {
					kresliOdkaz(g2, fm, r.rada.odkaz, x1, yy, r.rada.mapa, null);
					if (r.rada.odkaz2 != null) {
						kresliOdkaz(g2, fm, r.rada.odkaz2, x2, yy, r.rada.mapa, r.rada.rezim2);
					}
				}
				yy += fm.getHeight();
			}
		} finally {
			g2.dispose();
		}
	}

	/** Nakreslí odkaz rady a zapamatuje si, kam se dá kliknout a co stáhne. */
	private void kresliOdkaz(final Graphics2D g2, final FontMetrics fm, final String text, final int x, final int y, final KatalogovaMapa mapa, final PlanPruvodce.Rezim rezim) {
		g2.setColor(ODKAZ);
		g2.drawString(text, x, y);
		final int sirka = fm.stringWidth(text);
		g2.drawLine(x, y + 2, x + sirka, y + 2);
		odkazyRady.add(new Rectangle(x - 4, y - fm.getAscent(), sirka + 8, fm.getHeight()));
		mapyRady.add(mapa);
		rezimyRady.add(rezim);
	}

	/** Zalomí text po slovech na řádky nejvýš dané šířky. */
	static List<String> zalom(final String text, final FontMetrics fm, final int sirka) {
		final List<String> radky = new ArrayList<>();
		final StringBuilder radek = new StringBuilder();
		for (final String slovo : text.split(" ")) {
			if (radek.length() > 0 && fm.stringWidth(radek + " " + slovo) > sirka) {
				radky.add(radek.toString());
				radek.setLength(0);
			}
			if (radek.length() > 0) {
				radek.append(' ');
			}
			radek.append(slovo);
		}
		if (radek.length() > 0) {
			radky.add(radek.toString());
		}
		return radky;
	}
}
