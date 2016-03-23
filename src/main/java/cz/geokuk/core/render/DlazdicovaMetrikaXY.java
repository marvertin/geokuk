/**
 *
 */
package cz.geokuk.core.render;

import java.awt.Dimension;
import java.util.Iterator;

import cz.geokuk.core.render.DlazdicovaMetrikaXY.Dlazdice;

/**
 * @author veverka
 *
 */
public class DlazdicovaMetrikaXY implements Iterable<Dlazdice> {
	final DlazdicovaMetrika xx;
	final DlazdicovaMetrika yy;

	public int getPcoetDlazdic() {
		return xx.dlaPocet * yy.dlaPocet;
	}
	/**
	 * @param x
	 * @param y
	 */
	public DlazdicovaMetrikaXY(DlazdicovaMetrika xx, DlazdicovaMetrika yy) {
		super();
		this.xx = xx;
		this.yy = yy;
	}

	//  public void drz() {
	//    //int xDlazdice = 0, yDlazdice = 0;
	//    for (int x = xx.dlaSize / 2, xDlazdice=0; x < xx.sizeCele; x += xx.dlaRoztec, xDlazdice++) {
	//      for (int y = yy.dlaSize / 2, yDlazdice=0; y < yy.sizeCele; y += yy.dlaRoztec, yDlazdice++) {
	//        Rectangle r = new Rectangle(x, y, xx.dlaSize, yy.dlaSize);
	//      }
	//    }
	//  }

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Dlazdice> iterator() {
		return new Iterator<DlazdicovaMetrikaXY.Dlazdice>() {

			private int x = xx.dlaSize / 2;
			private int y = yy.dlaSize / 2;
			private int xn = 0;
			private int yn = 0;
			boolean projeto;

			@Override
			public void remove() {
			}

			@Override
			public Dlazdice next() {
				Dlazdice dla = new Dlazdice();
				dla.xn = xn;
				dla.yn = yn;
				dla.xs = x;
				dla.ys = y;
				dla.dim = new Dimension(xx.dlaSize, yy.dlaSize);
				y += yy.dlaRoztec;
				yn++;
				if (y >= yy.sizeCele) {
					y = yy.dlaSize / 2;
					yn=0;
					x += xx.dlaRoztec;
					xn++;
					if (x > xx.sizeCele) {
						projeto = true;
					}
				}
				return dla;
			}

			@Override
			public boolean hasNext() {
				return ! projeto;
			}
		};
	}


	class Dlazdice{
		int xn; // pořadové číslo na ose x počítáme od nuly
		int yn; // ořadové číslo na ose y počítáme od nly
		int xs; // stred x
		int ys; // stred y
		Dimension dim;
	}


}
