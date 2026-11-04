/**
 *
 */
package cz.geokuk.core.program;

import java.io.IOException;

import javax.swing.SwingUtilities;

import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.hledani.HledaciSluzba;
import cz.geokuk.core.napoveda.Diagnostika;
import cz.geokuk.core.napoveda.DiagnostikaMapy;
import cz.geokuk.core.napoveda.NapovedaModel;
import cz.geokuk.core.onoffline.OnofflineModel;
import cz.geokuk.core.ovladani.DalkoveOvladani;
import cz.geokuk.core.profile.ProfileModel;
import cz.geokuk.core.render.RenderModel;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.cesty.*;
import cz.geokuk.plugins.geocoding.GeocodingModel;
import cz.geokuk.plugins.kesoid.KesoidFilterModel;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.StavVrstevModel;
import cz.geokuk.plugins.kesoid.ToastModel;
import cz.geokuk.plugins.kesoidkruhy.KruhyModel;
import cz.geokuk.plugins.kesoidobsazenost.ObsazenostModel;
import cz.geokuk.plugins.kesoidpopisky.PopiskyModel;
import cz.geokuk.plugins.mapy.*;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.data.UzivatelskeMapy;
import cz.geokuk.plugins.mapy.kachle.podklady.KachleZiskavac;
import cz.geokuk.plugins.mapy.kachle.podklady.KachloDownloader;
import cz.geokuk.plugins.mapy.sprava.SpravaMapModel;
import cz.geokuk.plugins.mrizky.MrizkaModel;
import cz.geokuk.plugins.refbody.HlidacReferencnihoBodu;
import cz.geokuk.plugins.refbody.RefbodyModel;
import cz.geokuk.plugins.vylety.*;
import cz.geokuk.util.lang.FString;

/**
 * @author Martin Veverka
 *
 */
public class Inicializator {

	private final MainFrameHolder mainFrameHolder = new MainFrameHolder();
	private NapovedaModel napovedaModel;
	private SpravaMapModel spravaMapModel;
	private DalkoveOvladani dalkoveOvladani;
	private Akce akce;

	public void inicializace() {
		final BeanBag bb = new BeanBag();
		bb.registerSigleton(bb);
		bb.registerSigleton(new EventManager());
		bb.registerSigleton(new Prefe());

		bb.registerSigleton(new CestyZperzistentnovac());
		bb.registerSigleton(new VyletovyZperzistentnovac());

		// modely
		bb.registerSigleton(new PoziceModel());
		bb.registerSigleton(new VyrezModel());
		bb.registerSigleton(new CestyModel());
		bb.registerSigleton(new VyletModel());
		bb.registerSigleton(new CestyNacitaniKesoiduWatchDog());
		bb.registerSigleton(new VyletNacitaniKesoiduWatchDog());
		bb.registerSigleton(new KesoidFilterModel());
		bb.registerSigleton(new PopiskyModel());
		bb.registerSigleton(new KruhyModel());
		bb.registerSigleton(new ObsazenostModel());
		bb.registerSigleton(new StavVrstevModel());
		bb.registerSigleton(new ToastModel());

		bb.registerSigleton(new MrizkaModel("Dd", false));
		bb.registerSigleton(new MrizkaModel("DdMmMmm", false));
		bb.registerSigleton(new MrizkaModel("DdMmSs", false));
		bb.registerSigleton(new MrizkaModel("Utm", false));
		bb.registerSigleton(new MrizkaModel("S42", false));
		bb.registerSigleton(new MrizkaModel("JTSK", false));
		bb.registerSigleton(new MrizkaModel("Meritkovnik", true));

		bb.registerSigleton(new RefbodyModel());
		bb.registerSigleton(new KachleModel());
		spravaMapModel = bb.registerSigleton(new SpravaMapModel());
		bb.registerSigleton(new KachloDownloader());

		bb.registerSigleton(new MapyModel());
		bb.registerSigleton(new KesoidModel());

		bb.registerSigleton(new HlidacReferencnihoBodu());
		bb.registerSigleton(new ProfileModel());
		bb.registerSigleton(new ProgressModel());
		bb.registerSigleton(new OknoModel());
		bb.registerSigleton(new RenderModel());
		napovedaModel = bb.registerSigleton(new NapovedaModel());
		bb.registerSigleton(new OnofflineModel());
		bb.registerSigleton(new GeocodingModel());

		bb.registerSigleton(new HledaciSluzba());
		bb.registerSigleton(new DiagnostikaMapy());
		dalkoveOvladani = bb.registerSigleton(new DalkoveOvladani());

		bb.registerSigleton(new KachleZiskavac());

		final KesoidPluginManager kpm = new KesoidPluginManager();
		bb.registerSigleton(kpm);
		kpm.getPlugins().stream().forEach(bb::registerSigleton);

		// akce
		akce = new Akce();
		bb.registerSigleton(akce);
		bb.registrFieldsAsSingleton(akce);

		intMapAkce(bb, akce);
		//
		bb.registerSigleton(mainFrameHolder);
		bb.init();
		// Board.eveman = eveman;
	}

	public void intMapAkce(final BeanBag bb, final Akce akce) {
		UzivatelskeMapy.setZkratkyProgramu(akce.zkratky());
		final String chyby = UzivatelskeMapy.nacti();
		if (chyby != null) {
			Diagnostika.zaznamenejChybu(chyby);
			SwingUtilities.invokeLater(() -> Dlg.error(FString.text(UzivatelskeMapy.textDialogu(chyby))));
		}
		for (final EKaType ka : EKaType.values()) {
			final MapyAction0 jednamapoakce = new PodkladAction(ka);
			akce.mapoakce.add(jednamapoakce);
			bb.registerSigleton(jednamapoakce);

		}
	}

	public void setMainFrame(final JMainFrame frame) {
		mainFrameHolder.setMainFrame(frame);
	}

	public void spustDalkoveOvladani(final int port, final boolean vyvojova) {
		try {
			dalkoveOvladani.spust(port, vyvojova);
		} catch (final IOException | RuntimeException e) {
			Dlg.error(DalkoveOvladani.popisChyby(port, e));
		}
		akce.dalkoveOvladaniAction.ukazStav();
	}

	/** Nová instalace: průvodce nastavením map se otevře sám. */
	public void nabidniPruvodceMap(final boolean automatizace) {
		akce.pruvodceMapAction.nabidniPriPrvnimSpusteni(automatizace);
	}

	/** Okno je zobrazené, spustí se věci, které nemají zdržovat jeho stavbu. */
	public void poStartu() {
		spravaMapModel.poStartu();
	}

	public void zkontrolovatAktualizace() {
		napovedaModel.zkontrolujNoveAktualizacePoStartu();
	}
}
