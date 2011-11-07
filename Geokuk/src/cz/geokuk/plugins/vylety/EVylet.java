package cz.geokuk.plugins.vylety;

public enum EVylet {
  VSECHNY ("Všechny"),   // NE
  BEZ_IGNOROVANYCH ("Bez vynechaných"),  // NEVIM
  JEN_V_CESTE("Jen lovené"),  // ANO
  ;

  private final String doKomboBoxu;

  EVylet(String doKomboBoxu) {
    this.doKomboBoxu = doKomboBoxu;
  }

  @Override
  public String toString() {
    return doKomboBoxu;

  }
}
