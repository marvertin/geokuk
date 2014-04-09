package cz.geokuk.plugins.cesty.data;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.core.coordinates.Moud;

public class Usek extends Bousek0 {

    private Bod bvpred;
    private Bod bvzad;
    private boolean vzdusny = false;
    private double dalka = -1;

    Usek(Cesta cesta) {
        super(cesta);
    }

    void spoj(Bod b1, Bod b2) {
        bvzad = b1;
        bvpred = b2;
        b1.setUvpred(this);
        b2.setUvzad(this);
    }

    /**
     * V zorném poli bodu je úsek tehdy,
     * když je v obdélníku "opsaném" nad úsekem.
     * Opsaným obdélníkem rozumíme nejmenší možný obdélník,
     * jehož strany jsou rovnoběžné se souřadnicovými osami a to takový,
     * že celý úsek leží v tomto obdélníku.
     */
    @Override
    boolean jeVOpsanemObdelniku(Mou mou) {
        if (mou == null) return false;
        Mou mou1 = bvzad.getMou();
        Mou mou2 = bvpred.getMou();
        boolean jex = mou1.xx <= mou.xx && mou.xx <= mou2.xx
                || mou1.xx >= mou.xx && mou.xx >= mou2.xx;
        boolean jey = mou1.yy <= mou.yy && mou.yy <= mou2.yy
                || mou1.yy >= mou.yy && mou.yy >= mou2.yy;
        return jex && jey;
    }

    /**
     * Vrací nejbližší bod k danému úseku, ale jen
     * když pata kolmice leží na úsečce, tedy může vrátit ve speciálním případě i krajní body,
     * ale jen pokud pata kolmice padne přesně na krajní bod.
     *
     * @param mou
     * @return null, když je pata kolmice mimo úsečku.
     */
    public Mou getNejblizsiBodKolmoKUsecce(Mou mou) {
        if (mou == null) return null;
        Mou m = prusecikKolmice(bvzad.getMou(), bvpred.getMou(), mou);
        if (jeVOpsanemObdelniku(m)) {
            return m;
        } else {
            return null;
        }
    }

    /**
     * Vrací nejbližší bod k přímce zdadané daným úsekem.
     * nevrací tedy krajní body.
     *
     * @param mou
     * @return
     */
    public Mou getNejblizsiBodKPrimce(Mou mou) {
        if (mou == null) {
            return null;
        }
        return prusecikKolmice(bvzad.getMou(), bvpred.getMou(), mou);
    }

    public long computeKvadratVzdalenostiBoduKUsecce(Mou mou) {
        Mou m = getNejblizsiBodKolmoKUsecce(mou);
        if (m == null) return Long.MAX_VALUE;
        long kvadrat = mou.getKvadratVzdalenosti(m);
        return kvadrat;
    }


    /**
     * Spočítá kvadrát vzdálenosti bodu k úsečce úseku,
     * což může být i ke krajním bodům.
     *
     * @param mou
     * @return
     */
    @Override
    public long computeKvadratVzdalenosti(Mou mou) {
        Mou m = getNejblizsiBodKolmoKUsecce(mou);
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
     * Spočítá kvadrát vzdálenosti bodu k úsečce úseku,
     * což může být i ke krajním bodům.
     *
     * @param mou
     * @return
     */
    public long computeKvadratVzdalenostiKPrimce(Mou mou) {
        Mou m = getNejblizsiBodKPrimce(mou);
        long kvadrat = mou.getKvadratVzdalenosti(m);
        return kvadrat;
    }


    /**
     * Spočítá bod, který leží na přímce m1m2 a je nejblíže m3.
     * To je průsečík kolmice.
     *
     * @param m1
     * @param m2
     * @param m3
     * @return Průsečík kolmice, nikdy nevrací null.
     */
    private static Mou prusecikKolmice(Mou m1, Mou m2, Mou m3) {
        // TODO : what to do if m1 == m2?
        if (m1.equals(m2)) {
            return new Mou(m3);
        }

        long x1 = m1.xx;
        long y1 = m1.yy;
        long x2 = m2.xx;
        long y2 = m2.yy;
        long x3 = m3.xx;
        long y3 = m3.yy;

        long a11 = x1 - x2;
        long a12 = y1 - y2;
        long a21 = a12;
        long a22 = -a11;
        long b1 = -((a22) * x3 + (-a12) * y3);
        long b2 = -(x1 * y2 - x2 * y1);

        long d = a11 * a22 - a12 * a21;
        long d1 = b1 * a22 - a12 * b2;
        long d2 = a11 * b2 - b1 * a21;

        long x = d1 / d;
        long y = d2 / d;

        Mou mou = new Mou((int) x, (int) y);
        return mou;

    }

    Bod rozdelAZanikni(Mouable mouable) {
        Bod bod = getCesta().createBod(mouable);
        Usek u1 = getCesta().createUsek();
        Usek u2 = getCesta().createUsek();
        u1.spoj(bvzad, bod);
        u2.spoj(bod, bvpred);
        if (isVzdusny()) {
            long kvadratDalkyVzad = getBvzad().computeKvadratVzdalenosti(bod.getMou());
            long kvadratDalkyVpred = getBvpred().computeKvadratVzdalenosti(bod.getMou());
            if (kvadratDalkyVpred > kvadratDalkyVzad) {
                u2.setVzdusny(true);
            } else {
                u1.setVzdusny(true);
            }
        }
        return bod;

    }

    void setBvpred(Bod vpred) {
        bvpred = vpred;
        zneschopniDalku();
    }

    public Bod getBvpred() {
        return bvpred;
    }

    void setBvzad(Bod vzad) {
        bvzad = vzad;
        zneschopniDalku();
    }

    public Bod getBvzad() {
        return bvzad;
    }

    public boolean isVzdusny() {
        return vzdusny;
    }

    void setVzdusny(boolean vzdusny) {
        this.vzdusny = vzdusny;
    }

    public double getUhel() {
        Moud moud = bvpred.mouable.getMou().sub(bvzad.mouable.getMou());
        double uhel = Math.atan2(moud.dxx, moud.dyy);
        return uhel;
    }

    public double getMouDelkaVpred(Mou aMou) {
        if (isVzdusny()) return 0;
        Mou mou = getNejblizsiBodKPrimce(aMou);
        return Mou.dalka(mou, bvpred);
    }

    public double getMouDelkaVzad(Mou aMou) {
        if (isVzdusny()) return 0;
        Mou mou = getNejblizsiBodKPrimce(aMou);
        return Mou.dalka(mou, bvzad);
    }

    @Override
    public double dalkaCestaVpred(Mou aMou) {
        double delka = getMouDelkaVpred(aMou) + bvpred.dalkaCestaVpred(aMou);
        return delka;
    }

    @Override
    public double dalkaCestaVzad(Mou aMou) {
        double delka = getMouDelkaVzad(aMou) + bvzad.dalkaCestaVzad(aMou);
        return delka;
    }

    @Override
    public double dalka() {
        if (isVzdusny()) return 0;
        if (dalka < 0) {
            dalka = Mou.dalka(bvpred, bvzad);
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

    private void kon(boolean podm) {
        if (!podm)
            throw new RuntimeException("Selhala kontrola konzistence useku");
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
