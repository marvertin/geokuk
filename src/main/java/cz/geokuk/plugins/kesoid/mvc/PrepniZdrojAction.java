package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;

import javax.swing.*;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.importek.StavZdroju;
import cz.geokuk.plugins.kesoid.importek.StavZdroju.StavVyberu;
import cz.geokuk.plugins.kesoid.importek.TypZdroje;

/** Zapne nebo vypne typ zdroje keší; stejně jako zaškrtávátko v Přehledu zdrojů a se zkratkou Ctrl+číslo. */
public class PrepniZdrojAction extends ToggleAction0 {

	private static final long serialVersionUID = 1L;
	private final TypZdroje typ;
	private KesoidModel kesoidModel;
	private StavZdroju stav = StavZdroju.PRAZDNY;

	public PrepniZdrojAction(final TypZdroje typ) {
		super(typ.getNazev());
		this.typ = typ;
		final String zkratka = ZkratkyZdroju.text(typ);
		putValue(SHORT_DESCRIPTION, "Zapne nebo vypne načítání zdroje " + typ.getNazev() + (zkratka == null ? "." : " (" + zkratka + ")."));
		putValue(Action.ACCELERATOR_KEY, ZkratkyZdroju.zkratka(typ));
	}

	public TypZdroje getTyp() {
		return typ;
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void onEvent(final StavZdrojuEvent event) {
		stav = event.getStav();
		// Typ bez položek (zdroj není aktivní nebo nic nenašel) se nedá zapnout ani vypnout; položka menu a zkratka jsou pak vypnuté.
		setEnabled(!stav.getPolozky(typ).isEmpty());
		setSelected(stav.getStavVyberuTypu(typ) != StavVyberu.VYPNUTO);
	}

	/** Přepne typ stejně jako klik na jeho zaškrtávátko. */
	public void prepni() {
		if (!isEnabled()) {
			return;
		}
		JTabulkaZdroju.klikTyp(stav, typ, kesoidModel);
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		prepni();
	}

	/** Naváže na okno zkratku z číselné klávesnice; zkratky horní řady dává menu přes ACCELERATOR_KEY. */
	public static void navazNumpad(final JRootPane koren, final PrepniZdrojAction... akce) {
		for (final PrepniZdrojAction zdroj : akce) {
			final KeyStroke numpad = ZkratkyZdroju.zkratkaNumpad(zdroj.typ);
			if (numpad != null) {
				final String klic = "prepniZdroj" + zdroj.typ;
				koren.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(numpad, klic);
				koren.getActionMap().put(klic, new AbstractAction() {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(final ActionEvent e) {
						zdroj.prepni();
					}
				});
			}
		}
	}
}
