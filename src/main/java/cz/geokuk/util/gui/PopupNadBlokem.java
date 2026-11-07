package cz.geokuk.util.gui;

import java.awt.*;
import java.util.function.BooleanSupplier;

import javax.swing.*;

/**
 * Popup nad blokem stavového řádku, který se otevře najetím myši: otevře se, až myš na cíli vydrží, přepne se jen po delší prodlevě a zavře se, když je
 * myš chvíli mimo blok i popup, klávesou Esc nebo z kódu. Popup nezachytává myš ani fokus, aby první klik mimo něj nepropadl.
 */
public class PopupNadBlokem {

	/** Jak dlouho musí myš zůstat na cíli, než se popup otevře. */
	public static final int PRODLEVA_OTEVRENI_MS = 150;
	/** Když už je otevřený jiný popup: přejetí přes jiný cíl cestou k popupu ho nepřepne. */
	public static final int PRODLEVA_PREPNUTI_MS = 500;
	/** Jak dlouho smí být myš mimo blok i popup, než se popup zavře. */
	public static final int PRODLEVA_ZAVRENI_MS = 500;
	private static final int KROK_HLIDANI_MS = 50;
	/** Otevřené popupy (jen ve vlákně Swingu); jiné akce na Esc tak poznají, že Esc patří popupu. */
	private static final java.util.Set<PopupNadBlokem> OTEVRENE = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

	private final JComponent blok;
	private final Runnable predUkazanim;
	private final javax.swing.Timer hlidani;
	/** Popup se odjetím myši nezavírá, třeba když se v něm píše. */
	private BooleanSupplier drzOtevreny = () -> false;
	/** Zavolá se, když se popup zavře (ne když se jen znovu zobrazí po změně velikosti). */
	private Runnable poZavreni = () -> {};
	/** Běží dialog otevřený z popupu: popup se nezavírá. */
	private int dialogu;
	/** Po dialogu popup zůstane, dokud se myš nevrátí na blok nebo popup. */
	private boolean cekaNaNavrat;
	/** Od kdy je myš mimo blok i popup, -1 když není. */
	private long mimoOd = -1;
	/** Popup, který se otevře, když na jeho cíli myš vydrží; null, když se nic nečeká. */
	private JComponent cekajici;
	private Runnable cekajiciUkazani;
	private long cekaOd;
	private Popup popup;
	private JComponent obsahPopupu;
	/** Kde byl popup před zmenšením; dokud v tom místě myš zůstane, počítá se, jako by byla nad popupem. */
	private Rectangle predZmensenim;

	/**
	 * @param blok
	 *            blok stavového řádku, nad kterým se popup otevírá
	 * @param predUkazanim
	 *            zavolá se před každým zobrazením (třeba přizpůsobení velikosti obsahu)
	 */
	public PopupNadBlokem(final JComponent blok, final Runnable predUkazanim) {
		this.blok = blok;
		this.predUkazanim = predUkazanim;
		hlidani = new javax.swing.Timer(KROK_HLIDANI_MS, e -> tik());
		final String esc = "zavriPopup" + System.identityHashCode(this);
		blok.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), esc);
		blok.getActionMap().put(esc, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return popup != null;
			}

			@Override
			public void actionPerformed(final java.awt.event.ActionEvent e) {
				zavri();
			}
		});
	}

	/** Je otevřený některý popup nad blokem stavového řádku (Esc ho zavře). Ve vlákně Swingu. */
	public static boolean jeNejakyOtevreny() {
		return !OTEVRENE.isEmpty();
	}

	public void setPoZavreni(final Runnable poZavreni) {
		this.poZavreni = poZavreni;
	}

	public void setDrzOtevreny(final BooleanSupplier drzOtevreny) {
		this.drzOtevreny = drzOtevreny;
	}

	/**
	 * Zobrazí obsah těsně nad blokem a uvnitř obrazovky: bez kotvy zarovnaný k pravému okraji bloku, s kotvou k ní.
	 *
	 * @param znovu
	 *            zobrazí znovu i obsah, který už je vidět, když se mu změnila velikost
	 */
	public void ukaz(final JComponent obsah, final Component kotva, final boolean znovu) {
		if (!blok.isShowing() || obsahPopupu == obsah && !znovu) {
			return;
		}
		Rectangle predtim = null;
		if (obsahPopupu == obsah && obsah.isShowing()) {
			if (obsah.getSize().equals(obsah.getPreferredSize())) {
				obsah.revalidate();
				obsah.repaint();
				return;
			}
			predtim = new Rectangle(obsah.getLocationOnScreen(), obsah.getSize());
		}
		skryj();
		predUkazanim.run();
		obsah.setSize(obsah.getPreferredSize());
		final Point p = poloha(obsah.getPreferredSize(), kotva);
		SwingUtilities.convertPointToScreen(p, blok);
		final JRootPane koren = SwingUtilities.getRootPane(blok);
		final Rectangle kde = new Rectangle(p, obsah.getPreferredSize());
		if (koren != null && koren.isShowing() && new Rectangle(koren.getLocationOnScreen(), koren.getSize()).contains(kde)) {
			popup = new VrstvovyPopup(koren, obsah, kde);
		} else {
			popup = new OknoPopup(blok, obsah, p.x, p.y);
		}
		obsahPopupu = obsah;
		popup.show();
		OTEVRENE.add(this);
		// Kotva je dole: po sbalení oddílu klesne horní okraj a myš, která na šipku klikla, by byla nad popupem.
		predZmensenim = predtim;
		mimoOd = -1;
		hlidani.start();
	}

	/** Poloha popupu v souřadnicích bloku. */
	public Point poloha(final Dimension d, final Component kotva) {
		Rectangle obrazovka = obrazovka();
		final Window okno = SwingUtilities.getWindowAncestor(blok);
		if (okno != null && okno.isShowing()) {
			// Do okna, pokud se tam vejde; jinak aspoň na obrazovku.
			final Rectangle vOkne = obrazovka.intersection(new Rectangle(okno.getLocationOnScreen(), okno.getSize()));
			if (vOkne.width >= d.width) {
				obrazovka = new Rectangle(vOkne.x, obrazovka.y, vOkne.width, obrazovka.height);
			}
		}
		final Point bod = blok.getLocationOnScreen();
		int x = kotva == null ? blok.getWidth() - d.width : SwingUtilities.convertPoint(kotva.getParent(), kotva.getLocation(), blok).x;
		x = Math.max(obrazovka.x - bod.x, Math.min(x, obrazovka.x + obrazovka.width - bod.x - d.width));
		final int y = Math.max(horniHranice() - bod.y, -d.height);
		return new Point(x, y);
	}

	private Rectangle obrazovka() {
		return blok.getGraphicsConfiguration() != null ? blok.getGraphicsConfiguration().getBounds() : new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
	}

	/** Popup smí sahat až k horní liště okna (pod menu a panel nástrojů), nejvýš k hornímu okraji obrazovky. */
	public int horniHranice() {
		int hranice = obrazovka().y;
		final Window okno = SwingUtilities.getWindowAncestor(blok);
		if (okno instanceof RootPaneContainer && okno.isShowing()) {
			final Container obsah = ((RootPaneContainer) okno).getContentPane();
			Component horni = obsah;
			if (obsah.getLayout() instanceof BorderLayout) {
				final Component lista = ((BorderLayout) obsah.getLayout()).getLayoutComponent(BorderLayout.NORTH);
				if (lista != null && lista.isVisible()) {
					horni = lista;
				}
			}
			final int pod = horni.getLocationOnScreen().y + (horni == obsah ? 0 : horni.getHeight());
			hranice = Math.max(hranice, pod);
		}
		return hranice;
	}

	/** Provede dialog z popupu (potvrzení): popup za něj nezmizí, ani když myš odjede, a zůstane, dokud se myš po dialogu nevrátí. */
	public <T> T sDialogem(final java.util.function.Supplier<T> dialog) {
		dialogu++;
		try {
			return dialog.get();
		} finally {
			dialogu--;
			cekaNaNavrat = true;
		}
	}

	public void zavri() {
		cekaNaNavrat = false;
		if (popup != null) {
			skryj();
			poZavreni.run();
		}
	}

	private void skryj() {
		if (popup != null) {
			popup.hide();
			popup = null;
			OTEVRENE.remove(this);
			obsahPopupu = null;
			predZmensenim = null;
		}
	}

	/** Zavře popup a zastaví hlídání myši; při odebrání bloku z okna. */
	public void zastav() {
		hlidani.stop();
		zavri();
	}

	/** Obsah zobrazeného popupu, nebo null. */
	public JComponent viditelny() {
		return obsahPopupu;
	}

	/** Popup je otevřený, nebo se otevře, až na cíli myš vydrží. */
	public boolean otevira(final JComponent obsah) {
		return obsahPopupu == obsah || cekajici == obsah;
	}

	/** Myš vjela na cíl: popup se otevře nebo přepne, až na něm vydrží. */
	public void naCil(final JComponent obsah, final Runnable ukazani, final long ted) {
		if (obsahPopupu == obsah) {
			cekajici = null;
			return;
		}
		cekajici = obsah;
		cekajiciUkazani = ukazani;
		cekaOd = ted;
		hlidani.start();
	}

	public void opustenCil(final JComponent obsah) {
		if (cekajici == obsah) {
			cekajici = null;
		}
	}

	private void tik() {
		final PointerInfo info = MouseInfo.getPointerInfo();
		if (info != null) {
			tik(info.getLocation(), System.currentTimeMillis());
		}
		if (popup == null && cekajici == null) {
			hlidani.stop();
		}
	}

	/** Otevře čekající popup, když na cíli myš vydržela, a zavře otevřený, když je myš mimo blok i popup aspoň {@link #PRODLEVA_ZAVRENI_MS}. */
	public void tik(final Point mys, final long ted) {
		if (cekajici != null && ted - cekaOd >= (popup == null ? PRODLEVA_OTEVRENI_MS : PRODLEVA_PREPNUTI_MS)) {
			final Runnable ukazani = cekajiciUkazani;
			cekajici = null;
			ukazani.run();
		}
		if (predZmensenim != null && !predZmensenim.contains(mys)) {
			predZmensenim = null;
		}
		if (cekaNaNavrat && obsahPopupu != null && (obsahuje(blok, mys) || obsahuje(obsahPopupu, mys))) {
			cekaNaNavrat = false;
		}
		// Otevřené kontextové menu v popupu může popup přesahovat.
		if (obsahPopupu == null || dialogu > 0 || cekaNaNavrat || obsahuje(blok, mys) || obsahuje(obsahPopupu, mys) || predZmensenim != null || MenuSelectionManager.defaultManager().getSelectedPath().length > 0
				|| drzOtevreny.getAsBoolean()) {
			mimoOd = -1;
			return;
		}
		if (mimoOd < 0) {
			mimoOd = ted;
		} else if (ted - mimoOd >= PRODLEVA_ZAVRENI_MS) {
			zavri();
			cekajici = null;
		}
	}

	private static boolean obsahuje(final Component c, final Point obrazovka) {
		return c.isShowing() && new Rectangle(c.getLocationOnScreen(), c.getSize()).contains(obrazovka);
	}

	/** Popup ve vrstvě popupů okna: pole v něm dostane fokus a zavření nic neprochází, na rozdíl od těžkého popupu z PopupFactory. */
	private static final class VrstvovyPopup extends Popup {
		private final JLayeredPane vrstvy;
		private final Component obsah;
		private final Rectangle hranice;

		VrstvovyPopup(final JRootPane koren, final Component obsah, final Rectangle naObrazovce) {
			vrstvy = koren.getLayeredPane();
			this.obsah = obsah;
			final Point v = new Point(naObrazovce.x, naObrazovce.y);
			SwingUtilities.convertPointFromScreen(v, vrstvy);
			hranice = new Rectangle(v, naObrazovce.getSize());
		}

		@Override
		public void show() {
			obsah.setBounds(hranice);
			vrstvy.add(obsah, JLayeredPane.POPUP_LAYER);
			vrstvy.repaint(hranice);
		}

		@Override
		public void hide() {
			vrstvy.remove(obsah);
			vrstvy.repaint(hranice);
		}
	}

	/** Popup přesahující okno v nezávislém okně; těžký popup z PopupFactory při zavření prochází celý strom komponent hlavního okna. */
	private static final class OknoPopup extends Popup {
		private final JWindow okno;

		OknoPopup(final Component vlastnik, final Component obsah, final int x, final int y) {
			final Window rodic = SwingUtilities.getWindowAncestor(vlastnik);
			okno = new JWindow(rodic);
			okno.setFocusableWindowState(false);
			okno.getContentPane().add(obsah);
			okno.pack();
			okno.setLocation(x, y);
		}

		@Override
		public void show() {
			okno.setVisible(true);
		}

		@Override
		public void hide() {
			okno.setVisible(false);
			okno.dispose();
		}
	}
}
