package cz.geokuk.plugins.kesoid.mapicon;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import cz.geokuk.framework.Atom;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.KeyNode;
import cz.geokuk.util.file.LamUrl;
import cz.geokuk.util.file.MultiFolder;

/**
 *
 */

/**
 * @author veverka
 *
 */
public class IkonNacitacLoader {

	private static final String IKONA_SADY = "iconofsada.";

	private File imageMyDir;

	private File image3rdPartyDir;

	private MultiFolder lastScan;

	private static final String MAPUZEL = "map";


	public IkonBag nacti(Future<?> future, boolean aPrenacti, ASada jmenoSady) throws IOException {
		if (aPrenacti) lastScan = null;
		// Nejdříve skenovat, čímž také zjistíme, zda došlo ke změně
		MultiFolder mf = new MultiFolder();
		mf.addResourceTree("img");
		if (imageMyDir != null) mf.addFolderTree(imageMyDir);
		if (image3rdPartyDir != null) mf.addFolderTree(image3rdPartyDir);

		if (mf.equals(lastScan)) return null; // nedošlo ke změně
		lastScan = mf;

		IkonBag ikonBag = new IkonBag();
		KeyNode<String, LamUrl> mapNode = mf.getNode(MAPUZEL);

		ikonBag.setJmenaSad(mapNode.getKeys());
		ikonBag.setJmenaAIkonySad(nactiIkonySad(mapNode));


		IkonNacitacSada ikonNacitacSada = new IkonNacitacSada(ikonBag.getGenom());
		String key = MAPUZEL + "/" + jmenoSady;
		KeyNode<String, LamUrl> node = mf.getNode(key);
		Sada sada = ikonNacitacSada.loadSada(node);
		ikonBag.setSada(sada);
		return ikonBag;
	}


	public Map<ASada, Icon> nactiIkonySad(KeyNode<String, LamUrl> mapNode) {
		Map<ASada, Icon> jmenaAIkonySad = new TreeMap<>();
		List<KeyNode<String,LamUrl>> subNodes = mapNode.getSubNodes();
		for (KeyNode<String,LamUrl> nodeSada : subNodes) {
			Icon icon = null;
			for (String klic : nodeSada.getKeys()) {
				if (klic.startsWith(IKONA_SADY)) {
					KeyNode<String, LamUrl> node = nodeSada.getItems().get(klic);
					try {
						icon = new ImageIcon(ImageIO.read(node.getData().url));
					} catch (IOException e) {
						FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Problem s nacitanim ikony sady: " + node.getData().url);
					}
				}
			}
			jmenaAIkonySad.put(Atom.valueOf(ASada.class, nodeSada.getData().name), icon);
		}
		return jmenaAIkonySad;

	}


	public void setImage3rdPartyDir(File image3rdPartyDir) {
		this.image3rdPartyDir = image3rdPartyDir;
	}


	public void setImageMyDir(File imageMyDir) {
		this.imageMyDir = imageMyDir;
	}
}
