/**
 * 
 */
package cz.geokuk.core.program;

import java.awt.Frame;

import cz.geokuk.framework.Model0;

/**
 * @author veverka
 *
 */
public class OknoModel extends Model0 {

  private OknoUmisteniDto oknoUmisteni;
  private int stavOkna;
  
  /**
   * @return the stavOkna
   */
  public int getStavOkna() {
    return stavOkna;
  }

  /**
   * @param stavOkna the stavOkna to set
   */
  public void setStavOkna(int stavOkna) {
    if (stavOkna == this.stavOkna) return;
    this.stavOkna = stavOkna;
    currPrefe().node(FPref.OKNO_structure_node).putInt(FPref.STAV_OKNA_value, stavOkna);
  }

  /**
   * @param oknoUmisteni the oknoStatus to set
   */
  public void setOknoUmisteni(OknoUmisteniDto oknoUmisteni) {
    if (oknoUmisteni.equals(this.oknoUmisteni)) return;
    this.oknoUmisteni = oknoUmisteni;
    currPrefe().putStructure(FPref.OKNO_structure_node, oknoUmisteni);
  }

  /**
   * @return the oknoStatus
   */
  public OknoUmisteniDto getOknoStatus() {
    return oknoUmisteni;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.Model0#initAndFire()
   */
  @Override
  protected void initAndFire() {
    setOknoUmisteni(currPrefe().getStructure(FPref.OKNO_structure_node, new OknoUmisteniDto()));
    int stav = currPrefe().node(FPref.OKNO_structure_node).getInt(FPref.STAV_OKNA_value, Frame.NORMAL);
    if (stav == Frame.ICONIFIED) stav = Frame.NORMAL; // není moc dobré otvírat program ikonizovaně
    setStavOkna(stav);
    fire(new OknoStatusChangedEvent(stavOkna, oknoUmisteni));
  }


}
