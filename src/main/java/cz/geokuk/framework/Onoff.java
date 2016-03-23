/**
 *
 */
package cz.geokuk.framework;

/**
 * @author Martin Veverka
 *
 */
public class Onoff<T extends Model0> {

	private final Class<? extends OnoffEvent0<T>> clazz;
	private final T model;

	private Boolean onoff;

	/**
	 * @param model
	 * @param clazz
	 */
	public Onoff(final T model, final Class<? extends OnoffEvent0<T>> clazz) {
		super();
		this.model = model;
		this.clazz = clazz;
	}

	/**
	 * @return
	 */
	public boolean isOnoff() {
		return onoff;
	}

	public void setOnoff(final boolean onoff) {
		try {
			if (this.onoff != null && this.onoff == onoff) {
				return;
			}
			this.onoff = onoff;
			onSetOnOff(onoff); // nastavit do modelu, pokud model toto požaduje, například za čelem zperzostentnění
			if (clazz != null) {
				final OnoffEvent0<T> event = clazz.newInstance();
				event.setModel(model);
				event.onoff = onoff;
				model.fire(event);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected void onSetOnOff(final boolean onoff) {

	}

}
