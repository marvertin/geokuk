package cz.geokuk.plugins.kesoid.mapicon;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;

import cz.geokuk.util.gui.FComponent;
import cz.geokuk.util.lang.CounterMap;

public abstract class JVyberIkon0 extends Box {

  private static final long serialVersionUID = -6496737194139718970L;
  private JComponent jvyber1;
  private JComponent jvyber2;
  private final boolean iOdskrtnutiVybira;
  private final boolean iRadioButton;

  public JVyberIkon0(boolean aRadioButton, boolean aOdskrtnutiVybira) {
    super(BoxLayout.LINE_AXIS);
    iRadioButton = aRadioButton;
    iOdskrtnutiVybira = aOdskrtnutiVybira;
    initComponents();
  }

  protected void initComponents() {

    jvyber1 = Box.createVerticalBox();
    JScrollPane sp1 = new JScrollPane(jvyber1);
    sp1.setViewportBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    //    sp1.setAutoscrolls(true);
    FComponent.enableMouseSroll(jvyber1);

    jvyber2 = Box.createVerticalBox();
    JScrollPane sp2 = new JScrollPane(jvyber2);
    sp2.setViewportBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    FComponent.enableMouseSroll(sp2);

    add(sp1);
    add(Box.createHorizontalStrut(5));
    add(sp2);
    
  }


  protected final void refresh(final IkonBag bag, Set<String> aJmenaVybranychAlel, CounterMap<Alela> aPoctyVybranychAlel) {
  //	onInitRefreshing();
  	
    final Genom genom = bag.getGenom();
    final Set<String> jmenaVybranychAlel = new HashSet<>(aJmenaVybranychAlel);
    jvyber1.removeAll();
    jvyber2.removeAll();
    final Set<Alela> vybraneAlely  = new HashSet<>();

    for (Gen gen : genom.getGeny()) {
      ButtonGroup bg = new ButtonGroup();
      if (shouldRender(gen)) {
        //      if (pouziteGeny.contains(gen)) {
        //System.out.println("Nas gen: " + gen.getDisplayName());
        for (Grupa grupa : gen.getGrupy().values()) {
          Box boxgen = Box.createVerticalBox();
          boxgen.setBorder(BorderFactory.createTitledBorder(grupa.isOther() ? gen.getDisplayName() : grupa.getDisplayName()));
          for (Alela alela : grupa.getAlely()) {
            final Alela alelax = alela;
            // To tady musí být proto, že už na začátku se musí vytvořit instance
            // filtru, aby se dostaly ikony na toolbár, ale toto vytvoření pro moc ikon dost
            // dlouho trvá, tak se to tady optimalizuje.
            if (shouldRender(alelax)) {
              //System.out.println("Nase alela: " + agen.getDisplayName());
              final Icon ikona = najdiIkonu(alela, bag);
              JComponent radiosikonou = Box.createHorizontalBox();
              JLabel ikonilajbl = new JLabel(ikona);
              //ikonilajbl.setAlignmentX(LEFT_ALIGNMENT);
              radiosikonou.add(ikonilajbl);
              final JToggleButton rb = createToggleButton();
              if (alela.name().equals(alela.getDisplayName()) || ! iRadioButton) {
                if (aPoctyVybranychAlel != null) {
                  rb.setText("<html>" + alela.getDisplayName() + "  (<i>" + aPoctyVybranychAlel.count(alela) + "</i>)" );
                } else {
                  rb.setText(alela.getDisplayName());
                }
              } else {
                rb.setText("<html>" + alela.getDisplayName() + "  (<b><tt>" + alela.name() + "</tt></b>)" );
              }
              rb.setEnabled(shouldEnable(alelax));
              radiosikonou.add(rb);
              radiosikonou.add(Box.createHorizontalGlue());
              if (iRadioButton) bg.add(rb);
              boxgen.add(radiosikonou);
              if (jmenaVybranychAlel.contains(alela.name())) {
                rb.setSelected(!iOdskrtnutiVybira);
                vybraneAlely.add(alela);
              } else {
                rb.setSelected(iOdskrtnutiVybira);
              }
              // nastavit listener
              rb.getModel().addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                  if (e.getStateChange() == ItemEvent.DESELECTED ^ iOdskrtnutiVybira) {
                    vybraneAlely.remove(alelax);
                  }
                  if (e.getStateChange() == ItemEvent.SELECTED ^ iOdskrtnutiVybira) {
                    vybraneAlely.add(alelax);
                  }
                  // jen když máme správný počet alel
                  if (!iRadioButton || genom.getGeny().size() == vybraneAlely.size()) {
                    zmenaVyberu(vybraneAlely);
                  }
                  //System.out.println(alelax + " -- " + );
                }
              });
            }
          }
          if (boxgen.getComponentCount() > 0) {
            (genom.getSymGen() == gen ? jvyber1 : jvyber2).add(boxgen);
          }
        } 
        if (iRadioButton && bg.getSelection() == null) {
          bg.getElements().nextElement().setSelected(true);
        }
      } else {
        if (iRadioButton) vybraneAlely.add(gen.getVychoziAlela());
      }
    }
    //    }
    zmenaVyberu(vybraneAlely);
    
    repaint();
    //onDoneRefreshing();

  }

//  /** PRovede s před refreshem, aby se mohly připravit keše */
//  public abstract void onInitRefreshing(); 
//	  
//
//  /** PRovede s po refreshi, aby se mohlo uklidit */
//  public abstract void onDoneRefreshing();

	/**
   * @return
   */
  private JToggleButton createToggleButton() {
    return iRadioButton ? new JRadioButton() : new JCheckBox();
  }

  /**
   * @param aAlelax
   * @return
   */
  protected abstract boolean shouldRender(Alela aAlela);
  /**
   * @param aGen
   * @return
   */
  protected abstract boolean shouldRender(Gen aGen);

  protected abstract boolean shouldEnable(Alela alela);

  protected abstract void zmenaVyberu(Set<Alela> aVybraneAlely);

  private Icon najdiIkonu(Alela alela, IkonBag bag) {
    Genotyp genotypProAlelu = bag.getGenom().getGenotypProAlelu(alela);
    //genotypProAlelu.put(bag.getGenom().ALELA_H);
    return bag.seekIkon(genotypProAlelu);
  }


}
