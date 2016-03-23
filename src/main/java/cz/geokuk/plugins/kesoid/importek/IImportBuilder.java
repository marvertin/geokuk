package cz.geokuk.plugins.kesoid.importek;


public interface IImportBuilder {

	void addGpxWpt(GpxWpt gpxwpt);

	void addTrackWpt(GpxWpt wpt);

	void begTrackSegment();

	void endTrackSegment();

	void begTrack();

	void endTrack();

	void setTrackName(String aTrackName);

	GpxWpt get(String name);
}