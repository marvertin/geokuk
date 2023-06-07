package cz.geokuk.plugins.mapy;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.kachle.data.ConfigurableMapUrlBuilder;
import cz.geokuk.plugins.mapy.kachle.data.KaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.Files.newInputStream;
import static java.util.stream.Collectors.toList;

public class MapyModel extends Model0 {
    private static final Logger LOG = LoggerFactory.getLogger(MapyModel.class);
    private static final String DEFAULT_MAP_DEFINITIONS_JSON = "defaultMapDefinitions.utf-8.json";
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
        String defaultPodkladId = getMapy().stream().filter(m -> !m.isIgnored()).findFirst().map(KaType::getId).orElse(null);
        String selectedPodkladId = Optional.ofNullable(getConfiguredPodklad()).orElse(defaultPodkladId);
        getMapy().stream().filter(m -> Objects.equals(selectedPodkladId, m.getId())).findFirst().ifPresent(
                this::setPodklad
        );
    }

    public String getConfiguredPodklad() {
        return currPrefe().node(FPref.NODE_KTERE_MAPY_node).get(FPref.VALUE_MAPOVE_PODKLADY_value, null);
    }

    private Reader _mapDefinitionsJson() throws IOException {
        File json = currPrefe().node(FPref.NODE_KTERE_MAPY_node).getFile(FPref.VALUE_MAPOVE_PODKLADY_DEFINITIONS_FILE_value, null);
        if (json == null) {
            LOG.info("Název souboru s definicemi není nastaven: [{}]{}", currPrefe().node(FPref.NODE_KTERE_MAPY_node), FPref.VALUE_MAPOVE_PODKLADY_DEFINITIONS_FILE_value);
            return _useDefaultMapDefinitions();
        }
        if (json.getName().toLowerCase().endsWith(".json")) {
            LOG.info("Název souboru s definicemi map musí mít příponu .json: {}", json);
            return _useDefaultMapDefinitions();
        }
        Charset charset = _extractCharsetFromFileName(json);
        if (!json.exists()) {
            LOG.info("Soubor s definicemi map neexistuje: {}", json);
            return _useDefaultMapDefinitions();
        }
        if (!json.canRead()) {
            LOG.info("Soubor s definicemi map nelze číst: {}", json);
            return _useDefaultMapDefinitions();
        }
        return new InputStreamReader(newInputStream(json.toPath()), charset);
    }

    private Reader _useDefaultMapDefinitions() {
        LOG.info("Bude použita výchozí definice map z interního zdroje \"" + DEFAULT_MAP_DEFINITIONS_JSON + "\".");
        return Optional.ofNullable(getClass().getResourceAsStream(DEFAULT_MAP_DEFINITIONS_JSON))
            .map(is -> new InputStreamReader(is, StandardCharsets.UTF_8))
            .orElseThrow(() -> new IllegalStateException("Výchozí definice map \"" + DEFAULT_MAP_DEFINITIONS_JSON + "\" nebyla nalezena."))
        ;
    }

    private Charset _extractCharsetFromFileName(File f) {
        Matcher matcher = Pattern.compile("^.\\.(-\\w+)\\.json").matcher(f.getName());
        if (!matcher.matches()) {
            LOG.info("Název souboru s definicemi map neobsahuje kódování, předpokládá se UTF-8: {}", f);
            return StandardCharsets.UTF_8;
        }
        String charsetName = matcher.group(1);
        try {
            return Charset.forName(charsetName);
        } catch (IllegalCharsetNameException e) {
            LOG.info("Kódování \"{}\" uvedené v názvu souboru s definicemi map není platné: {}", charsetName, f);
            return StandardCharsets.UTF_8;
        } catch (UnsupportedCharsetException e) {
            LOG.info("Kódování \"{}\" uvedené v názvu souboru s definicemi map není podporováno: {}", charsetName, f);
            return StandardCharsets.UTF_8;
        }
    }
    private Collection<KaType> prepareAvailableMapsCollection() {
        return _configurableMapSources().collect(toList());
    }

    private Stream<KaType> _configurableMapSources() {
        List<Map<String, Object>> definitions = loadMapDefinitions();
        return mapToKaType(definitions);
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