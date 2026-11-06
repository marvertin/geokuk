package cz.geokuk.plugins.challenge;

import java.time.LocalDate;

/** Testovací keš pro D/T matici i kalendář. */
class PolicekKes implements VstupDT, VstupKalendar {

	final String id;
	final double lat;
	final double lon;
	final boolean nalezena;
	final int obtiznost;
	final int teren;
	final LocalDate datumNalezu;
	final LocalDate datum;
	final boolean event;

	PolicekKes(final String id, final double lat, final double lon, final boolean nalezena, final int obtiznost, final int teren, final LocalDate datumNalezu, final LocalDate datum,
			final boolean event) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.nalezena = nalezena;
		this.obtiznost = obtiznost;
		this.teren = teren;
		this.datumNalezu = datumNalezu;
		this.datum = datum;
		this.event = event;
	}

	static PolicekKes dt(final String id, final double lat, final double lon, final boolean nalezena, final int d, final int t) {
		return new PolicekKes(id, lat, lon, nalezena, d, t, null, null, false);
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public double lat() {
		return lat;
	}

	@Override
	public double lon() {
		return lon;
	}

	@Override
	public boolean nalezena() {
		return nalezena;
	}

	@Override
	public boolean lab() {
		return false;
	}

	@Override
	public boolean locationless() {
		return false;
	}

	@Override
	public StavLogu logNaGc() {
		return StavLogu.NEZNAMY;
	}

	@Override
	public int obtiznost() {
		return obtiznost;
	}

	@Override
	public int teren() {
		return teren;
	}

	@Override
	public LocalDate datumNalezu() {
		return datumNalezu;
	}

	@Override
	public LocalDate datum() {
		return datum;
	}

	@Override
	public boolean event() {
		return event;
	}
}
