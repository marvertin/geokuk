package cz.geokuk.plugins.vylety;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.vylety.data.Cesta;
import cz.geokuk.plugins.vylety.data.Doc;

public class VyletChangedEvent extends Event0<VyletModel> {
  private final Doc doc;

  public VyletChangedEvent(Doc doc, Cesta curta) {
    this.doc = doc;
  }

  public Doc getDoc() {
    return doc;
  }


}
