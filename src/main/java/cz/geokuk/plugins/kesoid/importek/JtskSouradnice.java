package cz.geokuk.plugins.kesoid.importek;

public class JtskSouradnice {

	public double y;
	public double x;
	public double z;

	public String pred;
	public String po;

	@Override
	public String toString() {
		return pred + "[y=" + y + ", x=" + x + ", " + z + " m n.m]" + po;
	}

}
