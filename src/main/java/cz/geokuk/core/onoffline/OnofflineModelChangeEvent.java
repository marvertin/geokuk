package cz.geokuk.core.onoffline;

import cz.geokuk.framework.Event0;

public class OnofflineModelChangeEvent extends Event0<OnofflineModel> {

  private final boolean online;

  public boolean isOnlineMOde() {
    return online;
  }

  public OnofflineModelChangeEvent(final boolean online) {
    super();
    this.online = online;
  }
}
