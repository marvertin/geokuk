/**
 *
 */
package cz.geokuk.core.render;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author veverka
 *
 */
public class KmzWriter {

	private final ZipOutputStream zos;
	private final Document xmldoc;
	private final Element eFolder;

	private int citacSouboru;

	/**
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 *
	 */
	public KmzWriter(File file, String name, String description) throws FileNotFoundException, ParserConfigurationException {
		zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation impl = builder.getDOMImplementation();
		xmldoc = impl.createDocument("http://www.opengis.net/kml/2.2", "kml", null);

		Element eRoot = xmldoc.getDocumentElement();
		eFolder = xmldoc.createElement("Folder");
		eRoot.appendChild(eFolder);
		Element eName = xmldoc.createElement("name");
		//eName.appendChild(xmldoc.createTextNode("Overlay for Garmin navigation"));
		eName.appendChild(xmldoc.createTextNode(name));
		if (description != null && !description.trim().isEmpty()) {
			eFolder.appendChild(xmldoc.createElement("description")).appendChild(xmldoc.createTextNode(description));
		}

		eFolder.appendChild(eName);
	}

	public void finish() throws ParserConfigurationException, TransformerException, IOException  {
		//appendGroudOverlay(xmldoc, eFolder);
		zos.putNextEntry(new ZipEntry("doc.kml"));

		DOMSource domSource = new DOMSource(xmldoc);
		StreamResult streamResult = new StreamResult(zos);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer = tf.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
		//serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
		serializer.setOutputProperty(OutputKeys.INDENT,"yes");
		serializer.transform(domSource, streamResult);
		zos.close();
	}



	public void appendDlazdici(BufferedImage image, KmzParams p) throws IOException {
		String soubor = "files/dlazdice" + ++citacSouboru + "." + p.imageType;
		zos.putNextEntry(new ZipEntry(soubor));
		ImageIO.write(image, p.imageType.getType(), zos);

		Element eGroundOverlay = xmldoc.createElement("GroundOverlay");
		eFolder.appendChild(eGroundOverlay);
		eGroundOverlay.appendChild(xmldoc.createElement("name")).appendChild(xmldoc.createTextNode("["+(p.xDlazdice+1)+ "," + (p.yDlazdice+1)+"]"));
		//eGroundOverlay.appendChild(xmldoc.createElement("description")).appendChild(xmldoc.createTextNode("popisisko sko"));
		eGroundOverlay.appendChild(xmldoc.createElement("drawOrder")).appendChild(xmldoc.createTextNode(p.drawOrder + ""));

		Node eIcon = eGroundOverlay.appendChild(xmldoc.createElement("Icon"));
		eIcon.appendChild(xmldoc.createElement("href")).appendChild(xmldoc.createTextNode(soubor));
		eIcon.appendChild(xmldoc.createElement("viewBoundScale")).appendChild(xmldoc.createTextNode("0.75"));

		Node eLatLonBox = eGroundOverlay.appendChild(xmldoc.createElement("LatLonBox"));
		eLatLonBox.appendChild(xmldoc.createElement("north")).appendChild(xmldoc.createTextNode(p.sever + ""));
		eLatLonBox.appendChild(xmldoc.createElement("south")).appendChild(xmldoc.createTextNode(p.jih + ""));
		eLatLonBox.appendChild(xmldoc.createElement("west")).appendChild(xmldoc.createTextNode(p.zapad + ""));
		eLatLonBox.appendChild(xmldoc.createElement("east")).appendChild(xmldoc.createTextNode(p.vychod + ""));
		if (p.rotation != 0.0) {
			eLatLonBox.appendChild(xmldoc.createElement("rotation")).appendChild(xmldoc.createTextNode(p.rotation + ""));
		}
	}


}


