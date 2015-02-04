package cz.geokuk.plugins.cesty;

public enum ECeystyVylet {
  VSECHNY ("Všechny"),   // NE
  BEZ_IGNOROVANYCH ("Bez vynechaných"),  // NEVIM
  JEN_V_CESTE("Jen lovené"),  // ANO
  ;

  private final String doKomboBoxu;

  ECeystyVylet(String doKomboBoxu) {
    this.doKomboBoxu = doKomboBoxu;
  }

  @Override
  public String toString() {
    return doKomboBoxu;

  }
}
