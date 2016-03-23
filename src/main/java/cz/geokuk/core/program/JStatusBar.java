/**
 *
 */
package cz.geokuk.core.program;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.cesty.CestyChangedEvent;
import cz.geokuk.plugins.cesty.data.Doc;
import cz.geokuk.plugins.kesoid.Ikonizer;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.importek.InformaceOZdrojich;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.plugins.vylety.*;

/**
 * @author veverka
 *
 */
public class JStatusBar extends JPanel {

	private static final long					serialVersionUID				= -6267502844907253041L;

	private Mou									cur								= new Mou(0, 0);

	private Poziceq								poziceq							= new Poziceq();

	private final JValue						souradnice						= new JValue();

	private final JValue						celkovePoctyVsude				= new JValue();
	private final JValue						filtrovanePocetyVsude			= new JValue();
	private final JSkrtnutaValue				celkovePoctyVyrez				= new JSkrtnutaValue();
	private final JSkrtnutaValue				filtrovanePocetyVyrez			= new JSkrtnutaValue();

	private final JValue						vzdalenost						= new JValue();
	private final JLabel						azimutSmer						= new JLabel();
	private final JValue						azimutCislo						= new JValue();

	private final JValue						vyletAno						= new JValue();
	private final JValue						vyletNe							= new JValue();

	private final JValue						souradnicePozice				= new JValue();
	private final JValue						meritkoMapy						= new JValue();

	private final JLabel						varovaniPoctuPrekrocenych		= new JLabel();

	private JPanel								odPozice;

	private final Map<Progressor, JProgressBar>	jFilterProgressMap				= new HashMap<>();
	private JPanel								jFilterProgressPanel;

	private final JValue						jZdrojeKesoiduPocetNactenych	= new JValue();
	private final JSkrtnutaValue				jZdrojeKesoiduPocetNenactenych	= new JSkrtnutaValue();
	private final JValue						jZdrojeKesoiduCas				= new JValue();

	private final JValue						jSouborSVyletem					= new JValue();
	private final JLabel						jSouborSVyletemPotrebujeUlozit	= new JLabel();

	private KesBag								filtrovane;
	private KesBag								vsechny;

	private Coord								moord;

	private Akce								akce;

	private final JValue						jPocetKesiVCestach				= new JValue();

	public JStatusBar() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		final JPanel souradnicePanel = createPanel();
		// souradnicePanel.setBorder(BorderFactory.createEtchedBorder());

		// souradnicePozice.setEditable(false);

		souradnicePanel.add(new JLabel("Myš:"));
		souradnicePanel.add(souradnice);
		// souradnice.setColumns(13);
		souradnice.setToolTipText("Souřadnice kurzoru myši");
		souradnicePanel.add(new JLabel("Z="));
		souradnicePanel.add(meritkoMapy);
		add(souradnicePanel);

		final JPanel jPozicePanel = createPanel();

		souradnicePozice.setToolTipText("Spouřadnice aktuálně vybrané pozice, možno vybrat a dát do clipboardu");
		jPozicePanel.add(new JLabel("Pozice:"));
		jPozicePanel.add(souradnicePozice);
		add(jPozicePanel);

		odPozice = createPanel();

		odPozice.add(new JLabel("Od pozice:"));
		odPozice.add(vzdalenost);
		vzdalenost.setToolTipText("Vzdálenost mezi vyznačenou pozicí nebo waypointem a myší kurzoru.");

		odPozice.add(azimutCislo);
		odPozice.add(azimutSmer);
		azimutSmer.setToolTipText("Azimut od vyznačené pozice k myši kurzoru.");
		azimutCislo.setToolTipText("Azimut od vyznačené pozice k myši kurzoru.");

		meritkoMapy.setToolTipText("Měřítko mapy, čím větší číslo, tím větší přiblížení");
		// add(mapoveMeritko);

		add(odPozice);

		// add(Box.createHorizontalGlue());

		// add(new JLabel("Meritko:"));
		// add(meritkoMapy);
		// meritkoMapy.setToolTipText("Aktuální měřítko zobrazené mapy, čím menší číslo, tím oddálenější mapa.");

		///////////////////////////////
		final JPanel poctyKesi = createPanel();
		// poctyKesi.setBorder(BorderFactory.createLoweredBevelBorder());
		// poctyKesi.setBorder(BorderFactory.createRaisedBevelBorder());
		// poctyKesi.setBorder(BorderFactory.createEtchedBorder());
		poctyKesi.add(new JLabel("Vše:"));
		poctyKesi.add(celkovePoctyVsude);
		celkovePoctyVsude.setToolTipText("Počet waypointů celkem / počet kešoidů celkem.");

		poctyKesi.add(new JLabel("Filtr:"));
		poctyKesi.add(filtrovanePocetyVsude);
		filtrovanePocetyVsude.setToolTipText("Počet waypointů po aplikaci filtru / počet kešoidů po apliakci filtru.");

		poctyKesi.add(celkovePoctyVyrez);
		celkovePoctyVyrez.setToolTipText("Počet všech waypointů ve výřezu, které by byly zobrazeny, pokud by nebyl filtr.");

		poctyKesi.add(filtrovanePocetyVyrez);
		filtrovanePocetyVyrez.setToolTipText("Počet všech zobrazených waypointů ve výřezu.");
		add(poctyKesi);

		////////////////////////////

		final JPanel vylety = createPanel();
		// vylety.setBorder(BorderFactory.createEtchedBorder());
		vylety.add(new JLabel("Výlet:"));
		vylety.add(vyletAno);
		vyletAno.setToolTipText("Počet keší, u kterých má vyznačen příznak, že je chci lovit.");
		vylety.add(new JLabel("/"));
		vylety.add(vyletNe);
		vyletAno.setToolTipText("Počet keší, u kterých má vyznačen příznak, že je budu ignorovat.");

		vylety.add(jSouborSVyletemPotrebujeUlozit);
		vylety.add(jSouborSVyletem);
		vylety.add(jPocetKesiVCestach);
		jPocetKesiVCestach.setToolTipText("Počet waypointů dohromady / počet cest.");
		add(vylety);

		add(varovaniPoctuPrekrocenych);
		jFilterProgressPanel = createPanel();
		// jFilterProgress.setVisible(false);
		// jFilterProgress.setStringPainted(true);
		add(jFilterProgressPanel);

		final JPanel zdrojeKesoiduPanel = createPanel();
		zdrojeKesoiduPanel.setToolTipText("Klikni a uvidíš detaily");
		zdrojeKesoiduPanel.add(jZdrojeKesoiduPocetNactenych);
		jZdrojeKesoiduPocetNactenych.setToolTipText("Počet načtených souborů s kešoidy.");
		zdrojeKesoiduPanel.add(jZdrojeKesoiduPocetNenactenych);
		jZdrojeKesoiduPocetNenactenych.setToolTipText("Počet souborů s kešoidy, jejichž načtení bylo zabráněno odškrtnutím.");
		zdrojeKesoiduPanel.add(new JLabel("zdroje:"));
		zdrojeKesoiduPanel.add(jZdrojeKesoiduCas);
		jZdrojeKesoiduCas.setToolTipText("Čas nejmladšího načteného souboru.");
		zdrojeKesoiduPanel.setCursor(FKurzory.KAM_SE_DA_KLIKNOUT);
		add(zdrojeKesoiduPanel);

		zdrojeKesoiduPanel.addMouseListener(new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 *
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(final MouseEvent aE) {
				akce.informaceoZdrojichAction.fire();
			}
		});
	}

	JPanel createPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		panel.setBorder(BorderFactory.createEtchedBorder());
		return panel;
	}

	public void onEvent(final VyrezChangedEvent event) {
		moord = event.getMoord();
		filtrovanePocetyVyrez.setText(veVyrezu(filtrovane));
		celkovePoctyVyrez.setText(veVyrezu(vsechny));
		meritkoMapy.setText(String.valueOf(moord.getMoumer()));

	}

	public void onEvent(final ZmenaSouradnicMysiEvent event) {
		if (cur != null && cur.equals(event.moucur)) {
			return;
		}
		cur = event.moucur;
		souradnice.setText(cur == null ? "?" : cur.toWgs().toString());
		prepocitejVzdalenostAAzimut();
	}

	public void onEvent(final KeskyVyfiltrovanyEvent aEvent) {
		filtrovane = aEvent.getFiltrovane();
		filtrovanePocetyVsude.setText(celkove(filtrovane));
		filtrovanePocetyVyrez.setText(veVyrezu(filtrovane));
		revalidate();
	}

	public void onEvent(final KeskyNactenyEvent aEvent) {
		vsechny = aEvent.getVsechny();
		celkovePoctyVsude.setText(celkove(vsechny));
		celkovePoctyVyrez.setText(veVyrezu(vsechny));

		final InformaceOZdrojich informaceOZdrojich = aEvent.getVsechny().getInformaceOZdrojich();
		// TODO : reenable
		jZdrojeKesoiduPocetNactenych.setText(informaceOZdrojich.getSourceCount(true) + "");
		final int pocetNenactenych = informaceOZdrojich.getSourceCount(false);
		jZdrojeKesoiduPocetNenactenych.setText(pocetNenactenych + "");
		jZdrojeKesoiduPocetNenactenych.setVisible(pocetNenactenych > 0);

		final String formatedCas = String.format("%tF %<tR", informaceOZdrojich.getYungest());
		jZdrojeKesoiduCas.setText(formatedCas);
		revalidate();
	}

	public void onEvent(final ProgressEvent event) {
		final Progressor progressor = event.getProgressor();
		// progressor = null;
		JProgressBar jProgressBar = jFilterProgressMap.get(progressor);
		// System.out.println(event);
		final boolean pridavat = event.isVisible();
		// pridavat = true;
		if (pridavat) {
			if (jProgressBar == null) {
				jProgressBar = new JProgressBar();
				jProgressBar.setStringPainted(true);
				jProgressBar.setVisible(true);
				jFilterProgressMap.put(progressor, jProgressBar);
				jFilterProgressPanel.add(jProgressBar);
				revalidate();
			}
			jProgressBar.setVisible(event.isVisible());
			jProgressBar.setValue(event.getProgress());
			jProgressBar.setMaximum(event.getMax());
			jProgressBar.setString(event.getText());
			jProgressBar.setToolTipText(event.getTooltip());
		} else {
			if (jProgressBar != null) {
				jFilterProgressMap.remove(progressor);
				jFilterProgressPanel.remove(jProgressBar);
				revalidate();
			}
		}
	}

	public void onEvent(final PoziceChangedEvent event) {
		poziceq = event.poziceq;
		prepocitejVzdalenostAAzimut();
		// vzdalenost.setText("");
		// azimutCislo.setText("");
		// azimutSmer.setText("");
		// azimutSmer.setIcon(null);
		// System.out.println("VELIKOST STAVOVEHO RADKU: " + JStatusBar.this.getSize());

		if (poziceq.isNoPosition()) {
			// souradnicePozice.setVisible(false);
			souradnicePozice.setText("N/A");
		} else {
			souradnicePozice.setText(poziceq.getWgs().toString());
			// souradnicePozice.setVisible(true);
		}
	}

	// public void onEvent(IgnoreListChangedEvent aEvent) {
	// CestyModel cestyModel = aEvent.getModel();
	// //vyletAno.setText(cestyModel.get(EVylet.ANO).size()+"");
	// vyletNe.setText(cestyModel.getPocetIgnorovanychKesoidu()+"");
	// }

	public void onEvent(final CestyChangedEvent aEvent) {
		final Doc doc = aEvent.getModel().getDoc();
		// vyletAno.setText(doc.getPocetWaypointu() + "");
		if (doc.isEmpty()) {
			jSouborSVyletem.setText(".");
			jSouborSVyletem.setToolTipText("Výlet není vůbec definován.");
			jSouborSVyletemPotrebujeUlozit.setText("");
		} else {
			if (doc.getFile() != null) {
				jSouborSVyletem.setText(doc.getFile().getName());
				jSouborSVyletem.setToolTipText(doc.getFile().toString());
				jSouborSVyletemPotrebujeUlozit.setText(doc.isChanged() ? "*" : "");
			} else {
				jSouborSVyletem.setText("-");
				jSouborSVyletem.setToolTipText("S výletem není spojen žádný soubor.");
				jSouborSVyletemPotrebujeUlozit.setText("");
			}
		}
		jPocetKesiVCestach.setText(doc.getPocetWaypointu() + "/" + doc.getPocetCest());
		// vyletNe.setText(cestyModel.get(EVylet.NE).size()+"");
	}

	public void onEvent(final VyletChangeEvent aEvent) {
		final VyletModel vyletModel = aEvent.getVyletModel();
		vyletAno.setText(vyletModel.get(EVylet.ANO).size() + "");
		vyletNe.setText(vyletModel.get(EVylet.NE).size() + "");
	}

	private void prepocitejVzdalenostAAzimut() {
		if (!poziceq.isNoPosition() && cur != null) {
			vzdalenost.setText(vzdalenostPoziceAMysia());
			azimutSmer.setIcon(Ikonizer.findSmerIcon(azimutPoziceAMysi()));
			// azimutSmer.set
			azimutCislo.setText(Math.round(azimutPoziceAMysi()) + "°");
			odPozice.setVisible(true);
			revalidate();
		} else {
			odPozice.setVisible(false);
		}
		// meritkoMapy.setText(coord.getMoumer() + "");
	}

	private String celkove(final KesBag bag) {
		final String s = bag.getWpts().size() + "/" + bag.getKesoidy().size();
		return s;
	}

	private String veVyrezu(final KesBag bag) {
		if (bag == null) {
			return null;
		}
		final int count = bag.getIndexator().count(moord.getBoundingRect());
		final String s = count + "";
		return s;
	}

	private String vzdalenostPoziceAMysia() {
		return Wgs.vzdalenostStr(cur.toWgs(), poziceq.getWgs());
	}

	private double azimutPoziceAMysi() {
		return poziceq.getWgs().azimut(cur.toWgs());
	}

	public void onEvent(final PrekrocenLimitWaypointuVeVyrezuEvent event) {
		setVarujPrekroceni(event.isPrekrocen());
	}

	public void onEvent(final KesoidOnoffEvent event) {
		celkovePoctyVyrez.setSkrtnuto(!event.isOnoff());
		filtrovanePocetyVyrez.setSkrtnuto(!event.isOnoff());
	}

	private void setVarujPrekroceni(final boolean b) {
		if (b) {
			varovaniPoctuPrekrocenych.setText("Překročen limit " + FConst.MAX_POC_WPT_NA_MAPE + " waypointů");
			varovaniPoctuPrekrocenych.setToolTipText("Přibližte mapu nebo vyfiltrujte zbytečné waypointy.");
			varovaniPoctuPrekrocenych.setForeground(Color.RED);
			varovaniPoctuPrekrocenych.setVisible(true);
		} else {
			varovaniPoctuPrekrocenych.setVisible(false);
		}
		revalidate();
	}

	private class JValue extends JTextField {
		private static final long serialVersionUID = 870515243956856500L;

		public JValue() {
			// setFocusable(false);
			setEditable(false);
			setCursor(FKurzory.TEXTOVY_KURZOR);
			// System.out.println("INPUTOVA MAPA: " + getInputMap().keys() );
			setMargin(new Insets(0, 5, 0, 0));
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.JTextField#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			Dimension preferredSize = super.getPreferredSize();
			preferredSize = new Dimension(preferredSize.width + 1, preferredSize.height);
			return preferredSize;
		}

		// @Override
		// public void setText(String s) {
		// super.setText(s);
		// //super.setColumns(s.length());
		// Dimension preferredSize = super.getPreferredSize();
		// preferredSize.width +=3;
		// setPreferredSize(preferredSize);
		// System.out.println(preferredSize);
		// }

	}

	private class JSkrtnutaValue extends JValue {

		private static final long	serialVersionUID	= 4571833579561872745L;

		private boolean				skrtnuto			= true;

		public void setSkrtnuto(final boolean skrtnuto) {
			if (this.skrtnuto == skrtnuto) {
				return;
			}
			this.skrtnuto = skrtnuto;
			repaint();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			if (skrtnuto) {
				g.setColor(Color.RED);
				g.drawLine(0, getHeight(), getWidth(), 0);
			}
		}

	}

	public void inject(final Akce akce) {
		this.akce = akce;
	}
}
