package cz.geokuk.core.render;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

public class JPapirMeritkoComboBox extends JComboBox<String> {

  private static final long serialVersionUID = -3121505662505169240L;

  private int naposledZadana;

  private static final int[] MERITKA = {10000, 15000, 20000, 25000, 50000, 100000 };

  public JPapirMeritkoComboBox() {
    setEditable(true);
    for (int mer : MERITKA) {
      addItem(formatuj(mer));
    }

    addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        // pokud zadal blbost tak tam dát,. co tam bylo
        if (event.getStateChange() == ItemEvent.SELECTED) {
          int mer = parser((String) getSelectedItem());
          setMeritko(mer);
        }
      }
    });
  }


  public void setMeritko(int mer) {
    if (mer == 0) return;
    naposledZadana = mer;
    setSelectedItem(formatuj(mer));
  }

  public int getMeritko() {
    return parser((String) getSelectedItem());
  }

  private String formatuj(int mer) {
    String result = String.format("1 : %,d", mer);
    return result;
  }

  private int parser(String s) {
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
      if (c == ':') {
        sb.setLength(0);
      }
      if (Character.isDigit(c)) {
        sb.append(c);
      }
    }
    try {
      return Integer.parseInt(sb.toString());
    } catch (NumberFormatException e) {
      // když to nejde, tak to nejde
      setMeritko(naposledZadana);
      return naposledZadana;
    }
  }


  @Override
  public Dimension getMaximumSize() {
    return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
  }
}
