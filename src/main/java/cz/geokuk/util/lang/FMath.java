package cz.geokuk.util.lang;

public class FMath {

	public static int fit(int x, int a, int b) {
		if (x < a)
			x = a;
		if (x > b)
			x = b;
		return x;
	}
}
