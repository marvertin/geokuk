package cz.geokuk.plugins.mapy.kachle.podklady;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/** Dávka stahování počká, dokud čekají online dlaždice. */
public class DavkaCekaNaOnlineTest {

	@Test(timeout = 10000)
	public void cekaNaVyprazdneniOnlineFronty() throws Exception {
		final PrioritniFronta online = new PrioritniFronta();
		final Runnable uloha = () -> {};
		assertTrue(online.offer(uloha));
		final List<Thread> vlakna = new ArrayList<>();
		final CountDownLatch dockala = PrioritniFrontaTest.cekajici(1, () -> KachleZiskavac.pockejNaOnline(online), vlakna);
		assertFalse("online dlaždice ještě čeká", dockala.await(100, TimeUnit.MILLISECONDS));
		assertSame(uloha, online.poll());
		assertTrue(dockala.await(2, TimeUnit.SECONDS));
	}

	@Test(timeout = 10000)
	public void prazdnaFrontaNezdrzi() throws Exception {
		KachleZiskavac.pockejNaOnline(new PrioritniFronta());
		KachleZiskavac.pockejNaOnline(new ArrayBlockingQueue<>(1));
	}

	@Test(expected = IllegalStateException.class, timeout = 10000)
	public void jinaNezPrioritniFrontaJeChyba() throws Exception {
		final ArrayBlockingQueue<Runnable> fronta = new ArrayBlockingQueue<>(1);
		assertTrue(fronta.offer(() -> {}));
		KachleZiskavac.pockejNaOnline(fronta);
	}
}
