package cz.geokuk.plugins.cesty.data;

import cz.geokuk.core.coordinates.*;
import cz.geokuk.plugins.kesoid.Wpt;

public class Bod extends Bousek0 implements Uchopenec {

	Mouable			mouable;

	private Usek	uvpred;
	private Usek	uvzad;

	Bod(final Cesta cesta, final Mouable mouable) {
		super(cesta);
		this.mouable = mouable;
	}

	@Override
	public Mou getMou() {
		return mouable.getMou();
	}

	@Override
	boolean jeVOpsanemObdelniku(final Mou mou) {
		return false;
	}

	@Override
	public long computeKvadratVzdalenosti(final Mou mou) {
		if (mou == null) {
			return Long.MAX_VALUE;
		}
		final Mou m = getMou();
		final long dx = m.xx - mou.xx;
		final long dy = m.yy - mou.yy;
		final long kvadrat = dx * dx + dy * dy;
		return kvadrat;
	}

	/**
	 * Určuje jak je bod daleko kolmo k úsečce, která spojuje druhé strany úseků vedoucí z tohoto bodu. Pokud je tento bod krajový nebo pokud kolmice spuštěná z tohoto bodu ke zmíněné úsečce na úsečce neleží, vrací MAC_VALUE, takže je to moc daleko.
	 *
	 * @return
	 */
	public long computeKvadratOdklonu() {
		if (uvpred == null || uvzad == null) {
			return Long.MAX_VALUE; // moc odkloneno, kdyz neurcime
		}
		final Bod b1 = uvzad.getBvzad();
		final Bod b2 = uvpred.getBvpred();
		final Usek pomUsek = getCesta().createUsek();
		pomUsek.setBvzad(b1);
		pomUsek.setBvpred(b2);
		final long odklon = pomUsek.computeKvadratVzdalenostiBoduKUsecce(getMou());
		return odklon;
	}

	/**
	 * Odstraní bod z cesty včetně přilehlých úseků a zbylé body spojí novým úsekem. Vrací nově vytvořený úsek
	 */
	Usek remove() {
		setChanged();
		if (isKrajovy()) {
			if (isStart()) {
				getCesta().setStart(getBvpred());
			}
			if (isCil()) {
				getCesta().setCil(getBvzad());
			}
			if (cesta.isEmpty()) { // prázdné cesty nechceme
				getCesta().remove();
			}
			return null;
		} else {
			final Usek usek = getCesta().createUsek();
			usek.setVzdusny(uvzad.isVzdusny() && uvpred.isVzdusny());
			usek.spoj(uvzad.getBvzad(), uvpred.getBvpred());
			return usek;
		}
	}

	public boolean isStart() {
		return uvzad == null;
	}

	public boolean isCil() {
		return uvpred == null;
	}

	public boolean isKrajovy() {
		return uvpred == null || uvzad == null;
	}

	public boolean isVnitrni() {
		return uvpred != null && uvzad != null;
	}

	public boolean isSamostatny() {
		return uvpred == null && uvzad == null;
	}

	/**
	 * Pokud je bod bodem krajovým, vrací krajový úsek, pokud je vnitřní nebo samsotatný, vrací null.
	 *
	 * @return
	 */
	public Usek getKrajovyUsek() {
		if (!isKrajovy()) {
			return null;
		}
		if (uvzad != null) {
			return uvzad;
		}
		return uvpred;
	}

	/**
	 * Vrátí bližší úsek z obou úseků, kpokud jsou oba. Pokud je jediný úsek, vrátí ho jen tehdy, kdy je ten ůsek blíž než tento bod, jinak vrací null.
	 *
	 * @param mou
	 * @return
	 */
	public Usek getBlizsiUsek(final Mou mou) {
		if (isVnitrni()) {
			final long kvadrat1 = uvzad.computeKvadratVzdalenostiKPrimce(mou);
			final long kvadrat2 = uvpred.computeKvadratVzdalenostiKPrimce(mou);
			final Usek usek = kvadrat1 > kvadrat2 ? uvzad : uvpred; // ano, pravdu maximum, protože díky křížení je to blíž
			return usek;
		}
		final Usek usek = getKrajovyUsek();
		if (usek == null) {
			return null; // tak uz to muze byt jen samsotatny bod
		}
		if (usek.getNejblizsiBodKolmoKUsecce(mou) != null) {
			return usek; // kdyz je kolmo k úsečce, je to ten úsek
		}
		return null; // jinak null, protože to není bližší úsek

	}

	public Mouable getMouable() {
		return mouable;
	}

	void setMouable(final Mouable mouable) {
		this.mouable = mouable;
		zneschopniDelkuPrilehlychUseku();
	}

	void setUvpred(final Usek vpred) {
		uvpred = vpred;
		zneschopniDelkuPrilehlychUseku();
	}

	void setUvzad(final Usek vzad) {
		uvzad = vzad;
		zneschopniDelkuPrilehlychUseku();
	}

	public Usek getUvpred() {
		return uvpred;
	}

	public Bod getBvpred() {
		return uvpred == null ? null : uvpred.getBvpred();
	}

	public Bod getBvzad() {
		return uvzad == null ? null : uvzad.getBvzad();
	}

	public Usek getUvzad() {
		return uvzad;
	}

	public boolean isNaHraniciSegmentu() {
		if (isKrajovy()) {
			return true;
		}
		final boolean result = getUvzad().isVzdusny() || getUvpred().isVzdusny();
		return result;
	}

	/**
	 * U kruhových cest je startocil startovní a cílový bod. U nekruhových není startocil.
	 *
	 * @return
	 */
	public boolean isStartocil() {
		return isKrajovy() && cesta.isKruh();
	}

	public boolean isWpt() {
		return mouable instanceof Wpt;
	}

	@Override
	public double dalka() {
		return 0;
	}

	@Override
	public double dalkaCestaVpred(final Mou aMou) {
		double delka = 0;
		for (Bod bod = this; !bod.isCil(); bod = bod.getBvpred()) {
			delka += bod.getUvpred().dalka();
		}
		return delka;
	}

	@Override
	public double dalkaCestaVzad(final Mou aMou) {
		double delka = 0;
		for (Bod bod = this; !bod.isStart(); bod = bod.getBvzad()) {
			delka += bod.getUvzad().dalka();
		}
		return delka;
	}

	private void zneschopniDelkuPrilehlychUseku() {
		if (uvpred != null) {
			uvpred.zneschopniDalku();
		}
		if (uvzad != null) {
			uvzad.zneschopniDalku();
		}
	}

	/**
	 * Rozdělí cestu v bodě a vrátí novou cestu. Tento bod zůstane v cestě původní
	 */
	public Cesta rozdelCestu() {
		final Cesta novaCesta = Cesta.create();
		final Bod novyBod = novaCesta.pridejNaKonec(mouable); // přidám první nový bod nad tímto mouablem
		if (!isCil()) { // pokud je to cílový bod, tak je vytvořením jednobodové cesty rozdělení dokončeno
			novaCesta.setCil(cesta.getCil()); // cíl nové cesty je stejný jako cíl této cesty
			novyBod.uvpred = uvpred; // tam, kam šel tento bod teď půjde nový bod
			novyBod.uvpred.setBvzad(novyBod); // a ten úsek co jde dál, ten nepůjde nikam
			cesta.setCil(this); // nastavením cílu odřízneme zbytek cesty
		}
		novaCesta.napojBouskyNaTutoCestu();
		cesta.getDoc().xadd(novaCesta);
		return novaCesta;
	}

	@Override
	public Bousek0 getBousekVpred() {
		return getUvpred();
	}

	@Override
	public Bousek0 getBousekVzad() {
		return getUvzad();
	}

	private void kon(final boolean podm) {
		if (!podm) {
			throw new RuntimeException("Selhala kontrola konzistence bodu");
		}
	}

	@Override
	protected void kontrolaKonzistence() {
		boolean assertsEnabled = false;
		assert assertsEnabled = true;
		if (!assertsEnabled) {
			return;
		}
		if (uvpred == null) {
			kon(cesta.getCil() == this);
		} else {
			kon(uvpred.getBvzad() == this);
		}
		if (uvzad == null) {
			kon(cesta.getStart() == this);
		} else {
			kon(uvzad.getBvpred() == this);
		}
	}

	@Override
	public Bod getKoncovyBodDruheCestyVhodnyProSpojeni() {
		if (cesta.isKruh()) {
			return null;
		}
		if (!isKrajovy()) {
			return null;
		}
		final Mou myMou = getMou();
		// System.out.println("MOZNOST SPOJENI proverujeme ");

		for (final Cesta c : cesta.getDoc()) {
			if (c == cesta) {
				continue; // ne na sebe
			}
			if (c.isKruh()) {
				continue; // na kruhové cesty nenapojujeme
			}
			Bod tenDruhy = null;
			if (isStart()) {
				tenDruhy = c.getCil();
			}
			if (isCil()) {
				tenDruhy = c.getStart();
			}
			if (tenDruhy == null) {
				continue;
			}
			// System.out.println(" MOUKY: " + tenDruhy.getMou() + " - " + myMou);
			if (tenDruhy.getMou().equals(myMou)) {
				return tenDruhy;
			}

		}
		return null;
	}

	public boolean isMeziVzdusnymiUseky() {
		return !isKrajovy() && uvpred.isVzdusny() && uvzad.isVzdusny();
	}
}
