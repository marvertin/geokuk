package cz.geokuk.plugins.kesoid.importek;


import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import cz.geokuk.core.coordinates.Wgs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */

/**
 * @author veverka
 */
public class NacitacGpx extends Nacitac0 {

    private static final Logger log =
            LogManager.getLogger(NacitacGpx.class.getSimpleName());

    private static final String PREFIX_USERDEFINOANYCH_GENU = "geokuk_";
    private static String TOPOGRAFIC_NAMESPACE_1_0 = "http://www.topografix.com/GPX/1/0";
    private static String TOPOGRAFIC_NAMESPACE_1_1 = "http://www.topografix.com/GPX/1/1";
    private static String GROUNSPEAK_NAMESPACE_1_0 = "http://www.groundspeak.com/cache/1/0";
    private static String GROUNSPEAK_NAMESPACE_1_0_1 = "http://www.groundspeak.com/cache/1/0/1";
    private static String GPXG_NAMESPACE = "http://geoget.ararat.cz/GpxExtensions/v2";

    private QName WPT;

    private QName TYPE;
    private QName TIME;
    private QName NAME;
    private QName DESC;
    private QName CMT;
    private QName URLNAME;
    private QName SYM;
    private QName ELE;

    private QName GS_CACHE;

    //  <groundspeak:name>Vyroba vapna</groundspeak:name>
    private QName GS_NAME;

    //  <groundspeak:placed_by>rodinka veverek</groundspeak:placed_by>
    private QName GS_PLACED_BY;


    // <groundspeak:owner id;
    private QName GS_OWNER;

    //  <groundspeak:type>Unknown Cache</groundspeak:type>
    private QName GS_TYPE;

    //  <groundspeak:container>Regular</groundspeak:container>
    private QName GS_CONTAINER;

    //  <groundspeak:difficulty>2.5</groundspeak:difficulty>
    private QName GS_DIFFICULTY;

    //  <groundspeak:terrain>4</groundspeak:terrain>
    private QName GS_TERREAIN;

    //  <groundspeak:country>Czech Republic</groundspeak:country>
    private QName GS_COUNTRY;

    //  <groundspeak:state>Jihomoravsky kraj</groundspeak:state>
    private QName GS_STATE;

    //  <groundspeak:encoded_hints>U balvanu. Hluboko zastrcit levou ruku a doprava.</groundspeak:encoded_hints>
    private QName GS_ENCODED_HINTS;

    //<groundspeak:short_description html;
    private QName GS_SHORT_DESCRIPTION;

    private static QName URL = new QName(TOPOGRAFIC_NAMESPACE_1_0, "url");
    private static QName LINK = new QName(TOPOGRAFIC_NAMESPACE_1_1, "link");
    private static QName LINK_TEXT = new QName(TOPOGRAFIC_NAMESPACE_1_1, "text");
    private static QName LINK_TYPE = new QName(TOPOGRAFIC_NAMESPACE_1_1, "type");


    //  <gpxg:GeogetExtension xmlns:gpxg="http://geoget.ararat.cz/GpxExtensions/v2">
    private static QName GPXG_GEOGET_EXTENSION = new QName(GPXG_NAMESPACE, "GeogetExtension");

    //  <gpxg:Found>2009-10-30T16:16:00.000</gpxg:Found>
    private static QName GPXG_FOUND = new QName(GPXG_NAMESPACE, "Found");

    //<gpxg:Tag Category="attribute"><![CDATA[quads-no]]></gpxg:Tag>
    private static QName GPXG_TAG = new QName(GPXG_NAMESPACE, "Tag");

    //    <gpxg:Tag Category="attribute"><![CDATA[available-yes]]></gpxg:Tag>
    //    <gpxg:Tag Category="attribute"><![CDATA[bicycles-yes]]></gpxg:Tag>
    //    <gpxg:Tag Category="attribute"><![CDATA[dogs-yes]]></gpxg:Tag>
    //    <gpxg:Tag Category="attribute"><![CDATA[jeeps-no]]></gpxg:Tag>
    //    <gpxg:Tag Category="attribute"><![CDATA[motorcycles-no]]></gpxg:Tag>
    //    <gpxg:Tag Category="attribute"><![CDATA[quads-no]]></gpxg:Tag>
    //    <gpxg:Tag Category="attribute"><![CDATA[ticks-yes]]></gpxg:Tag>
    //    <gpxg:Tag Category="CZ kraj"><![CDATA[Jihomoravsky]]></gpxg:Tag>
    //    <gpxg:Tag Category="CZ okres"><![CDATA[Brno-venkov]]></gpxg:Tag>
    //    <gpxg:Tag Category="Elevation"><![CDATA[467]]></gpxg:Tag>
    //    <gpxg:Tag Category="Hodnoceni"><![CDATA[50%]]></gpxg:Tag>
    //    <gpxg:Tag Category="Hodnoceni-Pocet"><![CDATA[2x]]></gpxg:Tag>
    //    <gpxg:Tag Category="import"><![CDATA[3747098.gpx]]></gpxg:Tag>
    //    <gpxg:Tag Category="PocketQuery"><![CDATA[rank 8: 21.10.2009 - ********]]></gpxg:Tag>
    //    <gpxg:Tag Category="Znamka"><![CDATA[75]]></gpxg:Tag>
    //  </gpxg:Tags>
    //</gpxg:GeogetExtension>

    //<gpxg:Flag>1</gpxg:Flag>
    private static QName GPXG_FLAG = new QName(GPXG_NAMESPACE, "Flag");

    // pro cesty
    private static QName TRK;
    private static QName TRKSEG;
    private static QName TRKPT;


    private void initNamesTopografic(String topograficNamespaceUri) {
        WPT = new QName(topograficNamespaceUri, "wpt");
        TYPE = new QName(topograficNamespaceUri, "type");
        TIME = new QName(topograficNamespaceUri, "time");
        NAME = new QName(topograficNamespaceUri, "name");
        DESC = new QName(topograficNamespaceUri, "desc");
        CMT = new QName(topograficNamespaceUri, "cmt");
        URL = new QName(topograficNamespaceUri, "url");
        URLNAME = new QName(topograficNamespaceUri, "urlname");
        SYM = new QName(topograficNamespaceUri, "sym");
        ELE = new QName(topograficNamespaceUri, "ele");

        TRK = new QName(topograficNamespaceUri, "trk");
        TRKSEG = new QName(topograficNamespaceUri, "trkseg");
        TRKPT = new QName(topograficNamespaceUri, "trkpt");
    }

    private void initNamesGroundspeak(String groundspeakNameSpaceUri) {
        GS_CACHE = new QName(groundspeakNameSpaceUri, "cache");

        //  <groundspeak:name>Vyroba vapna</groundspeak:name>
        GS_NAME = new QName(groundspeakNameSpaceUri, "name");

        //  <groundspeak:placed_by>rodinka veverek</groundspeak:placed_by>
        GS_PLACED_BY = new QName(groundspeakNameSpaceUri, "placed_by");


        // <groundspeak:owner id="1487776">rodinka veverek</groundspeak:owner>
        GS_OWNER = new QName(groundspeakNameSpaceUri, "owner");

        //  <groundspeak:type>Unknown Cache</groundspeak:type>
        GS_TYPE = new QName(groundspeakNameSpaceUri, "type");

        //  <groundspeak:container>Regular</groundspeak:container>
        GS_CONTAINER = new QName(groundspeakNameSpaceUri, "container");

        //  <groundspeak:difficulty>2.5</groundspeak:difficulty>
        GS_DIFFICULTY = new QName(groundspeakNameSpaceUri, "difficulty");

        //  <groundspeak:terrain>4</groundspeak:terrain>
        GS_TERREAIN = new QName(groundspeakNameSpaceUri, "terrain");

        //  <groundspeak:country>Czech Republic</groundspeak:country>
        GS_COUNTRY = new QName(groundspeakNameSpaceUri, "country");

        //  <groundspeak:state>Jihomoravsky kraj</groundspeak:state>
        GS_STATE = new QName(groundspeakNameSpaceUri, "state");

        //  <groundspeak:encoded_hints>U balvanu. Hluboko zastrcit levou ruku a doprava.</groundspeak:encoded_hints>
        GS_ENCODED_HINTS = new QName(groundspeakNameSpaceUri, "encoded_hints");

        //<groundspeak:short_description html="True"><![CDATA[http://dataz.cuzk.cz/gu/ztl_01/tl_0114/0114002a.gif]]></groundspeak:short_description>
        GS_SHORT_DESCRIPTION = new QName(groundspeakNameSpaceUri, "short_description");
    }

    /* (non-Javadoc)
     * @see Nacitac0#nactiKdyzUmis(java.io.InputStream, java.lang.String, java.util.Map)
     */
    @Override
    protected void nactiKdyzUmis(InputStream istm, String jmeno, IImportBuilder builder, Future<?> future) throws IOException {
        if (!jmeno.toLowerCase().trim().endsWith(".gpx")) return; // umíme jen GPX
        nacti(istm, builder, future);
    }

    public void nacti(InputStream istm, IImportBuilder builder, Future<?> future)
            throws FactoryConfigurationError, IOException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader;
        try {
            reader = inputFactory.createXMLStreamReader(istm);
            load(reader, builder, future);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    // priloudne do waypointu
    public void load(XMLStreamReader rdr, IImportBuilder builder, Future<?> future) throws XMLStreamException {
        // nejdrive naplnit tim co uz mame
        while (rdr.hasNext()) {
            if (future != null && future.isCancelled()) return;
            if (rdr.isStartElement()) {
                if (rdr.getName().getLocalPart().equals("gpx")) {
                    initNamesTopografic(rdr.getNamespaceURI());
                }
                //        System.out.println("/////////////////////" + rdr.getName() + " **** " + rdr.getNamespaceURI());
                //QName jmeno = rdr.getName();
                if (rdr.getName().equals(WPT)) {
                    GpxWpt wpt = new GpxWpt();
                    readWpt(rdr, wpt, WPT);
                    builder.addGpxWpt(wpt);
                    //System.out.println(wpt);
                    //System.out.println(kes);
                    //System.out.println(jmeno + " " + lat + " " + lon );
                }
                if (rdr.getName().equals(TRK)) {
                    builder.begTrack();
                    for (; !(rdr.isEndElement() && rdr.getName().equals(TRK)); rdr.next()) {
                        if (rdr.isStartElement() && rdr.getName().equals(NAME)) {
                            String nazev = rdr.getElementText();
                            builder.setTrackName(nazev);
                        }

                        if (rdr.isStartElement() && rdr.getName().equals(TRKSEG)) {
                            builder.begTrackSegment();
                            for (; !(rdr.isEndElement() && rdr.getName().equals(TRKSEG)); rdr.next()) {
                                if (rdr.isStartElement() && rdr.getName().equals(TRKPT)) {
                                    GpxWpt wpt = new GpxWpt();
                                    readWpt(rdr, wpt, TRKPT);
                                    builder.addTrackWpt(wpt);
                                    //System.out.println(wpt);
                                    //System.out.println(kes);
                                    //System.out.println(jmeno + " " + lat + " " + lon );
                                }
                            }
                            builder.endTrackSegment();
                        }
                    }
                    builder.endTrack();
                    //System.out.println(wpt);
                    //System.out.println(kes);
                    //System.out.println(jmeno + " " + lat + " " + lon );
                }
            }
            //System.out.println("velikost: " + mapa.size());

            rdr.next();
        }


    }

    private void readWpt(XMLStreamReader rdr, GpxWpt wpt, QName tag)
            throws XMLStreamException {
        String lat = rdr.getAttributeValue(null, "lat");
        String lon = rdr.getAttributeValue(null, "lon");
        if (lat == null || lon == null) {
            log.warn("The waypoint doesn't have latitude or longitude defined!");
        } else {
            try {
                Double dlat = Double.parseDouble(lat);
                Double dlon = Double.parseDouble(lon);
                wpt.wgs = new Wgs(dlat, dlon);
            } catch (NumberFormatException e) {
                log.error("Invalid latitude or longitude string! Lat : [" + lat + "], lon : [" + lon
                        + lon + "]", e);
            }
        }
        // pro celý wayipoint
        for (; !(rdr.isEndElement() && rdr.getName().equals(tag)); rdr.next()) {
            if (rdr.isStartElement()) {
                QName jmeno = rdr.getName();
                //System.out.println(" cyklim");
                if (jmeno.equals(TIME)) {
                    wpt.time = rdr.getElementText(); // nemůžeme to hend použít
                }
                if (jmeno.equals(ELE)) {
                    wpt.ele = Double.parseDouble(rdr.getElementText()); // nemůžeme to hend použít
                }
                if (jmeno.equals(NAME)) {
                    wpt.name = rdr.getElementText();
                }
                if (jmeno.equals(URL)) {
                    wpt.link.href = rdr.getElementText();
                }
                if (jmeno.equals(LINK)) {
                    wpt.link.href = rdr.getAttributeValue(null, "href");
                    for (; !(rdr.isEndElement() && rdr.getName().equals(LINK)); rdr.next()) {
                        if (!rdr.isStartElement()) {
                            continue;
                        }
                        QName jmeno3 = rdr.getName();
                        if (jmeno3.equals(LINK_TEXT)) {
                            wpt.link.text = rdr.getElementText();
                        }
                        if (jmeno.equals(LINK_TYPE)) {
                            wpt.link.type = rdr.getElementText();
                        }
                    }

                }
                if (jmeno.equals(URLNAME)) {
                    wpt.link.text = rdr.getElementText();
                }
                if (jmeno.equals(SYM)) {
                    wpt.sym = rdr.getElementText();
                }
                if (jmeno.equals(TYPE)) {
                    wpt.type = rdr.getElementText();
                }
                if (jmeno.equals(CMT)) {
                    wpt.cmt = rdr.getElementText();
                }
                if (jmeno.equals(DESC)) {
                    wpt.desc = rdr.getElementText();
                }
                if (rdr.getName().getNamespaceURI().equals(GROUNSPEAK_NAMESPACE_1_0)) {
                    initNamesGroundspeak(GROUNSPEAK_NAMESPACE_1_0);
                }
                if (rdr.getName().getNamespaceURI().equals(GROUNSPEAK_NAMESPACE_1_0_1)) {
                    initNamesGroundspeak(GROUNSPEAK_NAMESPACE_1_0_1);
                }
                if (jmeno.equals(GS_CACHE)) {
                    readGroudspeak(rdr, wpt);
                } // konec GROUND_SPEAK
                if (jmeno.equals(GPXG_GEOGET_EXTENSION)) {
                    readGeogetExtension(rdr, wpt);
                } // konec GPXG
            } // start element
        } // konec wpt cyklu
    }

    private void readGroudspeak(XMLStreamReader rdr, GpxWpt wpt)
            throws XMLStreamException {
        wpt.groundspeak = new Groundspeak();
        wpt.groundspeak.availaible = Boolean.valueOf(rdr.getAttributeValue(null, "available"));
        wpt.groundspeak.archived = Boolean.valueOf(rdr.getAttributeValue(null, "archived"));
        for (; !(rdr.isEndElement() && rdr.getName().equals(GS_CACHE)); rdr.next()) {
            if (!rdr.isStartElement()) {
                continue;
            }
            QName jmeno2 = rdr.getName();
            if (jmeno2.equals(GS_NAME)) {
                wpt.groundspeak.name = rdr.getElementText();
            }
            if (jmeno2.equals(GS_PLACED_BY)) {
                wpt.groundspeak.placedBy = rdr.getElementText();
            }
            if (jmeno2.equals(GS_OWNER)) {
                String ownerIdStr = rdr.getAttributeValue(null, "id");
                if (ownerIdStr != null && ownerIdStr.length() > 0) {
                    try {
                        wpt.groundspeak.ownerid = Integer.parseInt(ownerIdStr);
                    } catch (NumberFormatException e) {
                        System.err.println("Nenumerické číslo vlastníka: \"" + ownerIdStr + "\" " + wpt);
                        wpt.groundspeak.ownerid = -999;
                    }
                }
                wpt.groundspeak.owner = rdr.getElementText();
            }
            if (jmeno2.equals(GS_TYPE)) {
                wpt.groundspeak.type = rdr.getElementText();
            }
            if (jmeno2.equals(GS_CONTAINER)) {
                wpt.groundspeak.container = rdr.getElementText().intern();
            }
            if (jmeno2.equals(GS_DIFFICULTY)) {
                wpt.groundspeak.difficulty = rdr.getElementText().intern();
            }
            if (jmeno2.equals(GS_TERREAIN)) {
                wpt.groundspeak.terrain = rdr.getElementText().intern();
            }
            if (jmeno2.equals(GS_COUNTRY)) {
                wpt.groundspeak.country = rdr.getElementText().intern();
            }
            if (jmeno2.equals(GS_STATE)) {
                wpt.groundspeak.state = rdr.getElementText().intern();
            }
            if (jmeno2.equals(GS_SHORT_DESCRIPTION)) {
                String shortDescription = rdr.getElementText();
                //                    if (shortDescription.startsWith("http")
                //                    		|| (wpt.name.length() == 8 && wpt.name.startsWith("GC")) ) { // Tent test je výkonnostní optimalizace, protože víme, že krátké popisky potřebujeme jen pro České Geodetické Body a je zde URL
                wpt.groundspeak.shortDescription = shortDescription;
                //                    }
            }
            if (jmeno2.equals(GS_ENCODED_HINTS)) {
                wpt.groundspeak.encodedHints = rdr.getElementText();
                break; // už mě ten cyklus kolem groundspeak:cache nezajímá
            }
        }
    }

    private void readGeogetExtension(XMLStreamReader rdr, GpxWpt wpt)
            throws XMLStreamException {
        for (; !(rdr.isEndElement() && rdr.getName().equals(GPXG_GEOGET_EXTENSION)); rdr.next()) {
            if (!rdr.isStartElement()) {
                continue;
            }
            QName jmeno4 = rdr.getName();
            if (jmeno4.equals(GPXG_FOUND)) {
                wpt.gpxg.found = rdr.getElementText();
            }
            if (jmeno4.equals(GPXG_TAG)) {
                String category = rdr.getAttributeValue(null, "Category");
                String value = rdr.getElementText();
                if ("Hodnoceni".equals(category)) {
                    wpt.gpxg.hodnoceni = parseCislo(value);
                }
                if ("Hodnoceni-Pocet".equals(category)) {
                    wpt.gpxg.hodnoceniPocet = parseCislo(value);
                }
                if ("BestOf".equals(category)) {
                    wpt.gpxg.bestOf = parseCislo(value);
                }
                if ("favorites".equals(category)) {
                    wpt.gpxg.favorites = parseCislo(value);
                }
                if ("Znamka".equals(category)) {
                    wpt.gpxg.znamka = parseCislo(value);
                }
                if ("Elevation".equals(category)) {
                    wpt.gpxg.elevation = parseCislo(value);
                }
                if ("CZ kraj".equals(category)) {
                    wpt.gpxg.czkraj = value;
                }
                if ("CZ okres".equals(category)) {
                    wpt.gpxg.czokres = value;
                }
                if (category != null && category.startsWith(PREFIX_USERDEFINOANYCH_GENU)) {
                    wpt.gpxg.putUserTag(category.substring(PREFIX_USERDEFINOANYCH_GENU.length()), value);
                }
            }
            if (jmeno4.equals(GPXG_FLAG)) {
                wpt.gpxg.flag = parseCislo(rdr.getElementText());
            }
        }
    }


}
