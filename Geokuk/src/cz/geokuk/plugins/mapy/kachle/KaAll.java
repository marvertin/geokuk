package cz.geokuk.plugins.mapy.kachle;



public class KaAll extends Ka0 {

  //private int DOPLNKOVAC = 1<<28;

	public KaSet kaSet;
  
  public KaAll(KaLoc loc, KaSet kaSet) {
	  super(loc);
	  this.kaSet = kaSet;
  }



	@Override
  public String typToString() {
		StringBuilder sb = new StringBuilder();
		for (EKaType kt : kaSet.getKts()) {
			sb.append(kt.name());
			sb.append('_');
		}
		return sb.toString();
  }


	@Override
  public String toString() {
	  return "KaAll [kaSet=" + kaSet + ", getLoc()=" + getLoc() + "]";
  }



	@Override
  public int hashCode() {
	  final int prime = 31;
	  int result = super.hashCode();
	  result = prime * result + ((kaSet == null) ? 0 : kaSet.hashCode());
	  return result;
  }



	@Override
  public boolean equals(Object obj) {
	  if (this == obj)
		  return true;
	  if (!super.equals(obj))
		  return false;
	  if (getClass() != obj.getClass())
		  return false;
	  KaAll other = (KaAll) obj;
	  if (kaSet == null) {
		  if (other.kaSet != null)
			  return false;
	  } else if (!kaSet.equals(other.kaSet))
		  return false;
	  return true;
  }



	public EKaType getPodkladType() {
		return kaSet.getPodklad();
  }



	public KaOne getPodklad() {
		return new KaOne(getLoc(), getPodkladType());
  }




	
}
