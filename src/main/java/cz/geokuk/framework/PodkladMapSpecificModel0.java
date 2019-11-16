package cz.geokuk.framework;

import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;

public abstract class PodkladMapSpecificModel0<T extends Model0, S extends Copyable<S>> extends Model0 {

	/**
	 *
	 */
	protected MyPreferences prefNode;
	private EKaType podkladMap;

	private S structure;

	@SuppressWarnings("unchecked")
	private final T tthis = (T) this;

	public final Onoff<T> visible = new Onoff<T>(tthis, getOnoffEventClass()) {
		@Override
		protected void onSetOnOff(final boolean onoff) {
			putVisibleToPreferences(onoff);
		}
	};

	public S getData() {
		final S result = structure.copy();
		return result;
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		final EKaType podklad = event.getKatype();
		setPodkladMap(podklad);
	}

	public void setData(final S structure) {
		if (structure.equals(this.structure)) {
			return;
		}
		this.structure = structure;
		save(podkladMap, structure);
		fire(createEvent(structure));
	}

	/**
	 * @param podklad
	 */
	public void setPodkladMap(final EKaType podklad) {
		if (podkladMap == podklad) {
			return;
		}
		podkladMap = podklad;
		final S p = load(podklad);
		setData(p);
	}

	protected abstract S createDefaults();

	protected abstract Event0<?> createEvent(S structure);

	protected Class<? extends OnoffEvent0<T>> getOnoffEventClass() {
		return null;
	}

	protected abstract boolean getVisibleFromPreferences(boolean defaultOnoff);

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
		reloadPreferences();
	}

	protected abstract String preferenceNodeName();

	protected abstract void putVisibleToPreferences(boolean onoff);

	/**
	 *
	 */
	@Override
	protected void reloadPreferences() {
		prefNode = currPrefe().node(preferenceNodeName());
		visiblexxx().setOnoff(getVisibleFromPreferences(true));
		final S p = load(podkladMap);
		setData(p);
	}

	protected abstract Onoff<T> visiblexxx();

	/**
	 * @param podklad
	 * @return
	 */
	private String jmenoPodkladu(final EKaType podklad) {
		return podklad == null ? "bezmap" : podklad.name();
	}

	/**
	 * @return
	 */
	private S load(final EKaType podklad) {
		final S p = prefNode.getStructure(jmenoPodkladu(podklad), createDefaults());
		return p;
	}

	/**
	 * @param p
	 */
	private void save(final EKaType podklad, final S p) {
		prefNode.putStructure(jmenoPodkladu(podklad), p);
	}

}
