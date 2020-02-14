package cz.geokuk.plugins.kesoid.kind.kes;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

import javax.swing.*;

import cz.geokuk.framework.Dlg;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.Wpti;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.refbody.DomaciSouradniceSeZmenilyEvent;
import cz.geokuk.util.gui.JSmallPictureButton;

/**
 * Detailní informace o vybrané keši.
 *
 * @author Spikodrob
 *
 */
public class JKesDetail extends JKesoidDetail0 {

	private class Popisek extends JLabel {
		private static final long serialVersionUID = -79636254895417957L;

		public Popisek(final String text) {
			super(text);
			final Font font = getFont().deriveFont(0);
			setFont(font);

		}

	}

	/**
	 *
	 */
	private static final long serialVersionUID = -3323887260932949747L;

	private Kes kes;
	private JLabel size;
	private JLabel difficulty;

	private JLabel terrain;
	private final JLabel bestOf = new JLabel();
	private final JLabel hodnoceni = new JLabel();
	private final JLabel hodnoceniPocet = new JLabel();
	private final JLabel znamka = new JLabel();
	private JFavorit jFavorit;

	private JLabel jFoundTime;

	private JSmallPictureButton zobrazHint;

	private static Icon hvezdicky(final String kolik) {
		// TODO : revisit this...
		final String kolikAdjusted = kolik.replaceAll("\\.(0+|$)", "");
		final String path = "gccom/stars/stars" + kolikAdjusted.replace('.', '_') + ".gif";
		final BufferedImage image = ImageLoader.seekResImage(path, 61, 13);
		final Icon icon = new ImageIcon(image);
		return icon;
	}

	private static Icon velikost(final EKesSize size) {
		final String path = "gccom/container/" + size.name().toLowerCase() + ".gif";
		final BufferedImage image = ImageLoader.seekResImage(path, 45, 12);
		final Icon icon = new ImageIcon(image);
		return icon;
	}

	public JKesDetail() {
		initComponents();
	}

	@Override
	public void napln(final Wpt wpt) {
		kes = (Kes) ((Wpti)wpt).getKoid();
		napln();
		final boolean mameHint = kes.getHint() != null && !kes.getHint().trim().isEmpty();
		zobrazHint.setEnabled(mameHint);
	}

	public void onEvent(final DomaciSouradniceSeZmenilyEvent aEvent) {
		if (isVisible() && kes != null) {
			napln();
		}
	}

	private void initComponents() {
		size = new JLabel();
		size.setToolTipText("Velikost keše");
		difficulty = new JLabel();
		difficulty.setToolTipText("Počet hvězdiček obtížnosti.");
		terrain = new JLabel();
		terrain.setToolTipText("Počet hvězdiček terénu.");
		jFoundTime = new JLabel();
		jFoundTime.setForeground(Color.GREEN);
		jFoundTime.setToolTipText("Datum a čas nalezení keše.");

		zobrazHint = new JSmallPictureButton("Hint");
		zobrazHint.setToolTipText("Zobrazí rozkódovaný hint");

		final Box hlav = Box.createVerticalBox();
		add(hlav);

		final Box box2 = Box.createHorizontalBox();

		{

			final Box box3 = Box.createVerticalBox();
			box3.add(size);
			box3.add(Box.createGlue());
			box3.add(jFoundTime);

			box2.add(box3);
		}

		box2.add(zobrazHint);

		{
			final Box pan4a = Box.createVerticalBox();
			final JLabel lblObtiznost = new Popisek("D:");
			pan4a.add(lblObtiznost);
			final JLabel lblTeren = new Popisek("T:");
			pan4a.add(lblTeren);

			final Box pan4b = Box.createVerticalBox();
			pan4b.add(difficulty);
			pan4b.add(terrain);

			box2.add(Box.createHorizontalGlue());

			// pan4.setAlignmentX(RIGHT_ALIGNMENT);
			box2.add(pan4a);
			box2.add(pan4b);

		}

		hlav.add(box2);

		final JPanel box4 = new JPanel();
		box4.add(new Popisek("Hodnoceni:"));
		box4.add(hodnoceni);
		box4.add(new JLabel("/"));
		box4.add(hodnoceniPocet);
		box4.add(new Popisek("moje:"));
		box4.add(znamka);
		box4.add(new Popisek("BestOf:"));
		box4.add(bestOf);
		jFavorit = new JFavorit(0);
		box4.add(jFavorit);
		hlav.add(box4);

		// Box box5 = Box.createHorizontalBox();
		//
		// box5.add(Box.createGlue());
		// box5.add(zobrazHint);
		// hlav.add(box5);

		zobrazHint.addActionListener(e -> Dlg.info(kes.getHint(), "Hint"));
	}

	private void napln() {
		size.setIcon(velikost(kes.getSize()));
		difficulty.setIcon(hvezdicky(kes.getDifficulty().toNumberString()));
		terrain.setIcon(hvezdicky(kes.getTerrain().toNumberString()));
		jFoundTime.setText(JKesoidDetail0.formatujDatum(kes.getFountTime()));

		bestOf.setText(kes.getBestOf() == Kes.NENI_HODNOCENI ? "?" : kes.getBestOf() + "x");
		hodnoceni.setText(kes.getHodnoceni() == Kes.NENI_HODNOCENI ? "?" : kes.getHodnoceni() + "%");
		hodnoceniPocet.setText(kes.getHodnoceniPocet() == Kes.NENI_HODNOCENI ? "?" : kes.getHodnoceniPocet() + "x");
		znamka.setText(kes.getZnamka() == Kes.NENI_HODNOCENI ? "?" : kes.getZnamka() + "%");
		jFavorit.setKolik(kes.getFavorit());
		jFavorit.setVisible(kes.getFavorit() > 0);
	}
}
