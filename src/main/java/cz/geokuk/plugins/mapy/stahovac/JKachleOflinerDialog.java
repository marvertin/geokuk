package cz.geokuk.plugins.mapy.stahovac;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.VyrezChangedEvent;
import cz.geokuk.core.program.ZobrazServisniOknoAction;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.DiagnosticsData;
import cz.geokuk.plugins.mapy.kachle.JKachle;
import cz.geokuk.plugins.mapy.kachle.KaAll;
import cz.geokuk.plugins.mapy.kachle.KaAllReq;
import cz.geokuk.plugins.mapy.kachle.KaLoc;
import cz.geokuk.plugins.mapy.kachle.KaSet;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.mapy.kachle.Kaputer;
import cz.geokuk.plugins.mapy.kachle.Priority;

public class JKachleOflinerDialog extends JMyDialog0 implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = 7180968190465321695L;

	private static final Logger log = LogManager.getLogger(JKachleOflinerDialog.class.getSimpleName());

	private static final int LIMIT_DLAZDIC = 100000;

	private JTextPane uvod;
	private JButton spustit;

	private Coord moord;


	private TotoSeTaha totoSeTaha;

	private KachleOflinerSwingWorker kosw;

	private KachleModel kachleModel;

	private KaSet xkaSet;

	private int xminmoumer;

	private ZobrazServisniOknoAction zobrazServisniOknoAction;

	private JKachleOflinerPocetStazenychDialog jVysledek;

	public JKachleOflinerDialog() {
		setTitle("Hromadné dotažení mapových dlaždic");
		init();
	}

	private void registerEvents() {

		spustit.addActionListener(new ActionListener() {


			@Override
			public void actionPerformed(final ActionEvent e) {
				if (kosw != null) {
					kosw.cancel(true);
				}
				zobrazServisniOknoAction.actionPerformed(null);
				dispose();
				jVysledek = factory.init(new JKachleOflinerPocetStazenychDialog());
				jVysledek.setVisible(true);
				kosw = new KachleOflinerSwingWorker(true);
				kosw.execute();
			}
		});


	}

	public void onEvent(final VyrezChangedEvent event) {
		moord = event.getMoord();
		prepocetKachli();
	}

	private void prepocetKachli() {
		if (totoSeTaha != null) {
			if (kosw != null) {
				kosw.cancel(true);
			}
			kosw = new KachleOflinerSwingWorker(false);
			nastavCudl(0);
			kosw.execute();
		}
	}


	@Override
	protected void initComponents() {
		// Napřed registrovat, aby při inicializaci už byl výsledek tady
		final Box box = Box.createVerticalBox();
		add(box);

		uvod = new JTextPane();
		uvod.setContentType("text/html");
		uvod.setText("Tady bude kecání");
		uvod.setPreferredSize(new Dimension(400, 200));
		uvod.setAlignmentX(CENTER_ALIGNMENT);

		spustit = new JButton("Vyberte nějaký výřez");
		spustit.setEnabled(false);
		spustit.setAlignmentX(CENTER_ALIGNMENT);

		box.add(uvod);
		box.add(Box.createVerticalStrut(10));
		box.add(spustit);

	}

	/**
	 * @return
	 */
	private String pokecani() {
		return String.format(
				"<html>Budou stahovány dlaždice mapových pokladů <b>%s</b> v rozmění měřítek " +
						" <b>&lt;%d,%d&gt;</b>" +
						" nyní natavte v hlavním okně výřez mapy který chcete stáhnout. Výřez můžete" +
						" nastavit v libovolném měřítku a v na libovolném mapovém podkladu. " +
						" Pak spusťte stahování tlačítkem. Stahování poběží na pozadí. V servisním okně lze sledovat," +
						" jak se zkracují frony. Stahování nelze zastavit jinak než ukončením programu.",
						totoSeTaha.kaSet.getKts(),
						totoSeTaha.minmoumer,
						totoSeTaha.maxmoumer);
	}

	private void nastavCudl(final int pocetDlazdic)  {
		final boolean b = pocetDlazdic > 0 && pocetDlazdic <= LIMIT_DLAZDIC;
		spustit.setEnabled(b);
		if (b) {
			spustit.setText(String.format("Stáhnout %d dlaždic do cache",  pocetDlazdic));
		} else {
			if (pocetDlazdic == 0) {
				spustit.setText("Počítáme dlaždice");
			} else {
				spustit.setText("Spočítáno " + pocetDlazdic + ", limit je " + LIMIT_DLAZDIC);
			}

		}
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		xkaSet = event.getKaSet();
		xminmoumer = xkaSet.getPodklad().getMinMoumer();
		prepocetKachli();
	}

	public class KachleOflinerSwingWorker extends MySwingWorker0<Integer, JKachle> {

		private final boolean zafrontovati;

		KachleOflinerSwingWorker(final boolean zafrontovat) {
			this.zafrontovati = zafrontovat;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			if (zafrontovati) {
				new Thread(() -> { postahovatNeboSpocitat(true); }, "pro-offline").start();
				return 0;
			} else {
				return postahovatNeboSpocitat(false);
			}

		}

		private Integer postahovatNeboSpocitat(final boolean zafrontovat) {
			int moumer = moord.getMoumer(); // aktuální měřítko
			int w = moord.getWidth();
			int h = moord.getHeight();
			// nastavit na najvětší měřítko
			while (moumer <= totoSeTaha.maxmoumer) {
				w *= 2;
				h *= 2;
				moumer++;
			}

			// a teď projet směrem nahoru
			int pocetKachli = 0;
			while (moumer > totoSeTaha.minmoumer) {
				w /= 2;
				h /= 2;
				moumer--;
				final Coord coco = new Coord(moumer, moord.getMoustred(), new Dimension(w,h), 0.0);

				final Kaputer kaputer = new Kaputer(coco);
				if (log.isTraceEnabled()) {
					log.trace("Vykreslovani kachli od {} pro {} -- {}", kaputer.getKachlePoint(0, 0), kaputer.getKachleMou(0, 0), kaputer);
				}
				for (int yi = 0; yi < kaputer.getPocetKachliY(); yi++) {
					log.trace(" .... řádek", coco);
					for (int xi = 0; xi < kaputer.getPocetKachliX(); xi++) {
						final KaLoc kaloc = kaputer.getKaloc(xi, yi);
						pocetKachli += totoSeTaha.kaSet.getKts().size(); // po každý podklad jedna kachle
						if (zafrontovat) {
							final KaAll kaall = new KaAll(kaloc, totoSeTaha.kaSet);
							final DiagnosticsData.Listener diagListener = (diagnosticsData, diagnosticesFazeStr) ->  {
								log.trace("Stahování offline kache: {}: {}", diagnosticesFazeStr, diagnosticsData);
							};
							final KaAllReq req = new KaAllReq(kaall, kastat -> {
								log.debug("Stahování offline kache: STAŽENO: {} -> {}", kastat);
							}, Priority.STAHOVANI);
							//kachleModel.getZiskavac().hloupěČekejAžNebudeZabránoMocZdrojů();
							kachleModel.getZiskavac().ziskejObsah(req,  DiagnosticsData.create(null, "Stathování pro offline" , diagListener).with("kaAllReq", req));
							//kachleModel.getZiskavac().hloupěČekejAžNebudeZabránoMocZdrojů();
							final int p = pocetKachli;
							if (jVysledek != null) {
								SwingUtilities.invokeLater(() -> { jVysledek.setPocetStazenych(p); });
							}
						}
					}
				}

			}
			return pocetKachli;
		}


		@Override
		protected void donex() throws Exception {
			if (! isCancelled()) {
				final Integer pocet = get();
				nastavCudl(pocet);
			}
		}

	}

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	private class TotoSeTaha {
		private int minmoumer;
		private int maxmoumer;
		private KaSet kaSet;
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
	 */
	@Override
	public void initAfterEventReceiverRegistration() {
		totoSeTaha = new TotoSeTaha();
		totoSeTaha.maxmoumer = moord.getMoumer(); // zapamatovat si měřítko, do něj budeme kachlovat
		totoSeTaha.minmoumer = xminmoumer;
		totoSeTaha.kaSet = xkaSet;
		registerEvents();
		prepocetKachli();
		uvod.setText(pokecani());
	}

	public void inject(final ZobrazServisniOknoAction zobrazServisniOknoAction) {
		this.zobrazServisniOknoAction = zobrazServisniOknoAction;
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "StahovaniMapovychDlazdic";
	}


}
