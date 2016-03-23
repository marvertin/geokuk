package cz.geokuk.core.napoveda;

import java.awt.Dimension;
import java.util.List;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.text.html.HTMLEditorKit;

import cz.geokuk.framework.JMyDialog0;

public class JZpravyUzivatelumDialog extends JMyDialog0 {

	private static final long serialVersionUID = 5215923043342722378L;

	JScrollPane jZpravy;
	JButton jPrecteno;
	JButton jDalsi;
	JButton jPredchozi;

	private final List<ZpravaUzivateli> zpravyUzivatelum;
	private int ukazatel;
	private JEditorPane editorPane;

	private NapovedaModel napovedaModel;

	public JZpravyUzivatelumDialog(final List<ZpravaUzivateli> zpravyUzivatelum) {
		this.zpravyUzivatelum = zpravyUzivatelum;
		setTitle("Zprávy uživatelům");
		init();
	}

	public void inject(final NapovedaModel napovedaModel) {
		this.napovedaModel = napovedaModel;
		for (final ZpravaUzivateli zprava : zpravyUzivatelum) {
			if (zprava.msgnum >= napovedaModel.getLastViewedMsgNum()) { // rovnost raději ne, ale procházíme postupně
				ukazatel = zpravyUzivatelum.indexOf(zprava);
			}
		}
		ukazatel = 0; // když nenalezeno nic tak na začátek
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "ZpravyUzivatelum";
	}

	@Override
	protected void initComponents() {

		editorPane = new JEditorPane();
		editorPane.setEditable(false);

		editorPane.setEditorKit(new HTMLEditorKit());

		// Put the editor pane in a scroll pane.
		jZpravy = new JScrollPane(editorPane);
		jZpravy.setPreferredSize(new Dimension(600, 400));
		jZpravy.setMinimumSize(new Dimension(10, 10));

		jPrecteno = new JButton("Přečteno");
		jDalsi = new JButton(">>>");
		jPredchozi = new JButton("<<<");
		jZpravy.setAlignmentX(CENTER_ALIGNMENT);
		jPrecteno.setAlignmentX(CENTER_ALIGNMENT);
		final JPanel panel = new JPanel();
		add(panel);

		grlay(panel);
		naplndaty();

		jPrecteno.addActionListener(e -> {
			napovedaModel.setLastViewedMsgNum(zpravyUzivatelum.get(ukazatel).msgnum);
			dispose();
		});

		jPredchozi.addActionListener(e -> {
			// TODO Zapamoatovat si, že zprávy jsou přečteny
			ukazatel--;
			naplndaty();
		});

		jDalsi.addActionListener(e -> {
			// TODO Zapamoatovat si, že zprávy jsou přečteny
			ukazatel++;
			naplndaty();
		});

	}

	private void grlay(final JPanel panel) {
		final GroupLayout layout = new GroupLayout(panel);
		// panel.setBorder(BorderFactory.createTitledBorder("Nastvit nickVzorky popisků: " + SestavovacPopisku.getNahrazovaceDisplay()));
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// panel.add(jKesPatternEdit);
		// panel.add(jWaymarkPatternEdit);
		// panel.add(jCgpPatternEdit);
		// panel.add(jSimplewaypontPatternEdit);

		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER).addGroup(layout.createParallelGroup() // hroup
		        .addComponent(jZpravy).addGroup(layout.createSequentialGroup().addComponent(jPredchozi).addComponent(jDalsi).addComponent(jPrecteno))));
		layout.setVerticalGroup(layout.createSequentialGroup() // hroup
		        .addComponent(jZpravy).addGroup(layout.createParallelGroup().addComponent(jDalsi).addComponent(jPredchozi).addComponent(jPrecteno)));
	}

	private void naplndaty() {
		ukazatel = Math.min(Math.max(0, ukazatel), zpravyUzivatelum.size() - 1);
		editorPane.setText("<html>" + zpravyUzivatelum.get(ukazatel).text);
		jDalsi.setEnabled(ukazatel < zpravyUzivatelum.size() - 1);
		jPredchozi.setEnabled(ukazatel > 0);
	}

}
