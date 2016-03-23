package cz.geokuk.plugins.mapy.stahovac;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;

public class JKachleOflinerPocetStazenychDialog extends JMyDialog0 implements AfterEventReceiverRegistrationInit {

	private static final long	serialVersionUID	= 7180968190465321695L;

	private JLabel				pocetStazenychKachli;

	public JKachleOflinerPocetStazenychDialog() {
		setTitle("Průběh hrromadného dotažení mapových dlaždic");
		init();
	}

	@Override
	public void initAfterEventReceiverRegistration() {

	}

	public void setPocetStazenych(final int pocet) {
		pocetStazenychKachli.setText("Požadováno ke stažení jž " + pocet + " dlaždic");
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "StahovaniMapovychDlazdic";
	}

	@Override
	protected void initComponents() {
		// Napřed registrovat, aby při inicializaci už byl výsledek tady
		final Box box = Box.createVerticalBox();
		pocetStazenychKachli = new JLabel();
		box.add(Box.createVerticalStrut(20));
		pocetStazenychKachli.setAlignmentX(CENTER_ALIGNMENT);
		pocetStazenychKachli.setPreferredSize(new Dimension(400, 30));
		box.add(pocetStazenychKachli);
		box.add(Box.createVerticalStrut(20));
		pack();
		add(box);
	}

}
