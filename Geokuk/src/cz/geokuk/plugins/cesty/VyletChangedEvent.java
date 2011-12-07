package cz.geokuk.plugins.cesty;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.cesty.data.Doc;

public class VyletChangedEvent extends Event0<VyletModel> {
  private final Doc doc;

  public VyletChangedEvent(Doc doc, Cesta curta) {
    this.doc = doc;
  }

  public Doc getDoc() {
    return doc;
  }


}
