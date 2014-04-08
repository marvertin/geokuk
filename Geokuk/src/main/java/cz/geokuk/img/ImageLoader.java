package cz.geokuk.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;


public class ImageLoader {

  private static Map<String, BufferedImage> imagesCache = new HashMap<String, BufferedImage>();

  /**
   * Nahraje obeázek a dá do keše, vhodné tedy jen pro malé často zobrazované obrázky
   * @param path
   * @return
   */
  public static BufferedImage locateResImage(String path) {
    BufferedImage bi = imagesCache.get(path);
    if (bi != null) return bi;
    bi = locateResImageNoCache(path);
    imagesCache.put(path, bi);
    return bi;
  }

  /**
   * Nahraje obeázek a dá do keše, vhodné tedy jen pro malé často zobrazované obrázky
   * @param path
   * @return
   */
  public static BufferedImage locateResImageNoCache(String path) {
      // FIXME hack
      path = "/img/" + path;
    try {
      URL imgURL = ImageLoader.class.getResource(path);
      if (imgURL == null)
        throw new IOException("Cannot find resource \"" + path + "\" using + " + ImageLoader.class.getName());
      BufferedImage bi = ImageIO.read(imgURL);
      return bi;
    } catch (IOException e) {
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Vyhledavani obrazku \""+ path + "\"");
      // neexistující image
      return null;
    }
  }


  /** Returns an ImageIcon, or null if the path was invalid. */
  public static Icon locateResIcon(String path) {
    BufferedImage bi = locateResImageNoCache(path);
    if (bi == null)
      return null;
    else
      return new ImageIcon(bi);
  }

  /** Returns an ImageIcon, or null if the path was invalid. */
  public static Icon seekResIcon(String path) {
    Icon icon = locateResIcon(path);
    if (icon == null) {
      icon = new MissingIcon();
    }
    return icon;
  }

  public static BufferedImage seekResImage(String path, int xn, int yn) {
    BufferedImage img = locateResImageNoCache(path);
    if (img == null) {
      img = createMissingImage(xn, yn);
    }
    return img;
  }


  public static BufferedImage seekResImage(String path) {
    BufferedImage img = locateResImageNoCache(path);
    if (img == null) throw new RuntimeException("cannot find image: " + path);
    return img;
  }

  private static BufferedImage createMissingImage(int width, int height) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g2d = (Graphics2D) image.getGraphics();
    int x = 0;
    int y = 0;

    g2d.setColor(Color.WHITE);
    g2d.fillRect(x +1 ,y + 1,width -2 ,height -2);

    g2d.setColor(Color.BLACK);
    g2d.drawRect(x +1 ,y + 1,width -2 ,height -2);

    g2d.setColor(Color.RED);

    BasicStroke stroke = new BasicStroke(4);
    g2d.setStroke(stroke);
    g2d.drawLine(x +10, y + 10, x + width -10, y + height -10);
    g2d.drawLine(x +10, y + height -10, x + width -10, y + 10);

    g2d.dispose();

    return image;
  }

}
