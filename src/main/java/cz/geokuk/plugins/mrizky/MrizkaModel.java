package cz.geokuk.plugins.mrizky;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.BeanSubtypable;
import cz.geokuk.framework.Model0;

public class MrizkaModel extends Model0 implements BeanSubtypable {

	private Boolean onoff;
	private final String kteraMrizka;
	private final boolean defaultZobrazeni;

	public MrizkaModel(final String kteraMrizka, final boolean defaultZobrazeni) {
		super();
		this.kteraMrizka = kteraMrizka;
		this.defaultZobrazeni = defaultZobrazeni;
	}

	@Override
	public String getSubType() {
		return kteraMrizka;
	}

	public boolean isOnoff() {
		return onoff;
	}

	public void setOnoff(final boolean onoff) {
		if (this.onoff != null && onoff == this.onoff) {
			return;
		}
		this.onoff = onoff;
		currPrefe().node(FPref.MRIZKA_node).putBoolean(FPref.ZOBRAZIT_MMRIZKU_valuePrefix + kteraMrizka, onoff);
		fire(new MrizkaEvent(kteraMrizka, onoff));
	}

	@Override
	protected void initAndFire() {
		reloadPreferences();
	}

	@Override
	protected void reloadPreferences() {
		setOnoff(currPrefe().node(FPref.MRIZKA_node).getBoolean(FPref.ZOBRAZIT_MMRIZKU_valuePrefix + kteraMrizka, defaultZobrazeni));
		currPrefe().node(FPref.MRIZKA_node).remove("zobrazitMeritko");
	}
}
