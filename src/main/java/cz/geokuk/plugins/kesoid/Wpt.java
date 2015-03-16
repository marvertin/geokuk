package cz.geokuk.plugins.kesoid;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Uchopenec;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;

import java.util.HashMap;
import java.util.Map;

public class Wpt extends Weikoid0 implements Uchopenec {

    public static final String TRADITIONAL_CACHE = "Traditional Cache";

    private static Map<String, String> wptMapping = new HashMap<>();

    static {
        wptMapping.put("Virtual Stage", "Question to Answer");
        wptMapping.put("Physical Stage", "Stages of a Multicache");
    }

    /**
     * Jmené vejpointu, z GPS tag name
     */
    private String name;

    /**
     * symbol waypointu, zároveˇje jeho typem, určuje, co se zobrazí na napě,
     * není to přímo <sym>
     */
    private String sym;

    /**
     * Sožadnice
     */
    private int elevation;

    private boolean rucnePridany;


    /**
     * Název waypointu, je to nějaký delší název z GPS to bud <cmt> nebo <desc>, podle toho, co tam je
     */
    private String nazev;

    //private String prefix;

    // Podpora vykreslování
    private Sklivec sklivec;
    private int sklivecValidityCode;

    private static int currentSklivecValidityCode;

    public double lat;
    public double lon;

    private int xx = -1;
    private int yy = -1;

    private EZOrder zorder = EZOrder.OTHER;

    /**
     *
     */
    public Wpt() {
    }


    /**
     * @return the wgs
     */
    public Wgs getWgs() {
        return new Wgs(lat, lon);
    }

    @Override
    public Mou getMou() {
        if (yy == -1) { // testovat yy, protože se nastavuje později
            Mou mou = getWgs().toMou();
            xx = mou.xx;
            yy = mou.yy;
            return mou;
        } else {
            //      System.out.println("kesnuto " + xx + " " + yy);
            return new Mou(xx, yy);
        }
    }

    /**
     * @param aWgs the wgs to set
     */
    public void setWgs(Wgs aWgs) {
        lat = aWgs.lat;
        lon = aWgs.lon;
    }

    //  public void setPrefix(String prefix) {
    //    this.prefix = prefix.intern();
    //  }

    public void setNazev(String aNazev) {
        // Je tam strašne moc krátkých názvů jako TrB nebo ZhB
        nazev = aNazev.length() >= 5 ? aNazev : aNazev.intern();
    }

    //  public String getPrefix() {
    //    return prefix;
    //  }

    public EKesWptType getType() {
        return EKesWptType.decode(sym);
    }


    public String getNazev() {
        return nazev;
    }

    public Kesoid getKesoid() {
        for (Weikoid0 weik = next; ; weik = weik.next) {
            if (weik instanceof Kesoid) return (Kesoid) weik;
        }
    }

    public boolean obsazujeOblast() {
        if (getKesoid().getStatus() == EKesStatus.ARCHIVED) {
            return false;
        }
        EKesWptType type = getType();
        return type == EKesWptType.FINAL_LOCATION || type == EKesWptType.STAGES_OF_A_MULTICACHE
                || TRADITIONAL_CACHE.equals(sym);
    }

    public boolean nutnyKLusteni() {
        return isMainWpt()
                || getType() == EKesWptType.QUESTION_TO_ANSWER
                || getType() == EKesWptType.STAGES_OF_A_MULTICACHE;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Wpt [name=" + nazev + ", type=" + getType() + ", wgs=" + getWgs() + "] " + (getKesoid() == null ? "" : getKesoid().getIdentifier());
    }


    //  private Genotyp __;

    public Genotyp getGenotyp(Genom genom) {
        //  	if (__ != null) return __;
        //
        Genotyp g = genom.getGenotypVychozi();
        buildGenotyp(genom, g);
        getKesoid().doBuildGenotyp(genom, g);
        //    __ = g;
        return g;
    }


    private void buildGenotyp(Genom genom, Genotyp g) {
        GenotypBuilderWpt genotypBuilder = new GenotypBuilderWpt(genom, g);
        genotypBuilder.build(this);
    }


    public String getSym() {
        return sym;
    }

    public Sklivec getSklivec() {
        if (sklivecValidityCode != currentSklivecValidityCode) {
            sklivec = null;
        }
        return sklivec;
    }

    public void setSklivec(Sklivec sklivec) {
        this.sklivec = sklivec;
        sklivecValidityCode = currentSklivecValidityCode;
    }

    /**
     *
     */
    public void invalidate() {
        setSklivec(null);
    }

    public static void invalidateAllSklivec() {
        currentSklivecValidityCode++;
    }

    public String textToolTipu() {
        Wpt wpt = this;
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        // TODO Zpbrazení tooltipu nutno dořešit
        //    if (wpt.getType() != EKesWptType.CACHE && wpt.getType() != EKesWptType.FINAL_LOCATION) {
        //      sb.append("<i>" + wpt.getName() + ": " + wpt.getNazev() + "</i><br>");
        //    }
        //    sb.append("<b>");
        //    sb.append(wpt.getKesoid().getNazev());
        //    sb.append("</b>");
        //    sb.append("<small>");
        //    sb.append(" - ");
        //    sb.append(sym);
        //    sb.append("  (" + wpt.getKesoid().getIdentifier() + ")");
        //    sb.append("</small>");
        //    sb.append("<br>");

        getKesoid().prispejDoTooltipu(sb, wpt);

        //    sb.append("<br>");
        //    sb.append("<br>");
        //    sb.append("<b>");
        //    sb.append(wpt.getNazev());
        //    sb.append("</b>");
        //    sb.append("<small>");
        //    sb.append(" - ");
        //    sb.append(sym);
        //    sb.append("  (" + wpt.getName() + ")");
        //    sb.append("</small>");

        return sb.toString();
    }


    public boolean isMainWpt() {
        return getKesoid().getMainWpt() == this;
    }

    public void setSym(String sym) {
        String adjustedSym = null;
        adjustedSym = wptMapping.get(sym);
        if (adjustedSym == null) {
            adjustedSym = sym;
        }
        this.sym = adjustedSym.intern();
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the elevation
     */
    public int getElevation() {
        return elevation;
    }


    /**
     * @param aElevation the elevation to set
     */
    public void setElevation(int aElevation) {
        elevation = aElevation;
    }

    /**
     * @return the rucnePridany
     */
    public boolean isRucnePridany() {
        return rucnePridany;
    }


    /**
     * @param aRucnePridany the rucnePridany to set
     */
    public void setRucnePridany(boolean aRucnePridany) {
        rucnePridany = aRucnePridany;
    }

    /**
     * @param zorder the zorder to set
     */
    public void setZorder(EZOrder zorder) {
        this.zorder = zorder;
    }


    /**
     * @return the zorder
     */
    public EZOrder getZorder() {
        return zorder;
    }

    public static enum EZOrder {
        OTHER,
        KESWPT,
        FIRST,
        FINAL,
    }

}
