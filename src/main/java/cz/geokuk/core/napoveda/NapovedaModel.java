package cz.geokuk.core.napoveda;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import cz.geokuk.core.onoffline.OnofflineModelChangeEvent;
import cz.geokuk.core.program.FConst;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.util.process.BrowserOpener;

public class NapovedaModel extends Model0 {

	private List<ZpravaUzivateli>	zpravyUzivatelum;
	private int						lastViewedMsgNum;
	private boolean					onlineMode;

	public int getLastViewedMsgNum() {
		return lastViewedMsgNum;
	}

	public List<ZpravaUzivateli> getZpravyUzivatelum() {
		return zpravyUzivatelum;
	}

	public void onEvent(final OnofflineModelChangeEvent event) {
		onlineMode = event.isOnlineMOde();
	}

	public void setLastViewedMsgNum(final int aLastViewedMsgNum) {
		lastViewedMsgNum = aLastViewedMsgNum;
		currPrefe().node(FPref.VSEOBECNE_node).putInt(FPref.LAST_VIEWED_MSG_NUM_value, lastViewedMsgNum);
	}

	public void setZpravyUzivatelum(final List<ZpravaUzivateli> zpravyUzivatelum) {
		this.zpravyUzivatelum = zpravyUzivatelum;
		fire(new NapovedaModelChangedEvent());

		if (zpravyUzivatelum.size() > 0) {
			factoryInit(new ZpravyUzivatelumAction()).actionPerformed(null);
		}
	}

	public void zkontrolujNoveAktualizace(final boolean zobrazovatInfoPriSpravneVerzi) {
		if (onlineMode) {
			new ZkontrolovatAktualizaceSwingWorker(zobrazovatInfoPriSpravneVerzi, this).execute();
		}

	}

	public void zobrazNapovedu(final String tema) {
		try {
			BrowserOpener.displayURL(new URL(tema == null ? FConst.WEB_PAGE_WIKI : FConst.WEB_PAGE_WIKI + "/" + tema));
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	protected void initAndFire() {
		lastViewedMsgNum = currPrefe().node(FPref.VSEOBECNE_node).getInt(FPref.LAST_VIEWED_MSG_NUM_value, 0);

		fire(new NapovedaModelChangedEvent());
	}

}
