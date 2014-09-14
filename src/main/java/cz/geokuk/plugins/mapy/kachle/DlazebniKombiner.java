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

  public DlazebniKombiner(EnumSet<EKaType> coCekam) {
    super();
    this.coCekam = coCekam;
    pocitadloInstanci.inc();
  }

  public synchronized void add(EKaType type, Image img) {
    if (coMam.contains(type)) return; // to už mám
    int pocetPred = imgs.size();
    imgs.put(type, img);
    rekombinuj();
    int rozdil = (imgs == null ? 0 : imgs.size()) - pocetPred;
    pocitadloRozpracovanychObrazkuVKombinerech.add(rozdil);
  }

  private void rekombinuj() {
    for (EKaType typ : EKaType.values()) {
      boolean cekam = coCekam.contains(typ);
      boolean mam   = coMam.contains(typ);
      if (!cekam) {
        imgs.remove(typ); // kdyby to tam náhodou bylo, aŤ se uvolní, ale nemá proč.
        continue; // pokud to nečekám, tak se nic neděje
      }
      if (mam) continue; // to co mám už mám
      // takže toto čekám, ale nemám
      Image img = imgs.get(typ); // neto by sem patřil
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


  private void prikresli(Image img) {
    if (kombinedImage == null) {
      kombinedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
      mameTadyObrazek = true;
      pocitadloRozpracovanychObrazkuVKombinerech.inc();
    }
    Graphics2D g = kombinedImage.createGraphics();
    g.drawImage(img, 0, 0, null);
  }

  public EnumSet<EKaType> getCoMam() {
    return coMam.clone();
  }

  public BufferedImage getKombinedImage() {
    return kombinedImage;
  }

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
