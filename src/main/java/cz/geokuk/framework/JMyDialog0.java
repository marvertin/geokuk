package cz.geokuk.framework;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public abstract class JMyDialog0 extends JDialog {

	private class CancelAction extends AbstractAction {
		private static final long serialVersionUID = -4843379055570361691L;

		public CancelAction() {
			super("Zavřít");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			dispose();
		}
	}

	private static final long		serialVersionUID	= 1L;
	private static final Object		CANCEL_ACTION		= "kanclujToVsecko";
	protected Factory				factory;
	private EventManager			eventManager;
	private final JFrame			frame;

	private final NapovedaAction	napovedaAction		= new NapovedaAction("Dialog/" + getTemaNapovedyDialogu());

	public JMyDialog0() {
		super(Dlg.parentFrame());
		frame = Dlg.parentFrame();
		assert frame != null;
	}

	public void inject(final EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public void inject(final Factory factory) {
		this.factory = factory;
		factory.init(napovedaAction);
	}

	public void odregistrujUdalosti() {
		odregistrujUdalosti(this);

	}

	protected abstract String getTemaNapovedyDialogu();

	protected final void init() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final JButton jZavri = new JButton("Zavřít");
		final JButton jNapoveda = new JButton("Nápověda");
		final CancelAction cancelAction = new CancelAction();
		jZavri.setAction(cancelAction);
		jNapoveda.setAction(napovedaAction);
		final JPanel jDolniCudly = new JPanel();
		jDolniCudly.add(jZavri);
		if (getTemaNapovedyDialogu() != null) {
			jDolniCudly.add(jNapoveda);
		}
		add(jDolniCudly, BorderLayout.SOUTH);
		initComponents();
		pack();

		final Toolkit toolkit = getToolkit();
		final Dimension screenSize = toolkit.getScreenSize();
		final Dimension frmSize = frame.getSize();
		final Dimension mySize = getSize();
		final int xo = (frmSize.width - mySize.width) / 2;
		final int yo = (frmSize.height - mySize.height) / 2;
		final Point loc = frame.getLocation();
		final Point p = new Point(loc.x + xo, loc.y + yo);
		p.x = Math.max(Math.min(p.x, screenSize.width - mySize.width), 0);
		p.y = Math.max(Math.min(p.y, screenSize.height - mySize.height), 0);
		final Dimension novy = new Dimension(Math.min(mySize.width, screenSize.width - p.x), Math.min(mySize.height, screenSize.height - p.y)

		);
		setLocation(p);
		setSize(novy);

		final InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap am = getRootPane().getActionMap();
		im.put(KeyStroke.getKeyStroke("ESCAPE"), CANCEL_ACTION);
		am.put(CANCEL_ACTION, cancelAction);

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"), "napoveda");
		getRootPane().getActionMap().put("napoveda", napovedaAction);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent aE) {
				if (eventManager != null) { // jen když byl tento dialog registrován a tak má event managera
					eventManager.unregister(this);
					odregistrujUdalosti();
				}
			}
		});

	}

	protected abstract void initComponents();

	private void odregistrujUdalosti(final Component comp) {
		eventManager.unregister(this);
		if (comp instanceof Container) {
			final Container container = (Container) comp;
			for (final Component c : container.getComponents()) {
				odregistrujUdalosti(c);
			}
		}
	}
}
