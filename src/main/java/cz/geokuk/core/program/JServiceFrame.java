/**
 *
 */
package cz.geokuk.core.program;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.*;

import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.util.pocitadla.*;

/**
 * @author Martin Veverka
 *
 */
public class JServiceFrame extends JMyDialog0 implements Pocitadlo.Callback {

	private static final long serialVersionUID = 5761908785083097975L;

	private final Map<Pocitadlo, JLabel> hodmap = new WeakHashMap<>();

	public static void main(final String[] args) {
		final JServiceFrame serviceFrame = new JServiceFrame();
		serviceFrame.setVisible(true);

		for (int i = 0; i < 12; i++) {
			Pocitadlo poc = null;
			if (i % 3 == 0) {
				poc = new PocitadloRoste("pociA " + i, "Popis počítadla " + i);
			}
			if (i % 3 == 1) {
				poc = new PocitadloMalo("pociB " + i, "Popis počítadla " + i);
			}
			if (i % 3 == 2) {
				poc = new PocitadloNula("pociC " + i, "Popis počítadla " + i);
			}
			final Pocitadlo po = poc;
			final int ii = i;
			final Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(ii * 1000);
						for (;;) {
							po.add(ii);
							Thread.sleep((ii + 5) * 100);
						}
					} catch (final InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			};
			thr.setDaemon(true);
			thr.start();

		}

	}

	public JServiceFrame() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		initComponents();
		pack();
		Pocitadlo.callback = this;
		registerEvents();

		// TODO při zavírání JServiceFramese musí zlikvidovat také odkaz v počítadlech
	}

	@Override
	public void onChange() {
		SystemovaPocitadla.spustPocitani();
		final List<Pocitadlo> pocitadla = new ArrayList<>(SpravcePocitadel.getPocitadla());
		// initComponents();
		// pack();
		if (hodmap == null || pocitadla.size() != hodmap.size()) {
			// System.out.println("NEsouhlasi pocet pocitadel a hodnot: " + pocitadla.size() + " == " + hodmap.size());
			initComponents(pocitadla);
			pack();
		}
		for (final Pocitadlo pocitadlo : pocitadla) {
			final JLabel jLabel = hodmap.get(pocitadlo);
			if (jLabel != null) {
				jLabel.setText(pocitadlo.get() + "");
			}
		}
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "Service";
	}

	@Override
	protected void initComponents() {}

	protected void initComponents(final List<Pocitadlo> pocitadla) {
		final Box b = createtComponents(pocitadla);
		getContentPane().removeAll();
		getContentPane().add(b);
	}

	Set<String> seznamTypu(final List<Pocitadlo> pocitadla) {
		final Set<String> types = new HashSet<>();
		for (final Pocitadlo p : pocitadla) {
			types.add(p.getTextovyPopisTypu());
		}
		// System.out.println(types);
		return types;
	}

	private JPanel createGcButton() {
		final JPanel jGcPanel = new JPanel();
		final JLabel jMemoryPoGc = new JLabel();
		jGcPanel.add(jMemoryPoGc);
		final JButton gcbutt = new JButton("GC");
		jGcPanel.add(gcbutt);
		gcbutt.addActionListener(new ActionListener() {
			private long minulePouzitaPamet;

			@Override
			public void actionPerformed(final ActionEvent e) {
				System.out.println("Garbage collector spuštěn");
				System.gc();
				final long pouzitaPamet = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				final long narustPameti = pouzitaPamet - minulePouzitaPamet;
				minulePouzitaPamet = pouzitaPamet;
				jMemoryPoGc.setText(pouzitaPamet / 1000 + " KiB  |  rozdil=" + narustPameti / 1000 + " KiB");
				System.out.println("Garbage collector ukončen");
			}
		});
		return jGcPanel;
	}

	private Box createtComponents(final List<Pocitadlo> pocitadla) {
		Box b;
		// System.out.println("INICOMP");
		hodmap.clear();
		b = Box.createVerticalBox();
		final JPanel jGcPanel = createGcButton();
		b.add(jGcPanel);
		for (final String typ : seznamTypu(pocitadla)) {
			b.add(nadpis(typ));
			final JPanel panel = initJedenPanel(typ, pocitadla);
			b.add(panel);
			b.add(Box.createVerticalStrut(10));
		}
		return b;
	}

	/**
	 *
	 */
	private JPanel initJedenPanel(final String typPocitadla, final List<Pocitadlo> pocitadla) {
		final JPanel pan = new JPanel();
		pan.setBorder(BorderFactory.createEtchedBorder());
		pan.setLayout(new GridBagLayout());
		pan.removeAll();
		if (pocitadla == null || pocitadla.size() == 0) {
			pan.add(new JLabel("Nejsou pocitadla"));
			return pan;
		}
		final GridBagConstraints c = new GridBagConstraints();
		int i = 0;
		// hodnoty.clear();
		final List<Pocitadlo> pocitadlaFiltrovanaSerazena = pocitadla.stream().filter(p -> p.getTextovyPopisTypu().equals(typPocitadla)).sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
		        .collect(Collectors.toList());
		for (final Pocitadlo p : pocitadlaFiltrovanaSerazena) {
			final JLabel label = new JLabel(p.getName() + ": ");
			label.setToolTipText(p.getDescription());
			final JLabel value = new JLabel(p.get() + "");
			value.setToolTipText(p.getDescription());
			hodmap.put(p, value);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridy = i;
			c.gridx = 0;
			c.ipadx = 10;
			c.anchor = GridBagConstraints.LINE_START;
			pan.add(label, c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.LINE_END;
			pan.add(value, c);
			i++;
		}
		return pan;
	}

	private JTextField nadpis(final String typ) {
		final JTextField lbl = new JTextField(typ);
		lbl.setEditable(false);
		return lbl;
	}

	private void registerEvents() {
		// Zastavit počítání při uzavení okna
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent e) {
				Pocitadlo.callback = null;
			}

			@Override
			public void windowClosing(final WindowEvent e) {
				Pocitadlo.callback = null;
			}
		});
	}

}
