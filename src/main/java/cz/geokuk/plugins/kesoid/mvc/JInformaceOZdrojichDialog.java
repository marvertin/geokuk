package cz.geokuk.plugins.kesoid.mvc;

import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.importek.InformaceOZdroji;

public class JInformaceOZdrojichDialog extends JMyDialog0 implements AfterEventReceiverRegistrationInit {

	private class Model extends AbstractTreeTableModel {
		public Model() {
			super(vsechny.getInformaceOZdrojich().getRoot());
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(final int col) {
			Class<?> r = String.class;
			switch (col) {
			case 1:
				r = Boolean.class;
				break;
			case 2:
				r = String.class;
				break;
			case 3:
				r = Integer.class;
				break;
			case 4:
				r = Integer.class;
				break;
			}
			return r;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 4;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(final int col) {
			String r = "";
			switch (col) {
			case 0:
				r = "Zdroj";
				break;
			case 1:
				r = "Načíst";
				break;
			case 2:
				r = "WP braných";
				break;
			case 3:
				r = "WP celkem";
				break;
			}
			return r;
		}

		@Override
		public Object getChild(final Object parent, final int index) {
			final InformaceOZdroji p = (InformaceOZdroji) parent;
			return p.getChildren().get(index);
		}

		@Override
		public int getChildCount(final Object parent) {
			return ((InformaceOZdroji) parent).getChildren().size();
		}

		@Override
		public int getIndexOfChild(final Object parent, final Object child) {
			return ((InformaceOZdroji) parent).getChildren().indexOf(child);
		}

		@Override
		public Object getValueAt(final Object o, final int i) {
			if (o == null) {
				return null;
			}
			final InformaceOZdroji ioz = (InformaceOZdroji) o;

			switch (i) {
			case 0:
				return vsechny.getInformaceOZdrojich().getRoot() == ioz ? "1:" + ioz.jmenoZdroje.getFile().getAbsolutePath() : ioz.getDisplayName();
			case 1:
				return kesoidModel.maSeNacist(ioz.jmenoZdroje);
			case 2:
				return ioz.getPocetWaypointuBranychSDetmi();
			case 3:
				return ioz.getPocetWaypointuCelkemSDetmi();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(final Object node, final int column) {
			return column == 1;
		}

		@Override
		public void setValueAt(final Object value, final Object node, final int col) {
			final InformaceOZdroji ioz = (InformaceOZdroji) node;
			kesoidModel.setNacitatSoubor(ioz.jmenoZdroje, (Boolean) value);
		}
	}

	private static final long serialVersionUID = 5215923043342722378L;

	// private final InformaceOZdrojich iInformaceOZdrojich;

	private JXTreeTable jTable;

	private KesoidModel kesoidModel;

	private KesBag vsechny;

	public JInformaceOZdrojichDialog() {
		setTitle("Přehled zdrojů kešoidů");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
	 */
	@Override
	public void initAfterEventReceiverRegistration() {
		init();
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void nastavVlastnostiSloupcu() {
		TableColumn column;

		column = jTable.getColumnModel().getColumn(0);
		column.setMinWidth(200);
		column.setPreferredWidth(200);
		column.setResizable(true);

		column = jTable.getColumnModel().getColumn(1);
		// column.setMaxWidth(200);
		column.setMinWidth(20);
		column.setPreferredWidth(50);
		column.setResizable(true);
		column.setMaxWidth(100);

		column = jTable.getColumnModel().getColumn(2);
		column.setMaxWidth(100);
		column.setMinWidth(50);
		column.setPreferredWidth(100);
		column.setResizable(true);

		column = jTable.getColumnModel().getColumn(3);
		column.setMaxWidth(100);
		column.setMinWidth(50);
		column.setPreferredWidth(100);
		column.setResizable(true);

	}

	public void onEvent(final KeskyNactenyEvent event) {
		vsechny = event.getVsechny();
		invalidate();
		if (jTable != null) {
			jTable.repaint();
		}
		pack();
	}

	public void onEvent(final KesoidUmisteniSouboruChangedEvent event) {
		// Není to pravda, když jich míme více
		// setTitle("Přehled zdrojů kešoidů: \""+event.getUmisteniSouboru().getKesDir().getEffectiveFile()+ "\"");
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "ZdrojeKesoidu";
	}

	@Override
	protected void initComponents() {
		final Box box = Box.createVerticalBox();

		jTable = new JXTreeTable(new Model());
		// jTable.setPreferredScrollableViewportSize(new Dimension(600, 70));
		jTable.setFillsViewportHeight(true);

		// Create the scroll pane and add the table to it.
		final JScrollPane scrollPane = new JScrollPane(jTable);

		nastavVlastnostiSloupcu();
		box.add(scrollPane);
		add(box);
	}

}
