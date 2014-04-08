package cz.geokuk.plugins.kesoidobsazenost;

import cz.geokuk.framework.Event0;

public class ObsazenostPreferencesChangeEvent extends Event0<ObsazenostModel> {
  public final ObsazenostSettings obsazenost;

  ObsazenostPreferencesChangeEvent(ObsazenostSettings obsazenost) {
    this.obsazenost = obsazenost;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return obsazenost.toString();
  }
}
