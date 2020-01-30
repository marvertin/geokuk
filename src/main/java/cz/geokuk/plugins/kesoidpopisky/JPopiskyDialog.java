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

package cz.geokuk.plugins.kesoidpopisky;

/*
 * TextFieldDemo.java requires one additional file:
 * content.txt
 */

import java.awt.BorderLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import lombok.Data;

public class JPopiskyDialog extends JMyDialog0 implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = 7087453419069194768L;

//	private JTextField jKesPatternEdit;
//	private JTextField jWaymarkPatternEdit;
//	private JTextField jCgpPatternEdit;
//	private JTextField jSimplewaypontPatternEdit;

//	private final JLabel jKesPatternLabel = new JLabel("Keš:");
//	private final JLabel jWaymarkPatternLabel = new JLabel("Waymark:");
//	private final JLabel jCgpPatternLabel = new JLabel("Czech geodetic point:");
//	private final JLabel jSimplewaypontPatternLabel = new JLabel("Simple waypoint:");

	private PopiskyModel popiskyModel;

	private final KesoidPluginManager kesoidPluginManager;

	private JVlastnostiPisma jVlastnostiPisma = new JVlastnostiPisma();

	private List<Radek> jradky;

	public JPopiskyDialog(final KesoidPluginManager kesoidPluginManager) {
		setTitle("Nastavení paramtrů popisek keší na mapě");
		this.kesoidPluginManager = kesoidPluginManager;
		init();
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
	 */
	@Override
	public void initAfterEventReceiverRegistration() {
		registerEvents();
	}

	public void inject(final PopiskyModel popiskyModel) {
		this.popiskyModel = popiskyModel;
	}

	public void onEvent(final PopiskyPreferencesChangeEvent event) {
		final PopiskySettings pose = event.pose;

		final VlastnostiPismaModel vlastnostiPismaModel = jVlastnostiPisma.getVlastnostiPismaModel();
		vlastnostiPismaModel.setBackground(pose.background);
		vlastnostiPismaModel.setForeground(pose.foreground);
		vlastnostiPismaModel.setPosuX(pose.getPosuX());
		vlastnostiPismaModel.setPosuY(pose.getPosuY());
		vlastnostiPismaModel.setFont(pose.getFont());

		jradky.forEach(r -> {
			setPattern(r.jtext, pose.getPatterns2().get(r.kepodr));
		});
	}


	@Override
	protected String getTemaNapovedyDialogu() {
		return "PopiskyKesoidu";
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	@Override
	protected void initComponents() {
		jradky = kesoidPluginManager.getPopisekDefMap().entrySet().stream()
				.map(po -> new Radek(po.getKey(), new JLabel(po.getValue().getLabel()), new JTextField(), new JLabel(po.getValue().geHelpNahrazovace())))
				.collect(Collectors.toList());

		jVlastnostiPisma = new JVlastnostiPisma();

		final JPanel pan = new JPanel(new BorderLayout());
		pan.add(jVlastnostiPisma);
		final JPanel patternPan = new JPanel();
		grlay(patternPan);
		pan.add(patternPan, BorderLayout.NORTH);

		add(pan);
		pack();
	}

	private void grlay(final JPanel panel) {
		final GroupLayout layout = new GroupLayout(panel);
		// FIXME řešit nápovědu popisků
		panel.setBorder(BorderFactory.createTitledBorder("Vzorky popisků: TODO"));
		// + SestavovacPopisku.getNahrazovaceDisplay()));
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);


		{
			final SequentialGroup sg = layout.createSequentialGroup();
			{
				final ParallelGroup pg = layout.createParallelGroup();
				jradky.forEach(r -> pg.addComponent(r.jlabel));
				sg.addGroup(pg);
			}
			{
				final ParallelGroup pg = layout.createParallelGroup();
				jradky.forEach(r -> pg.addComponent(r.jtext));
				sg.addGroup(pg);
			}
			{
				final ParallelGroup pg = layout.createParallelGroup();
				jradky.forEach(r -> pg.addComponent(r.jhelpyNahrazovacu));
				sg.addGroup(pg);
			}
			layout.setHorizontalGroup(sg);
		}
		{
			final SequentialGroup sg = layout.createSequentialGroup();
			jradky.forEach(r -> sg.addGroup(layout.createParallelGroup() // h1
					.addComponent(r.jlabel)
					.addComponent(r.jtext)
					.addComponent(r.jhelpyNahrazovacu)));
			layout.setVerticalGroup(sg);
		}


//
//		layout.setHorizontalGroup(layout.createSequentialGroup() // hroup
//				.addGroup(layout.createParallelGroup() // h1
//						.addComponent(jKesPatternLabel)
//						.addComponent(jWaymarkPatternLabel)
//						.addComponent(jCgpPatternLabel)
//						.addComponent(jSimplewaypontPatternLabel))
//				.addGroup(layout.createParallelGroup() // h1
//						.addComponent(jKesPatternEdit)
//						.addComponent(jWaymarkPatternEdit)
//						.addComponent(jCgpPatternEdit)
//						.addComponent(jSimplewaypontPatternEdit)));
//		layout.setVerticalGroup(layout.createSequentialGroup() // hroup
//				.addGroup(layout.createParallelGroup() // h1
//						.addComponent(jKesPatternLabel)
//						.addComponent(jKesPatternEdit))
//				.addGroup(layout.createParallelGroup() // h1
//						.addComponent(jWaymarkPatternLabel).addComponent(jWaymarkPatternEdit))
//				.addGroup(layout.createParallelGroup() // h1
//						.addComponent(jCgpPatternLabel).addComponent(jCgpPatternEdit))
//				.addGroup(layout.createParallelGroup() // h1
//						.addComponent(jSimplewaypontPatternLabel).addComponent(jSimplewaypontPatternEdit)));
	}

	@Data
	private static class Radek {
		final Kepodr kepodr;
		final JLabel jlabel;
		final JTextField jtext;
		final JLabel jhelpyNahrazovacu;
	}
	// private JPanel obalRameckem(JComponent com, String title) {
	// }
	/**
	 *
	 */
	private void registerEvents() {

		jVlastnostiPisma.getVlastnostiPismaModel().addChangeListener(e -> {
			final VlastnostiPismaModel vlastnostiPismaModel = jVlastnostiPisma.getVlastnostiPismaModel();
			final PopiskySettings data = popiskyModel.getData();
			data.background = vlastnostiPismaModel.getBackground();
			data.foreground = vlastnostiPismaModel.getForeground();
			data.font = vlastnostiPismaModel.getFont();
			data.posuX = vlastnostiPismaModel.getPosuX();
			data.posuY = vlastnostiPismaModel.getPosuY();
			popiskyModel.setData(data);
		});

		jradky.forEach(r -> {
			r.getJtext().getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(final DocumentEvent e) {
					zmena();
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					zmena();
				}

				@Override
				public void removeUpdate(final DocumentEvent e) {
					zmena();
				}

				private void zmena() {
					final PopiskySettings data = popiskyModel.getData();
					data.getPatterns2().put(r.getKepodr(), r.getJtext().getText());
					popiskyModel.setData(data);
				}
			});
		});
	}


	private void setPattern(final JTextField jField, final String pattern) {
		if (!jField.getText().equals(pattern)) {
			jField.setText(pattern);
		}

	}

}
