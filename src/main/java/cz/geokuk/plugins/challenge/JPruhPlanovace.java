package cz.geokuk.plugins.challenge;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import cz.geokuk.core.napoveda.UkazatelVykonu;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JSlide0;
import cz.geokuk.framework.RozmisteniPrekryvu;

/**
 * Pruh plánovače challenge vlevo nahoře na mapě: typ, počet chybějících, filtr „jen kandidáti“ a přechod na předchozí a další chybějící. Je vidět jen při zapnutém plánovači;
 * mimo svůj obdélník nezachytává myš. Při malé šířce mapy se zkracuje na tři stupně podle {@link RozmisteniPrekryvu#sirkaPruhu}.
 */
public class JPruhPlanovace extends JSlide0 implements AfterInjectInit {

	private static final long serialVersionUID = 1L;

	private static final int PODSTAVEC = 4;
	private static final int MEZERA = 4;
	private static final int VYSKA_PRVKU = 20;
	private static final int SIRKA_TYPU = 44;
	private static final int SIRKA_SIPKY = 24;
	private static final Color CERVENA = new Color(200, 40, 40);

	/** Pořadí prvků v poli z {@link #rozloz}. */
	static final int TYP = 0, ODZNAK = 1, KANDIDATI = 2, ZPET = 3, DAL = 4;

	private final transient JLabel typ = new JLabel("360°", SwingConstants.CENTER);
	private final transient Odznak odznak = new Odznak();
	private final transient JToggleButton kandidati = new JToggleButton();
	private final transient JButton zpet = new JButton(new Sipka(true));
	private final transient JButton dal = new JButton(new Sipka(false));
	private transient ChallengeModel model;
	private transient PlanovacChallengeSluzba sluzba;
	private transient Runnable naKlikOdznaku;

	public JPruhPlanovace() {
		setOpaque(false);
		setLayout(null);
		kandidati.setIcon(new Trychtyr());
		kandidati.setText("jen kandidáti");
		kandidati.setToolTipText("Zobrazit jen kandidáty, kteří zaplní chybějící sektor");
		zpet.setToolTipText("Předchozí chybějící");
		dal.setToolTipText("Další chybějící");
		odznak.setToolTipText("Chybějící sektory");
		typ.setToolTipText("Typ challenge");
		typ.setForeground(Color.DARK_GRAY);
		for (final AbstractButton b : new AbstractButton[] { odznak, kandidati, zpet, dal }) {
			b.setFocusable(false);
			b.setMargin(new Insets(0, 2, 0, 2));
			add(b);
		}
		add(typ);
		kandidati.addActionListener(e -> prepniKandidaty());
		zpet.addActionListener(e -> prejdi(false));
		dal.addActionListener(e -> prejdi(true));
		odznak.addActionListener(e -> {
			if (naKlikOdznaku != null) {
				naKlikOdznaku.run();
			}
		});
		setVisible(false);
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void inject(final PlanovacChallengeSluzba sluzba) {
		this.sluzba = sluzba;
	}

	/** Co se stane po kliknutí na odznak (popup s podrobnostmi). */
	public void setNaKlikOdznaku(final Runnable naKlikOdznaku) {
		this.naKlikOdznaku = naKlikOdznaku;
	}

	@Override
	public void initAfterInject() {
		obnov();
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		obnov();
	}

	/**
	 * Obdélníky prvků pruhu v souřadnicích mapy pro danou šířku pruhu: typ, odznak, filtr, předchozí, další. Prvek, který se na tomto stupni neukazuje, má prázdný obdélník; zbytek šířky
	 * dostane filtr, na nejužším stupni odznak.
	 */
	static Rectangle[] rozloz(final int sirkaPruhu) {
		final Rectangle[] r = new Rectangle[5];
		final int y = RozmisteniPrekryvu.OKRAJ + (RozmisteniPrekryvu.VYSKA_PRUHU - VYSKA_PRVKU) / 2;
		final boolean uzky = sirkaPruhu <= RozmisteniPrekryvu.STUPNE_PRUHU[RozmisteniPrekryvu.STUPNE_PRUHU.length - 1];
		final int odznakZaklad = 40;
		int x = RozmisteniPrekryvu.OKRAJ + PODSTAVEC;
		final int konec = RozmisteniPrekryvu.OKRAJ + sirkaPruhu - PODSTAVEC;
		r[TYP] = new Rectangle(x, y, uzky ? 0 : SIRKA_TYPU, uzky ? 0 : VYSKA_PRVKU);
		x += uzky ? 0 : SIRKA_TYPU + MEZERA;
		final int sipky = 2 * SIRKA_SIPKY + MEZERA;
		final int zbytek = konec - x - sipky - MEZERA;
		if (uzky) {
			r[ODZNAK] = new Rectangle(x, y, zbytek, VYSKA_PRVKU);
			r[KANDIDATI] = new Rectangle(x, y, 0, 0);
		} else {
			r[ODZNAK] = new Rectangle(x, y, odznakZaklad, VYSKA_PRVKU);
			x += odznakZaklad + MEZERA;
			r[KANDIDATI] = new Rectangle(x, y, konec - x - sipky - MEZERA, VYSKA_PRVKU);
		}
		r[ZPET] = new Rectangle(konec - sipky, y, SIRKA_SIPKY, VYSKA_PRVKU);
		r[DAL] = new Rectangle(konec - SIRKA_SIPKY, y, SIRKA_SIPKY, VYSKA_PRVKU);
		return r;
	}

	/** Obdélník pruhu v souřadnicích mapy podle její šířky. */
	Rectangle oblastPruhu() {
		return RozmisteniPrekryvu.pruhPlanovace(RozmisteniPrekryvu.volnaSirkaPruhu(getWidth()));
	}

	@Override
	public boolean contains(final int x, final int y) {
		return isVisible() && oblastPruhu().contains(x, y);
	}

	@Override
	public void doLayout() {
		final Rectangle pruh = oblastPruhu();
		final Rectangle[] r = rozloz(pruh.width);
		final Component[] c = { typ, odznak, kandidati, zpet, dal };
		for (int i = 0; i < c.length; i++) {
			c[i].setBounds(r[i]);
			c[i].setVisible(!r[i].isEmpty());
		}
		kandidati.setText(r[KANDIDATI].width >= 100 ? "jen kandidáti" : "");
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Rectangle p = oblastPruhu();
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(255, 255, 255, 215));
			g2.fillRoundRect(p.x, p.y, p.width, p.height, 8, 8);
			g2.setColor(new Color(0, 0, 0, 90));
			g2.drawRoundRect(p.x, p.y, p.width - 1, p.height - 1, 8, 8);
		} finally {
			g2.dispose();
		}
	}

	private Challenge challenge() {
		final List<Challenge> aktivni = model == null ? java.util.Collections.<Challenge> emptyList() : model.aktivni();
		return aktivni.isEmpty() ? null : aktivni.get(0);
	}

	private void obnov() {
		final boolean vidim = model != null && model.jePruhViditelny();
		if (vidim) {
			final Challenge c = challenge();
			final int chybi = model.pocetChybejicich(c);
			odznak.nastav(chybi < 0 ? "…" : Integer.toString(chybi));
			kandidati.setSelected(c.equals(model.jenKandidatiPro()));
			typ.setText("360°");
		}
		if (vidim != isVisible()) {
			setVisible(vidim);
		}
		UkazatelVykonu.setPodPruhem(vidim);
		revalidate();
		repaint();
	}

	private void prepniKandidaty() {
		final Challenge c = challenge();
		if (c == null) {
			return;
		}
		try {
			model.setJenKandidatiPro(kandidati.isSelected() ? c : null);
		} catch (final ChallengeChyba e) {
			kandidati.setSelected(false);
		}
	}

	private void prejdi(final boolean dopredu) {
		final Challenge c = challenge();
		if (c == null || sluzba == null || sluzba.planovac() == null) {
			return;
		}
		if (dopredu) {
			sluzba.planovac().dalsiChybejici(c);
		} else {
			sluzba.planovac().predchoziChybejici(c);
		}
	}

	/** Červený odznak s počtem chybějících. */
	private static final class Odznak extends JButton {
		private static final long serialVersionUID = 1L;

		Odznak() {
			super("…");
			setContentAreaFilled(false);
			setBorderPainted(false);
			setForeground(Color.WHITE);
			setFont(getFont().deriveFont(Font.BOLD));
		}

		void nastav(final String text) {
			setText(text);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			final Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(CERVENA);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
			} finally {
				g2.dispose();
			}
			super.paintComponent(g);
		}
	}

	/** Trojúhelníková šipka doleva nebo doprava. */
	private static final class Sipka implements Icon {
		private final boolean doleva;

		Sipka(final boolean doleva) {
			this.doleva = doleva;
		}

		@Override
		public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
			final Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(c.isEnabled() ? c.getForeground() : Color.GRAY);
				final int[] xs = doleva ? new int[] { x + 8, x, x + 8 } : new int[] { x, x + 8, x };
				g2.fillPolygon(xs, new int[] { y, y + 5, y + 10 }, 3);
			} finally {
				g2.dispose();
			}
		}

		@Override
		public int getIconWidth() {
			return 8;
		}

		@Override
		public int getIconHeight() {
			return 10;
		}
	}

	/** Ikona trychtýře filtru. */
	private static final class Trychtyr implements Icon {
		@Override
		public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
			final Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(c.getForeground());
				g2.fillPolygon(new int[] { x, x + 12, x + 7, x + 7, x + 5, x + 5 }, new int[] { y, y, y + 6, y + 11, y + 11, y + 6 }, 6);
			} finally {
				g2.dispose();
			}
		}

		@Override
		public int getIconWidth() {
			return 12;
		}

		@Override
		public int getIconHeight() {
			return 12;
		}
	}
}
