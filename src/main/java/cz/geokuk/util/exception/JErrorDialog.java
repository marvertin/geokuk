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
 * TextFieldDemo.java requires one additional file:
 * content.txt
 */

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.*;

import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.util.process.BrowserOpener;

public class JErrorDialog extends JMyDialog0 {

	private static final long	serialVersionUID	= 7087453419069194768L;

	private JErrorTable			jErrorTable;
	private JButton				jVymaz;
	private JButton				jOtviracVyjimky;
	private AExcId				currentExcId;

	public JErrorDialog() {
		setTitle("Přehled problémů");
		init();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		registerEvents();

	}

	private void registerEvents() {
		// Board.eveman.registerWeakly(this);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */

	@Override
	protected void initComponents() {

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		add(panel);

		jErrorTable = new JErrorTable();
		panel.add(jErrorTable);

		jVymaz = new JButton("Vymaž vše");
		final Box box = Box.createHorizontalBox();
		panel.add(box, BorderLayout.SOUTH);

		jOtviracVyjimky = new JButton();
		jOtviracVyjimky.setEnabled(false);
		jOtviracVyjimky.setText("Není nic vybráno");

		box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		box.add(jOtviracVyjimky);
		box.add(Box.createHorizontalGlue());
		box.add(jVymaz);

		jVymaz.addActionListener(e -> {
			jErrorTable.getProblemList().clear();
			jErrorTable.revalidate();
			jErrorTable.repaint();
			jOtviracVyjimky.setText("Tabulka problemu vymazana");
			jOtviracVyjimky.setEnabled(false);
		});

		final ListSelectionModel lsm = jErrorTable.table.getSelectionModel();
		lsm.addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				final int row = lsm.getLeadSelectionIndex();
				currentExcId = jErrorTable.tableModel.getProblemlist().get(row).excId;
				if (currentExcId != null) {
					jOtviracVyjimky.setText("Zobraz " + currentExcId + "");
					jOtviracVyjimky.setEnabled(true);
				} else {
					jOtviracVyjimky.setText("Není výjimka");
					jOtviracVyjimky.setEnabled(false);
				}

			}
		});

		jOtviracVyjimky.addActionListener(aE -> {
			final URL url = FExceptionDumper.getExceptionUrl(currentExcId);
			BrowserOpener.displayURL(url);
		});

		pack();
	}

	public void addProblem(final String problem, final AExcId excid) {
		jErrorTable.addProblem(problem, excid);
		jErrorTable.repaint(50);
	}

	public static void main(final String args[]) {

		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.

		final JFrame frm = new JFrame();
		frm.setVisible(true);

		new Timer(1000, aE -> {
			System.out.println("LOPU");
			FError.report("To je teda texytik " + System.currentTimeMillis());
		}).start();

		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		// //Turn off metal's use of bold fonts
		// // UIManager.put("swing.boldMetal", Boolean.FALSE);
		// JErrorDialog jErrorDialog = new JErrorDialog(null);
		// jErrorDialog.setVisible(true);
		// for (int i = 0; i < 100; i++) {
		// AExcId excid = FExceptionDumper.dump(new RuntimeException("Jen takova pokusna"), EExceptionSeverity.DISPLAY, "Pokusnik.");
		// jErrorDialog.addProblem("Problem: " + i, excid);
		// }
		// }
		// });
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "ErrorList";
	}

}
