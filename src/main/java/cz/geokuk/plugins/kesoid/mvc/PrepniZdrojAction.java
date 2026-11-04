package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.ToastModel;
import cz.geokuk.plugins.kesoid.ZpravyZkratek;
import cz.geokuk.plugins.kesoid.importek.StavZdroju;
import cz.geokuk.plugins.kesoid.importek.StavZdroju.StavVyberu;
import cz.geokuk.plugins.kesoid.importek.TypZdroje;

/** Zapne nebo vypne typ zdroje keší; stejně jako zaškrtávátko v Přehledu zdrojů a se zkratkou Ctrl+číslo. */
public class PrepniZdrojAction extends ToggleAction0 {

	private static final long serialVersionUID = 1L;
	private final TypZdroje typ;
	private KesoidModel kesoidModel;
	private ToastModel toast;
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

	public void inject(final ToastModel toast) {
		this.toast = toast;
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
		// Událost se stavem přijde později, stav po přepnutí je hned v registru.
		if (toast != null) {
			toast.ukaz(ZpravyZkratek.zdroje(zapnuteZdroje(kesoidModel.getRegistrStavuZdroju().getSnimek())));
		}
	}

	/** Názvy typů zdrojů, které jsou po přepnutí zapnuté, i částečně. */
	static List<String> zapnuteZdroje(final StavZdroju stav) {
		final List<String> nazvy = new ArrayList<>();
		for (final TypZdroje t : TypZdroje.values()) {
			if (!stav.getPolozky(t).isEmpty() && stav.getStavVyberuTypu(t) != StavVyberu.VYPNUTO) {
				nazvy.add(t.getNazev());
			}
		}
		return nazvy;
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
