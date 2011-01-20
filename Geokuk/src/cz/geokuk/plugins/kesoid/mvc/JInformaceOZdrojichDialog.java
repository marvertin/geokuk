package cz.geokuk.plugins.kesoid.mvc;

import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.importek.InformaceOZdroji;
import cz.geokuk.util.lang.ATimestamp;

public class JInformaceOZdrojichDialog extends JMyDialog0 implements AfterEventReceiverRegistrationInit {

  private static final long serialVersionUID = 5215923043342722378L;

  private JTable jTable;

  //private final InformaceOZdrojich iInformaceOZdrojich;

  private KesoidModel kesoidModel;

  private KesBag vsechny;

  public JInformaceOZdrojichDialog() {
    setTitle("Přehled zdrojhů kešoidů");
  }

  @Override
  protected void initComponents() {
    Box box = Box.createVerticalBox();

    jTable = new JTable(new Model());
    //jTable.setPreferredScrollableViewportSize(new Dimension(600, 70));
    jTable.setFillsViewportHeight(true);

    //Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(jTable);


    nastavVlastnostiSLoupcu();
    box.add(scrollPane);
    add(box);
  }

  public void nastavVlastnostiSLoupcu() {
    TableColumn column;
    column = jTable.getColumnModel().getColumn(0);
    //column.setMaxWidth(200);
    column.setMinWidth(20);
    column.setPreferredWidth(50);
    column.setResizable(false);
    column.setMaxWidth(50);


    column = jTable.getColumnModel().getColumn(1);
    //column.setMaxWidth(200);
    column.setMinWidth(200);
    column.setPreferredWidth(200);
    column.setResizable(true);

    column = jTable.getColumnModel().getColumn(2);
    column.setMaxWidth(200);
    column.setMinWidth(200);
    column.setPreferredWidth(200);
    column.setResizable(false);

    column = jTable.getColumnModel().getColumn(3);
    column.setMaxWidth(50);
    column.setMinWidth(50);
    column.setPreferredWidth(50);
    column.setResizable(false);

    column = jTable.getColumnModel().getColumn(4);
    column.setMaxWidth(50);
    column.setMinWidth(50);
    column.setPreferredWidth(50);
    column.setResizable(false);

  }


  private class Model extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    //    private final KesBag vsechny2;
    //    /**
    //     *
    //     */
    //    public Model(KesBag vsechny) {
    //      vsechny2 = vsechny;
    //    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
      return 5;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
      return vsechny == null ? 0 : vsechny.getInformaceOZdrojich().getSourceCount();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int col) {
      if (vsechny == null) return null;
      InformaceOZdroji ioz = vsechny.getInformaceOZdrojich().get(row);
      Object r = null;
      switch (col) {
      case 0: r = kesoidModel.maSeNacist(ioz.jmenoZDroje); break;
      case 1: r = ioz.jmenoZDroje; break;
      case 2: r = ATimestamp.from(ioz.lastModified).toIsoStringLocal(); break;
      case 3: r = ioz.pocetWaypointuBranych; break;
      case 4: r = ioz.pocetWaypointuCelkem; break;
      }
      return r;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex == 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int col) {
      String r = "xxx";
      switch (col) {
      case 0: r = "Načíst"; break;
      case 1: r = "Zdroj"; break;
      case 2: r = "Čas změny"; break;
      case 3: r = "Waypointů braných"; break;
      case 4: r = "Waypointů celkem"; break;
      }
      return r;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int col) {
      Class<?> r = String.class;
      switch (col) {
      case 0: r = Boolean.class; break;
      case 1: r = String.class; break;
      case 2: r = String.class; break;
      case 3: r = String.class; break;
      case 4: r = String.class; break;
      }
      return r;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex == 0) {
        kesoidModel.setNacitatSoubor(vsechny.getInformaceOZdrojich().get(rowIndex).jmenoZDroje, (Boolean) aValue);
      }
    }
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }

  public void onEvent(KeskyNactenyEvent event) {
    vsechny = event.getVsechny();
    invalidate();
    if (jTable != null) jTable.repaint();
    pack();
  }

  public void onEvent(KesoidUmisteniSouboruChangedEvent event) {
    setTitle("Přehled zdrojů kešoidů: \""+event.getUmisteniSouboru().getKesDir().getEffectiveFile()+ "\"");
  }


  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    init();
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return "ZdrojeKesoidu";
  }

}
