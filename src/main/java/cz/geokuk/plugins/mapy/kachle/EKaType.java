package cz.geokuk.plugins.mapy.kachle;


import java.util.EnumSet;

import cz.geokuk.plugins.mapy.EMapDekorace;
import cz.geokuk.plugins.mapy.EMapPodklad;


public enum EKaType {
    BASE_N(true, 4, 16, 16, "base-n"),
    TURIST(true, 4, 16, 16, "turist"),
    TURIST_WINTER(true, 4, 16, 16, "turist_winter"),
    OPHOTO(true, 4, 18, 16, "ophoto"),
    ARMY2(true, 5, 13, 13, "army2"),
    OPHOTO0203(true, 4, 16, 16, "ophoto0203"),
    ZEMEPIS(true, 4, 16, 16, "zemepis"),
    _BEZ_PODKLADU(true, 4, 18, 18, null),

    RELIEF(false),
    TCYKLO(false),
    TTUR(false),
    HYBRID(false),;

    private final boolean podklad;
    private final int minMoumer;
    private final int maxMoumer;
    private final int maxAutoMoumer;
    private final String url_type;

    public boolean isPodklad() {
        return podklad;
    }

    private EKaType(boolean podklad) {
        this.podklad = podklad;
        minMoumer = 3;
        maxMoumer = 18;
        maxAutoMoumer = 17;
        url_type = name().toLowerCase();
    }

    private EKaType(boolean podklad, int minMoumer, int maxMoumer, int maxAutoMoumer, String url_type) {
        this.podklad = podklad;
        this.minMoumer = minMoumer;
        this.maxMoumer = maxMoumer;
        this.maxAutoMoumer = maxAutoMoumer;
        this.url_type = url_type;
    }

    public void addToUrl(StringBuilder sb) {
        if (this == _BEZ_PODKLADU) {
            throw new RuntimeException("není možné downloadovat žádný podklad");
        }
        sb.append(url_type);
    }

    public int getMinMoumer() {
        return minMoumer;
    }

    public int getMaxMoumer() {
        return maxMoumer;
    }

    public int fitMoumer(int moumer) {
        if (moumer < minMoumer) moumer = minMoumer;
        if (moumer > maxMoumer) moumer = maxMoumer;
        return moumer;
    }

    public static EnumSet<EKaType> compute(EMapPodklad p, EnumSet<EMapDekorace> d) {
        if (p == null) return null;
        EnumSet<EKaType> set = EnumSet.noneOf(EKaType.class);
        switch (p) {
            case BASE_N:
                set.add(EKaType.BASE_N);
                break;
            case TURIST:
                set.add(EKaType.TURIST);
                break;
            case TURIST_WINTER:
                set.add(EKaType.TURIST_WINTER);
                break;
            case OPHOTO:
                set.add(EKaType.OPHOTO);
                break;
            case ARMY2:
                set.add(EKaType.ARMY2);
                break;
            case OPHOTO0203:
                set.add(EKaType.OPHOTO0203);
                break;
            case ZEMEPIS:
                set.add(EKaType.ZEMEPIS);
                break;
            case ZADNE:
                set.add(EKaType._BEZ_PODKLADU);
                break;
            // ostatni jsou bez podkladu
        }
        for (EMapDekorace dek : d) {
            switch (dek) {
                case TTUR:
                    set.add(EKaType.TTUR);
                    break;
                case HYBRID:
                    if (p.isJeMoznyHybrid()) set.add(EKaType.HYBRID);
                    break;
                case TCYKLO:
                    set.add(EKaType.TCYKLO);
                    break;
                case RELIEF:
                    set.add(EKaType.RELIEF);
                    break;
            }
        }
        return set;
    }

    public int getMaxAutoMoumer() {
        return maxAutoMoumer;
    }
}
