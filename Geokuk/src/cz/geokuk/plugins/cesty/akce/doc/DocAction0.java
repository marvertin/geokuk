package cz.geokuk.plugins.cesty.akce.doc;

import java.awt.event.ActionEvent;

import cz.geokuk.plugins.cesty.akce.PositionedAction0;
import cz.geokuk.plugins.cesty.data.Doc;

public abstract class DocAction0 extends PositionedAction0 {

  private static final long serialVersionUID = 1L;
  private final Doc kontextovyDoc;

  public DocAction0(Doc kontextovyDoc) {
    this.kontextovyDoc = kontextovyDoc;
    if (kontextovyDoc != null) {
      kontextovyDoc.kontrolaKonzistence();
    }
  }

  @Override
  protected final void vyletChanged() {
    vyhodnotitPovolenost();
  }


  private void vyhodnotitPovolenost() {
    Doc doc = getDoc();
    if (doc == null) {
      setEnabled(false);
    } else {
      if (mamPovolitProDoc(doc)) {
        setEnabled(true);
        nastavJmenoAkce(doc, kontextovyDoc != null);
      } else {
        setEnabled(false);
      }
    }
  }

  @Override
  public final void actionPerformed(ActionEvent aE) {
    Doc doc = getDoc();
    if (doc != null) {
      provedProDoc(doc);
    }
  }

  /**
   * Pokud je daný Cesta, tak se nechá natavit jméno. Natavovací metoda také
   * ví, zda je akce z kontexotvého menu nebo z hlavního
   * @param Cesta
   * @param aZKontextovehoMenu
   */
  protected abstract void nastavJmenoAkce(Doc doc, boolean aZKontextovehoMenu);

  /**
   * Zda se pro dany Cesta ma akce povolit.
   * @param Cesta Nikdy nebude predano null
   * @return
   */
  protected abstract boolean mamPovolitProDoc(Doc doc);

  /**
   * Provedení akce pro daný Cesta.
   * @param Cesta
   */
  protected abstract void provedProDoc(Doc doc);

  private Doc getDoc() {
    if (curdoc() != null) {
      curdoc().kontrolaKonzistence();
    }
    if (kontextovyDoc != null)
      //System.out.println(System.identityHashCode(kontextovaCesta) + ": CestaAction0-getClass " + getClass());
      //kontextovaCesta.kontrolaKonzistence();
      return kontextovyDoc;
    else
      return curdoc();
  }



}

