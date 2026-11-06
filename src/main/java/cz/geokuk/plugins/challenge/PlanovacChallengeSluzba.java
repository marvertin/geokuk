package cz.geokuk.plugins.challenge;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.refbody.DomaciSouradniceSeZmenilyEvent;
import cz.geokuk.plugins.refbody.RefbodyModel;
import cz.geokuk.util.file.Filex;

/**
 * Napojení {@link PlanovacChallenge} na program: události načtených keší, domova a stavu {@link ChallengeModel}, domov, střed mapy, složka pro předpočet
 * (podsložka {@code challenge} složky cache) a vystředění mapy na vybranou keš.
 */
public class PlanovacChallengeSluzba implements AfterInjectInit {

	private ChallengeModel challengeModel;
	private RefbodyModel refbodyModel;
	private VyrezModel vyrezModel;
	private PoziceModel poziceModel;
	private KesoidModel kesoidModel;
	private KachleModel kachleModel;
	private PlanovacChallenge planovac;

	public void inject(final ChallengeModel challengeModel) {
		this.challengeModel = challengeModel;
	}

	public void inject(final RefbodyModel refbodyModel) {
		this.refbodyModel = refbodyModel;
	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	@Override
	public void initAfterInject() {
		planovac = new PlanovacChallenge(challengeModel, new ZdrojovyPoskytovatelOblasti(), refbodyModel::getHc, this::adresarPredpoctu, this::stredMapy, this::vystredit);
	}

	public PlanovacChallenge planovac() {
		return planovac;
	}

	public void onEvent(final KeskyNactenyEvent event) {
		planovac.keseNacteny(KesVstup.zKesoidu(event.getVsechny().getKesoidy()));
	}

	public void onEvent(final DomaciSouradniceSeZmenilyEvent event) {
		planovac.domovZmenen();
	}

	public void onEvent(final cz.geokuk.core.coord.VyrezChangedEvent event) {
		planovac.stredMapyZmenen();
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		if (planovac != null) {
			planovac.modelZmenen(event);
		}
		switch (event.druh()) {
		case JEN_KANDIDATI:
		case VYSLEDEK:
		case AKTIVNI:
		case DEFINICE:
		case NACTENO:
			obnovFiltrKandidatu();
			break;
		default:
			break;
		}
	}

	/** Filtr „jen kandidáti“ se promítne do filtru keší na mapě; bez výsledku výpočtu se nefiltruje. */
	void obnovFiltrKandidatu() {
		if (kesoidModel == null) {
			return;
		}
		final Challenge c = challengeModel.jenKandidatiPro();
		kesoidModel.setDoplnkovyFiltr(c == null ? null : FiltrKandidatu.pro(challengeModel, c));
	}

	private Path adresarPredpoctu() {
		final Filex slozka = kachleModel.getKachleCacheFolderHolder().getKachleCacheFolder();
		final File cache = slozka == null ? null : slozka.getEffectiveFile();
		return cache == null ? Paths.get(System.getProperty("java.io.tmpdir"), "geokuk-challenge") : cache.toPath().resolve("challenge");
	}

	private Wgs stredMapy() {
		return vyrezModel.getMoord() == null ? null : vyrezModel.getMoord().getMoustred().toWgs();
	}

	/** Vystředí mapu na vybranou keš; když je keš v datech, nastaví na ni i pozici, jako stávající akce vystředění. */
	private void vystredit(final VybranyKandidat kandidat) {
		if (kesoidModel.getVsechnyKesoidy() != null) {
			for (final Kesoid k : kesoidModel.getVsechnyKesoidy().getKesoidy()) {
				if (kandidat.id().equalsIgnoreCase(k.getIdentifier())) {
					final Wpt wpt = k.getMainWpt();
					if (wpt != null) {
						poziceModel.setPozice(wpt);
						vyrezModel.vystredovatNaPozici();
						return;
					}
				}
			}
		}
		vyrezModel.presunMapuNaMoustred(new Wgs(kandidat.lat(), kandidat.lon()).toMou());
	}
}
