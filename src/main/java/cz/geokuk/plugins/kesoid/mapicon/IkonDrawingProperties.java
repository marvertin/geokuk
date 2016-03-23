package cz.geokuk.plugins.kesoid.mapicon;

import java.net.URL;
import java.util.Properties;

import cz.geokuk.api.mapicon.Drawer0;

/**
 * Vlastnosti, které jsou nutné pro vykreslení ikony. Ne tedy pro její výběr.
 * 
 * @author tatinek
 *
 */
public class IkonDrawingProperties {

	public URL			url;
	public Properties	properties;	// property ze soboru
	Drawer0				vykreslovac;

	// Offset levého horního roku obrázku k platné pozici. Při vykreslování se o tento offset popsune
	public int			xoffset;
	public int			yoffset;

}
