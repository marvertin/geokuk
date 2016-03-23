/**
 *
 */
package cz.geokuk.plugins.geocoding;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.Hledac0;
import cz.geokuk.core.hledani.HledaciPodminka0;

/**
 * @author veverka
 *
 */
public class Hledac extends Hledac0<Nalezenec> {

	private XPathFactory factory = XPathFactory.newInstance();

	public Hledac() {
	}

	@Override
	public List<Nalezenec> hledej(HledaciPodminka0 aPodm) {
		HledaciPodminka podm = (HledaciPodminka) aPodm;
		// starý způsob:
		// http://maps.google.com/maps/geo?q=vranovice&output=xml&sensor=false&key=geokuk&gl=CZ
		URL url = podm.computeUrl();

		XPath xpath = factory.newXPath();

		try (InputStream stm = url.openStream()){
			InputSource inputXml = new InputSource(stm);
			NodeList adressList = (NodeList) xpath.evaluate(
					"GeocodeResponse/result", inputXml,
					XPathConstants.NODESET);
			List<Nalezenec> list = new ArrayList<>();

			for (int i = 0; i < adressList.getLength(); i++) {
				Node item = adressList.item(i);
				//System.out.println("xpath    " + item.getTextContent());

				String formatovanaAdresa = xpath.evaluate("formatted_address", item);
				//System.out.println("xpath    " + formatovanaAdresa);
				Nalezenec nalezenec = new Nalezenec();
				nalezenec.adresa = formatovanaAdresa;
				nalezenec.wgs = new Wgs(Double.parseDouble(xpath.evaluate("geometry/location/lat", item)),
						Double.parseDouble(xpath.evaluate("geometry/location/lng", item)));
				nalezenec.locationType = xpath.evaluate("geometry/location_type", item);
				list.add(nalezenec);
			}
			return list;
		} catch (XPathExpressionException | IOException e) {
			throw new RuntimeException(e);
		}
	}


}

// <GeocodeResponse>
// <status>OK</status>
// <result>
// <type>neighborhood</type>
// <type>political</type>
// <formatted_address>Třebčín, 783 42 Lutín, Česká republika</formatted_address>
// <address_component>
// <long_name>Třebčín</long_name>
// <short_name>Třebčín</short_name>
// <type>neighborhood</type>
// <type>political</type>
// </address_component>
// <address_component>
// <long_name>Lutín</long_name>
// <short_name>Lutín</short_name>
// <type>locality</type>
// <type>political</type>
// </address_component>
// <address_component>
// <long_name>Olomouc</long_name>
// <short_name>Olomouc</short_name>
// <type>administrative_area_level_2</type>
// <type>political</type>
// </address_component>
// <address_component>
// <long_name>Olomoucký kraj</long_name>
// <short_name>Olomoucký kraj</short_name>
// <type>administrative_area_level_1</type>
// <type>political</type>
// </address_component>
// <address_component>
// <long_name>Česká republika</long_name>
// <short_name>CZ</short_name>
// <type>country</type>
// <type>political</type>
// </address_component>
// <address_component>
// <long_name>783 42</long_name>
// <short_name>783 42</short_name>
// <type>postal_code</type>
// </address_component>
// <geometry>
// <location>
// <lat>49.5486497</lat>
// <lng>17.1150528</lng>
// </location>
// <location_type>APPROXIMATE</location_type>
// <viewport>
// <southwest>
// <lat>49.5414098</lat>
// <lng>17.0990454</lng>
// </southwest>
// <northeast>
// <lat>49.5558885</lat>
// <lng>17.1310602</lng>
// </northeast>
// </viewport>
// </geometry>
// </result>
// </GeocodeResponse>