package cz.geokuk.plugins.mapy;

import com.fasterxml.jackson.databind.ObjectMapper;
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
                KaType.simpleKaType("osm", 			"OSM",                  5, 18, 0, null, new ConfigurableMapUrlBuilder("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", "abc")),
                KaType.simpleKaType("ocmde", 		"OSM German Style",     5, 18, 0, null, new ConfigurableMapUrlBuilder("https://{s}.tile.openstreetmap.de/tiles/osmde/{z}/{x}/{y}.png")),
                KaType.simpleKaType("ocm", 			"OpenCycleMap",         5, 18, 0, null, new ConfigurableMapUrlBuilder("https://tile.thunderforest.com/cycle/{z}/{x}/{y}.png")),
                KaType.simpleKaType("googlemaps", 	"Google Maps",          5, 22, 0, null, new ConfigurableMapUrlBuilder("https://mt.google.com/vt?&x={x}&y={y}&z={z}", "1234", 256)),
                KaType.simpleKaType("googlemapssat", "Google Satellite",    5, 22, 0, null, new ConfigurableMapUrlBuilder("https://mt.google.com/vt?lyrs=s&x={x}&y={y}&z={z}", "1234", 256)),
                KaType.simpleKaType("googlehybr", 	"Google Maps Hybrid",   5, 20, 0, null, new ConfigurableMapUrlBuilder("https://mt0.google.com/vt/lyrs=s,m@110&hl=en&x={x}&y={y}&z={z}", 256)),
                KaType.simpleKaType("bingmap", 		"Bing Maps", 			1, 20, 0, null, new ConfigurableMapUrlBuilder("https://ecn.t{s}.tiles.virtualearth.net/tiles/r{q}?g=864&mkt=en-gb&lbl=l1&stl=h&shading=hill&n=z", "0123")),
                KaType.simpleKaType("esriimagy", 	"Esri WorldImagery", 	5, 18, 0, null, new ConfigurableMapUrlBuilder("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}")),
                KaType.simpleKaType("esriworld", 	"Esri WorldStreetMap", 	5, 18, 0, null, new ConfigurableMapUrlBuilder("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}")),
                KaType.simpleKaType("esritopo",     "Esri WorldTopoMap", 	5, 18, 0, null, new ConfigurableMapUrlBuilder("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}")),
                KaType.simpleKaType("binglondon",   "London Street Maps",  14, 17, 0, null, new ConfigurableMapUrlBuilder("https://ecn.t{s}.tiles.virtualearth.net/tiles/r{q}?g=864&productSet=mmCB", "0123")),
                KaType.simpleKaType("nazev",        "MTBmap.cz", 			1, 18, 0, null, new ConfigurableMapUrlBuilder("https://tile.mtbmap.cz/mtbmap_tiles/{z}/{x}/{y}.png")),
                KaType.simpleKaType("cuzktop",      "ČUZK - Topografická", 10, 19, 0, null, new ConfigurableMapUrlBuilder("https://ags.cuzk.cz/arcgis/services/zmwm/MapServer/WMSServer")),
                KaType.simpleKaType("cuzksat",      "ČUZK - Satelitní",    10, 20, 0, null, new ConfigurableMapUrlBuilder("https://ags.cuzk.cz/arcgis/services/ortofoto_wm/MapServer/WMSServer")),
                KaType.simpleKaType("cuzkater",     "ČUZK - Jen terén",    10, 20, 0, null, new ConfigurableMapUrlBuilder("https://ags.cuzk.cz/arcgis2/services/dmr5g/ImageServer/WMSServer\",\"crs\":\"EPSG:4326\",\"layers\":\"dmr5g:GrayscaleHillshade")),
                KaType.simpleKaType("cuzklidar",    "ČUZK - LIDAR",        10, 20, 0, null, new ConfigurableMapUrlBuilder("https://ags.cuzk.cz/arcgis2/services/dmr5g/ImageServer/WMSServer")),
                KaType.simpleKaType("googlelabels", "Google labels",        5, 22, 0, null, new ConfigurableMapUrlBuilder("https://mt1.google.com/vt/lyrs=h@218000000&hl=en&src=app&x={x}&y={y}&z={z}&s="))
        );
    }

    public List<Map<String, Object>> loadMapDefinitions() {
        try {
            return new ObjectMapper().readValue(_mapDefinitionsJson(), List.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<KaType> mapToKaType(List<Map<String, Object>> definitions) {
        return definitions.stream().map(this::mapToKaType);
    }

    private KaType mapToKaType(Map<String, Object> definition) {
        ConfiguredKaType result = new ConfiguredKaType();
        definition.forEach((key, value) -> {
            Class<?> expectedType = null;
            switch (key) {
                case "name":
                    if (value instanceof String) {
                        result.setName((String) value);
                    } else {
                        expectedType = String.class;
                    }
                    break;
                case "ignore":
                    if (value instanceof Boolean) {
                        result.setIgnore((Boolean) value);
                    } else {
                        expectedType = Boolean.class;
                    }
                    break;
                case "transparent":
                    if (value instanceof Boolean) {
                        result.setTransparent((Boolean) value);
                    } else {
                        expectedType = Boolean.class;
                    }
                    break;
                case "overlay":
                    if (value instanceof Boolean) {
                        result.setOverlay((Boolean) value);
                    } else {
                        expectedType = Boolean.class;
                    }
                    break;
                case "alt":
                    if (value instanceof String) {
                        result.setAlt((String) value);
                    } else {
                        expectedType = String.class;
                    }
                    break;
                case "tileUrl":
                    if (value instanceof String) {
                        result.setTileUrl((String) value);
                    } else {
                        expectedType = String.class;
                    }
                    break;
                case "subdomains":
                    if (value instanceof String) {
                        result.setSubdomains((String) value);
                    } else {
                        expectedType = String.class;
                    }
                    break;
                case "attribution":
                    if (value instanceof String) {
                        result.setAttribution((String) value);
                    } else {
                        expectedType = String.class;
                    }
                    break;
                case "minZoom":
                    if (value instanceof Number) {
                        result.setMinZoom(((Number) value).intValue());
                    } else {
                        expectedType = Integer.class;
                    }
                    break;
                case "maxZoom":
                    if (value instanceof Number) {
                        result.setMaxZoom(((Number) value).intValue());
                    } else {
                        expectedType = Integer.class;
                    }
                    break;
                case "tileSize":
                    if (value instanceof Number) {
                        result.setTileSize(((Number) value).intValue());
                    } else {
                        expectedType = Integer.class;
                    }
                    break;
                case "format":
                case "crs":
                case "layers":
                    if (!(value instanceof String)) {
                        expectedType = String.class;
                    }
                    // Používáno mapami od ČÚZK, zatím není podporováno.
                    result.setIgnore(true);
                    result.setUnsupported(true);
                    break;
                default:
                    LOG.warn("Neznámý atribut \"{}\" v definici mapy.", key);
                    break;
            }
            if (expectedType != null) {
                LOG.error("Atribut \"{}\" definice mapy má být typu {}, ale je {}. Hodnota \"{}\" bude ignorována."
                        , key, expectedType.getSimpleName(), value.getClass().getSimpleName(), value
                );
            }
        });
        return result;
    }
}

class ConfiguredKaType implements KaType {
    private static int DEFAULT_MIN_ZOOM = 5;
    private static int DEFAULT_MAX_ZOOM = 20;
    private static int DEFAULT_TILE_SIZE = 256;
    private static boolean DEFAULT_IS_IGNORED = false;
    private static boolean DEFAULT_IS_TRANSPARENT = false;
    private static boolean DEFAULT_IS_OVERLAY = false;
    private static boolean DEFAULT_IS_UNSUPPORTED = false;

    private String name;
    private String alt;
    private String description;
    private String attribution;
    private int minZoom = DEFAULT_MIN_ZOOM;
    private int maxZoom = DEFAULT_MAX_ZOOM;
    private int tileSize = DEFAULT_TILE_SIZE;
    private String tileUrl;
    private String subdomains;
    private int klavesa;
    private KeyStroke keyStroke;
    private boolean ignore = DEFAULT_IS_IGNORED;
    private boolean transparent = DEFAULT_IS_TRANSPARENT;
    private boolean overlay = DEFAULT_IS_OVERLAY;
    private boolean unsupported = DEFAULT_IS_UNSUPPORTED;

    void setName(String value) {
        name = value;
    }
    void setAlt(String value) {
        alt = value;
    }
    void setDescription(String value) {
        description = value;
    }
    void setAttribution(String value) {
        attribution = value;
    }
    void setMinZoom(int value) {
        minZoom = value;
    }
    void setMaxZoom(int value) {
        maxZoom = value;
    }
    void setTileSize(int value) {
        tileSize = value;
    }
    void setTileUrl(String value) {
        tileUrl = value;
    }
    void setSubdomains(String value) {
        subdomains = value;
    }
    void setKlavesa(int value) {
        klavesa = value;
    }
    void setKeyStroke(KeyStroke value) {
        keyStroke = value;
    }
    void setIgnore(boolean value) {
        ignore = value;
    }
    void setTransparent(boolean value) {
        transparent = value;
    }
    void setOverlay(boolean value) {
        overlay = value;
    }
    void setUnsupported(boolean value) {
        unsupported = value;
    }

    @Override public int getMinMoumer() {
        return minZoom;
    }
    @Override public int getMaxMoumer() {
        return maxZoom;
    }
    @Override public int getMaxAutoMoumer() {
        return maxZoom;
    }
    @Override public String getId() {
        return name;
    }
    @Override public String getNazev() {
        return alt;
    }
    @Override public String getPopis() {
        return description;
    }
    public String getAttribution() {
        return attribution;
    }
    @Override public int getKlavesa() {
        return klavesa;
    }
    @Override public KeyStroke getKeyStroke() {
        return keyStroke;
    }
    @Override public ConfigurableMapUrlBuilder getUrlBuilder() {
        return new ConfigurableMapUrlBuilder(tileUrl, subdomains, tileSize);
    }
    @Override public boolean isIgnored() {
        return ignore;
    }
    @Override public boolean isTransparent() {
        return transparent;
    }
    @Override public boolean isOverleay() {
        return overlay;
    }
    public boolean isUnsupported() {
        return unsupported;
    }
}