package cz.geokuk.util.gui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.LongSupplier;

import javax.swing.Timer;

/**
 * Bublinka s hodnotou u posuvníku nad mapou: plynule se objeví při začátku tažení, po puštění se drží a plynule zmizí. Animaci řídí časovač na EDT, který se na konci zastaví.
 */
public final class BublinkaHodnoty {

	/** Za jak dlouho se bublinka při začátku tažení plně objeví. */
	public static final int ZACATEK_MS = 140;
	/** Jak dlouho se po puštění drží beze změny. */
	public static final int DRZENI_MS = 700;
	/** Za jak dlouho po držení zmizí. */
	public static final int ZANIKNUTI_MS = 300;
	static final int KROK_ANIMACE_MS = 16;

	private static final int PRUHLEDNOST_POZADI = 200;
	private static final int PRUHLEDNOST_OBRYSU = 120;

	/** Spouští a zastavuje opakované překreslování; v testech se nahrazuje. */
	interface Casovac {
		void spust();

		void zastav();

		boolean bezi();
	}

	private final LongSupplier hodinyMs;
	private final Casovac casovac;
	private final Runnable prekresli;
	private String text = "";
	private boolean aktivni;
	private long zacatek;
	/** Kdy se posuvník pustil, -1 při tažení. */
	private long pusteno = -1;

	public BublinkaHodnoty(final Runnable prekresli) {
		this.hodinyMs = () -> System.nanoTime() / 1_000_000;
		this.prekresli = prekresli;
		final Timer timer = new Timer(KROK_ANIMACE_MS, e -> tik());
		timer.setCoalesce(true);
		this.casovac = new Casovac() {
			@Override
			public void spust() {
				timer.start();
			}

			@Override
			public void zastav() {
				timer.stop();
			}

			@Override
			public boolean bezi() {
				return timer.isRunning();
			}
		};
	}

	/** Časovač na EDT, který volá {@code krok}; {@code krok} se nastavuje později, zatím může být null. */
	static Casovac swingovyCasovac(final Runnable krok) {
		final Timer timer = new Timer(KROK_ANIMACE_MS, e -> {
			if (krok != null) {
				krok.run();
			}
		});
		timer.setCoalesce(true);
		return new Casovac() {
			@Override
			public void spust() {
				timer.start();
			}

			@Override
			public void zastav() {
				timer.stop();
			}

			@Override
			public boolean bezi() {
				return timer.isRunning();
			}
		};
	}

	BublinkaHodnoty(final LongSupplier hodinyMs, final Casovac casovac, final Runnable prekresli) {
		this.hodinyMs = hodinyMs;
		this.casovac = casovac;
		this.prekresli = prekresli;
	}

	/**
	 * Viditelnost bublinky 0 až 1 v čase. {@code msOdZacatku} je čas od začátku tažení, {@code msOdPusteni} čas od puštění, záporný, dokud se táhne.
	 */
	public static float viditelnost(final long msOdZacatku, final long msOdPusteni) {
		final float nabeh = omez(msOdZacatku / (float) ZACATEK_MS);
		if (msOdPusteni <= DRZENI_MS) {
			return nabeh;
		}
		final float utlum = omez((msOdPusteni - DRZENI_MS) / (float) ZANIKNUTI_MS);
		return nabeh * (1 - utlum);
	}

	private static float omez(final float v) {
		return Math.max(0f, Math.min(1f, v));
	}

	/** Začátek nebo pokračování tažení; mezi tažením a zánikem se bublinka vrací od aktuální viditelnosti, bez skoku. */
	public void tahne(final String novyText) {
		final long ted = hodinyMs.getAsLong();
		if (!aktivni || pusteno >= 0) {
			zacatek = ted - Math.round(viditelnost() * ZACATEK_MS);
		}
		pusteno = -1;
		aktivni = true;
		text = novyText;
		if (!casovac.bezi()) {
			casovac.spust();
		}
		prekresli.run();
	}

	/** Okamžitě skryje bublinku a zastaví časovač, třeba když se vrstva skryje uprostřed tažení. */
	public void zrus() {
		aktivni = false;
		pusteno = -1;
		casovac.zastav();
	}

	/** Posuvník se pustil. */
	public void pust() {
		if (aktivni && pusteno < 0) {
			pusteno = hodinyMs.getAsLong();
		}
	}

	/** Změna hodnoty bez tažení (kolečko, klávesy): bublinka se krátce ukáže. */
	public void dotkni(final String novyText) {
		tahne(novyText);
		pust();
	}

	public float viditelnost() {
		if (!aktivni) {
			return 0f;
		}
		final long ted = hodinyMs.getAsLong();
		return viditelnost(ted - zacatek, pusteno < 0 ? -1 : ted - pusteno);
	}

	public boolean jeAktivni() {
		return aktivni;
	}

	public boolean casovacBezi() {
		return casovac.bezi();
	}

	public String getText() {
		return text;
	}

	/** Krok animace: překreslí a na konci zastaví časovač. */
	void tik() {
		if (aktivni && pusteno >= 0 && hodinyMs.getAsLong() - pusteno >= DRZENI_MS + ZANIKNUTI_MS) {
			aktivni = false;
		}
		prekresli.run();
		if (!aktivni) {
			casovac.zastav();
		}
	}

	/**
	 * Nakreslí bublinku vlevo od zadaného bodu, svisle vystředěnou. Vrací obdélník, který zabrala (nebo null, když není vidět).
	 *
	 * @param pravyOkraj
	 *            x pravého okraje bublinky
	 */
	public Rectangle kresli(final Graphics2D g, final Font font, final int pravyOkraj, final int stredY) {
		final float v = viditelnost();
		if (v <= 0f) {
			return null;
		}
		final Graphics2D gg = (Graphics2D) g.create();
		try {
			gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			gg.setFont(font);
			final FontMetrics fm = gg.getFontMetrics();
			final int vodorovne = 8;
			final int svisle = 4;
			final int w = fm.stringWidth(text) + 2 * vodorovne;
			final int h = fm.getHeight() + 2 * svisle;
			final Rectangle r = new Rectangle(pravyOkraj - w, stredY - h / 2, w, h);
			final RoundRectangle2D.Float tvar = new RoundRectangle2D.Float(r.x, r.y, r.width - 1, r.height - 1, h, h);
			gg.setColor(new Color(20, 20, 20, Math.round(PRUHLEDNOST_POZADI * v)));
			gg.fill(tvar);
			gg.setColor(new Color(255, 255, 255, Math.round(PRUHLEDNOST_OBRYSU * v)));
			gg.draw(tvar);
			gg.setColor(new Color(255, 255, 255, Math.round(255 * v)));
			gg.drawString(text, r.x + vodorovne, r.y + svisle + fm.getAscent());
			return r;
		} finally {
			gg.dispose();
		}
	}
}
