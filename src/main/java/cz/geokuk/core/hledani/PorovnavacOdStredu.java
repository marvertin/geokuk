package cz.geokuk.core.hledani;

import java.util.Comparator;
import java.util.concurrent.Future;

public class PorovnavacOdStredu implements Comparator<Nalezenec0> {

	private final Future<?> future;

	public PorovnavacOdStredu(Future<?> future) {
		super();
		this.future = future;
	}

	@Override
	public int compare(Nalezenec0 w1, Nalezenec0 w2) {
		if (future.isCancelled())
			throw new XZaknclovanoRazeni();

		// double kvadrat1 = (utm1.ux - stredHledani.ux) * (utm1.ux - stredHledani.ux)
		// + (utm1.uy - stredHledani.uy) * (utm1.uy - stredHledani.uy);
		// double kvadrat2 = (utm2.ux - stredHledani.ux) * (utm2.ux - stredHledani.ux)
		// + (utm2.uy - stredHledani.uy) * (utm2.uy - stredHledani.uy);

		double result = Math.signum(w1.getVzdalenost() - w2.getVzdalenost());
		return (int) result;
	}

}
