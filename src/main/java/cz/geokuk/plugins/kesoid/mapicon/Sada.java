package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.plugins.kesoid.Repaintanger;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp.Otisk;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;
import cz.geokuk.util.pocitadla.PocitadloRoste;

public class Sada {

  private Pocitadlo pocitSklivcu = new PocitadloMalo("Sklivce - počet", "Kolik vlastně máme typů konkrétních vzhledů ikon");
  private static Pocitadlo pocitSklivcuZasah = new PocitadloRoste("Sklivce - zásah cache", "");

  List<SkloAplikant> skloAplikanti = new ArrayList<>();

  Map<Genotyp.Otisk, Sklivec> cache = new HashMap<>();

  private final String name;

  private Repaintanger repaintanger = new Repaintanger();

  private Set<Alela> pouziteAlely;
  private Set<Gen> pouziteGeny;
  private Icon icon;



  public Sada(String name) {
    this.name = name;
  }


  /**
   * @return the skloAplikanti
   */
  public List<SkloAplikant> getSkloAplikanti() {
    return Collections.unmodifiableList(skloAplikanti);
  }


  /**
   * Vrací sklivec pro daný genotyp s tím, že se hrabe v keši,
   * aby se jednak zvýšila rychnlost, druhak, aby se šetřila paměť 
   * 
   * @param genotyp
   * @return
   */
  public synchronized Sklivec getSklivec (Genotyp genotyp) {
    zuzNaObrazkove(genotyp); // aby se nekešovalo pro alely, ke kterým nic nemáme
    Otisk otisk = genotyp.getOtisk();
		Sklivec sklivec = cache.get(otisk);
    if (sklivec == null) {
      sklivec = new Sklivec();
      for (SkloAplikant skloAplikant : skloAplikanti) {
        sklivec.imaganti.add(getRenderedImage(genotyp, skloAplikant));
      }
      cache.put(otisk, sklivec);
      repaintanger.include(sklivec);
      pocitSklivcu.set(cache.size());
      //System.out.println("REPREPA: " + repaintanger);
    } else {
      pocitSklivcuZasah.inc();
    }
    return sklivec;
  }

  private void zuzNaObrazkove(Genotyp genotyp) {
    Set<Alela> pouziteAlely2 = getPouziteAlely();
    for (Alela alela : new ArrayList<>(genotyp.getAlely())) {
      if (! pouziteAlely2.contains(alela)) {
      	if (! pouziteAlely2.contains(alela.getGen().getVychoziAlela())) {
      		genotyp.remove(alela);
      	}
      }
    }
  }


  public Insets getBigiestIconInsets() {
    return repaintanger.getInsets();
  }

  /**
   * @param genotyp
   * @param skloAplikant
   * @return
   */
  private Imagant getRenderedImage(Genotyp genotyp, SkloAplikant skloAplikant) {
    Imagant imagant = skloAplikant.sklo.getRenderedImage(genotyp);
    return imagant;
  }


  public String getName() {
    return name;
  }

  /**
   * Vrátí všechny alely, které jsou použity a podle nichž se dá něco zobrazit.
   */
  public Set<Alela> getPouziteAlely() {
    if (pouziteAlely == null) {
      Set<Alela> alely = new HashSet<>();
      for (SkloAplikant skloAplikant : skloAplikanti) {
        for (Vrstva vrstva : skloAplikant.sklo.vrstvy) {
          		alely.addAll(vrstva.getPouziteAlely());
        }
      }
      pouziteAlely = alely;
    }
    return pouziteAlely;
  }

  public Set<Gen> getPouziteGeny() {
    if (pouziteGeny == null) {
      Set<Gen> geny = new HashSet<>();
      Set<Alela> pouziteAlely = getPouziteAlely();
      for (Alela alela : pouziteAlely) {
        geny.add(alela.getGen());
      }
      pouziteGeny = geny;
    }
    return pouziteGeny;
  }


  /**
   * @param aImageIcon
   */
  public void setIcon(Icon icon) {
    this.icon = icon;
  }


  /**
   * @return the icon
   */
  public Icon getIcon() {
    return icon;
  }

 

}
