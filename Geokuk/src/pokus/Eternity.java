package pokus;

public class Eternity {

  Ctverec c = new Ctverec();
  Kostky ko = new Kostky();
  
  int citac;

  public static void main(String[] args) {
    new Eternity().lusti();

  }

  boolean zkus(Kostka k, int poz) {
    Misto m = c.mlist.get(poz); // ziskat misto, kam to pujde
    assert m.k == null;
    m.k = k;
    k.jepouzita = true;
    boolean sedi = m.sedi();

    if (sedi) {
      citac++;
      zkusNaDalsi(poz +1);
    }


    k.jepouzita = false;
    m.k = null;
    return sedi;

  }

  private void zkusNaDalsi(int poz) {
    if (poz > 15) {
      System.out.println(citac);
      c.vypis();
    }
    for (int i = 1; i<=16; i++) {
      Kostka kk = ko.ka[i];
      if (! kk.jepouzita) {
        kk.nuluj();
        do {
          zkus(kk, poz);
          kk.rotuj();
        } while (kk.rotace != 0);
      }
    }
  }

  private void lusti() {
    long cas = System.currentTimeMillis();
    for (int i =0; i <1; i++)
      zkusNaDalsi(0);
    System.out.println("-------------");
    System.out.println(citac);
    System.out.println(System.currentTimeMillis() - cas);
  }
}