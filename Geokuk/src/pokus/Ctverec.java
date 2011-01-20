package pokus;

import java.util.ArrayList;
import java.util.List;

public class Ctverec {

  Misto[][] mista = new Misto[6][];

  List<Misto> mlist = new ArrayList<Misto>();

  public Ctverec() {
    for (int i= 0; i<=5; i++) {
      mista[i] = new Misto[6];
    }

    for (int i=0; i<=5; i++) {
      for (int j=0; j<=5; j++) {
        Misto m = new Misto(i,j);
        mista[i][j] = m;
        m.k = new Kostka();
      }
    }
    
    for (int i=1; i<=4; i++) {
      for (int j=1; j<=4; j++) {
        Misto m = mista[i][j];
        m.k = null;
        mlist.add(m);
        m.s = mista[i-1][j];
        m.v = mista[i ][j+1];
        m.j = mista[i+1][j];
        m.z = mista[i  ][j-1];
      }
      
    }

    mista[0][1].k.j = E.Z;
    mista[0][2].k.j = E.C;
    mista[0][3].k.j = E.X;
    mista[0][4].k.j = E.M;
    
    mista[1][5].k.z = E.C;
    mista[2][5].k.z = E.Z;
    mista[3][5].k.z = E.Z;
    mista[4][5].k.z = E.M;

    mista[5][1].k.s = E.C;
    mista[5][2].k.s = E.M;
    mista[5][3].k.s = E.X;
    mista[5][4].k.s = E.X;
    
    mista[1][0].k.v = E.X;
    mista[2][0].k.v = E.C;
    mista[3][0].k.v = E.Z;
    mista[4][0].k.v = E.Z;

  }
  
  
  void vypis() {
    System.out.println("------------------------");
    for (int i=1; i<=4; i++) {
      for (int j=1; j<=4; j++) {
        Misto m = mista[i][j];
        if (m.k == null)         System.out.printf("   "); else
        System.out.printf("%3d", m.k.n);
      }
      System.out.println();
    }
    
  }
}
