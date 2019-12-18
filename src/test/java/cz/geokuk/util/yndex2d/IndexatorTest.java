package cz.geokuk.util.yndex2d;

import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;
import lombok.SneakyThrows;


public class IndexatorTest {


	private TestBod b(final int x, final int y) {
		return new TestBod(x, y);
	}

	private void pro(final BoundingRect br, final Collection<TestBod> col) {
		final List<TestBod> zdroj = col.parallelStream()
				.filter(b -> b.getX() >= br.xx1 && b.getX() < br.xx2 && b.getY() >= br.yy1 && b.getY() < br.yy2
						).collect(Collectors.toList());
		Collections.sort(zdroj);

		final Indexator<TestBod> indexator = col.parallelStream().reduce(new Indexator<TestBod>(BoundingRect.ALL),
				(ind, b) -> ind.add(b.getX(), b.getY(), b),
				Indexator::merge).bound(br);
		System.out.println(indexator.getCount());
		//indexator.vypis();
		final List<TestBod> result = indexator.parallelStream().collect(Collectors.toList());
		Collections.sort(result);

		//System.out.println(zdroj);
		//System.out.println(result);
		Assert.assertEquals(zdroj, result);

	}

	private List<TestBod> matice(final int n, final int factor) {
		final List<TestBod> list = new ArrayList<>(n);
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < n; y++) {
				list.add(b(x * factor ,y * factor));
			}
		}
		return list;
	}

	@Test
	public void test0() {
		pro(BoundingRect.ALL, Collections.emptySet());
	}

	@Test
	public void test1() {
		pro(BoundingRect.ALL, Collections.singleton(b(13, 27)));
	}

	@Test
	public void test2a() {
		pro(BoundingRect.ALL, Lists.newArrayList(b(13, 27), b(500,800)));
	}

	@Test
	public void test2b() {
		pro(BoundingRect.ALL, Lists.newArrayList(b(13, 27), b(13,27)));
	}

	@Test
	public void testMatice2() {
		pro(BoundingRect.ALL, matice(2, 100));
	}

	@Test
	public void testMatice10() {
		pro(BoundingRect.ALL, matice(10, 100));
	}

	@Test
	public void testMatice10a() {
		pro(BoundingRect.ALL, matice(10, 100_000_000));
	}

	@Test
	public void testMatice10b() {
		pro(BoundingRect.ALL, matice(10, 1000_000_000));
	}

	@Test
	public void testMatice100() {
		pro(BoundingRect.ALL, matice(100, 45));
	}

	@Test
	public void testMatice100a() {
		pro(BoundingRect.ALL, matice(100, 1));
	}

	@Test
	public void testMatice100b() {
		pro(BoundingRect.ALL, matice(100, 0));
	}

	@Test
	public void testMatice100c() {
		pro(BoundingRect.ALL, matice(100, -1));
	}

	@Test
	public void testMatice2e() {
		pro(BoundingRect.ALL, matice(2, -1));
	}

	@Test
	public void testMatice2d() {
		pro(BoundingRect.ALL, matice(2, 1));
	}

	//@Test
	public void testMatice1000() {
		pro(BoundingRect.ALL, matice(1000, 45));
	}


	@Test
	public void testBr10() {
		pro(new BoundingRect(23, 35, 68, 77), matice(10, 10));
	}

	@Test
	public void testMaticeBr100a() {
		pro(new BoundingRect(40, 10, 70, 20), matice(100, 1));
	}

	@Test
	public void testMaticeBr100b() {
		pro(new BoundingRect(40, 10, 70, 20), matice(100, 0));
	}

	@Test
	public void testMaticeBr100c() {
		pro(new BoundingRect(40, 10, 70, 20), matice(100, -1));
	}

	@Test
	public void locateNearest0() {
		Assert.assertSame(Optional.empty(), new Indexator<TestBod>(BoundingRect.ALL).locateNearestOne(789, 887));
	}

	@Test
	public void locateNearest1() {
		Assert.assertEquals(b(60,80), mat10().locateNearestOne(61, 83).get());
	}

	@Test
	public void locateNearest2() {
		Assert.assertEquals(b(60,90), mat10().locateNearestOne(61, 88).get());
	}

	@Test
	public void locateNearest3() {
		Assert.assertEquals(b(0,0), mat10().locateNearestOne(-61, -88).get());
	}

	@Test
	public void locateNearest4() {
		Assert.assertEquals(b(60,0), mat10().locateNearestOne(56, -88).get());
	}

	@Test
	public void locateNearest5() {
		Assert.assertEquals(b(40,20), mat10().locateNearestOne(40, 20).get());
	}

	@Test
	@SneakyThrows
	public void paralelismus() {
		final long start = System.currentTimeMillis();
		final int N = 40;
		mat(N).parallelStream().forEach(x -> {
			try {
				Thread.sleep(1);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("cas: " + (System.currentTimeMillis() - start));
		// skekvenčně se to nedá dřív stihnout
		assertTrue(System.currentTimeMillis() - start < N * N);
	}

	private Indexator<TestBod> mat(final int n) {
		final int factor = 10;
		Indexator<TestBod> indr = new Indexator<TestBod>(BoundingRect.ALL);
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < n; y++) {
				final TestBod bod = new TestBod(x * factor, y * factor);
				indr = indr.add(bod.getX(), bod.getY(), bod);
			}
		}
		System.out.println("V matici je: " + indr.getCount());
		return indr;
	}

	private Indexator<TestBod> mat10() {
		return mat(10);
	}
}
