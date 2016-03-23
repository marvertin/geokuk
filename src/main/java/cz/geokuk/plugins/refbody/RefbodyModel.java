package cz.geokuk.plugins.refbody;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;

public class RefbodyModel extends Model0 {

	private static final Logger log = LogManager.getLogger(RefbodyModel.class.getSimpleName());

	private static final Wgs DEFAULTNI_DOMACI_SOURADNICE = new Wgs(49.8, 15.5);

	private Wgs hc;

	private Factory factory;

	private KesoidModel kesoidModel;

	public Wgs getHc() {
		return hc;
	}

	@Override
	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public List<NaKonkretniBodAction> nacti() {
		// TODO Předělat načítání z Geogetu, nevhodně se zde kombinuje model a controlery
		final List<NaKonkretniBodAction> list = new ArrayList<>();
		if (!kesoidModel.getUmisteniSouboru().getGeogetDataDir().isActive()) {
			return list;
		}
		final File file = new File(kesoidModel.getUmisteniSouboru().getGeogetDataDir().getEffectiveFile(), "geohome.ini");
		try {
			if (file.canRead()) {
				// TODO prozkoumat, zda opravdu geogetí data jsou v tomto kódování
				try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "CP1250"))) {
					String line;
					while ((line = br.readLine()) != null) {
						final String[] aa = line.split(" +", 3);
						if (aa.length != 3) {
							continue;
						}
						try {
							final Wgs wgs = new Wgs(Double.parseDouble(aa[0]), Double.parseDouble(aa[1]));
							final NaKonkretniBodAction action = factory.init(new NaKonkretniBodAction(aa[2], wgs));
							list.add(action);
						} catch (final Throwable e) {
							FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Pokus nacist data ze souboru " + file + ", radek " + (list.size() + 1));
						}
					}
				}
			} else {
				log.error("Soubor \"" + file + "\" nelze číst!");
			}
			return list;
		} catch (final IOException e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Pokus nacist data ze souboru " + file + ", radek " + (list.size() + 1));
			return list;
		}
	}

	public void setHc(final Wgs hc) {
		if (hc.equals(this.hc)) {
			return;
		}
		this.hc = hc;
		currPrefe().node(FPref.DOMACI_SOURADNICE_node).putWgs(FPref.HC_value, hc);
		fire(new DomaciSouradniceSeZmenilyEvent(hc));
	}

	@Override
	protected void initAndFire() {
		setHc(currPrefe().node(FPref.DOMACI_SOURADNICE_node).getWgs(FPref.HC_value, DEFAULTNI_DOMACI_SOURADNICE));
	}

}
