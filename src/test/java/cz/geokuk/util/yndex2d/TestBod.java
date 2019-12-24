package cz.geokuk.util.yndex2d;

import lombok.Data;

@Data
public class TestBod implements Comparable<TestBod> {
	private final int x;
	private final int y;

	@Override
	public int compareTo(final TestBod o) {
		if (x < o.x) {
			return -1;
		} else if (x > o.x) {
			return 1;
		} else {
			if (y < o.y) {
				return -1;
			} else if (y > o.y) {
				return 1;
			} else {
				return 0;
			}

		}
	}
}
