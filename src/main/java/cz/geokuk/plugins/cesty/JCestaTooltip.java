package cz.geokuk.plugins.cesty;

import java.awt.*;

import javax.swing.JComponent;
import javax.swing.JLabel;

import cz.geokuk.core.coordinates.FGeoKonvertor;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.cesty.data.Bousek0;
import cz.geokuk.plugins.cesty.data.Cesta;

public class JCestaTooltip extends JComponent {

	private static final long	serialVersionUID	= -7455935457383912752L;

	private final JLabel		jVpred				= new JLabel("xxxxxxxx");
	private final JLabel		jVzad				= new JLabel("yyyyyyyy");
	private final JLabel		jNazevCesty			= new JLabel("yyyyyyyy");

	public JCestaTooltip() {
		setLayout(new FlowLayout());
		setOpaque(false);
		add(jNazevCesty);
		add(jVzad);
		add(jVpred);
		jNazevCesty.setForeground(Color.WHITE);
	}

	public void setDalkoviny(final Bousek0 blizkyBousek, final Mou aMou) {
		jVpred.setText("<html>" + blizkyBousek.dalkaCestaVpredHtml(aMou));
		jVzad.setText("<html>" + blizkyBousek.dalkaCestaVzadHtml(aMou));
		jVpred.setVisible(true);
		jVzad.setVisible(true);
		jNazevCesty.setText(blizkyBousek.getCesta().getNazev());
		jNazevCesty.setVisible(blizkyBousek.getCesta().getNazev() != null);

	}

	public void setPridavaciDalkoviny(final Cesta cesta, final Mou mou) {

		final double dalkaPuvodni = cesta == null ? 0 : cesta.dalka();
		final double dalkaDodana = cesta == null ? 0 : FGeoKonvertor.dalka(cesta.getCil(), mou);
		jVzad.setText("<html>" + Cesta.dalkaHtml(dalkaPuvodni, FBarvy.CURTA_NORMALNE) + "<font color=white> +</font>" + Cesta.dalkaHtml(dalkaDodana, Color.WHITE));
		jVzad.setVisible(true);
		jVpred.setVisible(false);
		jNazevCesty.setText(cesta.getNazev());
		jNazevCesty.setVisible(cesta.getNazev() != null);

	}

	@Override
	protected void paintComponent(final Graphics g) {
		g.setColor(new Color(0, 0, 0, 128));
		g.fillRect(0, 0, getWidth(), getHeight());
	}
}
