package cz.geokuk.plugins.kesoid.mvc;

import javax.swing.SpinnerNumberModel;

import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.KesBag;

public class JVybiracHodnoceni extends JVybiracCiselny0  {

  private static final long serialVersionUID = 2417664157609045262L;

  public final SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 100, 1);

  public JVybiracHodnoceni() {
    super("Hodnocení:");
    setToolTipText("Filte dle prahu hodnocení keší na geocaching.cz, zobrazí se jen keše mající hodnocení větší nebo rovné prahu.");
  }


  @Override
  protected void setPrah(FilterDefinition filterDefinition, int prah) {
    filterDefinition.setPrahHodnoceni(prah);
  }


  @Override
  protected int getPrah(FilterDefinition filterDefinition) {
    return filterDefinition.getPrahHodnoceni();
  }


  @Override
  protected int getMaximum(KesBag vsechny) {
    return vsechny.getMaximalniHodnoceni();
  }


}