/**
 * 
 */
package cz.geokuk.core.profile;


import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.ToggleAction0;



/**
 * @author veverka
 *
 */
public class UlozitNastaveniKProgramuAction extends ToggleAction0 {

  private static final long serialVersionUID = -2882817111560336824L;
  private ProfileModel profileModel;

  /**
   * @param aBoard
   */
  public UlozitNastaveniKProgramuAction() {
    super("Ukládat nastavení k programu");
    putValue(SHORT_DESCRIPTION, "Zapne ukládání nastavení k programu do souboru \"" + FConst.PREFERENCES_FILE
        + ", při příštím spuštění budou těmito nastaveními nahrazeny nastavení v Java preferences, pokud budou nastavení v souboru novější");
  }

  public void onEvent(NastaveniUkladatDoSouboruEvent event) {
    setSelected(event.getModel().isUkladatDoSouboru());
    setEnabled(event.getModel().isUkladaniDoSouboruMozne());
  }

  //  /* (non-Javadoc)
  //   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
  //   */
  //  @Override
  //  public void actionPerformed(ActionEvent aE) {
  //    if (dialog().anone("Opravdu chcete uložit nastavení do souboru \"" + FConst.PREFERENCES_FILE + "\"? " +
  //    "Akci nelze vrátit, ledaže byste soubor sami vymazali.")) {
  //      FPreferencesInNearFile.saveNearToProgramAndSwitchOn();
  //      dialog().info("Nastavení byla uložena do souboru \"" + FConst.PREFERENCES_FILE
  //          + ", při příštím spuštění budou těmito nastaveními nahrazeny nastavení v Java preferences, " +
  //          "pokud budou nastavení v souboru novější a naopak bude soubor automaticky aktualizován, pokud budou novější data v Java preferences", "Oznámení");
  //    }
  //  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.ToggleAction0#onSlectedChange(boolean)
   */
  @Override
  protected void onSlectedChange(boolean nastaveno) {
    if (nastaveno) {
      profileModel.spustUlozeniDoSouboru();
      //      File file = profileModel.ulozDoSouboruAZapniUkladani();
      //      dialog().info("Nastavení byla uložena do souboru \"" + file
      //          + ", při příštím spuštění budou těmito nastaveními nahrazeny nastavení v Java preferences, " +
      //          "pokud budou nastavení v souboru novější a naopak bude soubor automaticky aktualizován, pokud budou novější data v Java preferences", "Oznámení");
    } else {
      profileModel.zrusUkladaciSouborAVypniUkladani();
    }
  }


  public void inject(ProfileModel profileModel) {
    this.profileModel = profileModel;
  }

}

