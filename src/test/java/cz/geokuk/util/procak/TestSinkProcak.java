package cz.geokuk.util.procak;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestSinkProcak implements Procak<String>{


	final TestProcakAccum accum;

	@Override
	public EProcakResult process(final String s) {
		accum.add("~" + s);
		return EProcakResult.DONE;
	}

	@Override
	public void roundDone() {
		accum.add("<~>");
	}

	@Override
	public void allDone() {
		accum.add(".~");
	}

}
