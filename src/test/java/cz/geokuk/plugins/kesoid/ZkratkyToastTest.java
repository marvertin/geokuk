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
		Assert.assertEquals("Kešoidy skryté včetně kruhů, obsazenosti a popisků", posledni());
		m.doClick();
		Assert.assertEquals("Kešoidy zobrazené", posledni());
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
	public void f8JenKdyzNejsouVideKruhy() {
		final KruhyOnoffAction akce = new KruhyOnoffAction();
		akce.inject(kruhyModel(new KruhySettings()));
		akce.inject(stav);
		akce.inject(toast);
		final JCheckBoxMenuItem m = zapoj(akce);
		m.doClick();
		Assert.assertNull("kruhy jsou vidět, F8 je zřejmé", posledni());
		m.doClick();
		stav.nahlas(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY, DuvodNekresleni.PRILIS_HUSTE, 0);
		m.doClick();
		Assert.assertEquals("Kruhy zapnuté, ale skryté: příliš husté, přibližte mapu", posledni());
	}

	@Test
	public void altF8ZapneObojeKdyzF8NebyloAJinakMlci() {
		final KruhySettings s = new KruhySettings();
		s.setOnoff(false);
		final JednotkoveKruhyAction akce = new JednotkoveKruhyAction();
		akce.inject(kruhyModel(s));
		akce.inject(stav);
		akce.inject(toast);
		final JCheckBoxMenuItem m = zapoj(akce);
		m.doClick();
		Assert.assertEquals("Zapnuté zvýrazňovací i jednotkové kruhy", posledni());
		Assert.assertTrue(s.isOnoff());
		zpravy.clear();
		m.doClick();
		Assert.assertTrue("vypnutí jednotkových nic nevysvětluje", zpravy.isEmpty());
		m.doClick();
		Assert.assertTrue("F8 už svítilo", zpravy.isEmpty());
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
		Assert.assertNull(posledni());
		m.doClick();
		stav.nahlas(VrstvaNadMapou.OBSAZENOST, DuvodNekresleni.POLOMER_PRILIS_MALY, 0);
		m.doClick();
		Assert.assertEquals("Obsazenost se kreslí až po přiblížení mapy", posledni());
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
		Assert.assertEquals("Zdroje keší: žádný", posledni());
		akce.setEnabled(false);
		zpravy.clear();
		akce.prepni();
		Assert.assertTrue("vypnutá akce nic nepřepne, zpráva nemá smysl", zpravy.isEmpty());
	}
}
