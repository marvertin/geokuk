package cz.geokuk.plugins.kesoid;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.framework.Onoff;
import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.importek.StavZdroju;
import cz.geokuk.plugins.kesoid.importek.TypZdroje;
import cz.geokuk.plugins.kesoid.importek.RegistrStavuZdroju;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidyOnoffAction;
import cz.geokuk.plugins.kesoid.mvc.PrepniZdrojAction;
import cz.geokuk.plugins.kesoidkruhy.JednotkoveKruhyAction;
import cz.geokuk.plugins.kesoidkruhy.KruhyModel;
import cz.geokuk.plugins.kesoidkruhy.KruhyOnoffAction;
import cz.geokuk.plugins.kesoidkruhy.KruhySettings;
import cz.geokuk.plugins.kesoidobsazenost.ObsazenostModel;
import cz.geokuk.plugins.kesoidobsazenost.ObsazenostOnoffAction;

/** Zkratky ukazují zprávu jen po uživatelově akci a podle skutečného stavu; zapojení akcí na zprávy. */
public class ZkratkyToastTest {

	private final List<Event0<?>> zpravy = new ArrayList<>();
	private final ToastModel toast = new ToastModel();
	private final StavVrstevModel stav = new StavVrstevModel();

	public ZkratkyToastTest() {
		toast.inject((EventFirer) zpravy::add);
		stav.inject((EventFirer) e -> {
		});
	}

	private static JCheckBoxMenuItem zapoj(final ToggleAction0 akce) {
		final JCheckBoxMenuItem m = new JCheckBoxMenuItem();
		akce.join(m);
		return m;
	}

	private String posledni() {
		return zpravy.isEmpty() ? null : ((ToastEvent) zpravy.get(zpravy.size() - 1)).getText();
	}

	@Test
	public void f5ZpravaPodleNovehoStavu() {
		final KesoidyOnoffAction akce = new KesoidyOnoffAction();
		akce.inject(mock(KesoidModel.class));
		akce.inject(toast);
		akce.setSelected(true);
		final JCheckBoxMenuItem m = zapoj(akce);
		m.doClick();
		Assert.assertEquals("Kešoidy – Vypnuto, včetně kruhů, obsazenosti a popisků", posledni());
		m.doClick();
		Assert.assertEquals("Kešoidy – Zapnuto", posledni());
	}

	@Test
	public void programovaZmenaStavuZpravuNeukaze() {
		final KesoidyOnoffAction akce = new KesoidyOnoffAction();
		akce.inject(mock(KesoidModel.class));
		akce.inject(toast);
		zapoj(akce);
		akce.setSelected(true);
		akce.setSelected(false);
		Assert.assertTrue(zpravy.isEmpty());
	}

	private KruhyModel kruhyModel(final KruhySettings s) {
		final KruhyModel model = mock(KruhyModel.class);
		when(model.getData()).thenReturn(s);
		return model;
	}

	@Test
	public void f8AkceStavAParametrIKdyzJsouKruhySkryte() {
		final KruhyOnoffAction akce = new KruhyOnoffAction();
		akce.inject(kruhyModel(new KruhySettings()));
		akce.inject(stav);
		akce.inject(toast);
		final JCheckBoxMenuItem m = zapoj(akce);
		m.doClick();
		Assert.assertEquals("Zvýrazňovací kruhy – Zapnuto, 20 px", posledni());
		m.doClick();
		Assert.assertEquals("Zvýrazňovací kruhy – Vypnuto, 20 px", posledni());
		stav.nahlas(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY, DuvodNekresleni.PRILIS_HUSTE, 0);
		m.doClick();
		Assert.assertEquals("Zvýrazňovací kruhy – Zapnuto, 20 px, ale skryté: příliš husté, přibližte mapu", posledni());
	}

	@Test
	public void f8VMetrechUvedePoloměr() {
		final KruhySettings s = new KruhySettings();
		s.setJednotkovaVelikost(true);
		s.setPolomerMetru(1500);
		final KruhyOnoffAction akce = new KruhyOnoffAction();
		akce.inject(kruhyModel(s));
		akce.inject(stav);
		akce.inject(toast);
		zapoj(akce).doClick();
		Assert.assertEquals("Zvýrazňovací kruhy – Zapnuto, 1,5 km", posledni());
	}

	@Test
	public void altF8AkceStavPolomerAZapnutiF8() {
		final KruhySettings s = new KruhySettings();
		s.setOnoff(false);
		final JednotkoveKruhyAction akce = new JednotkoveKruhyAction();
		akce.inject(kruhyModel(s));
		akce.inject(stav);
		akce.inject(toast);
		final JCheckBoxMenuItem m = zapoj(akce);
		m.doClick();
		Assert.assertEquals("Kruhy v metrech – Zapnuto, 100 m (zapnuly se i zvýrazňovací kruhy)", posledni());
		Assert.assertTrue(s.isOnoff());
		m.doClick();
		Assert.assertEquals("Kruhy v metrech – Vypnuto, 100 m", posledni());
		m.doClick();
		Assert.assertEquals("F8 už svítilo", "Kruhy v metrech – Zapnuto, 100 m", posledni());
	}

	@Test
	public void f9ZoomPodJedenactNeboLimit() throws Exception {
		final ObsazenostModel model = mock(ObsazenostModel.class);
		final Field f = ObsazenostModel.class.getField("visible");
		f.setAccessible(true);
		f.set(model, mock(Onoff.class));
		final ObsazenostOnoffAction akce = new ObsazenostOnoffAction();
		akce.inject(model);
		akce.inject(stav);
		akce.inject(toast);
		final JCheckBoxMenuItem m = zapoj(akce);
		m.doClick();
		Assert.assertEquals("Obsazenost – Zapnuto, 161 m", posledni());
		m.doClick();
		Assert.assertEquals("Obsazenost – Vypnuto, 161 m", posledni());
		stav.nahlas(VrstvaNadMapou.OBSAZENOST, DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		m.doClick();
		Assert.assertEquals("Obsazenost – Zapnuto, 161 m, ale kreslí se až po přiblížení mapy", posledni());
	}

	@Test
	public void zdrojeZpravaZRegistruPoPrepnuti() {
		final KesoidModel model = mock(KesoidModel.class);
		final RegistrStavuZdroju registr = mock(RegistrStavuZdroju.class);
		when(registr.getSnimek()).thenReturn(StavZdroju.PRAZDNY);
		when(model.getRegistrStavuZdroju()).thenReturn(registr);
		final PrepniZdrojAction akce = new PrepniZdrojAction(TypZdroje.values()[2]);
		akce.inject(model);
		akce.inject(toast);
		akce.setEnabled(true);
		akce.prepni();
		Assert.assertEquals("Zdroj keší " + TypZdroje.values()[2].getNazev() + " – Vypnuto, žádný zdroj keší není zapnutý", posledni());
		akce.setEnabled(false);
		zpravy.clear();
		akce.prepni();
		Assert.assertEquals("vypnutá akce nic nepřepne, ale zkratka řekne proč", "Zdroj keší " + TypZdroje.values()[2].getNazev() + " – nemá načtené soubory", posledni());
	}

	@Test
	public void tooltipVypnuteAkceVysvetliProc() {
		final PrepniZdrojAction akce = new PrepniZdrojAction(TypZdroje.GPX);
		final String zapnuta = (String) akce.getValue(javax.swing.Action.SHORT_DESCRIPTION);
		Assert.assertTrue(zapnuta, zapnuta.contains("Ctrl+1"));
		akce.setEnabled(false);
		final String vypnuta = (String) akce.getValue(javax.swing.Action.SHORT_DESCRIPTION);
		Assert.assertEquals("Zdroj GPX nemá žádné načtené soubory. Složku zdroje zkontrolujte v Soubor > Umístění souborů.", vypnuta);
		akce.setEnabled(true);
		Assert.assertEquals(zapnuta, akce.getValue(javax.swing.Action.SHORT_DESCRIPTION));
	}

	@Test
	public void horniRadaCtrlCisloVysvetliVypnutyZdrojAJinakMlci() {
		final KesoidModel model = mock(KesoidModel.class);
		final PrepniZdrojAction akce = new PrepniZdrojAction(TypZdroje.GPX);
		akce.inject(model);
		akce.inject(toast);
		akce.setEnabled(false);
		final javax.swing.JRootPane koren = new javax.swing.JRootPane();
		PrepniZdrojAction.navazNumpad(koren, akce);
		final javax.swing.KeyStroke ctrl1 = cz.geokuk.plugins.kesoid.mvc.ZkratkyZdroju.zkratka(TypZdroje.GPX);
		final Object klic = koren.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).get(ctrl1);
		Assert.assertNotNull("horní řada je u vypnutého zdroje navázaná", klic);
		final javax.swing.Action vysvetleni = koren.getActionMap().get(klic);
		Assert.assertTrue(vysvetleni.isEnabled());
		vysvetleni.actionPerformed(new java.awt.event.ActionEvent(this, 0, ""));
		Assert.assertEquals("Zdroj keší GPX – nemá načtené soubory", posledni());
		akce.setEnabled(true);
		Assert.assertFalse("zapnutý zdroj dostane zkratku z menu, náhradní vazba se vypne", vysvetleni.isEnabled());
		akce.setEnabled(false);
		Assert.assertTrue(vysvetleni.isEnabled());
	}

	@Test
	public void horniRadaJeUZapnutehoZdrojeBezNahradniVazby() {
		final PrepniZdrojAction akce = new PrepniZdrojAction(TypZdroje.GPX);
		akce.setEnabled(true);
		final javax.swing.JRootPane koren = new javax.swing.JRootPane();
		PrepniZdrojAction.navazNumpad(koren, akce);
		final Object klic = koren.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).get(cz.geokuk.plugins.kesoid.mvc.ZkratkyZdroju.zkratka(TypZdroje.GPX));
		Assert.assertFalse("zapnutý zdroj: zkratku zpracuje menu, náhradní vazba je vypnutá", koren.getActionMap().get(klic).isEnabled());
	}
}
