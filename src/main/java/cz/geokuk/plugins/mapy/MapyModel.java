package cz.geokuk.plugins.mapy;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.kachle.data.ConfigurableMapUrlBuilder;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.data.KaType;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class MapyModel extends Model0 {

    private Collection<KaType> mapy;
    private KaType podklad;

    public Collection<KaType> getMapy() {
        return mapy;
    }
    public void setMapy(Collection<KaType> mapy) {
        this.mapy = mapy;
    }

    public KaType getKaSet() {
        return podklad;
    }
    public KaType getPodklad() {
        return podklad;
    }
    public void setPodklad(final KaType podklad) {
        if (podklad == this.podklad) {
            return;
        }
        this.podklad = podklad;
        currPrefe().node(FPref.NODE_KTERE_MAPY_node).put(FPref.VALUE_MAPOVE_PODKLADY_value, podklad.getId());
        fajruj();
    }

    public void init() {
        _init();
    }

    @Override
    protected void initAndFire() {
        _init();
        // Nemělo by se tu volat fajruj()?
    }

    private void fajruj() {
        if (podklad != null) {
            fire(new ZmenaMapNastalaEvent(getKaSet()));
        }
    }

    private void _init() {
        setMapy(prepareAvailableMapsCollection());
        String podkladId = currPrefe().node(FPref.NODE_KTERE_MAPY_node).get(FPref.VALUE_MAPOVE_PODKLADY_value, EKaType.TURIST_M.name());
        getMapy().stream().filter(m -> Objects.equals(podkladId, m.getId())).findFirst().ifPresent(
                this::setPodklad
        );
    }

    private Collection<KaType> prepareAvailableMapsCollection() {
        return Stream.concat(
                _builtinMapSources(),
                _configurableMapSources()
        ).collect(toList());
    }

    private Stream<KaType> _builtinMapSources() {
        return Stream.of(EKaType.values());
    }
    private Stream<KaType> _configurableMapSources() {
        return Stream.of(
                KaType.simpleKaType("osm", 			"OSM",                  0, 18, 0, null, new ConfigurableMapUrlBuilder("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", "abc")),
                KaType.simpleKaType("ocmde", 		"OSM German Style",     0, 18, 0, null, new ConfigurableMapUrlBuilder("https://{s}.tile.openstreetmap.de/tiles/osmde/{z}/{x}/{y}.png")),
                KaType.simpleKaType("ocm", 			"OpenCycleMap",         0, 18, 0, null, new ConfigurableMapUrlBuilder("https://tile.thunderforest.com/cycle/{z}/{x}/{y}.png")),
                KaType.simpleKaType("googlemaps", 	"Google Maps",          0, 22, 0, null, new ConfigurableMapUrlBuilder("https://mt.google.com/vt?&x={x}&y={y}&z={z}", "1234", 256)),
                KaType.simpleKaType("googlemapssat", "Google Satellite",    0, 22, 0, null, new ConfigurableMapUrlBuilder("https://mt.google.com/vt?lyrs=s&x={x}&y={y}&z={z}", "1234", 256)),
                KaType.simpleKaType("googlehybr", 	"Google Maps Hybrid",   0, 20, 0, null, new ConfigurableMapUrlBuilder("https://mt0.google.com/vt/lyrs=s,m@110&hl=en&x={x}&y={y}&z={z}", 256)),
                KaType.simpleKaType("bingmap", 		"Bing Maps", 			1, 20, 0, null, new ConfigurableMapUrlBuilder("https://ecn.t{s}.tiles.virtualearth.net/tiles/r{q}?g=864&mkt=en-gb&lbl=l1&stl=h&shading=hill&n=z", "0123")),
                KaType.simpleKaType("esriimagy", 	"Esri WorldImagery", 	0, 18, 0, null, new ConfigurableMapUrlBuilder("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}")),
                KaType.simpleKaType("esriworld", 	"Esri WorldStreetMap", 	0, 18, 0, null, new ConfigurableMapUrlBuilder("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}")),
                KaType.simpleKaType("esritopo",     "Esri WorldTopoMap", 	0, 18, 0, null, new ConfigurableMapUrlBuilder("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}")),
                KaType.simpleKaType("binglondon",   "London Street Maps",  14, 17, 0, null, new ConfigurableMapUrlBuilder("https://ecn.t{s}.tiles.virtualearth.net/tiles/r{q}?g=864&productSet=mmCB", "0123")),
                KaType.simpleKaType("nazev",        "MTBmap.cz", 			1, 18, 0, null, new ConfigurableMapUrlBuilder("https://tile.mtbmap.cz/mtbmap_tiles/{z}/{x}/{y}.png")),
                KaType.simpleKaType("cuzktop",      "ČUZK - Topografická", 10, 19, 0, null, new ConfigurableMapUrlBuilder("https://ags.cuzk.cz/arcgis/services/zmwm/MapServer/WMSServer")),
                KaType.simpleKaType("cuzksat",      "ČUZK - Satelitní",    10, 20, 0, null, new ConfigurableMapUrlBuilder("https://ags.cuzk.cz/arcgis/services/ortofoto_wm/MapServer/WMSServer")),
                KaType.simpleKaType("cuzkater",     "ČUZK - Jen terén",    10, 20, 0, null, new ConfigurableMapUrlBuilder("https://ags.cuzk.cz/arcgis2/services/dmr5g/ImageServer/WMSServer\",\"crs\":\"EPSG:4326\",\"layers\":\"dmr5g:GrayscaleHillshade")),
                KaType.simpleKaType("cuzklidar",    "ČUZK - LIDAR",        10, 20, 0, null, new ConfigurableMapUrlBuilder("https://ags.cuzk.cz/arcgis2/services/dmr5g/ImageServer/WMSServer")),
                KaType.simpleKaType("googlelabels", "Google labels",        0, 22, 0, null, new ConfigurableMapUrlBuilder("https://mt1.google.com/vt/lyrs=h@218000000&hl=en&src=app&x={x}&y={y}&z={z}&s="))
        );
    }
}
