package cz.geokuk.plugins.cesty.akce.usek;


import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.cesty.data.Usek;

public class PrepniVzdusnostUseku extends UsekAction0 {

  private static final long serialVersionUID = 1L;

  public PrepniVzdusnostUseku(Usek usek, Mou mouMysi) {
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
    if (usek.isVzdusny()) {
      putValue(NAME,  "<html>Přepni úsek " + dalkas +  " na běžný");
      putValue(SHORT_DESCRIPTION, "Vybraný vzdušný úsek přepne na úsek běžný, což způsobí že bude vykreslen plnou čarou a že se tak sousední stávající segmenty trasy spojí. Běžný úsek se na rozdíl od vzdušného počítá do délky trasy.");
    } else {
      putValue(NAME,  "<html>Přepni úsek " + dalkas + " na vzdušný");
      putValue(SHORT_DESCRIPTION, "Vybraný úsek přepne na úsek vzdušný, což způsobí že bude vykreslen přerušovanou čarou a že tak stávající segment rozdělí na dva nové segmenty trasy. Vzdušné úseky se nezapočítávají do délky cesty.");
    }
  }

  @Override
  protected void provedProUsek(Usek usek, Mou mou) {
    cestyModel.prepniVzdusnostUseku(usek, ! usek.isVzdusny());
  }



}
