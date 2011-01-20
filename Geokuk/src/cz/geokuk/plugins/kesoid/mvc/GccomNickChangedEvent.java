package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;

public class GccomNickChangedEvent extends Event0<KesoidModel> {

  private final String gccomNick;

  public GccomNickChangedEvent(String gccomNick) {
    super();
    this.gccomNick = gccomNick;
  }

  public String getGccomNick() {
    return gccomNick;
  }
  
  
}
