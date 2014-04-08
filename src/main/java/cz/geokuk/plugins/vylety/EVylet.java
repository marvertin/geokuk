package cz.geokuk.plugins.vylety;

public enum EVylet {
  NE ("Všechny"),
  NEVIM ("Bez vynechaných"),
  ANO ("Jen lovené"),
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
