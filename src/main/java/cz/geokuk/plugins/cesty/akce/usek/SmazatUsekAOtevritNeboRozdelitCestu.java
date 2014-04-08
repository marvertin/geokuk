package cz.geokuk.plugins.cesty.akce.usek;


import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.cesty.data.Usek;

public class SmazatUsekAOtevritNeboRozdelitCestu extends UsekAction0 {

  private static final long serialVersionUID = 1L;

  public SmazatUsekAOtevritNeboRozdelitCestu(Usek usek, Mou mouMysi) {
    super(usek, mouMysi);
    //putValue(MNEMONIC_KEY, KeyEvent.VK_V);
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
    //putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
  }

  @Override
  protected boolean mamPovolitProUsek(Usek usek, Mou mou) {
    return true;
  }

  @Override
  protected void nastavJmenoAkce(Usek usek, Mou mou) {
    String dalkas = usek.dalkaHtml();
    if (usek.getCesta().isKruh()) {
      putValue(NAME,  "<html>Smazat úsek " + dalkas + " a otevřít cestu" + usek.getCesta().getNazevADalkaHtml());
      putValue(SHORT_DESCRIPTION, "Smaže vybraný úsek, čímž se uzavřená cesta otevře. Koncové body mazaného úseku se stanou startovním a koncovým bodem cesty.");
    } else {
      putValue(NAME,  "<html>Smazat úsek " + dalkas + " a rozdělit cestu" + usek.getNazevADalkaHtml(mou));
      putValue(SHORT_DESCRIPTION, "Smaže vybraný úsek, čímž se cesta rozdělí na dvě cesty, odřízne se přitom zadek cesty.");
    }
  }

  @Override
  protected void provedProUsek(Usek usek, Mou mou) {
    //LATER zamyšlet se nad krajovými úseky
    cestyModel.smazatUsekAOtevritNeboRozdelitCestu(usek);
  }



}
