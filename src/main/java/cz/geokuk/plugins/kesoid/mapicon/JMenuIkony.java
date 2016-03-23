package cz.geokuk.plugins.kesoid.mapicon;

import java.util.Map;

import javax.swing.*;

import cz.geokuk.framework.Factory;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;

public class JMenuIkony extends JMenu {

	/**
	 *
	 */
	private static final long	serialVersionUID	= -1589308487736315040L;

	private int					pocetSad;

	private final JMenu			menu;

	private Factory				factory;

	public JMenuIkony() {
		super("Ikony");
		setToolTipText("Výběr sady ikok, řízení, co se na ikonách zobrazuje");
		menu = this;
	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void onEvent(final IkonyNactenyEvent event) {
		final IkonBag bag = event.getBag();
		while (pocetSad > 0) {
			pocetSad--;
			menu.remove(0);
		}
		final ASada aktualniSada = event.getJmenoAktualniSady();
		SwitchSadaAction standard = null;
		final ButtonGroup bg = new ButtonGroup();
		for (final Map.Entry<ASada, Icon> jmenoAIkonaSady : bag.getJmenaAIkonySad().entrySet()) {
			final ASada sada = jmenoAIkonaSady.getKey();
			final SwitchSadaAction akce = new SwitchSadaAction(sada, jmenoAIkonaSady.getValue());
			factory.init(akce);
			final JMenuItem item = new JRadioButtonMenuItem();
			akce.join(item);
			menu.add(item);
			menu.remove(item);
			menu.insert(item, pocetSad);
			bg.add(item);
			pocetSad++;
			if (sada == aktualniSada) {
				akce.setSelected(true);
			}
			if (sada == ASada.STANDARD) {
				standard = akce;
			}
		}
		if (aktualniSada == null && standard != null) {
			standard.setSelected(true);
		}

	}

}
