package cz.geokuk.core.render;

import javax.swing.KeyStroke;

import cz.geokuk.core.onoffline.OnofflineModelChangeEvent;
import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.img.ImageLoader;


public class RenderAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -5465641756515262340L;

	public RenderAction() {
		super("Tisknout/Rendrovat...");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl P"));
		putValue(SHORT_DESCRIPTION, "Zobrazí dialog s možností vyrendrovat mapový podklad libovolné velikosti i s kešemi a mřížkami s možností kalibrací pro GoogleEarth, OziExplorer a jiné programy.");
		putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/printer.jpg"));

	}

	public void onEvent(final OnofflineModelChangeEvent event) {
		//    setEnabled(event.getModel().isOnlineMode());
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.framework.DialogOpeningAction0#createDialog()
	 */
	@Override
	public JMyDialog0 createDialog() {
		return new JRenderDialog();
	}
}
