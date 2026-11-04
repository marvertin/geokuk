package cz.geokuk.plugins.kesoid.mvc;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.importek.TypZdroje;

/** Zpráva po Ctrl+číslo vychází ze skutečného stavu zdrojů: počítají se jen zapnuté typy, které mají položky. */
public class PrepniZdrojActionTest {

	@Test
	public void vsechnyTypyZapnute() {
		final StavyZdrojuProTesty s = StavyZdrojuProTesty.vzorek();
		Assert.assertEquals(Arrays.asList(TypZdroje.GPX.getNazev(), TypZdroje.GEOGET.getNazev(), TypZdroje.GSAK.getNazev(), TypZdroje.OPENSAK.getNazev()),
				PrepniZdrojAction.zapnuteZdroje(s.snimek()));
	}

	@Test
	public void vypnutyTypSeNepocita() {
		final StavyZdrojuProTesty s = StavyZdrojuProTesty.vzorek();
		s.vypnuteTypy.add(TypZdroje.GEOGET);
		s.prepisZapnuti();
		Assert.assertEquals(Arrays.asList(TypZdroje.GPX.getNazev(), TypZdroje.GSAK.getNazev(), TypZdroje.OPENSAK.getNazev()), PrepniZdrojAction.zapnuteZdroje(s.snimek()));
	}

	@Test
	public void typBezPolozekSeNepocita() {
		final StavyZdrojuProTesty s = new StavyZdrojuProTesty();
		final File gpx = s.pridej(TypZdroje.GPX, "praha.gpx");
		final File gsak = s.pridej(TypZdroje.GSAK, "Domov.db3");
		s.prepis();
		s.registr.hotovo(s.registr.getGenerace(), gpx, 10, 10);
		s.registr.hotovo(s.registr.getGenerace(), gsak, 10, 10);
		Assert.assertEquals(Arrays.asList(TypZdroje.GPX.getNazev(), TypZdroje.GSAK.getNazev()), PrepniZdrojAction.zapnuteZdroje(s.snimek()));
	}

	@Test
	public void bezJakychkolivZdrojuNicNeniZapnute() {
		Assert.assertTrue(PrepniZdrojAction.zapnuteZdroje(new StavyZdrojuProTesty().prepis().snimek()).isEmpty());
	}
}
