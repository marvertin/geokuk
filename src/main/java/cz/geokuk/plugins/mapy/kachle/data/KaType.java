package cz.geokuk.plugins.mapy.kachle.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

public interface KaType {
    /** Minimální měřítko, ke kterému jsou podklady. Věřme, že to bude dnes většinou 0. (U starých seznamových map to bylo 3 nebo 4). */
    int getMinMoumer();

    /** Maximální měřítko, pro který jsou kachle. */
    int getMaxMoumer();
    /** Už přesně nevím [veverka], nutno analyzovat, nastavujem to stejnějako maxMoumer. */
    int getMaxAutoMoumer();
    /** Id mapy, jak se zapisuje do konfigurace. */
    String getId();
    /** Název mapy, tak se objeví v menu. */
    String getNazev();
    /** Bližší popis mapy, objeví se jako tooltip v menu. */
    String getPopis();
    /** Hot-key, která mapu vyvolá. */
    int getKlavesa();
    /** Písmeno z nazev, které lze použít pro výběr při rozbaleném menu. */
    KeyStroke getKeyStroke();
    /** Implementace třídy, která sestaví URL pro zobrazení mapy. */
    KachleUrlBuilder getUrlBuilder();
    default int fitMoumer(int moumer)  {
		if (moumer < getMinMoumer()) {
			moumer = getMinMoumer();
		}
		if (moumer > getMaxMoumer()) {
			moumer = getMaxMoumer();
		}
		return moumer;
	}

    static KaType simpleKaType(
            @Nonnull String id,
            @Nonnull String nazev,
            int minMoumer,
            int maxMoumer,
            int klavesa,
            @Nullable KeyStroke keyStroke,
            @Nonnull KachleUrlBuilder urlBuilder
    ) {
        return simpleKaType(id, nazev, null, minMoumer, maxMoumer, klavesa, keyStroke, urlBuilder);
    }
    static KaType simpleKaType(
            @Nonnull String id,
            @Nonnull String nazev,
            @Nullable String popis,
            int minMoumer,
            int maxMoumer,
            int klavesa,
            @Nullable KeyStroke keyStroke,
            @Nonnull KachleUrlBuilder urlBuilder
    ) {
        return new KaType() {
            @Override public int getMinMoumer() {
                return minMoumer;
            }
            @Override public int getMaxMoumer() {
                return maxMoumer;
            }
            @Override public int getMaxAutoMoumer() {
                return maxMoumer;
            }
            @Override public String getId() {
                return id;
            }
            @Override public String getNazev() {
                return nazev;
            }
            @Override public String getPopis() {
                return popis == null ? nazev : popis;
            }
            @Override public int getKlavesa() {
                return klavesa;
            }
            @Override public KeyStroke getKeyStroke() {
                return keyStroke;
            }
            @Override public KachleUrlBuilder getUrlBuilder() {
                return urlBuilder;
            }

            @Override public String toString() {
                return _toString();
            }
        };
    };

    default String _toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + getId() +
                ", nazev=" + getNazev() +
                ", popis=" + getPopis() +
                ", minMoumer=" + getMinMoumer() +
                ", maxMoumer=" + getMaxMoumer() +
                ", maxAutoMoumer=" + getMaxAutoMoumer() +
                ", klavesa=" + getKlavesa() +
                ", keyStroke=" + getKeyStroke() +
                ", urlBuilder=" + getUrlBuilder() +
            '}';
    }
}
