package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.KesBag;

public class JVybiracFavorit extends JVybiracCiselny0  {

  public JVybiracFavorit() {
    super("Favorit:");
  }

  private static final long serialVersionUID = -484273090975902036L;


  @Override
  protected void setPrah(FilterDefinition filterDefinition, int prah) {
    filterDefinition.setPrahFavorit(prah);
  }

  @Override
  protected int getPrah(FilterDefinition filterDefinition) {
    return filterDefinition.getPrahFavorit();
  }

  @Override
  protected int getMaximum(KesBag vsechny) {
    return vsechny.getMaximalniFavorit();
  }



}