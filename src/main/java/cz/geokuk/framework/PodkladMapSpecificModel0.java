package cz.geokuk.framework;


import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.EKaType;

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
		protected void onSetOnOff(boolean onoff) {
			putVisibleToPreferences(onoff);
		}
	};


	protected abstract Onoff<T> visiblexxx();

	protected abstract S createDefaults();

	protected abstract Event0<?> createEvent(S structure);

	protected Class<? extends OnoffEvent0<T>> getOnoffEventClass() {
		return null;
	}


	protected abstract void putVisibleToPreferences(boolean onoff);

	protected abstract boolean getVisibleFromPreferences(boolean defaultOnoff);

	protected abstract String preferenceNodeName();

	/**
	 * @param podklad
	 */
	public void setPodkladMap(EKaType podklad) {
		if (podkladMap == podklad) return;
		podkladMap = podklad;
		S p = load(podklad);
		setData(p);
	}

	public S getData() {
		S result = structure.copy();
		return result;
	}

	public void setData(S structure) {
		if (structure.equals(this.structure)) return;
		this.structure = structure;
		save(podkladMap, structure);
		fire(createEvent(structure));
	}

	/**
	 * @return
	 */
	private S load(EKaType podklad) {
		S p = prefNode.getStructure(jmenoPodkladu(podklad), createDefaults());
		return p;
	}


	/**
	 * @param p
	 */
	private void save(EKaType podklad, S p) {
		prefNode.putStructure(jmenoPodkladu(podklad), p);
	}

	/**
	 * @param podklad
	 * @return
	 */
	private String jmenoPodkladu(EKaType podklad) {
		return podklad == null ? "bezmap" : podklad.name();
	}

	public void onEvent(ZmenaMapNastalaEvent event) {
		EKaType podklad = event.getKaSet().getPodklad();
		setPodkladMap(podklad);
	}


	/* (non-Javadoc)
	 * @see cz.geokuk.program.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
		reloadPreferences();
	}

	/**
	 *
	 */
	@Override
	protected void reloadPreferences() {
		prefNode = currPrefe().node(preferenceNodeName());
		visiblexxx().setOnoff(getVisibleFromPreferences(true));
		S p = load(podkladMap);
		setData(p);
	}


}
