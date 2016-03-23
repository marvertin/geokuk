package cz.geokuk.framework;

public class Event0<T extends Model0> implements BeanSubtypable {

	protected String	subType;
	private T			model;
	/** Firer, který poslal událost, vhodné pro účelypřeposlání dál nebo vygenerování následné události */
	private EventFirer	eventFirer;

	private boolean		locked;

	/**
	 * @return the model
	 */
	public T getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(Model0 model) {
		checkLocked();
		@SuppressWarnings("unchecked")
		T m = (T) model;
		this.model = m;
	}

	@Override
	public String getSubType() {
		return subType;
	}

	public void setEventFirer(EventFirer eventFirer) {
		if (this.eventFirer == eventFirer)
			return;
		checkLocked();
		this.eventFirer = eventFirer;
	}

	public EventFirer getEventFirer() {
		return eventFirer;
	}

	/** Zamkne událost, že nelze nic měnit */
	void lock() {
		locked = true;
	}

	private void checkLocked() {
		if (locked)
			throw new RuntimeException("Locked event: " + getClass().getName() + " - " + this);
	}

}
