package cz.geokuk.core.lookandfeel;

/*
 * @(#)SwingSet2.java	1.54 06/05/31
 */

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.metal.*;

import cz.geokuk.framework.MyPreferences;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;

/**
 * A demo that shows all of the Swing components.
 *
 * @version 1.54 05/31/06
 * @author Jeff Dinkins
 */
public class LafSupport {

	static class ChangeLookAndFeelAction extends AbstractAction {
		private static final long	serialVersionUID	= -910826756747137014L;
		private final LafItem		laf;

		protected ChangeLookAndFeelAction(final LafItem laf) {
			super("ChangeTheme");
			this.laf = laf;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setLookAndFeel(laf);
		}
	}

	static class ChangeThemeAction extends AbstractAction {
		private static final long	serialVersionUID	= -910826756747137014L;
		MetalTheme					theme;

		protected ChangeThemeAction(final MetalTheme theme) {
			super("ChangeTheme");
			this.theme = theme;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			MetalLookAndFeel.setCurrentTheme(theme);
			updateLookAndFeel();
		}
	}

	private static class LafItem {
		UIManager.LookAndFeelInfo	info;
		JMenuItem					mi;

	}

	private static final String		LOOK_AND_FEEL	= "lookAndFeel";

	private static final String		metal			= "javax.swing.plaf.metal.MetalLookAndFeel";
	private static LafItem			current;
	private static LafItem			metalli;
	private static JMenu			lafMenu			= null;

	private static JMenu			themesMenu		= null;
	private static ButtonGroup		lafMenuGroup	= new ButtonGroup();

	private static ButtonGroup		themesMenuGroup	= new ButtonGroup();

	private static List<LafItem>	lafitems		= new ArrayList<>();

	// Used only if swingset is an application
	private static JFrame			frame;

	static {
		createLafList(); // vytvořit seznam a vybrat aktuální
		updateLookAndFeel();
		createLafMenu();
	}

	public static JMenu getLafMenu() {
		return lafMenu;
	}

	// *******************************************************
	// ****************** Utility Methods ********************
	// *******************************************************

	public static void setFrame(final JFrame frame) {
		LafSupport.frame = frame;
	}

	/**
	 * Sets the current L&F on each demo module
	 */
	public static void updateLookAndFeel() {
		try {
			UIManager.setLookAndFeel(current.info.getClassName());
			updateThisSwingSet();
			MyPreferences.current().put(LOOK_AND_FEEL, current.info.getClassName());
		} catch (final Exception ex) {
			FExceptionDumper.dump(ex, EExceptionSeverity.WORKARROUND, "Failed loading L&F: " + current.info.getClassName());
		}
	}

	private static void createLafList() {

		final String prefferencedLookAndFeel = getPrefferencedLookAndFeel();
		final UIManager.LookAndFeelInfo[] lafInfos = UIManager.getInstalledLookAndFeels();
		for (final UIManager.LookAndFeelInfo lafInfo : lafInfos) {
			final LafItem li = new LafItem();
			li.info = lafInfo;
			if (lafInfo.getClassName().equals(prefferencedLookAndFeel)) {
				current = li;
			}
			System.out.println("LookAndFeel: " + lafInfo.getName() + "  " + lafInfo.getClassName());
			if (lafInfo.getClassName().equals(metal)) {
				metalli = li;
			}
			lafitems.add(li);
		}
		if (current == null) {
			current = metalli; // ten musí být vždy
		}
	}

	private static void createLafMenu() {
		final JMenuItem miMetal = null;
		// Create these menu items for the first SwingSet only.
		// ***** create laf switcher menu
		lafMenu = new JMenu("Skin");
		lafMenu.setMnemonic('N');

		for (final LafItem li : lafitems) {
			li.mi = lafMenu.add(new JRadioButtonMenuItem(li.info.getName()));
			lafMenuGroup.add(li.mi);
			li.mi.addActionListener(new ChangeLookAndFeelAction(li));
			if (li == current) {
				li.mi.setSelected(true);
			}
		}

		// ***** create themes menu
		themesMenu = new JMenu("Témata");
		themesMenu.setMnemonic('T');

		// *** now back to adding color/font themes to the theme menu
		final JMenuItem mi = createThemesMenuItem(themesMenu, "Ocean", 'O', new OceanTheme());
		mi.setSelected(miMetal != null && miMetal.isSelected()); // This is the default theme

		createThemesMenuItem(themesMenu, "Steel", 'S', new DefaultMetalTheme());
		createThemesMenuItem(themesMenu, "Aqua", 'A', new AquaTheme());
		createThemesMenuItem(themesMenu, "Charcoal", 'C', new CharcoalTheme());

		createThemesMenuItem(themesMenu, "High Contrast", 'H', new ContrastTheme());

		createThemesMenuItem(themesMenu, "Emerald", 'E', new EmeraldTheme());
		createThemesMenuItem(themesMenu, "Ruby", 'R', new RubyTheme());

		lafMenu.addSeparator();
		lafMenu.add(themesMenu);

		themesMenu.setEnabled(current == metalli);
	}

	/**
	 * Creates a JRadioButtonMenuItem for the Themes menu
	 */
	private static JMenuItem createThemesMenuItem(final JMenu menu, final String label, final char mnemonic, final MetalTheme theme) {
		final JRadioButtonMenuItem mi = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(label));
		themesMenuGroup.add(mi);
		mi.addActionListener(new ChangeThemeAction(theme));
		mi.setMnemonic(mnemonic);
		return mi;
	}

	private static String getPrefferencedLookAndFeel() {
		try {
			final String s = MyPreferences.current().get(LOOK_AND_FEEL, null);
			if (s == null) {
				return metal;
			}
			return s;
		} catch (final Throwable e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Když nejde nastavit skin, tak nejde!");
			return metal;
		}
	}

	/**
	 * Stores the current L&F, and calls updateLookAndFeel, below
	 */
	private static void setLookAndFeel(final LafItem li) {
		if (current == li) {
			return;
		}
		current = li;
		current.mi.setSelected(true);
		themesMenu.setEnabled(current == metalli);

		/*
		 * The recommended way of synchronizing state between multiple controls that represent the same command is to use Actions. The code below is a workaround and will be replaced in future version of SwingSet2 demo.
		 */
		updateLookAndFeel();
	}

	private static void updateThisSwingSet() {
		if (frame != null) {
			SwingUtilities.updateComponentTreeUI(frame);
		}
	}

	/**
	 * SwingSet2 Constructor
	 */
	private LafSupport() {}
}
