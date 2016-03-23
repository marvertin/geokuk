package cz.geokuk.core.render;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

public class JPapirMeritkoComboBox extends JComboBox<String> {

	private static final long	serialVersionUID	= -3121505662505169240L;

	private int					naposledZadana;

	private static final int[]	MERITKA				= { 10000, 15000, 20000, 25000, 50000, 100000 };

	public JPapirMeritkoComboBox() {
		setEditable(true);
		for (final int mer : MERITKA) {
			addItem(formatuj(mer));
		}

		addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent event) {
				// pokud zadal blbost tak tam dát,. co tam bylo
				if (event.getStateChange() == ItemEvent.SELECTED) {
					final int mer = parser((String) getSelectedItem());
					setMeritko(mer);
				}
			}
		});
	}

	public void setMeritko(final int mer) {
		if (mer == 0) {
			return;
		}
		naposledZadana = mer;
		setSelectedItem(formatuj(mer));
	}

	public int getMeritko() {
		return parser((String) getSelectedItem());
	}

	private String formatuj(final int mer) {
		final String result = String.format("1 : %,d", mer);
		return result;
	}

	private int parser(final String s) {
		final StringBuilder sb = new StringBuilder();
		for (final char c : s.toCharArray()) {
			if (c == ':') {
				sb.setLength(0);
			}
			if (Character.isDigit(c)) {
				sb.append(c);
			}
		}
		try {
			return Integer.parseInt(sb.toString());
		} catch (final NumberFormatException e) {
			// když to nejde, tak to nejde
			setMeritko(naposledZadana);
			return naposledZadana;
		}
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
	}
}
