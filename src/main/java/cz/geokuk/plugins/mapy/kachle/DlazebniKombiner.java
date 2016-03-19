package cz.geokuk.plugins.mapy.kachle;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;

/**
 * Kombinuje do sebe různé dlaždice, aby se to nemusel dělat draze v paintu.
 * Není nijak aktivní ve smyslu, že by něco provolával, když je hotov.
 * @author tatinek
 *
 */
class DlazebniKombiner {
  private static final Logger log = LogManager.getLogger(DlazebniKombiner.class.getSimpleName());

  private static Pocitadlo pocitadloInstanci = new PocitadloMalo("Počet dlažebních kombinérů.",
      "Počítá, kolik máme instancí " + DlazebniKombiner.class.getName() + ".");

  private EnumSet<EKaType> coCekam; // co čekám, že nakombinuju
  private final EnumSet<EKaType> coMam = EnumSet.noneOf(EKaType.class);   // co už mám nakombinováno

  private BufferedImage kombinedImage;

  private boolean hotovo;

  private EnumMap<EKaType, ImageOrException> imgs = new EnumMap<>(EKaType.class);

  private Throwable firstException;

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
    log.debug("add image type={}", type);
    add(type, new ImageOrException(img));
  }

  /**
   * Vloží výjimku při získávání kachle daného typu.
   * Vzhledem k ostatnímu posuzování je to jako prázdný obrázek.
   * @param type
   * @param img
   */
  public synchronized void add(final EKaType type, final Throwable thr) {
    log.debug("add exception type={}", type);
    add(type, new ImageOrException(thr));
  }

  private synchronized void add(final EKaType type, final ImageOrException imageOrException) {
    if (hotovo) {
      return;
    }
    if (coMam.contains(type))  {
      return; // to už mám
    }
    if (!coCekam.contains(type))  {
      return; // to nečekám, tak nevím proč to posílají. Nechci to
    }
    imgs.put(type, imageOrException);
    rekombinuj();
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
      final ImageOrException imageOrException = imgs.get(typ); // neto by sem patřil
      if (imageOrException == null) {
        //        if (kombinedImage == null) {
        //          System.out.println("NEMAM JESTE: " + typ + ", ale mam uz " + imgs.keySet());
        //        }
        return; // ukončím celý cyklus i metodu, nemohu přidat, protože to nemám
      }
      prikresli(imageOrException); // obrázek je připraven na přikreslení
      coMam.add(typ); // a už ho mám
      imgs.remove(typ); // a už ho nebudu potřebovat
    }
    // když už konečně dojdu na konec cyklu, vše je zkominováno
    imgs = null;
    coCekam = null;
    hotovo = true;
  }

  /**
   * Vrací první výjimku, která byla zjiětěna. To znamená na první dlaždici, která se stahovala.
   * @return
   */
  public synchronized Throwable getFirstException() {
    return firstException;
  }


  private void prikresli(final ImageOrException imageOrException) {
    prikresliImage(imageOrException.image);
    prikresliRxception(imageOrException.throwable);
  }

  private void prikresliRxception(final Throwable thr) {
    if (firstException == null && thr != null) {
      firstException = thr;
    }
  }

  private void prikresliImage(final Image image) {
    if (image == null) {
      return;
    }
    if (kombinedImage == null) {
      kombinedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    }
    final Graphics2D g = kombinedImage.createGraphics();
    g.drawImage(image, 0, 0, null);
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
  }

  /**
   * Jen kvůli úpravě statistik.
   */
  //  public synchronized void uzTeNepotrebuju() {
  //    pocitadloRozpracovanychObrazkuVKombinerech.sub(imgs == null ? 0 : imgs.size());
  //    imgs = null;
  //    if (hotovo) {
  //      pocitadloInstanciSHotovymObrazkem.dec();
  //      hotovo = false;
  //    }
  //    if (kombinedImage != null) {
  //      pocitadloRozpracovanychObrazkuVKombinerech.dec();
  //      kombinedImage = null;
  //      mameTadyObrazek = false;
  //    }
  //  }

  private static class ImageOrException {
    final Image image;
    final Throwable throwable;

    ImageOrException(final Image image) {
      this.image = image;
      this.throwable = null;
    }

    ImageOrException(final Throwable throwable) {
      this.throwable = throwable;
      this.image = null;
    }

  }
}
