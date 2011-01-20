package cz.geokuk.plugins.mapy.kachle;





import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;






/**
 * @author veverka
 *
 */
class KachleFileManager {



  private static final String KACHLE_EXT = ".kachle";
  private final KachleCacheFolderHolder kachleCacheFolderHolder;

  //private static final String regexpstr = "([0-9a-z-]+)_([0-9]+)_([0-9a-f]+)_([0-9a-f]+)\\" + KACHLE_EXT;


  //private static Pattern pat = Pattern.compile(regexpstr);

  public KachleFileManager(KachleCacheFolderHolder kachleCacheFolderHolder) {
    this.kachleCacheFolderHolder = kachleCacheFolderHolder;
  }

  public boolean exists(Ka0 ki) {
    File file = identToFullPath(ki);
    boolean b = file.canRead();
    return b;
  }

  /**
   * Loadne obrázek kachle, žádnák keš.
   * @return Vrátí null, pokud nenajde nebo je chyba.
   */
  public Image load(Ka0 ki)  {
    File file = identToFullPath(ki);
    InputStream stm = null;

    try {
      stm = new BufferedInputStream(new FileInputStream(file), 256 * 256);
      Image img = ImageIO.read(stm);
      stm.close();
      //      ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file), 256 * 256));
      //      Image img = (Image) stream.readObject();
      if (img == null) {
        throw new RuntimeException("Nacteno null pri cteni:  " + file);
      }
      return img;
    } catch (Exception e) {
      e.printStackTrace(); // jen vypsat, ale nedumpovat, že nelze načíst
      //FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Loading image: " + ki + "  " + file);
      try {
        if (stm != null) {
          stm.close();
        }
      } catch (IOException e1) {
        // Nedumpuje se výjimka, když nelze zavřít
        //FExceptionDumper.dump(e1, EExceptionSeverity.WORKARROUND, "Closing image file: " + ki + "  " + file);
      }
      deleteFile(file);
      return null;
    }
  }


  /**
   * @param file
   */

  /**
   * Uloží obrázek kachle, nic nedělá s keší.
   * Pokud se uložit nepouží, soubor se pokusí smazat.
   * Vyhodí výjimku, když se operace nezadaří.
   * @param ki
   * @param img
   * @return true, pokud se podařilo uložit, jinak false
   */
  public boolean save(Ka0 ki, DiskSaveSpi dss)  {
    File file = identToFullPath(ki);
    try {
      file.getParentFile().mkdirs();
      BufferedOutputStream stm = new BufferedOutputStream(new FileOutputStream(file), 256*256);
      dss.save(stm);
      stm.close();
      return true;
    } catch (IOException e) {
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Saving image: " + ki + "  " + file);
      return false;
    }
  }


  private void deleteFile(File file) {
    try {
      file.delete();
    } catch (Exception e) {
      // Tady se dumpuje, když ani nelze smazat, je to špatné
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Deleting image: " + file);
    }
  }





  /**

   * Prohledá kešovou složku, nalezne všechny kachle a provolá listener

   */

  //  public void scanFolder(KachloIdentListener listener) {
  //    if (! root.isDirectory()) return;
  //    scan(listener, root);
  //  }



  //  private void scan(KachloIdentListener listener, File aDir) {
  //    for (String ford : aDir.list()) {
  //      KaOne ident = fileNameToIdent(ford);
  //      if (ident != null) {
  //        listener.kachleExistsAsFile(ident);
  //      } else {
  //        File fordf = new File(aDir, ford);
  //        if (fordf.isDirectory()) {
  //          scan(listener, fordf);
  //        } else {
  //          //deleteFile(fordf); // jiný soubor tam nemá co dělat
  //        }
  //      }
  //    }
  //  }



  //  /**
  //   * @param aFord
  //   * @return
  //   */
  //
  //  private KaOne fileNameToIdent(String aFileName) {
  //    Matcher mat = pat.matcher(aFileName);
  //    if (!mat.matches()) return null;
  //
  //    int moumer = Integer.parseInt(mat.group(2));
  //    int xx = Integer.parseInt(mat.group(3),16);
  //    int yy = Integer.parseInt(mat.group(4),16);
  //    EKaType kachloType = Enum.valueOf(EKaType.class, mat.group(1).toUpperCase());
  //
  //    KaLoc loc = new KaLoc(new Mou(xx,yy), moumer);
  //		KaOne kaOne = new KaOne(loc, kachloType);
  //    return kaOne;
  //  }



  /**

   * @param aFord

   * @return

   */

  private String identToFileName(Ka0 ki) {
    StringBuffer sb = new StringBuffer(50);
    sb.append(ki.typToString());
    sb.append('_');
    sb.append(ki.getLoc().getMoumer());
    sb.append('_');
    sb.append(Integer.toHexString(ki.getLoc().getMou().xx));
    sb.append('_');
    sb.append(Integer.toHexString(ki.getLoc().getMou().yy));
    sb.append(KACHLE_EXT);
    return sb.toString().toLowerCase();

  }



  /**

   * @param aKi

   * @return

   */

  private File identToFullPath(Ka0 ki) {
    String hx = Integer.toHexString(ki.getLoc().getMou().xx) + "0000" ;
    String hy = Integer.toHexString(ki.getLoc().getMou().yy) + "0000";
    StringBuffer sb = new StringBuffer();
    sb.append(hx.substring(0, 2));
    sb.append('/');
    sb.append(hy.substring(0, 2));
    sb.append('/');
    sb.append(hx.substring(2, 4));
    sb.append('/');
    sb.append(hy.substring(2, 4));
    sb.append('/');
    sb.append(identToFileName(ki));
    return new File(kachleCacheFolderHolder.getKachleCacheFolder().getEffectiveFile(), sb.toString());

  }



}


