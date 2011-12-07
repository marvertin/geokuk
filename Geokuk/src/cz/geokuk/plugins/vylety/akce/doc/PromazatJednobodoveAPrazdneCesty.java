package cz.geokuk.plugins.vylety.akce.doc;


import java.util.ArrayList;
import java.util.List;

import cz.geokuk.plugins.vylety.data.Cesta;
import cz.geokuk.plugins.vylety.data.Doc;


public class PromazatJednobodoveAPrazdneCesty extends DocAction0 {

  private static final long serialVersionUID = -7547868179813232769L;


  public PromazatJednobodoveAPrazdneCesty(Doc doc) {
    super(doc);
    putValue(NAME, "Promazat jednobodové cesty");
    putValue(SHORT_DESCRIPTION, "Promaže bšechny jednobodové a prázdné vesty, pokud však nejsou nad waypointy.");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
  }


  @Override
  protected boolean mamPovolitProDoc(Doc doc) {
    return doc.getPocetJednobodovychCest() + doc.getPocetPrazdnychCest() > 0;
  }

  @Override
  protected void nastavJmenoAkce(Doc doc, boolean aZKontextovehoMenu) {
    putValue(NAME, "Promazat jednobodové cesty (" + (doc.getPocetJednobodovychCest() + doc.getPocetPrazdnychCest()) + ")");
  }

  @Override
  protected void provedProDoc(Doc doc) {
    final List<Cesta> cesty = new ArrayList<Cesta>();
    for (Cesta cesta : doc) {
      if (cesta.isEmpty() || cesta.isJednobodova()) {
        cesty.add(cesta);
      }
    }
    for (Cesta cesta : cesty) {
      vyletModel.removeCestu(cesta);
    }
  }




}
