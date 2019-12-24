package cz.geokuk.util.procak;

public class EmptyProcak<T> implements Procak<T> {

	@Override
	public EProcakResult process(final T obj) {
		return EProcakResult.NEVER;
	}

	@Override
	public void roundDone() {
	}

	@Override
	public void allDone() {
	}

}
