package cz.geokuk.plugins.kesoid;

import java.awt.*;

import cz.geokuk.framework.JSlide0;
import cz.geokuk.util.gui.BublinkaHodnoty;
import cz.geokuk.util.gui.PopisekNaMape;

/** Krátká zpráva uprostřed dole nad měřítkem; plynule se objeví, drží a zmizí. Myš ani klávesy nezachytává. */
public class JToast extends JSlide0 {

	private static final long serialVersionUID = 1L;

	/** Jak dlouho se zpráva drží beze změny. */
	public static final int DRZENI_MS = 2500;
	/** Nejmenší odstup zprávy od levého a pravého okraje mapy v úzkém okně. */
	static final int OKRAJ = 12;
	/** Svislý střed zprávy nad spodním okrajem mapy: nad měřítkem, které stojí uprostřed dole. */
	static final int NAD_DOLNIM_OKRAJEM = 90;

	private final transient BublinkaHodnoty bublinka = new BublinkaHodnoty(this::prekresliZpravu, DRZENI_MS);
	/** Kde se zpráva naposledy kreslila; při animaci se překresluje jen tato oblast a oblast nového textu. */
	private Rectangle naposledy = new Rectangle();

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

	/** Obdélník zprávy: vodorovně na středu mapy, svisle nad měřítkem; v úzkém okně se zarovná k levému okraji, aby se nezkrátila zleva. */
	static Rectangle umisteni(final int sirkaMapy, final int vyskaMapy, final int sirkaZpravy, final int vyskaZpravy) {
		final int x = Math.max(OKRAJ, (sirkaMapy - sirkaZpravy) / 2);
		return new Rectangle(x, vyskaMapy - NAD_DOLNIM_OKRAJEM - vyskaZpravy / 2, sirkaZpravy, vyskaZpravy);
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

	/** Písmo zprávy; před zobrazením, kdy komponenta ještě písmo nemá, běžné písmo štítků. */
	Font font() {
		final Font zaklad = getFont() != null ? getFont() : javax.swing.UIManager.getFont("Label.font");
		return zaklad.deriveFont(Font.BOLD);
	}

	/** Obdélník aktuálního textu zprávy v této vrstvě. */
	private Rectangle oblastZpravy(final FontMetrics fm) {
		final Rectangle velikost = PopisekNaMape.hranice(fm, bublinka.getText(), 0, 0);
		return umisteni(getWidth(), getHeight(), velikost.width, velikost.height);
	}

	/** Překreslí jen oblast zprávy, ne celou průhlednou vrstvu přes mapu. */
	private void prekresliZpravu() {
		final Rectangle oblast = new Rectangle(naposledy);
		if (!bublinka.getText().isEmpty()) {
			oblast.add(oblastZpravy(getFontMetrics(font())));
		}
		if (oblast.isEmpty()) {
			repaint();
			return;
		}
		oblast.grow(2, 2);
		repaint(oblast.x, oblast.y, oblast.width, oblast.height);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final float v = bublinka.viditelnost();
		if (v <= 0f) {
			return;
		}
		final Graphics2D g2 = (Graphics2D) g;
		final Rectangle r = oblastZpravy(g2.getFontMetrics(font()));
		naposledy = PopisekNaMape.nakresli(g2, font(), bublinka.getText(), r.x + r.width, r.y + r.height / 2, v);
	}
}
