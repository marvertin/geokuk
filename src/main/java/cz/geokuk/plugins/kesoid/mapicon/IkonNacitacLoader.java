package cz.geokuk.plugins.kesoid.mapicon;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import cz.geokuk.framework.Atom;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.*;

/**
 *
 */

/**
 * @author veverka
 *
 */
public class IkonNacitacLoader {

	private static final String	IKONA_SADY	= "iconofsada.";

	private File				imageMyDir;

	private File				image3rdPartyDir;

	private MultiFolder			lastScan;

	private static final String	MAPUZEL		= "map";

	public IkonBag nacti(final Future<?> future, final boolean aPrenacti, final ASada jmenoSady) throws IOException {
		if (aPrenacti)
			lastScan = null;
		// Nejdříve skenovat, čímž také zjistíme, zda došlo ke změně
		final MultiFolder mf = new MultiFolder();
		mf.addResourceTree("img");
		if (imageMyDir != null)
			mf.addFolderTree(imageMyDir);
		if (image3rdPartyDir != null)
			mf.addFolderTree(image3rdPartyDir);

		if (mf.equals(lastScan))
			return null; // nedošlo ke změně
		lastScan = mf;

		final IkonBag ikonBag = new IkonBag();
		final KeyNode<String, LamUrl> mapNode = mf.getNode(MAPUZEL);

		ikonBag.setJmenaSad(mapNode.getKeys());
		ikonBag.setJmenaAIkonySad(nactiIkonySad(mapNode));

		final IkonNacitacSada ikonNacitacSada = new IkonNacitacSada(ikonBag.getGenom());
		final String key = MAPUZEL + "/" + jmenoSady;
		final KeyNode<String, LamUrl> node = mf.getNode(key);
		final Sada sada = ikonNacitacSada.loadSada(node);
		ikonBag.setSada(sada);
		return ikonBag;
	}

	public Map<ASada, Icon> nactiIkonySad(final KeyNode<String, LamUrl> mapNode) {
		final Map<ASada, Icon> jmenaAIkonySad = new TreeMap<>();
		final List<KeyNode<String, LamUrl>> subNodes = mapNode.getSubNodes();
		for (final KeyNode<String, LamUrl> nodeSada : subNodes) {
			Icon icon = null;
			for (final String klic : nodeSada.getKeys()) {
				if (klic.startsWith(IKONA_SADY)) {
					final KeyNode<String, LamUrl> node = nodeSada.getItems().get(klic);
					try {
						icon = new ImageIcon(ImageIO.read(node.getData().url));
					} catch (final IOException e) {
						FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Problem s nacitanim ikony sady: " + node.getData().url);
					}
				}
			}
			jmenaAIkonySad.put(Atom.valueOf(ASada.class, nodeSada.getData().name), icon);
		}
		return jmenaAIkonySad;

	}

	public void setImage3rdPartyDir(final File image3rdPartyDir) {
		this.image3rdPartyDir = image3rdPartyDir;
	}

	public void setImageMyDir(final File imageMyDir) {
		this.imageMyDir = imageMyDir;
	}
}
