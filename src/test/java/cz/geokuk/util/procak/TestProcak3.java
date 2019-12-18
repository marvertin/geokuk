package cz.geokuk.util.procak;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestProcak3 implements Procak<String>{


	final TestProcakAccum accum;

	@Override
	public EProcakResult process(final String s) {
		if (s.startsWith("3H*")) {
			return EProcakResult.NEVER;
		}
		if (s.startsWith("3") || s.startsWith("Q3")) {
			accum.add("z" + s);
			return EProcakResult.DONE;
		} else {
			return EProcakResult.NEVER;
		}
	}

	@Override
	public void roundDone() {
		accum.add("<3>");
	}

	@Override
	public void allDone() {
		accum.add(".3");
	}

}
