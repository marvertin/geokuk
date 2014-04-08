package cz.geokuk.plugins.kesoid.mapicon;

import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import cz.geokuk.api.mapicon.Imagant;

public class IkonBag {

  private Set<String> jmenaSad;
  private Sada sada;
  private final Genom genom = new Genom();
  private Map<ASada, Icon> iJmenaAIkonySad;

  public Set<String> getSadyNames() {
    return jmenaSad;
  }

  public void setJmenaSad(Set<String> jmenaSad) {
    this.jmenaSad = jmenaSad;
  }

  public void setSada(Sada sada) {
    this.sada = sada;
  }

  public Sada getSada() {
    return sada;
  }

  public void print() {
    System.out.println("Sady: " + jmenaSad);
    for (SkloAplikant skloAplikant : sada.skloAplikanti) {
      System.out.println("   *** sklo: " + skloAplikant.aplikaceSkla);
      //      for (Vrstva vrstva : skloAplikant.sklo.vrstvy) {
      System.out.println("        ### vrstva");
      //        for (IconDef iconDef : vrstva.getAllIconDefs()) {
      //          System.out.println("            " + iconDef.getSubdefs() + " ::: " + iconDef.idp.url);
      //        }
      //      }
    }
  }

  public Genom getGenom() {
    return genom;
  }

  public Sklivec getSklivec (Genotyp genotyp) {
    return sada.getSklivec(genotyp);

  }

  public Icon seekIkon(Genotyp genotyp) {
    Sklivec sklivec = getSklivec(genotyp);
    Imagant imagant = Sklo.prekresliNaSebe(sklivec.imaganti);
    if (imagant == null) return null;
    return new ImageIcon(imagant.getImage());
  }


  /**
   * @param aNactiIkonySad
   */
  public void setJmenaAIkonySad(Map<ASada, Icon> aJmenaAIkonySad) {
    iJmenaAIkonySad = aJmenaAIkonySad;
  }

  /**
   * @return the jmenaAIkonySad
   */
  public Map<ASada, Icon> getJmenaAIkonySad() {
    return iJmenaAIkonySad;
  }


}
