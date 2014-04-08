package cz.geokuk.plugins.kesoidpopisky;

import cz.geokuk.framework.Event0;

public class PopiskyPreferencesChangeEvent extends Event0<PopiskyModel> {
  public final PopiskySettings pose;

  PopiskyPreferencesChangeEvent(PopiskySettings pose) {
    this.pose = pose;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return pose.toString();
  }
}
