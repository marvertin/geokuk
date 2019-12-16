package cz.geokuk.util.procak;

import static org.junit.Assert.fail;

import java.util.*;

import org.junit.Test;

import lombok.Data;

public class TestVykonnostiPoleOprotiMape {

	static int N = 6;
	final static TestVykonnostiPoleOprotiMape[] zdroj = new TestVykonnostiPoleOprotiMape[N];

	static {
		for (int i =0; i< N; i++) {
			zdroj[i] = new TestVykonnostiPoleOprotiMape();
		}
	}


	final static Map<TestVykonnostiPoleOprotiMape, String> map = new IdentityHashMap<TestVykonnostiPoleOprotiMape, String>();
	final static List<Dvoj> list = new LinkedList<>();
	final static Dvoj[] pole = new Dvoj[zdroj.length];

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	private  static String getZPole(final TestVykonnostiPoleOprotiMape ppp) {
		for (final Dvoj d : pole) {
			if (ppp == d.obj) {
				return d.vysl;
			}
		}
		return null;
	}

	private static String getZListu(final TestVykonnostiPoleOprotiMape ppp) {
		for (final Dvoj d : list) {
			if (ppp == d.obj) {
				return d.vysl;
			}
		}
		return null;
	}


	private  static String getZMapy(final TestVykonnostiPoleOprotiMape ppp) {
		return map.get(ppp);
	}


	public static void main(final String[] args) {


		for (int i=0; i<zdroj.length; i++) {
			pole[i] = new Dvoj(zdroj[i], "v poli " + i);
			map.put(zdroj[i], "v mapě " + i);
			list.add(new Dvoj(zdroj[i], "v mapě " + i));
		}

		final long start = System.currentTimeMillis();

		for (int i=0; i< 1_000_000; i++) {
			final int j = i % N;
			//final int j = 0;
			final TestVykonnostiPoleOprotiMape p = zdroj[j];
			//getZListu(p);
			//getZPole(p);
			getZMapy(p);
		}
		final long stop = System.currentTimeMillis();
		System.out.println("Trvani: " + (stop - start));
	}

	@Data
	private static class Dvoj {
		final TestVykonnostiPoleOprotiMape obj;
		final String vysl;
	}
}
