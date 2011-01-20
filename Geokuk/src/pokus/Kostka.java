package pokus;

public class Kostka {

  E s;
  E v;
  E j;
  E z;
  public int n;
  int rotace = 0;
  
  boolean jepouzita;
  
  
  void rotuj() {
    E pom = s;
    s = z;
    z = j;
    j = v;
    v = pom;
    rotace = (rotace + 1) % 4;
  }
  
  
  void nuluj () {
    while (rotace != 0) rotuj();
    
  }
}
