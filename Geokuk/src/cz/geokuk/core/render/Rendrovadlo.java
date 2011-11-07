/**
 * 
 */
package cz.geokuk.core.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Future;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coord.SlideListProvider;
import cz.geokuk.framework.Dlg;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.Progressor;
import cz.geokuk.plugins.mapy.kachle.JKachlovnikRendrovaci;

/**
 * @author veverka
 *
 */
public class Rendrovadlo {

  private static int citac;
  static final int KOLIK_PROGRESUJEME_NA_KACHLICH = 10000;

  // Používané služby
  private Factory factory;

  // pracovní oblast


  // parametry
  private List<JSingleSlide0> slides;
  private final Future<?> future;

  public Rendrovadlo(Future<?> future) {
    this.future = future;
  }

  public synchronized BufferedImage rendruj(RenderParams p, Progressor progressor) throws InterruptedException  {

    System.out.printf("Vytvarim obrazek [%d,%d]\n", p.roord.getWidth(), p.roord.getHeight());
    BufferedImage image = createImage(p);
    synchronized (image) {
      System.out.printf("Vytvoren obrazek [%d,%d]\n", image.getWidth(), image.getHeight());
      Graphics ggOriginal = image.getGraphics();
      Graphics gg = ggOriginal.create();
      gg.setColor(Color.CYAN);
      gg.fillOval(0, 0, image.getWidth(), image.getHeight());
      System.out.printf("Vybarvern obrazek [%d,%d]\n", image.getWidth(), image.getHeight());
      rendruj(p, progressor, ggOriginal);
    }
    return image;
  }

  public void rendruj(RenderParams p, Progressor progressor,
      Graphics ggOriginal) throws InterruptedException {
    //resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);


    //    Graphics g = p.resultImagex.getGraphics();
    //    rendrujPsanicko(g, Color.MAGENTA);

    // a teď projet směrem nahoru
    //postahujAVyrendrujKachleMap();

    // TODO řešit kanclování
    //if (isCancelled()) return null;


    System.out.println("Rendrovani rendrovadlem spusteno: " +  ++citac);
    for (JSingleSlide0 slidePuvodni : slides) {
      if (slidePuvodni.isVisible()) {
        //          for (int i=0; i<100; i++) {
        //            slidePuvodni.createRenderableSlide();
        //          }
        JSingleSlide0 slide = slidePuvodni.createRenderableSlide();
        if (slide != null) {
          ////          if (slide instanceof JKachlovnikRendrovaci) {
          ////            continue;
          //          }
          //          if (slide instanceof JKesoidy) {
          //            continue;
          //          }
          System.out.println("    RENDROVANI: " + slide.getClass());
          Graphics2D g = (Graphics2D) ggOriginal.create();
          Coord coco = p.roord;
          switch(slide.jakOtacetProRendrovani()) {
          case COORD:
            break;
          case GRAPH2D:
            coco = coco.derive(0.0);
            if (p.natacetDoSeveru) {
              g.translate(coco.getWidth() /2, coco.getHeight() / 2);
              g.rotate(- p.roord.computNataceciUhel());
              g.translate(-coco.getWidth() /2, -coco.getHeight() / 2);
            }
            break;
          default:
            coco = coco.derive(0.0);
          }
          factory.init(slide);
          slide.setSoord(coco);
          slide.setSize(coco.getWidth(), coco.getHeight());
          nastavProgressorKachlovniku(slide, progressor);
          slide.render(g);
        }
      }
    }
  }


  private void nastavProgressorKachlovniku(JSingleSlide0 slide, final Progressor progressor) {

    final int progressPocatek = progressor.getProgress();
    //final Progressor prgs = progressModel.start(500, "Rendrování map");
    if (slide instanceof JKachlovnikRendrovaci) {
      JKachlovnikRendrovaci kach = (JKachlovnikRendrovaci) slide;
      kach.setProgressor(new JKachlovnikRendrovaci.Progressor() {

        @Override
        public void setProgress(int value, int maxlue) {
          //System.out.println("PROGRESEK: " + value + "/" + maxlue);
          //prgs.setMax(maxlue);

          if (maxlue != 0) {
            // Může se stát, že bude načten obrázek nějaké dlaždice ještě dřív, než budou vydány požadavky na všechny dlaždice,
            // v tom případě se neví, kolik máme dlaždic a přijde nula. Lepší je pak neprogresovat.
            progressor.setProgress(progressPocatek + value * KOLIK_PROGRESUJEME_NA_KACHLICH / maxlue);
          }
          //progressor.setText("Mapiska " + citac);
        }
      });

    }
  }


  public void inject(Factory factory) {
    this.factory = factory;
  }

  public void inject(SlideListProvider slideListProvider) {
    slides = slideListProvider.getSlides();
  }

  BufferedImage createImage(RenderParams p) throws InterruptedException {
    @SuppressWarnings("unused") // opravdu není potřeba ten objekt použít, jen se zruší, aby byla paměť
    Object ucpavka = new byte[1024*1024*20]; // na ucpani pameti, aby po naalokovani obrazku ještě neco bylo
    try {
      BufferedImage result = new BufferedImage(p.roord.getWidth(), p.roord.getHeight(), p.pruhledne ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_3BYTE_BGR);
      ucpavka = null;
      return result;
    } catch (OutOfMemoryError e) {
      ucpavka = null;
      Dlg.error("Nedostatek operační paměti pro rendrování. Zmenši obrázek nebo přidej paměť pro Java Heap");
      future.cancel(true);
      return null;
      //throw new InterruptedException("Není paměť: " + e.toString());
    }
  }

}
