/**
 * 
 */
package cz.geokuk.plugins.geocoding;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.Hledac0;
import cz.geokuk.core.hledani.HledaciPodminka0;



/**
 * @author veverka
 *
 */
public class Hledac extends Hledac0<Nalezenec> {

  private final DocumentBuilderFactory builderFactory;

  public Hledac() {
    builderFactory = DocumentBuilderFactory.newInstance();
  }

  @Override
  public List<Nalezenec> hledej(HledaciPodminka0 aPodm) {
    HledaciPodminka podm = (HledaciPodminka) aPodm;
    //http://maps.google.com/maps/geo?q=vranovice&output=xml&sensor=false&key=geokuk&gl=CZ
    URL url = podm.computeUrl();
    try {
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      InputStream stm = url.openStream();
      Document document;
      try {
        document = builder.parse(stm);
      } finally {
        stm.close();
      }
      System.out.println("Vysledek: "+ document.toString());
      document.getDocumentElement().normalize();
      System.out.println(document.getDocumentElement().getNodeName());
      Element rootElement = document.getDocumentElement();
      List<Nalezenec> list = new ArrayList<Nalezenec>();
      NodeList adressList = rootElement.getElementsByTagName("address");
      NodeList coordinatesList = rootElement.getElementsByTagName("coordinates");
      NodeList adressDetailsList = rootElement.getElementsByTagName("AddressDetails");
      //      public String administrativeArea; // Jihomoravský Kraj, vYSOČINA
      //      public String subAdministrativeAreaName; // Brno, Jihlava, Maršov; Veverská Bítýška
      //      public String locality; // Černá pole; Henčov; -; -; Pazderna
      //      public String thoroughfare; // Lidická 1880/50 ; 4079; 163

      NodeList administrativeAreaList = rootElement.getElementsByTagName("AdministrativeAreaName");
      NodeList subAdministrativeAreaList = rootElement.getElementsByTagName("SubAdministrativeAreaName");
      NodeList localityList = rootElement.getElementsByTagName("LocalityName");
      NodeList thoroughfareList = rootElement.getElementsByTagName("ThoroughfareName");

      for (int i=0; i < adressList.getLength(); i++) {
        Node item = adressList.item(i);
        System.out.println("    " + item.getTextContent());
        Nalezenec nalezenec = new Nalezenec();
        nalezenec.adresa = item.getTextContent();
        if (administrativeAreaList != null && i < administrativeAreaList.getLength()) {
          nalezenec.administrativeArea = administrativeAreaList.item(i).getTextContent();
        }
        if (subAdministrativeAreaList != null && i < subAdministrativeAreaList.getLength()) {
          nalezenec.subAdministrativeArea = subAdministrativeAreaList.item(i).getTextContent();
        }
        if (localityList != null && i < localityList.getLength()) {
          nalezenec.locality = localityList.item(i).getTextContent();
        }
        if (thoroughfareList != null && i < thoroughfareList.getLength()) {
          nalezenec.thoroughfare = thoroughfareList.item(i).getTextContent();
        }
        nalezenec.wgs = parseWgs(coordinatesList.item(i).getTextContent());
        nalezenec.accurancy = adressDetailsList.item(i).getAttributes().getNamedItem("Accuracy").getTextContent();
        list.add(nalezenec);
      }
      return list;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }

  }


  private Wgs parseWgs(String coords) {
    String[] ss = coords.split(",");
    Wgs result = new Wgs(Double.parseDouble(ss[1]), Double.parseDouble(ss[0]));
    return result;
  }



}
