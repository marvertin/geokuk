package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;

public class JDebugIkonyDialog extends JMyDialog0 implements AfterInjectInit {

	private static final long	serialVersionUID	= -6496737194139718970L;
	private JComponent			jvse;
	private JComponent			jskelneikony;

	private JDebugVyberIkon		debugVyberIkon;

	public JDebugIkonyDialog() {
		setTitle("Ladění ikon");
		init();
	}

	@Override
	protected void initComponents() {
		jvse = Box.createHorizontalBox();
		jvse.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(jvse);

		jskelneikony = Box.createVerticalBox();
		jskelneikony.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		jskelneikony.setMinimumSize(new Dimension(250, 150));

		debugVyberIkon = new JDebugVyberIkon(jskelneikony);
		jvse.add(debugVyberIkon);
		jvse.add(Box.createHorizontalStrut(5));
		jvse.add(jskelneikony);
		jvse.setPreferredSize(new Dimension(700, 600));
	}

	public void onEvent(final IkonyNactenyEvent event) {
		debugVyberIkon.resetBag(event.getBag());
		jvse.revalidate();
	}

	@Override
	public void initAfterInject() {
		factory.init(debugVyberIkon);
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "DebugIkon";
	}

}
