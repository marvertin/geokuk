/**
 *
 */
package cz.geokuk.core.render;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

/**
 * @author Martin Veverka
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
	public KmzWriter(final File file, final String name, final String description) throws FileNotFoundException, ParserConfigurationException {
		zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final DOMImplementation impl = builder.getDOMImplementation();
		xmldoc = impl.createDocument("http://www.opengis.net/kml/2.2", "kml", null);

		final Element eRoot = xmldoc.getDocumentElement();
		eFolder = xmldoc.createElement("Folder");
		eRoot.appendChild(eFolder);
		final Element eName = xmldoc.createElement("name");
		// eName.appendChild(xmldoc.createTextNode("Overlay for Garmin navigation"));
		eName.appendChild(xmldoc.createTextNode(name));
		if (description != null && !description.trim().isEmpty()) {
			eFolder.appendChild(xmldoc.createElement("description")).appendChild(xmldoc.createTextNode(description));
		}

		eFolder.appendChild(eName);
	}

	public void appendDlazdici(final BufferedImage image, final KmzParams p) throws IOException {
		final String soubor = "files/dlazdice" + ++citacSouboru + "." + p.imageType;
		zos.putNextEntry(new ZipEntry(soubor));
		ImageIO.write(image, p.imageType.getType(), zos);

		final Element eGroundOverlay = xmldoc.createElement("GroundOverlay");
		eFolder.appendChild(eGroundOverlay);
		eGroundOverlay.appendChild(xmldoc.createElement("name")).appendChild(xmldoc.createTextNode("[" + (p.xDlazdice + 1) + "," + (p.yDlazdice + 1) + "]"));
		// eGroundOverlay.appendChild(xmldoc.createElement("description")).appendChild(xmldoc.createTextNode("popisisko sko"));
		eGroundOverlay.appendChild(xmldoc.createElement("drawOrder")).appendChild(xmldoc.createTextNode(p.drawOrder + ""));

		final Node eIcon = eGroundOverlay.appendChild(xmldoc.createElement("Icon"));
		eIcon.appendChild(xmldoc.createElement("href")).appendChild(xmldoc.createTextNode(soubor));
		eIcon.appendChild(xmldoc.createElement("viewBoundScale")).appendChild(xmldoc.createTextNode("0.75"));

		final Node eLatLonBox = eGroundOverlay.appendChild(xmldoc.createElement("LatLonBox"));
		eLatLonBox.appendChild(xmldoc.createElement("north")).appendChild(xmldoc.createTextNode(p.sever + ""));
		eLatLonBox.appendChild(xmldoc.createElement("south")).appendChild(xmldoc.createTextNode(p.jih + ""));
		eLatLonBox.appendChild(xmldoc.createElement("west")).appendChild(xmldoc.createTextNode(p.zapad + ""));
		eLatLonBox.appendChild(xmldoc.createElement("east")).appendChild(xmldoc.createTextNode(p.vychod + ""));
		if (p.rotation != 0.0) {
			eLatLonBox.appendChild(xmldoc.createElement("rotation")).appendChild(xmldoc.createTextNode(p.rotation + ""));
		}
	}

	public void finish() throws ParserConfigurationException, TransformerException, IOException {
		// appendGroudOverlay(xmldoc, eFolder);
		zos.putNextEntry(new ZipEntry("doc.kml"));

		final DOMSource domSource = new DOMSource(xmldoc);
		final StreamResult streamResult = new StreamResult(zos);
		final TransformerFactory tf = TransformerFactory.newInstance();
		final Transformer serializer = tf.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		// serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.transform(domSource, streamResult);
		zos.close();
	}

}
