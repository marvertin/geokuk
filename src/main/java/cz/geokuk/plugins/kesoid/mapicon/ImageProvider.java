package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Poskytne image daný URL, což je z apliakce nebo i z filesystému pro uživatelem definované obrázky. Dotahuje skutečné obrázky, ne nějaké skripty na jejich tvorbu.
 * 
 * @author veverka
 *
 */
public interface ImageProvider {

	BufferedImage getImage(URL url);
}
