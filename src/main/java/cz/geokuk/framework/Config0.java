package cz.geokuk.framework;

/**
 * Takový předeček pro prosté konfigurace.
 *
 * @since ISSUE#48 [2016-04-01, Bohusz]
 * @todo Možná by z toho mohl dědit {@link Model0}.
 */
public abstract class Config0 {
	private Prefe prefe;

	public void inject(final Prefe prefe) {
		this.prefe = prefe;
	}

	protected MyPreferences currPrefe() {
		return prefe.curr();
	}
}
