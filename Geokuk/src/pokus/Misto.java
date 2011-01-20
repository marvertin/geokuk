package pokus;

public class Misto {

  public Misto(int i, int j2) {
    // TODO Auto-generated constructor stub
  }
  public Misto s;
  public Misto v;
  public Misto j;
  public Misto z;
  
  Kostka k;
  
  boolean sedi() {
    if (s.k != null && s.k.j != k.s) return false;
    if (v.k != null && v.k.z != k.v) return false;
    if (j.k != null && j.k.s != k.j) return false;
    if (z.k != null && z.k.v != k.z) return false;
    return true;
  }
}
