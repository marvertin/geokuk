package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.mapy.kachle.data.KaType;

import java.util.Collection;

public class ConfigurableMapSourcesChangedEvent extends Event0<KesoidModel> {

    private final Collection<KaType> iConfigurableMapSources;

    public ConfigurableMapSourcesChangedEvent(final Collection<KaType> aConfigurableMapSources) {
        super();
        iConfigurableMapSources = aConfigurableMapSources;
    }

    public Collection<KaType> getConfigurableMapSources() {
        return iConfigurableMapSources;
    }

}
