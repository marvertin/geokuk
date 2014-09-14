/**
 * 
 */
package cz.geokuk.core.program;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;
import cz.geokuk.util.pocitadla.PocitadloNula;
import cz.geokuk.util.pocitadla.PocitadloRoste;
import cz.geokuk.util.pocitadla.SpravcePocitadel;
import cz.geokuk.util.pocitadla.SystemovaPocitadla;

/**
 * @author veverka
 *
 */
public class JServiceFrame extends JMyDialog0 implements Pocitadlo.Callback {

  private static final long serialVersionUID = 5761908785083097975L;


  private final Map<Pocitadlo, JLabel> hodmap = new WeakHashMap<>();

  public JServiceFrame() {
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    initComponents();
    pack();
    Pocitadlo.callback = this;
    registerEvents();

    // TODO při zavírání JServiceFramese musí zlikvidovat také odkaz v počítadlech
  }

  private void registerEvents() {
    // Zastavit počítání při uzavení okna
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        Pocitadlo.callback = null;
      }
      @Override
      public void windowClosed(WindowEvent e) {
        Pocitadlo.callback = null;
      }
    });
  }

  @Override
  public void finalize() {
    System.out.println("Finalizován JServiceFrmame");
  }

  @Override
  public void onChange() {
    SystemovaPocitadla.spustPocitani();
    List<Pocitadlo> pocitadla = new ArrayList<>(SpravcePocitadel.getPocitadla());
    //    initComponents();
    //    pack();
    if (hodmap == null || pocitadla.size() != hodmap.size()) {
      //System.out.println("NEsouhlasi pocet pocitadel a hodnot: " + pocitadla.size() + " == " + hodmap.size());
      initComponents(pocitadla);
      pack();
    }
    for (Pocitadlo pocitadlo : pocitadla) {
      JLabel jLabel = hodmap.get(pocitadlo);
      if (jLabel != null) {
        jLabel.setText(pocitadlo.get() + "");
      }
    }
  }


  protected void initComponents(List<Pocitadlo> pocitadla) {
    Box b = createtComponents(pocitadla);
    getContentPane().removeAll();
    getContentPane().add(b);
  }


  private Box createtComponents(List<Pocitadlo> pocitadla) {
    Box b;
    //System.out.println("INICOMP");
    hodmap.clear();
    b = Box.createVerticalBox();
    JPanel jGcPanel = createGcButton();
    b.add(jGcPanel);
    for (String typ : seznamTypu(pocitadla)) {
      b.add(nadpis(typ));
      JPanel panel = initJedenPanel(typ, pocitadla);
      b.add(panel);
      b.add(Box.createVerticalStrut(10));
    }
    return b;
  }

  private JPanel createGcButton() {
    final JPanel jGcPanel = new JPanel();
    final JLabel jMemoryPoGc = new JLabel();
    jGcPanel.add(jMemoryPoGc);
    final JButton gcbutt = new JButton("GC");
    jGcPanel.add(gcbutt);
    gcbutt.addActionListener(new ActionListener() {
      private long minulePouzitaPamet;

      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("Garbage collector spuštěn");
        System.gc();
        long pouzitaPamet = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long narustPameti = pouzitaPamet - minulePouzitaPamet;
        minulePouzitaPamet = pouzitaPamet;
        jMemoryPoGc.setText(pouzitaPamet/1000 + " KiB  |  rozdil=" + narustPameti/1000 + " KiB");
        System.out.println("Garbage collector ukončen");
      }
    });
    return jGcPanel;
  }


  private JTextField nadpis(String typ) {
    JTextField lbl = new JTextField(typ);
    lbl.setEditable(false);
    return lbl;
  }

  /**
   * 
   */
  private  JPanel initJedenPanel(String typPocitadla, List<Pocitadlo> pocitadla) {
    JPanel pan = new JPanel();
    pan.setBorder(BorderFactory.createEtchedBorder());
    pan.setLayout(new GridBagLayout());
    pan.removeAll();
    if (pocitadla == null || pocitadla.size() == 0) {
      pan.add(new JLabel("Nejsou pocitadla"));
      return pan;
    }
    GridBagConstraints c = new GridBagConstraints();
    int i = 0;
    //hodnoty.clear();
    for (Pocitadlo p : pocitadla) {
      if (!p.getTextovyPopisTypu().equals(typPocitadla)) {
        continue;
      }
      JLabel label = new JLabel(p.getName()+ ": ");
      label.setToolTipText(p.getDescription());
      JLabel value = new JLabel(p.get() + "");
      value.setToolTipText(p.getDescription());
      hodmap.put(p, value);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridy = i;
      c.gridx = 0;
      c.ipadx = 10;
      c.anchor = GridBagConstraints.LINE_START;
      pan.add(label, c);
      c.gridx = 1;
      c.anchor = GridBagConstraints.LINE_END;
      pan.add(value, c);
      i++;
    }
    return pan;
  }

  Set<String> seznamTypu(List<Pocitadlo> pocitadla) {
    Set<String> types = new HashSet<>();
    for (Pocitadlo p : pocitadla) {
      types.add(p.getTextovyPopisTypu());
    }
    //System.out.println(types);
    return types;
  }


  public static void main(String[] args) {
    JServiceFrame serviceFrame = new JServiceFrame();
    serviceFrame.setVisible(true);

    for (int i = 0; i < 12; i++) {
      Pocitadlo poc = null;
      if ( i % 3 == 0) {
        poc = new PocitadloRoste("pociA " + i, "Popis počítadla " + i);
      }
      if ( i % 3 == 1) {
        poc = new PocitadloMalo("pociB " + i, "Popis počítadla " + i);
      }
      if ( i % 3 == 2) {
        poc = new PocitadloNula("pociC " + i, "Popis počítadla " + i);
      }
      final Pocitadlo po = poc;
      final int ii = i;
      Thread thr = new Thread() {
        @Override
        public void run() {
          try {
            Thread.sleep(ii * 1000);
            for (;;) {
              po.add(ii);
              Thread.sleep((ii + 5) * 100);
            }
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      };
      thr.setDaemon(true);
      thr.start();

    }

  }

  @Override
  protected void initComponents() {
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return "Service";
  }

}
