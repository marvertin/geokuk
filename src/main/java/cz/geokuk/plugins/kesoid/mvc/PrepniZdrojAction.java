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
		putValue(SHORT_DESCRIPTION, popis(true));
		putValue(Action.ACCELERATOR_KEY, ZkratkyZdroju.zkratka(typ));
	}

	/** Tooltip položky menu: co dělá, a když je vypnutá, proč. */
	String popis(final boolean zapnutelny) {
		final String zkratka = ZkratkyZdroju.text(typ);
		if (zapnutelny) {
			return "Zapne nebo vypne načítání zdroje " + typ.getNazev() + (zkratka == null ? "." : " (" + zkratka + ").");
		}
		return "Zdroj " + typ.getNazev() + " nemá žádné načtené soubory. Složku zdroje zkontrolujte v Soubor > Umístění souborů.";
	}

	@Override
	public void setEnabled(final boolean enabled) {
		putValue(SHORT_DESCRIPTION, popis(enabled));
		super.setEnabled(enabled);
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
			// Vypnutý zdroj nemá co přepnout; zkratka, která by jinak mlčela, vysvětlí proč.
			if (toast != null) {
				toast.ukaz(ZpravyZkratek.zdrojBezSouboru(typ.getNazev()));
			}
			return;
		}
		JTabulkaZdroju.klikTyp(stav, typ, kesoidModel);
		// Událost se stavem přijde později, stav po přepnutí je hned v registru.
		if (toast != null) {
			final StavZdroju snimek = kesoidModel.getRegistrStavuZdroju().getSnimek();
			toast.ukaz(ZpravyZkratek.zdroj(typ.getNazev(), snimek.getStavVyberuTypu(typ), zapnuteZdroje(snimek)));
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

	/**
	 * Naváže na okno zkratku z číselné klávesnice; zkratky horní řady dává menu přes ACCELERATOR_KEY, ale vypnutá položka menu zkratku nezpracuje, takže pro vypnutý zdroj se horní řada
	 * váže zvlášť (jen dokud je zdroj vypnutý) a ukáže, proč se nic nestalo.
	 */
	public static void navazNumpad(final JRootPane koren, final PrepniZdrojAction... akce) {
		for (final PrepniZdrojAction zdroj : akce) {
			final KeyStroke horniRada = ZkratkyZdroju.zkratka(zdroj.typ);
			if (horniRada != null) {
				final String klicVypnuteho = "prepniZdrojVypnuty" + zdroj.typ;
				final AbstractAction vysvetleni = new AbstractAction() {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(final ActionEvent e) {
						zdroj.prepni();
					}
				};
				vysvetleni.setEnabled(!zdroj.isEnabled());
				zdroj.addPropertyChangeListener(e -> {
					if ("enabled".equals(e.getPropertyName())) {
						vysvetleni.setEnabled(!zdroj.isEnabled());
					}
				});
				koren.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(horniRada, klicVypnuteho);
				koren.getActionMap().put(klicVypnuteho, vysvetleni);
			}
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
