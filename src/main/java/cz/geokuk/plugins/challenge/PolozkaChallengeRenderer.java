package cz.geokuk.plugins.challenge;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.function.Supplier;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import cz.geokuk.util.gui.ZkraceniTextu;

/**
 * Položka seznamu challenge: značka zapnutí, název, „(šablona)“ a „(nedostupný)“. Dlouhý název se zkrátí „…“ podle šířky seznamu, aby seznam nedostal vodorovný posuvník a přípona
 * zůstala vidět; plný název a souhrn jsou v tooltipu.
 */
final class PolozkaChallengeRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;
	private final transient Supplier<ChallengeModel> model;

	PolozkaChallengeRenderer(final Supplier<ChallengeModel> model) {
		this.model = model;
	}

	@Override
	public Component getListCellRendererComponent(final JList<?> l, final Object v, final int i, final boolean sel, final boolean fokus) {
		final Challenge c = (Challenge) v;
		final ChallengeModel m = model.get();
		final JLabel s = (JLabel) super.getListCellRendererComponent(l, c.nazev(), i, sel, fokus);
		final String predpona = m != null && m.jeAktivni(c) ? "● " : "   ";
		final String pripona = (m != null && m.jeSablona(c) ? "  (šablona)" : "") + (m != null && !m.jeVUi(c) ? "  (nedostupný)" : "");
		final FontMetrics fm = s.getFontMetrics(s.getFont());
		final int volno = l.getWidth() - s.getInsets().left - s.getInsets().right - fm.stringWidth(predpona) - fm.stringWidth(pripona);
		s.setText(predpona + (l.getWidth() > 0 ? ZkraceniTextu.zkrat(c.nazev(), fm, volno) : c.nazev()) + pripona);
		// Šířku seznamu určuje okno, ne délka názvu.
		s.setPreferredSize(new Dimension(1, s.getPreferredSize().height));
		s.setToolTipText("<html>" + escape(c.nazev()) + "<br>" + escape(FormularDefinice.souhrn(c)) + "</html>");
		return s;
	}

	private static String escape(final String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
