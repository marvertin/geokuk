package cz.tconsult.geokuk_container_test.good0;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cz.geokuk.framework.container.Container;

public class ContainerTest0 {

	private Container container;

	@Before
	public void setUp() {
		container = Container.scan(this.getClass().getPackage().getName());
	}

	@Test
	public void test0() {
		final Byn0 byn = container.getSingletonInstance(Byn0.class);
		assertNotNull(byn);
	}

	@Test
	public void test1() {
		final TestObj inst1 = container.newInstance(TestObj.class);
		final TestObj inst2 = container.newInstance(TestObj.class);
		assertNotSame(inst1, inst2);

		assertSame(inst1.byn1, inst2.byn1);
		assertSame(inst1.byn2, inst2.byn2);
		assertSame(inst1.byn1, inst2.byn2);
		assertSame(inst1.byn1, inst2.byn1);
	}
}
