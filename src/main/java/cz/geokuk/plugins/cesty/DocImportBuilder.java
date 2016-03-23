package cz.geokuk.plugins.cesty;

import java.util.ArrayList;
import java.util.List;

import cz.geokuk.plugins.cesty.data.*;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.importek.IImportBuilder;

public class DocImportBuilder implements IImportBuilder {

	private Cesta			cesta;
	private List<Cesta>		cesty	= new ArrayList<>();

	private boolean			zacatekSegmentu;

	private final Updator	updator	= new Updator();

	public DocImportBuilder() {}

	@Override
	public void addGpxWpt(final GpxWpt gpxwpt) {}

	@Override
	public void addTrackWpt(final GpxWpt wpt) {
		final Bod bod = updator.pridejNaKonec(cesta, wpt.wgs.toMou());
		final Usek usek = bod.getUvzad(); // právě přidaný úsek, pokud nějaký
		if (usek != null && zacatekSegmentu) {
			updator.setVzdusny(usek, true);
		}
		zacatekSegmentu = false;
	}

	@Override
	public void begTrack() {
		cesta = Cesta.create();
	}

	@Override
	public void begTrackSegment() {
		zacatekSegmentu = true;
	}

	@Override
	public void endTrack() {
		cesty.add(cesta);
		cesta = null;
	}

	@Override
	public void endTrackSegment() {}

	@Override
	public GpxWpt get(final String aName) {
		throw new RuntimeException("Neimplementovano");
	}

	@Override
	public void setTrackName(final String aTrackName) {
		updator.setNazev(cesta, aTrackName);
	}

	List<Cesta> getCesty() {
		return cesty;
	}

	void setCesty(final List<Cesta> cesty) {
		this.cesty = cesty;
	}

}
