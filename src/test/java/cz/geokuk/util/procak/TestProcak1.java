package cz.geokuk.util.procak;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestProcak1 implements Procak<String>{


	final TestProcakAccum accum;

	@Override
	public EProcakResult process(final String s) {
		if (s.startsWith("1") || s.startsWith("*")) {
			accum.add("x" + s);
			return EProcakResult.DONE;
		} else {
			if (s.startsWith("QQ")) {
				accum.add("x(" + s + ")");
			}
			return EProcakResult.NEVER;
		}
	}

	@Override
	public void roundDone() {
		accum.add("<1>");
	}

	@Override
	public void allDone() {
		accum.add(".1");
	}

}
