package cz.geokuk.util.procak;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestProcak2 implements Procak<String>{


	final TestProcakAccum accum;
	int kolo;

	@Override
	public EProcakResult process(final String s) {
		if (s.startsWith("2") || s.startsWith("*")) {
			accum.add("y" + s);
			return EProcakResult.DONE;
		} else if (s.startsWith("Q")) {
			if (kolo > 0) {
				if (kolo == 1 && s.startsWith("QQ")) {
					accum.add("/");
					return EProcakResult.NEXT_ROUND;
				} else {
					accum.add("q" + s);
					return EProcakResult.DONE;
				}
			} else {
				return EProcakResult.NEXT_ROUND;
			}
		} else if (s.startsWith("3H")) {
			if (kolo == 0) {
				accum.add("y(" + s +")a");
				return EProcakResult.PROBABLY_MY_BUT_NEXT_ROUND;
			} else {
				accum.add("y(" + s +")b");
				if (s.startsWith("3H+")) {
					accum.add("y" + s);
					return EProcakResult.DONE;
				} else {
					return EProcakResult.NEVER;
				}
			}
		} else {
			return EProcakResult.NEVER;
		}
	}

	@Override
	public void roundDone() {
		kolo ++;
		accum.add("<2>");
	}

	@Override
	public void allDone() {
		accum.add(".2");
	}

}
