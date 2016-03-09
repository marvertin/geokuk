package cz.geokuk.plugins.mapy.kachle;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumMap;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloRoste;


public class KachloDownloader {

    private final Pocitadlo pocitDownloadleDlazdice = new PocitadloRoste("Downloadlé dlaždice",
            "Počet dlaždic, které byly downloadovány.");

    private static final Logger log =
            LogManager.getLogger(KachloDownloader.class.getSimpleName());

    private KachleModel kachleModel;



    private final EnumMap<EPraznyObrazek, Image> prazdneObrazky = new EnumMap<>(EPraznyObrazek.class);

    public KachloDownloader() {
    }

    public String[] getServers() {
        return new String[]{"m1", "m2", "m3", "m4"};
    }

    public ImageWithData downloadImage(KaOne kaOne, String server) {
        URL url = null;
        try {
            if (kaOne.getType() == EKaType._BEZ_PODKLADU)
                return prazdnyObrazekBezDat(EPraznyObrazek.BEZ_PODKLADU); // v offline módu jen vyprazdňuji frontu
            if (!kachleModel.isOnlineMode())
                return prazdnyObrazekBezDat(EPraznyObrazek.OFFLINE); // v offline módu jen vyprazdňuji frontu
            url = kaOne.getType().getUrlBuilder().buildUrl(kaOne);
            ImageWithData imda = new ImageWithData();
            DataHoldingInputStream dhis = new DataHoldingInputStream(url.openStream());
            InputStream stm = new BufferedInputStream(dhis);
            imda.img = ImageIO.read(stm);
            stm.close();
            imda.data = dhis.getData();
            pocitDownloadleDlazdice.inc();
            //Thread.sleep(300);
            return imda;
        } catch (Exception e) {
            log.error("Unable to download tile image! \"" + url + "\"", e);
            return prazdnyObrazekBezDat(EPraznyObrazek.ERROR);
        }
    }


    /**
     * @return
     */
    private ImageWithData prazdnyObrazekBezDat(EPraznyObrazek typPrazdneho) {
        ImageWithData imda = new ImageWithData();
        imda.data = null; // tím, že nevrátíme bytová data, neuloží se ta napodobenina na disk
        imda.img = getOfflineImage(typPrazdneho);
        return imda;
    }

    /**
     *
     */
    private synchronized Image getOfflineImage(EPraznyObrazek typPrazdnehoObrazku) {
        // TODO Offline image by měl být průhledný a rozhodně s lepší grafikou.

        Image image = prazdneObrazky.get(typPrazdnehoObrazku);
        InputStream istm = null;
        if (image == null) {
            try {
                istm = getClass().getResourceAsStream(typPrazdnehoObrazku.getRecourceName());
                if (istm != null) {
                    image = ImageIO.read(istm);
                }
            } catch (IOException e) {
                log.error("Nelze nacist prazdny obrazek! " + typPrazdnehoObrazku.getRecourceName(), e);
            } finally {
                if (istm != null) {
                    try {
                        istm.close();
                    } catch (IOException e) {
                        log.error("Nelze zavrit stream pro " + typPrazdnehoObrazku.getRecourceName(), e);
                    }
                }
            }
            if (image == null) {
                // K tomu pravděpodobně nedojde, ale co když ,tak vyplníme nesmyslem
                image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.setColor(new Color(128, 128, 128, 128));
                g.fillOval(30, 30, 196, 196);
            }

            prazdneObrazky.put(typPrazdnehoObrazku, image);
        }
        return image;
    }

    public void inject(KachleModel kachleModel) {
        this.kachleModel = kachleModel;
    }


    /**
     * Nepřejmenovávat hodnoty, odvozuje se z něj název resourcu
     */
    private enum EPraznyObrazek {
        OFFLINE, ERROR, BEZ_PODKLADU;

        /**
         * @return
         */
        public String getRecourceName() {
            return name() + ".png";
        }
    }
}
