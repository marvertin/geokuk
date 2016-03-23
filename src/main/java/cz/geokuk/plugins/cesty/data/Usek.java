package cz.geokuk.plugins.cesty.data;

import cz.geokuk.core.coordinates.*;

public class Usek extends Bousek0 {

	private Bod		bvpred;
	private Bod		bvzad;
	private boolean	vzdusny	= false;
	private double	dalka	= -1;

	Usek(final Cesta cesta) {
		super(cesta);
	}

	void spoj(final Bod b1, final Bod b2) {
		bvzad = b1;
		bvpred = b2;
		b1.setUvpred(this);
		b2.setUvzad(this);
	}

	/**
	 * V zorném poli bodu je úsek tehdy, když je v obdélníku "opsaném" nad úsekem. Opsaným obdélníkem rozumíme nejmenší možný obdélník, jehož strany jsou rovnoběžné se souřadnicovými osami a to takový, že celý úsek leží v tomto obdélníku.
	 */
	@Override
	boolean jeVOpsanemObdelniku(final Mou mou) {
		if (mou == null) {
			return false;
		}
		final Mou mou1 = bvzad.getMou();
		final Mou mou2 = bvpred.getMou();
		final boolean jex = mou1.xx <= mou.xx && mou.xx <= mou2.xx || mou1.xx >= mou.xx && mou.xx >= mou2.xx;
		final boolean jey = mou1.yy <= mou.yy && mou.yy <= mou2.yy || mou1.yy >= mou.yy && mou.yy >= mou2.yy;
		return jex && jey;
	}

	/**
	 * Vrací nejbližší bod k danému úseku, ale jen když pata kolmice leží na úsečce, tedy může vrátit ve speciálním případě i krajní body, ale jen pokud pata kolmice padne přesně na krajní bod.
	 *
	 * @param mou
	 * @return null, když je pata kolmice mimo úsečku.
	 */
	public Mou getNejblizsiBodKolmoKUsecce(final Mou mou) {
		if (mou == null) {
			return null;
		}
		final Mou m = prusecikKolmice(bvzad.getMou(), bvpred.getMou(), mou);
		if (jeVOpsanemObdelniku(m)) {
			return m;
		} else {
			return null;
		}
	}

	/**
	 * Vrací nejbližší bod k přímce zdadané daným úsekem. nevrací tedy krajní body.
	 *
	 * @param mou
	 * @return
	 */
	public Mou getNejblizsiBodKPrimce(final Mou mou) {
		if (mou == null) {
			return null;
		}
		return prusecikKolmice(bvzad.getMou(), bvpred.getMou(), mou);
	}

	public long computeKvadratVzdalenostiBoduKUsecce(final Mou mou) {
		final Mou m = getNejblizsiBodKolmoKUsecce(mou);
		if (m == null) {
			return Long.MAX_VALUE;
		}
		final long kvadrat = mou.getKvadratVzdalenosti(m);
		return kvadrat;
	}

	/**
	 * Spočítá kvadrát vzdálenosti bodu k úsečce úseku, což může být i ke krajním bodům.
	 *
	 * @param mou
	 * @return
	 */
	@Override
	public long computeKvadratVzdalenosti(final Mou mou) {
		final Mou m = getNejblizsiBodKolmoKUsecce(mou);
		long kvadrat;
		if (mou == null) { // je to jeden z krajnich bodul
			kvadrat = Math.min(bvzad.getMou().getKvadratVzdalenosti(m), bvpred.getMou().getKvadratVzdalenosti(m));
		} else {
			// je to komo k usecce, takze to je bod na usecce
			kvadrat = mou.getKvadratVzdalenosti(m);
		}
		return kvadrat;
	}

	/**
	 * Spočítá kvadrát vzdálenosti bodu k úsečce úseku, což může být i ke krajním bodům.
	 *
	 * @param mou
	 * @return
	 */
	public long computeKvadratVzdalenostiKPrimce(final Mou mou) {
		final Mou m = getNejblizsiBodKPrimce(mou);
		final long kvadrat = mou.getKvadratVzdalenosti(m);
		return kvadrat;
	}

	/**
	 * Spočítá bod, který leží na přímce m1m2 a je nejblíže m3. To je průsečík kolmice.
	 *
	 * @param m1
	 * @param m2
	 * @param m3
	 * @return Průsečík kolmice, nikdy nevrací null.
	 */
	private static Mou prusecikKolmice(final Mou m1, final Mou m2, final Mou m3) {
		// TODO : what to do if m1 == m2?
		if (m1.equals(m2)) {
			return new Mou(m3);
		}

		final double x1 = m1.xx;
		final double y1 = m1.yy;
		final double x2 = m2.xx;
		final double y2 = m2.yy;
		final double x3 = m3.xx;
		final double y3 = m3.yy;

		final double a11 = x1 - x2;
		final double a12 = y1 - y2;
		final double a21 = a12;
		final double a22 = -a11;
		final double b1 = -((a22) * x3 + (-a12) * y3);
		final double b2 = -(x1 * y2 - x2 * y1);

		final double d = a11 * a22 - a12 * a21;
		final double d1 = b1 * a22 - a12 * b2;
		final double d2 = a11 * b2 - b1 * a21;

		final double x = d1 / d;
		final double y = d2 / d;

		final Mou mou = new Mou((int) x, (int) y);
		// System.out.printf("pruseCikk [%d,%d] --- [%d,%d] | [%d,%d] = [%d,%d]%n", x1, y1, x2, y2, x3, y3, x, y);
		// System.out.printf("pruseCikk (%d,%d) %n", a11, a12);
		return mou;

	}

	Bod rozdelAZanikni(final Mouable mouable) {
		final Bod bod = getCesta().createBod(mouable);
		final Usek u1 = getCesta().createUsek();
		final Usek u2 = getCesta().createUsek();
		u1.spoj(bvzad, bod);
		u2.spoj(bod, bvpred);
		if (isVzdusny()) {
			final long kvadratDalkyVzad = getBvzad().computeKvadratVzdalenosti(bod.getMou());
			final long kvadratDalkyVpred = getBvpred().computeKvadratVzdalenosti(bod.getMou());
			if (kvadratDalkyVpred > kvadratDalkyVzad) {
				u2.setVzdusny(true);
			} else {
				u1.setVzdusny(true);
			}
		}
		return bod;

	}

	void setBvpred(final Bod vpred) {
		bvpred = vpred;
		zneschopniDalku();
	}

	public Bod getBvpred() {
		return bvpred;
	}

	void setBvzad(final Bod vzad) {
		bvzad = vzad;
		zneschopniDalku();
	}

	public Bod getBvzad() {
		return bvzad;
	}

	public boolean isVzdusny() {
		return vzdusny;
	}

	void setVzdusny(final boolean vzdusny) {
		this.vzdusny = vzdusny;
	}

	public double getUhel() {
		final Moud moud = bvpred.mouable.getMou().sub(bvzad.mouable.getMou());
		final double uhel = Math.atan2(moud.dxx, moud.dyy);
		return uhel;
	}

	public double getMouDelkaVpred(final Mou aMou) {
		if (isVzdusny()) {
			return 0;
		}
		final Mou mou = getNejblizsiBodKPrimce(aMou);
		return FGeoKonvertor.dalka(mou, bvpred);
	}

	public double getMouDelkaVzad(final Mou aMou) {
		if (isVzdusny()) {
			return 0;
		}
		final Mou mou = getNejblizsiBodKPrimce(aMou);
		return FGeoKonvertor.dalka(mou, bvzad);
	}

	@Override
	public double dalkaCestaVpred(final Mou aMou) {
		final double delka = getMouDelkaVpred(aMou) + bvpred.dalkaCestaVpred(aMou);
		return delka;
	}

	@Override
	public double dalkaCestaVzad(final Mou aMou) {
		final double delka = getMouDelkaVzad(aMou) + bvzad.dalkaCestaVzad(aMou);
		return delka;
	}

	@Override
	public double dalka() {
		if (isVzdusny()) {
			return 0;
		}
		if (dalka < 0) {
			dalka = FGeoKonvertor.dalka(bvpred, bvzad);
		}
		return dalka;
	}

	void zneschopniDalku() {
		dalka = -1;
	}

	@Override
	public Bousek0 getBousekVpred() {
		return getBvpred();
	}

	@Override
	public Bousek0 getBousekVzad() {
		return getBvzad();
	}

	private void kon(final boolean podm) {
		if (!podm) {
			throw new RuntimeException("Selhala kontrola konzistence useku");
		}
	}

	@Override
	protected void kontrolaKonzistence() {
		kon(bvpred != null);
		kon(bvzad != null);
		kon(bvpred != bvzad);
		kon(bvpred.getUvzad() == this);
		kon(bvzad.getUvpred() == this);
	}

	@Override
	public Bod getKoncovyBodDruheCestyVhodnyProSpojeni() {
		return null;
	}

}
