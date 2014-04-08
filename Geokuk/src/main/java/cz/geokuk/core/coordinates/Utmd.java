package cz.geokuk.core.coordinates;

public class Utmd {

	public double dux;
	public double duy;

	public Utmd() {}
	
	public Utmd(double dxx, double dyy) {
		this.dux = dxx;
		this.duy = dyy;
	}
	
    public Utmd(Utmd mou) {
    	dux = mou.dux;
    	duy = mou.duy;
	}
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(dux);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(duy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Utmd other = (Utmd) obj;
		if (Double.doubleToLongBits(dux) != Double.doubleToLongBits(other.dux))
			return false;
		if (Double.doubleToLongBits(duy) != Double.doubleToLongBits(other.duy))
			return false;
		return true;
	}

	public Utmd add(double dxx, double dyy) {
    	return new Utmd(this.dux + dxx, this.duy + dyy);
    }

    public Utmd sub(double dxx, double dyy) {
    	return new Utmd(this.dux - dxx, this.duy - dyy);
    }
    
    
    @Override
    public String toString() {
    	return "UTM-D[" + dux  + "," + duy  + "]";
    }
	
}
