package cz.geokuk.plugins.cesty.akce.cesta;


import cz.geokuk.plugins.cesty.data.Cesta;

public class SmazatCestuAction extends CestaAction0 {

  private static final long serialVersionUID = 1L;

  public SmazatCestuAction(Cesta cesta) {
    super(cesta);
    //putValue(NAME,  "<html>Odstraň cestu <i>" + jCestaMenu.getNazev() + "</i> " + (jCestaMenu.getMouDelkaCesta() + " mou"));
    putValue(NAME,  "Smazat cestu");
    putValue(SHORT_DESCRIPTION, "Vybranou cestu zcela odstraní z výletu se všemi jejími body a úseky");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_V);
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
    //putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
  }


  @Override
  protected boolean mamPovolitProCestu(Cesta cesta) {
    return true;
  }

  @Override
  protected void nastavJmenoAkce(Cesta cesta, boolean aZKontextovehoMenu) {
    putValue(NAME, "<html>Smazat cestu" + cesta.getNazevHtml() + " " + cesta.dalkaHtml());
  }

  @Override
  protected void provedProCestu(Cesta cesta) {
    vyletModel.removeCestu(cesta);
  }



}
