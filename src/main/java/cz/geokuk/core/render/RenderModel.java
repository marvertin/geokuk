package cz.geokuk.core.render;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.VyrezChangedEvent;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.util.file.Filex;

public class RenderModel extends Model0 {

	static class Dvoj {
		double a;
		double b;
	}

	// private static final double OKRAJ_CENTIMETR = 0.01;
	private static final double OKRAJ_CENTIMETR = 0;

	private double natoceni;

	// rendrované hodnoty, to jsou ty, jejichž obrázky se budou staovat a rendrovat
	private Coord moord;

	// Nastavované během přípravy na rendrování
	private Dimension dim;

	private RenderUmisteniSouboru umisteniSouboru;

	private RenderSettings renderSettings;

	private RendererSwingWorker0 koswx;

	// Je true, pokud jsme v dialogu, kde se nastavuje rendrování
	private boolean jsmeVRendrovani;

	// public Mou SZ;
	//
	// public Mou SV;
	//
	// public Mou JZ;
	//
	// public Mou JV;

	/**
	 * Spočítá veliksot obdélníku tak, aby nový obdélník vešel do obdélníku daném zadanými stranami, když je pootočen o zadaný úhel.
	 *
	 * @param uhel
	 * @param a
	 * @param b
	 * @return
	 */
	private static Dvoj spocti(final double uhel, final double a, final double b) {

		final double q = Math.abs(Math.tan(uhel));
		final double q2m = 1 - q * q;

		final double a1 = (a - q * b) / q2m;
		final double b1 = (b - q * a) / q2m;
		final double a2 = a - a1;
		final double b2 = b - b1;

		final double ar = Math.hypot(a1, b2);
		final double br = Math.hypot(b1, a2);

		final Dvoj dvoj = new Dvoj();
		dvoj.a = ar;
		dvoj.b = br;

		dvoj.a *= 0.95;
		dvoj.b *= 0.95;

		return dvoj;
	}

	public Coord createRenderedCoord() {
		return moord.derive(getRenderedMoumer(), dim, getOKolikNatacet());
	}

	public void finishRenderingProcess() {
		jsmeVRendrovani = false;
		fire();
	}

	/**
	 * @return the renderedMoumer
	 */
	public int getCurrentMoumer() {
		return moord.getMoumer();
	}

	/**
	 * @return the dim
	 */
	public Dimension getDim() {
		return dim;
	}

	/**
	 * @return the coord
	 */
	public Coord getMoord() {
		return moord;
	}

	// public void onEvent(ZmenaMapNastalaEvent event) {
	// kaSet = event.getKaSet();
	// }

	/**
	 * Bere natočení mapy od severu na aktuálním středu. Nebere v úvahu inforamci o tom, jak se má moc natáčet.
	 *
	 * @return
	 */
	public double getNatoceni() {
		return natoceni;
	}

	public double getOKolikNatacet() {
		if (!getRenderSettings().isSrovnatDoSeveru()) {
			return 0;
		}
		return -getNatoceni();
	}

	public File getOutputFolder() {
		Filex fx = null;
		switch (renderSettings.getWhatRender()) {
		case JEN_OBRAZEK:
			fx = getUmisteniSouboru().getPictureDir();
			break;
		case GOOGLE_EARTH:
			fx = getUmisteniSouboru().getKmzDir();
			break;
		case OZI_EXPLORER:
			fx = getUmisteniSouboru().getOziDir();
			break;
		case TISK:
			fx = null;
			break;
		default:
			assert false : "Nenene: " + renderSettings.getWhatRender();
		}
		return fx == null ? null : fx.getEffectiveFile();
	}

	public PapirovaMetrika getPapirovaMetrika() {
		final Coord roord = getRoord();
		final int meritko = getRenderSettings().getPapiroveMeritko();
		return new PapirovaMetrika(roord.getWidthMetru() / meritko, roord.getHeightMetru() / meritko, OKRAJ_CENTIMETR);
	}

	/**
	 * @return the renderedMoumer
	 */
	public int getRenderedMoumer() {
		return renderSettings.getRenderedMoumer();
	}

	// /**
	// *
	// */
	// public void vypisChybySouradnic(double pixlunametr) {
	// Utm utmSZ = SZ.toUtm();
	// Utm utmSV = SV.toUtm();
	// Utm utmJZ = JZ.toUtm();
	// Utm utmJV = JV.toUtm();
	//
	//
	//
	// double chybaMetruS = Math.abs(utmSZ.uy - utmSV.uy);
	// double chybaMetruJ = Math.abs(utmJZ.uy - utmJV.uy);
	// double chybaMetruZ = Math.abs(utmSZ.ux - utmJZ.ux);
	// double chybaMetruV = Math.abs(utmSV.ux - utmJV.ux);
	// System.out.println("SZ: " + SZ.toWgs() + " " + utmSZ + " " + SZ.toWgs().lat);
	// System.out.println("SV: " + SV.toWgs() + " " + utmSV);
	// System.out.println("JZ: " + JZ.toWgs() + " " + utmJZ);
	// System.out.println("JV: " + JV.toWgs() + " " + utmJV);
	//
	// System.out.printf("Chyby v metrech: S=%d, J=%d, V=%d, Z=%d\n", (int) chybaMetruS, (int)chybaMetruJ, (int)chybaMetruZ, (int)chybaMetruV);
	// //System.out.printf("Chyby v pixlech: S=%d, J=%d, V=%d, Z=%d\n", pixlunametr*chybaMetruS, pixlunametr*chybaMetruJ, pixlunametr*chybaMetruZ, pixlunametr*chybaMetruV);
	//
	// }

	/**
	 * @return the renderSettings
	 */
	public RenderSettings getRenderSettings() {
		return renderSettings.copy();
	}

	/** Vrací rendrovací informace */
	public Coord getRoord() {
		final Mou moustred = getMoord().getMoustred();
		final int width = getDim().width;
		final int height = getDim().height;
		final Coord roord = new Coord(getRenderedMoumer(), moustred, new Dimension(width, height), getOKolikNatacet());
		return roord;
	}

	public EStavRendrovani getStavRendrovani() {
		if (koswx != null) {
			if (koswx.isCancelled()) {
				return EStavRendrovani.PRERUSOVANO; // běží
			} else {
				return EStavRendrovani.BEH;
			}
		}
		if (jsmeVRendrovani) {
			return EStavRendrovani.PRIPRAVA;
		} else {
			return EStavRendrovani.NIC; // běží
		}
	}

	public RenderUmisteniSouboru getUmisteniSouboru() {
		return umisteniSouboru;
	}

	public long odhadniMnozstviZabranePameti() {
		final int velikostPixlu = renderSettings.getImageType() == EImageType.png ? 4 : 3;
		long pamet;
		if (getRenderSettings().getWhatRender() == EWhatRender.GOOGLE_EARTH) {
			final DlazdicovaMetrikaXY metrika = spoctiDlazdicovouMetriku();
			pamet = (long) metrika.xx.dlaSize * metrika.yy.dlaSize * velikostPixlu;
		} else {
			pamet = (long) dim.height * dim.width * velikostPixlu;
		}
		return pamet;
	}

	public void onEvent(final VyrezChangedEvent event) {
		natoceni = event.getMoord().computNataceciUhel();
		moord = event.getMoord();
		spocitejVelikost();
		fire();
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		final EKaType podklad = event.getKatype();
		final RenderSettings rs = renderSettings.copy();
		rs.setRenderedMoumer(podklad.fitMoumer(rs.getRenderedMoumer()));
		setRenderSettings(rs);
	}

	public void prerusRendrovani() {
		if (koswx != null) {
			koswx.cancel(true);
		}
		// koswx = null;
		fire();
	}

	public void rendrovaniSkoncilo(final RenderResult renderResult) {
		koswx = null;
		fire();
		if (renderResult == null) {
			Dlg.error("Rendrování bylo přerušeno uživatelem.");
		} else {
			if (renderResult.file != null) {
				Dlg.info("Byl vytvořen soubor \"" + renderResult.file + "\"", "Výsledek rendrování");
			}
		}
	}

	/**
	 * @param renderSettings
	 *            the renderSettings to set
	 */
	public void setRenderSettings(final RenderSettings renderSettings) {
		if (renderSettings.equals(this.renderSettings)) {
			return;
		}
		this.renderSettings = renderSettings;
		spocitejVelikost();
		currPrefe().putStructure(FPref.RENDER_structure_node, renderSettings);
		fire();
	}

	/**
	 * @param umisteniSouboru
	 *            the umisteniSouboru to set
	 */
	public void setUmisteniSouboru(final RenderUmisteniSouboru umisteniSouboru) {
		if (umisteniSouboru.equals(this.umisteniSouboru)) {
			return;
		}
		this.umisteniSouboru = umisteniSouboru;
		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		pref.putFilex(FPref.RENDER_KMZ_DIR_value, umisteniSouboru.getKmzDir());
		pref.putFilex(FPref.RENDER_OZI_DIR_value, umisteniSouboru.getOziDir());
		pref.putFilex(FPref.RENDER_PICTURE_DIR_value, umisteniSouboru.getOziDir());
		fire(new RenderUmisteniSouboruChangedEvent(umisteniSouboru));
		fire();
	}

	public List<Wgs> spocitejKalibracniBody() {
		return spocitejKalibracniBody(getRoord(), renderSettings.getKalibrBodu());
	}

	public List<Wgs> spocitejKalibracniBody(final Coord cocox, final int kalibrBodu) {
		final List<Wgs> wgss = new ArrayList<>();
		final int width = cocox.getDim().width;
		final int height = cocox.getDim().height;
		final int kalistrana = (int) Math.ceil(Math.sqrt(kalibrBodu)); // počet kalibračních bodů rastru ve sloupci a řádku
		final double kalifaktor = (kalistrana * kalistrana - 1) / ((double) kalibrBodu - 1); // po jaké vzdálenosti kalibrační bod
		System.out.println("kalistrana: " + kalistrana);
		double kalicitac = 0;
		int bodocitac = 0;
		for (int x = 0; x < kalistrana; x++) {
			for (int y = 0; y < kalistrana; y++) {
				if (Math.round(kalicitac) == bodocitac) {
					final int xp = x * width / (kalistrana - 1);
					final int yp = y * height / (kalistrana - 1);
					final Wgs wgs = cocox.transform(new Point(xp, yp)).toWgs();
					wgss.add(wgs);
					kalicitac += kalifaktor;
				}
				bodocitac++;
			}
		}
		return wgss;
	}

	public DlazdicovaMetrikaXY spoctiDlazdicovouMetriku() {

		final Coord coord = createRenderedCoord();
		return new DlazdicovaMetrikaXY(new DlazdicovaMetrika(renderSettings.getKmzMaxDlazdiceX(), coord.getWidth()), new DlazdicovaMetrika(renderSettings.getKmzMaxDlazdiceY(), coord.getHeight()));

	}

	/**
	 * @return
	 */
	public Dvoj spoctiMalyOrezavaciOblednik() {
		final Dvoj dvoj = spoctiOrezavaciOblednik();
		int moumera = moord.getMoumer(); // aktuální měřítko
		final int renderedMoumer = getRenderedMoumer();
		while (moumera < renderedMoumer) {
			dvoj.a /= 2;
			dvoj.b /= 2;
			moumera++;
		}
		return dvoj;
	}

	/**
	 * @return
	 */
	public Dvoj spoctiOrezavaciOblednik() {
		return spoctiOrezavaciOblednik(moord.getWidth(), moord.getHeight());
	}

	/**
	 *
	 */
	public void spustRendrovani() {
		if (koswx != null) {
			koswx.cancel(true);
		}
		// zobrazServisniOknoAction.actionPerformed(null);
		final EWhatRender whatRender = renderSettings.getWhatRender();
		switch (whatRender) {
		case GOOGLE_EARTH:
			koswx = new GoogleEarthRenderSwingWorker();
			break;
		case OZI_EXPLORER:
			koswx = new OziExplorerRenderSwingWorker(whatRender);
			break;
		case TISK:
			koswx = new PrintingSwingWorker();
			break;
		default:
			koswx = new OziExplorerRenderSwingWorker(whatRender);
			break;
		}
		super.factoryInit(koswx);
		koswx.execute();
		fire();
	}

	public void startRenderingProcess() {
		jsmeVRendrovani = true;
		spocitejVelikost();
		fire();
	}

	public void uschovejAktualniMeritko() {
		// TODO asi testovat
		final RenderSettings rs = renderSettings.copy();
		rs.setRenderedMoumer(getMoord().getMoumer());
		setRenderSettings(rs);
	}

	@Override
	protected void initAndFire() {
		final RenderUmisteniSouboru u = new RenderUmisteniSouboru();
		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		u.setKmzDir(pref.getFilex(FPref.RENDER_KMZ_DIR_value, RenderUmisteniSouboru.KMZ_DIR));
		u.setOziDir(pref.getFilex(FPref.RENDER_OZI_DIR_value, RenderUmisteniSouboru.OZI_DIR));
		u.setPictureDir(pref.getFilex(FPref.RENDER_PICTURE_DIR_value, RenderUmisteniSouboru.PICURE_DIR));
		setUmisteniSouboru(u);
		setRenderSettings(currPrefe().getStructure(FPref.RENDER_structure_node, new RenderSettings()));
	}

	private void fire() {
		spocitejVelikost();
		fire(new PripravaRendrovaniEvent(renderSettings, getStavRendrovani()));
	}

	private void spocitejVelikost() {
		if (moord == null) {
			return;
		}
		int moumera = moord.getMoumer(); // aktuální měřítko
		int widthSurove = moord.getWidth();
		int heightSurove = moord.getHeight();
		// nastavit na najvětší měřítko
		final int renderedMoumer = getRenderedMoumer();
		while (moumera < renderedMoumer) {
			widthSurove *= 2;
			heightSurove *= 2;
			moumera++;
		}
		while (moumera > renderedMoumer) {
			widthSurove /= 2;
			heightSurove /= 2;
			moumera--;
		}
		final Dvoj dvoj = spoctiOrezavaciOblednik(widthSurove, heightSurove);
		dim = new Dimension((int) dvoj.a, (int) dvoj.b);
		// return new Dimension(width, height);
	}

	private Dvoj spoctiOrezavaciOblednik(final double a, final double b) {
		return spocti(-getOKolikNatacet(), a, b);
	}

}
