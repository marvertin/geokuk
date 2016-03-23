package cz.geokuk.core.render;

import cz.geokuk.framework.Event0;

public class PripravaRendrovaniEvent extends Event0<RenderModel> {

	private final RenderSettings renderSettings;
	private final EStavRendrovani stavRendrovani;

	/**
	 * @param renderSettings
	 */
	public PripravaRendrovaniEvent(final RenderSettings renderSettings, final EStavRendrovani stavRendrovani) {
		super();
		this.renderSettings = renderSettings;
		this.stavRendrovani = stavRendrovani;
	}

	/**
	 * @return the renderSettings
	 */
	public RenderSettings getRenderSettings() {
		return renderSettings;
	}

	/**
	 * @return the stavRendrovani
	 */
	public EStavRendrovani getStavRendrovani() {
		return stavRendrovani;
	}

}
