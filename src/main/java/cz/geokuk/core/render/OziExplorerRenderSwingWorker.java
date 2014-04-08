package cz.geokuk.core.render;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Dlg;

public class OziExplorerRenderSwingWorker extends RendererSwingWorker0 {

  private final EWhatRender whatRender;

  OziExplorerRenderSwingWorker(EWhatRender whatRender) {
    this.whatRender = whatRender;
  }

  @Override
  protected RenderResult doInBackground() throws Exception {
    progressor.setMax(Rendrovadlo.KOLIK_PROGRESUJEME_NA_KACHLICH);
    File dir = renderModel.getOutputFolder();

    EImageType imageType = renderModel.getRenderSettings().getImageType();

    //TODO správně by se parametry měly spočítat v konstruktoru, aby se nemohly v rendermodelu změnit
    Rendrovadlo rendrovadlo = factory.init(new Rendrovadlo(this));
    RenderParams p = new RenderParams();
    p.roord = renderModel.getRoord();
    p.natacetDoSeveru = renderModel.getRenderSettings().isSrovnatDoSeveru();
    //p.resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    // TODO typ obrázku musí být určen podle toho, zda se rendruje JPG nebo PNG.
    String imageFileName = renderModel.getRenderSettings().getPureFileName().getText();
    progressor.setText("Rendrování " + imageFileName);
    dir.mkdirs();
    String imageShortName = imageFileName + "." + imageType;
    File imagePathName = new File(dir, imageShortName);
    File mapPathName = new File(dir, imageFileName + ".map");
    if (! Dlg.prepsatSoubor(imagePathName)) return null;
    if (whatRender == EWhatRender.OZI_EXPLORER) {
      if (! Dlg.prepsatSoubor(mapPathName)) return null;
    }

    progressor.setTooltip("Probíhá rendrování pro " + whatRender + " do soubor: \"" + imagePathName + "\"");
    p.pruhledne = imageType.isUmoznujePruhlednost();

    try {
      BufferedImage image = rendrovadlo.rendruj(p, progressor);
      //    renderModel.vypisChybySouradnic(cocox.getPixluNaMetr());

      System.out.printf("Zapis obrazku [%d,%d] do souboru \"%s\"%n", image.getWidth(), image.getHeight(), imagePathName);
      ImageIO.write(image, imageType.getType(), imagePathName);

      File vytvorenySoubor;
      if (whatRender == EWhatRender.OZI_EXPLORER) {
        int width = p.roord.getDim().width;
        int height = p.roord.getDim().height;
        Coord cocox = p.roord;
        PrintWriter pwrt = new PrintWriter(mapPathName);
        List<Wgs> kalibody = renderModel.spocitejKalibracniBody();
        printOziMetafile(pwrt, imageShortName, width, height, cocox, renderModel.getRenderSettings().getKalibrBodu(), kalibody);
        pwrt.close();
        vytvorenySoubor = mapPathName;
      } else {
        vytvorenySoubor = imagePathName;
      }
      System.out.println("Konec rendrovani");

      RenderResult result = new RenderResult();
      result.file = vytvorenySoubor;
      System.out.println("Konecc OZI rendrování");
      return result;
    } catch (Exception e) {
      try {  imagePathName.delete(); } catch (Exception e1) { e.fillInStackTrace(); }
      try {  mapPathName.delete(); } catch (Exception e1) {e.fillInStackTrace(); }
      throw e;
    }
  }


  private void printOziMetafile(PrintWriter p, String fileName, int width, int height, Coord cocox, int kalibrBodu, List<Wgs> kalibody) {
    Wgs sz = cocox.transform(new Point(0,0)).toWgs();
    Wgs sv = cocox.transform(new Point(width,0)).toWgs();
    Wgs jz = cocox.transform(new Point(0,height)).toWgs();
    Wgs jv = cocox.transform(new Point(width,height)).toWgs();



    p.println("OziExplorer Map Data File Version 2.2");
    p.println(fileName);
    p.println(fileName);
    p.println("1 ,Map Code,");
    p.println("WGS 84,,   0.0000,   0.0000,WGS 84");
    p.println("Reserved 1             ");
    p.println("Reserved 2");
    p.println("Magnetic Variation,,,E");
    p.println("Map Projection,Latitude/Longitude,PolyCal,No,AutoCalOnly,No,BSBUseWPX,No");
    int i = 0;
    for (Wgs kalibod : kalibody) {
      i++;
      Point point = cocox.transform(kalibod.toMou());
      printOziKalibracniBod(p, i , point.x, point.y, kalibod);
    }
    //    printOziKalibracniBod(p, 1, 0, height, jz);
    //    printOziKalibracniBod(p, 2, width, 0, sv);
    p.println("Projection Setup,,,,,,,,,,");
    p.println("Map Feature = MF ; Map Comment = MC     These follow if they exist");
    p.println("Track File = TF      These follow if they exist");
    p.println("Moving Map Parameters = MM?    These follow if they exist");
    p.println("MM0,Yes");
    p.println("MMPNUM,4");
    p.println("MMPXY,1,0,0");
    p.printf("MMPXY,2,%d,0%n", width);
    p.printf("MMPXY,3,%d,%d%n", width, height);
    p.printf("MMPXY,4,0,%d%n",height);
    p.printf(Locale.ENGLISH, "MMPLL,1,  %f,%f%n", sz.lon, sz.lat);
    p.printf(Locale.ENGLISH, "MMPLL,2,  %f,%f%n", sv.lon, sv.lat);
    p.printf(Locale.ENGLISH, "MMPLL,3,  %f,%f%n", jv.lon, jv.lat);
    p.printf(Locale.ENGLISH, "MMPLL,4,  %f,%f%n", jz.lon, jz.lat);

  }


  private void printOziKalibracniBod(PrintWriter p, int cisloBodu, int x, int y, Wgs wgs) {
    int latStup = (int) Math.floor(wgs.lat);
    int lonStup = (int) Math.floor(wgs.lon);
    double latMinut = (wgs.lat - latStup) * 60;
    double lonMinut = (wgs.lon - lonStup) * 60;
    p.printf(Locale.ENGLISH, "Point%02d,xy,%d,%d,in, deg,%d,%10.3f,N,%d, %10.3f,E, grid,,,,N%n",
        cisloBodu, x, y, latStup,  latMinut,  lonStup, lonMinut);

  }

  //  OziExplorer Map Data File Version 2.2
  //  Maršovsko.bmp
  //  Maršovsko.bmp
  //  1 ,Map Code,
  //  WGS 84,,   0.0000,   0.0000,WGS 84
  //  Reserved 1
  //  Reserved 2
  //  Magnetic Variation,,,E
  //  Map Projection,Latitude/Longitude,PolyCal,No,AutoCalOnly,No,BSBUseWPX,No
  //  Point01,xy,   27,  615,in, deg,  49,  15.421,N,  16,  18.341,E, grid,   ,           ,           ,N
  //  Point02,xy,  884,   49,in, deg,  49,  17.798,N,  16,  24.056,E, grid,   ,           ,           ,N
  //  Projection Setup,,,,,,,,,,
  //  Map Feature = MF ; Map Comment = MC     These follow if they exist
  //  Track File = TF      These follow if they exist
  //  Moving Map Parameters = MM?    These follow if they exist
  //  MM0,Yes
  //  MMPNUM,4
  //  MMPXY,1,0,0
  //  MMPXY,2,1185,0
  //  MMPXY,3,1185,671
  //  MMPXY,4,0,671
  //  MMPLL,1,  16.302682,  49.300063
  //  MMPLL,2,  16.434388,  49.300063
  //  MMPLL,3,  16.434388,  49.253097
  //  MMPLL,4,  16.302682,  49.253097

}