package cz.geokuk.plugins.kesoid.mapicon;


import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import cz.geokuk.framework.Factory;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;

public class JMenuIkony extends JMenu {

	/**
	 *
	 */
	private static final long serialVersionUID = -1589308487736315040L;

	private int pocetSad;

	private final JMenu menu;

	private Factory factory;


	public JMenuIkony() {
		super ("Ikony");
		setToolTipText("Výběr sady ikok, řízení, co se na ikonách zobrazuje");
		this.menu = this;
	}


	public void onEvent(IkonyNactenyEvent event) {
		IkonBag bag = event.getBag();
		while (pocetSad > 0) {
			pocetSad --;
			menu.remove(0);
		}
		ASada aktualniSada = event.getJmenoAktualniSady();
		SwitchSadaAction standard = null;
		ButtonGroup bg = new ButtonGroup();
		for (Map.Entry<ASada, Icon> jmenoAIkonaSady : bag.getJmenaAIkonySad().entrySet()) {
			final ASada sada = jmenoAIkonaSady.getKey();
			SwitchSadaAction akce = new SwitchSadaAction(sada, jmenoAIkonaSady.getValue());
			factory.init(akce);
			JMenuItem item = new JRadioButtonMenuItem();
			akce.join(item);
			menu.add(item);
			menu.remove(item);
			menu.insert(item, pocetSad);
			bg.add(item);
			pocetSad++;
			if (sada == aktualniSada) akce.setSelected(true);
			if (sada == ASada.STANDARD) standard = akce;
		}
		if (aktualniSada == null && standard != null) {
			standard.setSelected(true);
		}

	}

	public void inject(Factory factory) {
		this.factory = factory;
	}


}
