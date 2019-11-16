package cz.geokuk.plugins.mapy.kachle.podklady;

import java.awt.Image;

import lombok.Data;

/**
 * Obsahuje obrázek a vedle něho binární data tohoto obrázku.
 *
 * @author Martin Veverka
 *
 */
@Data
class ImageWithData {
	private final Image img;
	private final byte[] data;

}
