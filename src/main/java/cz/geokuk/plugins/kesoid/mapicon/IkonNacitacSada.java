package cz.geokuk.plugins.kesoid.mapicon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cz.geokuk.util.file.KeyNode;
import cz.geokuk.util.file.LamUrl;

public class IkonNacitacSada {

  private static final String SKLA_TXT = "skla.txt";
  private static final String GROUPS_PROPERTIES = "groups.properties";

  private final Genom genom;
  private final Properties groupDisplayNames = new Properties();

  public IkonNacitacSada(Genom genom) {
    this.genom = genom;
  }

  public Sada loadSada(KeyNode<String, LamUrl> nodeSada) throws IOException {
    loadGroupDisplayNames(nodeSada);


    KeyNode<String, LamUrl> sadyTxt = nodeSada.locate(SKLA_TXT);
    Sada sada = new Sada(nodeSada.getData().name);
    if (sadyTxt == null) {
      error("Nenalezena definice skel " + SKLA_TXT + " v " + nodeSada);
      return null;
    }

      try (BufferedReader br = new BufferedReader(new InputStreamReader(sadyTxt.getData().url.openStream()))) {
          String line;
          Map<String, Sklo> nactenaSkla = new HashMap<>();
          while ((line = br.readLine()) != null) {
              line = line.trim();
              if (line.length() == 0) continue;
              String[] dvoj = line.split("\\.", 2);
              String skloName = dvoj[0];
              String aplikaceSklaName = dvoj[1];

              SkloAplikant skloAplikant = new SkloAplikant();
              skloAplikant.aplikaceSkla = Enum.valueOf(EAplikaceSkla.class, aplikaceSklaName);

              Sklo sklo = nactenaSkla.get(skloName);
              if (sklo == null) {
                  KeyNode<String, LamUrl> skloNode = nodeSada.locate(skloName);
                  if (skloNode == null || skloNode.getData() == null) {
                      error("Nenalezeno sklo " + skloName + " referencing from " + sadyTxt.getData().url);
                      continue;
                  }
                  sklo = loadSklo(skloNode);
                  nactenaSkla.put(skloName, sklo);
              }
              skloAplikant.sklo = sklo;
              sada.skloAplikanti.add(skloAplikant);
          }
      }
    return sada;
  }

  /**
   * @param nodeSada
   * @throws IOException
   */
  private void loadGroupDisplayNames(KeyNode<String, LamUrl> nodeSada) throws IOException {
    KeyNode<String, LamUrl> gropNamesNode = nodeSada.locate(GROUPS_PROPERTIES);
    if (gropNamesNode != null) {
        try (InputStream stm = gropNamesNode.getData().url.openStream()) {
            groupDisplayNames.load(stm);
        }
    }
  }

  private Sklo loadSklo(KeyNode<String, LamUrl> skloNode) {
    Sklo sklo = new Sklo(skloNode.getData().name);
    for (KeyNode<String, LamUrl> vrstvaNode : skloNode.getSubNodes()) {
      Vrstva vrstva = loadVrstva(vrstvaNode, sklo);
      sklo.vrstvy.add(vrstva);
    }
    return sklo;
  }

  private Vrstva loadVrstva(KeyNode<String, LamUrl> vrstvaNode, ImagantCache imagantCache) {
    Vrstva vrstva = new Vrstva();
    for (Map.Entry<String, KeyNode<String, LamUrl>> entry : vrstvaNode.getItems().entrySet()) {
      IconDefNacitac idn = new IconDefNacitac(genom, entry.getKey(),  entry.getValue().getData().url, this);
      IconDef iconDef = idn.loadIconDef(imagantCache);
      vrstva.add(iconDef);
    }
    return vrstva;
  }

  private void error(String errstr) {
    System.err.println(errstr);
  }

  String getGroupDisplayName(String groupName) {
    return groupName == null ? null : groupDisplayNames.getProperty(groupName);
  }


}
