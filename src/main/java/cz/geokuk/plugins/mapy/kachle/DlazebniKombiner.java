package cz.geokuk.plugins.mapy.kachle;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.EnumSet;

import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;
import cz.geokuk.util.pocitadla.PocitadloNula;

/**
 * Kombinuje do sebe různé dlaždice, aby se to nemusel dělat draze v paintu.
 * Není nijak aktivní ve smyslu, že by něco provolával, když je hotov.
 * @author tatinek
 *
 */
class DlazebniKombiner {
  private static Pocitadlo pocitadloInstanci = new PocitadloMalo("Počet dlažebních kombinérů.",
      "Počítá, kolik máme instancí " + DlazebniKombiner.class.getName() + ".");
  private static Pocitadlo pocitadloInstanciSHotovymObrazkem = new PocitadloNula("Počet dlažebních kombinérů s hotovým obrázkem.",
      "Počítá, kolik máme instancí " + DlazebniKombiner.class.getName() + " ve kterých je už hotový obrázek.");
  private static Pocitadlo pocitadloRozpracovanychObrazkuVKombinerech = new PocitadloNula("Počet rozpracovaných obrázků v dlažebních kombinérech.",
      "Počítá, kolik máme celkem obrázků v dlažebních kombinérech ve fázo rozpracovanostik.");

  private EnumSet<EKaType> coCekam; // co čekám, že nakombinuju
  private final EnumSet<EKaType> coMam = EnumSet.noneOf(EKaType.class);   // co už mám nakombinováno

  private BufferedImage kombinedImage;
  // Jen pro potřeby čítačů, jinak přímo použít kombinedImage
  private boolean mameTadyObrazek;

  private boolean hotovo;

  private EnumMap<EKaType, Image> imgs = new EnumMap<>(EKaType.class);

  /**
   * Nová instance bude kombinovat kachle daných mapových podkladů.
   * Až nakombinuje, vrací. Ale vrátit dokáže i postupně.
   * @param coCekam
   */
  public DlazebniKombiner(final EnumSet<EKaType> coCekam) {
    super();
    this.coCekam = coCekam;
    pocitadloInstanci.inc();
  }

  /**
   * Vloží obrázek kachle daného typu.
   * Pro daný typ lpe obrázek vložit jen jednou. Pokud ho dostane podruhé (nemělo by se stát), neudělá nic.
   * Pokud vloží obrázek, který nečekám, tak také neudělám nic.
   * Pokud mám hotovo, neudělá nic.
   * @param type
   * @param img
   */
  public synchronized void add(final EKaType type, final Image img) {
    if (hotovo) {
      return;
    }
    if (coMam.contains(type))  {
      return; // to už mám
    }
    if (!coCekam.contains(type))  {
      return; // to nečekám, tak nevím proč to posílají. Nechci to
    }
    final int pocetPred = imgs.size();
    imgs.put(type, img);
    rekombinuj();
    final int rozdil = (imgs == null ? 0 : imgs.size()) - pocetPred;
    pocitadloRozpracovanychObrazkuVKombinerech.add(rozdil);
  }

  private void rekombinuj() {
    for (final EKaType typ : EKaType.values()) {
      final boolean cekam = coCekam.contains(typ);
      final boolean mam   = coMam.contains(typ);
      if (!cekam) {
        imgs.remove(typ); // kdyby to tam náhodou bylo, aŤ se uvolní, ale nemá proč.
        continue; // pokud to nečekám, tak se nic neděje
      }
      if (mam)
      {
        continue; // to co mám už mám
      }
      // takže toto čekám, ale nemám
      final Image img = imgs.get(typ); // neto by sem patřil
      if (img == null) {
        //        if (kombinedImage == null) {
        //          System.out.println("NEMAM JESTE: " + typ + ", ale mam uz " + imgs.keySet());
        //        }
        return; // ukončím celý cyklus i metodu, nemohu přidat, protože to nemám
      }
      prikresli(img); // obrázek je připraven na přikreslení
      coMam.add(typ); // a už ho mám
      imgs.remove(typ); // a už ho nebudu potřebovat
    }
    // když už konečně dojdu na konec cyklu, vše je zkominováno
    imgs = null;
    coCekam = null;
    pocitadloInstanciSHotovymObrazkem.inc();
    hotovo = true;
  }


  private void prikresli(final Image img) {
    if (kombinedImage == null) {
      kombinedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
      mameTadyObrazek = true;
      pocitadloRozpracovanychObrazkuVKombinerech.inc();
    }
    final Graphics2D g = kombinedImage.createGraphics();
    g.drawImage(img, 0, 0, null);
  }

  /**
   * Vrátí co mám.
   * @return Pokud nic nemám, je set prázdný.
   */
  public EnumSet<EKaType> getCoMam() {
    return coMam.clone();
  }

  /**
   * Vrátí nakombinovaný image. Může být částečně nakombinovaný, pokdu nedošlo vše.
   * Ale nikdy se vrstvy nepřekračují. Když už došla 1. a 2. a 4. vrstva, vrací image nakombinovaný jen
   * z prvních dvou vrstev. Když mám jen vrstvy 2. a 3., vrací null. Nemůže tedy vrátit jen vrstvu turistických tras bez podkladu.
   * @return Pokud nic nemám, vracéí se null.
   */
  public BufferedImage getKombinedImage() {
    return kombinedImage;
  }

  /**
   * Pokud je už vše nakombinováno, co má být.
   * @return
   */
  public boolean isHotovo() {
    return hotovo;
  }

  @Override
  public void finalize() {
    pocitadloInstanci.dec();
    if (hotovo) {
      pocitadloInstanciSHotovymObrazkem.dec();
      kombinedImage = null;
    }
    if (mameTadyObrazek) {
      pocitadloRozpracovanychObrazkuVKombinerech.dec();
    }
  }

  /**
   * Jen kvůli úpravě statistik.
   */
  public synchronized void uzTeNepotrebuju() {
    pocitadloRozpracovanychObrazkuVKombinerech.sub(imgs == null ? 0 : imgs.size());
    imgs = null;
    if (hotovo) {
      pocitadloInstanciSHotovymObrazkem.dec();
      hotovo = false;
    }
    if (kombinedImage != null) {
      pocitadloRozpracovanychObrazkuVKombinerech.dec();
      kombinedImage = null;
      mameTadyObrazek = false;
    }
  }

}
