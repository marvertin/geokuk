package cz.geokuk.core.program;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.SledovaniVybraneKese;

/** EventManager drží posluchače jen slabě; kdo je nikde nedrží, o události přijde. */
public class JMainFrameDrziPosluchaceTest {

	@Test
	public void sledovaniVybraneKeseJeDrzenoPoleHlavnihoOkna() {
		boolean nalezeno = false;
		for (final Field f : JMainFrame.class.getDeclaredFields()) {
			nalezeno |= f.getType() == SledovaniVybraneKese.class && !Modifier.isStatic(f.getModifiers());
		}
		Assert.assertTrue(nalezeno);
	}
}
