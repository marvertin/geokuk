package cz.geokuk.plugins.kesoid.importek;


public interface IImportBuilder {

  public abstract void addGpxWpt(GpxWpt gpxwpt);

  public abstract void addTrackWpt(GpxWpt wpt);

  public abstract void begTrackSegment();

  public abstract void endTrackSegment();

  public abstract void begTrack();

  public abstract void endTrack();

  public abstract void setTrackName(String aTrackName);
  
  public abstract GpxWpt get(String name);

}