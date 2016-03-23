/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.geokuk.util.exception;

/*
 * TableDemo.java requires no other files.
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * TableDemo is just like SimpleTableDemo, except that it uses a custom TableModel.
 */
public class JErrorTable extends JPanel {
	class MyTableModel extends AbstractTableModel {
		private static final long	serialVersionUID	= -1777521413836209700L;

		private final List<Problem>	problemlist			= new ArrayList<>();

		private final String[]		columnNames			= { "Závažnost", "Výjimka", "Popis",

															};

		/*
		 * JTable uses this method to determine the default renderer/ editor for each cell. If we didn't implement this method, then the last column would contain text ("true"/"false"), rather than a check box.
		 */
		@Override
		public Class<?> getColumnClass(final int col) {
			Class<?> cls = null;
			switch (col) {
			case 0:
				cls = String.class;
				break;
			case 1:
				cls = Integer.class;
				break;
			case 2:
				cls = String.class;
				break;
			}
			return cls;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(final int col) {
			return columnNames[col];
		}

		/**
		 * @return the keslist
		 */
		public List<Problem> getProblemlist() {
			return problemlist;
		}

		@Override
		public int getRowCount() {
			return problemlist.size();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			final Problem nalezenec = problemlist.get(row);
			Object s = null;
			switch (col) {
			case 0:
				s = row + 1;
				break;
			// case 0: s = nalezenec.getZavaznost(); break;
			case 1:
				s = nalezenec.getExcId();
				break;
			case 2:
				s = nalezenec.getPopis();
				break;
			}
			return s;
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		@Override
		public boolean isCellEditable(final int row, final int col) {
			return false;
		}

	}

	static class Problem {
		String	zavaznost;
		String	popis;
		AExcId	excId;

		public AExcId getExcId() {
			return excId;
		}

		public String getPopis() {
			return popis;
		}

		public String getZavaznost() {
			return zavaznost;
		}

		public void setExcId(final AExcId excId) {
			this.excId = excId;
		}

		public void setPopis(final String popis) {
			this.popis = popis;
		}

		public void setZavaznost(final String zavaznost) {
			this.zavaznost = zavaznost;
		}

	}

	private static final long	serialVersionUID	= 7687619215661046034L;

	MyTableModel				tableModel;

	JTable						table;

	public JErrorTable() {
		super(new GridLayout(1, 0));

		tableModel = new MyTableModel();

		table = new JTable(tableModel);
		table.setPreferredScrollableViewportSize(new Dimension(500, 700));
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Create the scroll pane and add the table to it.
		final JScrollPane scrollPane = new JScrollPane(table);

		// Add the scroll pane to this panel.
		add(scrollPane);
		nastavVlastnostiSLoupcu();
		// table.setDefaultRenderer(AExcId.class, new TableCellRenderer() {
		//
		// @Override
		// public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow, int aColumn) {
		// JButton jButton = new JButton(aValue == null ? "nuull" : aValue.toString());
		// jButton.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent aE) {
		// System.out.println("aaaaaaaaaaaaaaaaaaaaaaa");
		//
		// }
		// });
		// return jButton;
		// }
		// });

	}

	public void addListSelectionListener(final ListSelectionListener listener) {
		table.getSelectionModel().addListSelectionListener(listener);
	}

	public void addProblem(final String problem, final AExcId excId) {
		final Problem pbm = new Problem();
		pbm.setPopis(problem);
		pbm.setExcId(excId);
		tableModel.getProblemlist().add(pbm);

	}

	public Problem getCurrent() {
		final int selectedRow = table.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= tableModel.getProblemlist().size()) {
			return null;
		}
		final Problem nalezenec = tableModel.getProblemlist().get(selectedRow);
		return nalezenec;
	}

	/**
	 * @return the keslist
	 */
	public List<Problem> getProblemList() {
		return tableModel.getProblemlist();
	}

	private void nastavVlastnostiSLoupcu() {
		TableColumn column;
		column = table.getColumnModel().getColumn(0);
		column.setMaxWidth(30);
		// column.setResizable(false);

		column = table.getColumnModel().getColumn(1);
		column.setMaxWidth(130);
		// column.setResizable(false);

		column = table.getColumnModel().getColumn(2);
		// column.setMaxWidth(25);

		// column = table.getColumnModel().getColumn(3);
		// column.setMaxWidth(100);
		// column.setResizable(false);
		//
		// column = table.getColumnModel().getColumn(4);
		// column.setPreferredWidth(400);
		//
		// column = table.getColumnModel().getColumn(5);
		// column.setPreferredWidth(100);
	}
}
