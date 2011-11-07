package cz.geokuk.plugins.vylety.akce;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.Poziceq;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.vylety.cesty.Bod;

public class VyletAnoAction extends VyletAction0 {

  private static final long serialVersionUID = 1L;

  private final Mouable kontextoveMouable;
  private Poziceq poziceq;

  public VyletAnoAction(Mouable kontextoveMouable) {
    this.kontextoveMouable = kontextoveMouable;

    putValue(NAME, "Přidej do cesty");
    putValue(SHORT_DESCRIPTION, "Zařadí waypoint nebo pozici vybrané cesty, pokud žádná cesta není vybraná, vybere se automaticky nejbližší cesta. Když však žádná cesta neexistuje, je založena." +
    " Pokud se jedná o waypoint keše, která byla je na ignore listu, je z ignore listu odstraněna.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_L);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("INSERT"));
    putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    vyletModel.addToVylet(effectiveMouable());
  }

  protected void enablujPokudMaSmysl() {
    setEnabled(effectiveMouable() != null && ! vyletModel.isOnVylet(effectiveMouable()));
  }


  public void onEvent(PoziceChangedEvent aEvent) {
    poziceq = aEvent.poziceq;
    enablujPokudMaSmysl();
  }

  @Override
  protected final void vyletChanged() {
    super.vyletChanged();
    enablujPokudMaSmysl();
  }
  /**
   * Do vyýletu se dá přidat jen WPT, ale ne Bod, ale také volná pozice.
   * @param mouable
   * @return
   */
  private Mouable proPridaniDoVyletu(Mouable mouable) {
    if (mouable instanceof Wpt)
      return mouable;
    if (mouable instanceof Bod)
      return null; // připojovat na bod se nebudeme
    return mouable.getMou(); // a ke všemu ostatnímu se připojíme, což je zřejmě notmální bod
  }

  private Mouable effectiveMouable() {
    if (kontextoveMouable != null)
      return proPridaniDoVyletu(kontextoveMouable);
    // jinak se musíme spolehnout na pozici
    if (! poziceq.isNoPosition())
      return proPridaniDoVyletu(poziceq.getPoziceMouable());
    return null;
  }

}
