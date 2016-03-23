package cz.geokuk.plugins.kesoid.importek;

public interface IImportBuilder {

	void addGpxWpt(GpxWpt gpxwpt);

	void addTrackWpt(GpxWpt wpt);

	void begTrack();

	void begTrackSegment();

	void endTrack();

	void endTrackSegment();

	GpxWpt get(String name);

	void setTrackName(String aTrackName);
}