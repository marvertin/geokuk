package cz.geokuk.plugins.kesoid;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import cz.geokuk.framework.JSlide0;
import cz.geokuk.framework.RozmisteniPrekryvu;
import cz.geokuk.util.gui.BublinkaHodnoty;
import cz.geokuk.util.gui.PopisekNaMape;

/** Krátká zpráva uprostřed dole nad měřítkem; plynule se objeví, drží a zmizí. Myš ani klávesy nezachytává. */
public class JToast extends JSlide0 {

	private static final long serialVersionUID = 1L;

	/** Jak dlouho se zpráva drží beze změny. */
	public static final int DRZENI_MS = 2500;
	private transient Supplier<? extends Collection<Rectangle>> prekazky = Collections::emptyList;
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

	/** Překážky, které zpráva přeskočí (panely v rozích, legenda); volá se při kreslení. */
	public void setPrekazky(final Supplier<? extends Collection<Rectangle>> prekazky) {
		this.prekazky = prekazky;
	}

	/** Obdélník zprávy podle {@link RozmisteniPrekryvu#zprava}. */
	static Rectangle umisteni(final int sirkaMapy, final int vyskaMapy, final int sirkaZpravy, final int vyskaZpravy) {
		return RozmisteniPrekryvu.zprava(new Dimension(sirkaMapy, vyskaMapy), new Dimension(sirkaZpravy, vyskaZpravy), Collections.<Rectangle> emptyList());
	}

	/** Text zkrácený o konec s třemi tečkami, aby se vešel do {@code sirka}; text, který se vejde, beze změny. */
	static String zkrat(final String text, final FontMetrics fm, final int sirka) {
		return cz.geokuk.util.gui.ZkraceniTextu.zkrat(text, fm, sirka);
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
		return RozmisteniPrekryvu.zprava(getSize(), new Dimension(velikost.width, velikost.height), prekazky.get());
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
		final FontMetrics fm = g2.getFontMetrics(font());
		final Rectangle velikost = PopisekNaMape.hranice(fm, bublinka.getText(), 0, 0);
		final Rectangle r = oblastZpravy(fm);
		final String text = zkrat(bublinka.getText(), fm, r.width - (velikost.width - fm.stringWidth(bublinka.getText())));
		naposledy = PopisekNaMape.nakresli(g2, font(), text, r.x + r.width, r.y + r.height / 2, v);
	}
}
