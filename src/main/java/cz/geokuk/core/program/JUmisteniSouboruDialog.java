package cz.geokuk.core.program;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;

public class JUmisteniSouboruDialog extends JMyDialog0 implements AfterInjectInit {

	private static final long serialVersionUID = 7180968190465321695L;
	static final java.awt.Dimension VYCHOZI_VELIKOST = new java.awt.Dimension(900, 700);
	static final java.awt.Dimension NEJMENSI_VELIKOST = new java.awt.Dimension(640, 420);
	private JPrehledSouboru jPrehledSoubor;

	public JUmisteniSouboruDialog() {
		setTitle("Umístění souborů");
		init();
		nastavVelikost(VYCHOZI_VELIKOST, NEJMENSI_VELIKOST);
	}

	public void fokusni(final ESouborPanelName panelName) {
		jPrehledSoubor.fokusni(panelName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterInjectInit#initAfterInject()
	 */
	@Override
	public void initAfterInject() {
		factory.init(jPrehledSoubor);
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "UmisteniSouboru";
	}

	@Override
	protected void initComponents() {
		jPrehledSoubor = new JPrehledSouboru(null);
		// add(new JScrollPane(jPrehledSoubor));
		add(jPrehledSoubor);
	}

}
