package pokus;

public class Kostky {

  Kostka[] ka = new Kostka[17];
  
  Kostky () {
    add( 1, "ZXXX");
    add( 2, "ZXCX");
    add( 3, "ZXCM");
    add( 4, "ZMCX");
    add( 5, "ZCMC");
    add( 6 ,"ZCXC");
    add( 7, "ZMXM");
    add( 8, "ZCXM");
    add( 9, "ZCCC");
    add(10, "ZCZM");
    add(11, "CXMX");
    add(12, "CMXM");
    add(13, "CMMM");
    add(14, "CCXM");
    add(15, "CCMM");
    add(16, "CCCM");
    
  }

  void add(int i, String s) {
    Kostka k = new Kostka();
    k.s = E.valueOf(s.substring(0,1));
    k.v = E.valueOf(s.substring(1,2));
    k.j = E.valueOf(s.substring(2,3));
    k.z = E.valueOf(s.substring(3,4));
    k.n = i;
    ka[i] = k;
    
  }
}
