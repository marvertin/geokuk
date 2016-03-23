package cz.geokuk.framework;

public class Event0<T extends Model0> implements BeanSubtypable {

	protected String	subType;
	private T			model;
	/** Firer, který poslal událost, vhodné pro účelypřeposlání dál nebo vygenerování následné události */
	private EventFirer	eventFirer;

	private boolean		locked;

	public EventFirer getEventFirer() {
		return eventFirer;
	}

	/**
	 * @return the model
	 */
	public T getModel() {
		return model;
	}

	@Override
	public String getSubType() {
		return subType;
	}

	public void setEventFirer(final EventFirer eventFirer) {
		if (this.eventFirer == eventFirer) {
			return;
		}
		checkLocked();
		this.eventFirer = eventFirer;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(final Model0 model) {
		checkLocked();
		@SuppressWarnings("unchecked")
		final T m = (T) model;
		this.model = m;
	}

	/** Zamkne událost, že nelze nic měnit */
	void lock() {
		locked = true;
	}

	private void checkLocked() {
		if (locked) {
			throw new RuntimeException("Locked event: " + getClass().getName() + " - " + this);
		}
	}

}
