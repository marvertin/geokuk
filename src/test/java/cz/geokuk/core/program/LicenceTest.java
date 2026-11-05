package cz.geokuk.core.program;

import org.junit.Assert;
import org.junit.Test;

public class LicenceTest {

	@Test
	public void programObsahujeLicenciASeznamKnihoven() {
		final String text = JOProgramuDialog.textLicence().replace("\r\n", "\n");
		Assert.assertTrue(text.contains("SQLJet"));
		Assert.assertTrue(text.contains("© ČÚZK, RÚIAN, licence CC BY 4.0"));
		Assert.assertTrue(text.contains("Sun Microsystems"));
		Assert.assertTrue(text.contains("Copyright (c) 1995 - 2008 Sun Microsystems"));
		Assert.assertTrue(text.contains("GNU GENERAL PUBLIC LICENSE"));
		Assert.assertTrue(text.contains("Copyright (c) 2004-2023 QOS.ch"));
		Assert.assertTrue(text.contains("Copyright (c) 2005-2009 Terence Parr"));
		Assert.assertTrue(text.contains("Copyright (c) Adobe Systems Incorporated"));
		Assert.assertTrue(text.contains("Copyright (c) 2006, David Crawshaw"));
		Assert.assertTrue(text.contains("GNU LESSER GENERAL PUBLIC LICENSE"));
		Assert.assertTrue(text.contains("Apache License"));
		Assert.assertTrue(text.contains("sqljet-1.1.15-sources.jar"));
		Assert.assertTrue(text.contains("ČÚZK"));
		Assert.assertTrue(text.contains("Freemap Slovakia"));
		Assert.assertTrue(text.contains("ZBGIS"));
		Assert.assertTrue(text.contains("mapsforge"));
		Assert.assertTrue(text.contains("mapsforge-map-0.25.0-sources.jar"));
		Assert.assertTrue(text.contains("Copyright (c) 2004, Mark McKay"));
		Assert.assertTrue(text.contains("Copyright (c) 2002,2003, Stefan Haustein"));
		Assert.assertTrue(text.contains("GNU LESSER GENERAL PUBLIC LICENSE\n                       Version 3, 29 June 2007"));
	}
}
