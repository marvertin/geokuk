package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;

public class GccomNickChangedEvent extends Event0<KesoidModel> {

  private final GccomNick gccomNick;

  public GccomNickChangedEvent(GccomNick gccomNick) {
    super();
    this.gccomNick = gccomNick;
  }

  public GccomNick getGccomNick() {
    return gccomNick;
  }


}
