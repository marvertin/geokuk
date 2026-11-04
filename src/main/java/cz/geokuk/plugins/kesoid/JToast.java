package cz.geokuk.plugins.kesoid;

import java.awt.*;

import cz.geokuk.framework.JSlide0;
import cz.geokuk.util.gui.BublinkaHodnoty;
import cz.geokuk.util.gui.PopisekNaMape;

/** Krátká zpráva vlevo dole nad mapou; plynule se objeví, drží a zmizí. Myš ani klávesy nezachytává. */
public class JToast extends JSlide0 {

	private static final long serialVersionUID = 1L;

	/** Jak dlouho se zpráva drží beze změny. */
	public static final int DRZENI_MS = 2500;
	private static final int ODSAZENI_ZLEVA = 12;
	/** Svislý střed zprávy nad spodním okrajem; nad měřítkem. */
	private static final int NAD_DOLNIM_OKRAJEM = 62;

	private final transient BublinkaHodnoty bublinka = new BublinkaHodnoty(this::repaint, DRZENI_MS);

	public JToast() {
		setOpaque(false);
	}

	public void onEvent(final ToastEvent event) {
		bublinka.dotkni(event.getText());
	}

	String text() {
		return bublinka.getText();
	}

	boolean bezi() {
		return bublinka.casovacBezi();
	}

	/** Svislý střed zprávy podle výšky komponenty. */
	static int stredY(final int vyska) {
		return vyska - NAD_DOLNIM_OKRAJEM;
	}

	@Override
	public boolean contains(final int x, final int y) {
		return false;
	}

	@Override
	public void removeNotify() {
		bublinka.zrus();
		super.removeNotify();
	}

	@Override
	public void setVisible(final boolean visible) {
		if (!visible) {
			bublinka.zrus();
		}
		super.setVisible(visible);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final float v = bublinka.viditelnost();
		if (v <= 0f) {
			return;
		}
		final Graphics2D g2 = (Graphics2D) g;
		final Font font = getFont().deriveFont(Font.BOLD);
		final int sirka = PopisekNaMape.hranice(g2.getFontMetrics(font), bublinka.getText(), 0, 0).width;
		PopisekNaMape.nakresli(g2, font, bublinka.getText(), ODSAZENI_ZLEVA + sirka, stredY(getHeight()), v);
	}
}
