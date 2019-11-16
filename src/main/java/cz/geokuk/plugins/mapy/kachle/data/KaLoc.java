package cz.geokuk.plugins.mapy.kachle.data;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;

public class KaLoc {

	private final int moumer;
	private final int ksx;
	private final int ksy;

	public static void main(final String[] args) {

		for (int i = -20; i < 20; i++) {

			System.out.println(i + " " + maskuj(i, 3));

		}
	}

	/**
	 * Konstruuje lokaci kachle na základě snalosti souřadnic jihozápadního rohu.
	 *
	 * @param mouJZ
	 * @param moumer
	 * @return
	 */
	public static KaLoc ofJZ(final Mou mouJZ, final int moumer) {
		if (moumer == 0) {
			return new KaLoc(0, 0, moumer);
		}
		final int ksx = mouJZ.xx >> Coord.MOU_BITS - moumer;
		final int ksy = mouJZ.yy >> Coord.MOU_BITS - moumer;
		return new KaLoc(ksx, ksy, moumer);
	}

	/**
	 * Konstruuje lokaci kachle na základě snalosti souřadnic severozápadního rohu.
	 *
	 * @param mouSZ
	 * @param moumer
	 * @return
	 */
	public static KaLoc ofSZ(final Mou mouSZ, final int moumer) {
		if (moumer == 0) {
			return new KaLoc(0, 0, moumer);
		}
		return ofJZ(new Mou(mouSZ.xx, mouSZ.yy - (1 << Coord.MOU_BITS - moumer)), moumer);
	}

	private static int maskuj(final int a, final int bitu) {
		final int m = (1 << bitu) - 1;
		return (a & 1 << bitu - 1) != 0 ? a | ~m : a & m;

	}

	/**
	 * @param mou
	 *            Souřadnice levého horního (SZ) rohu kachle.
	 * @param moumer
	 *            měřítko
	 */
	private KaLoc(final int ksx, final int ksy, final int moumer) {
		this.ksx = ksx;
		this.ksy = ksy;
		this.moumer = moumer;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final KaLoc other = (KaLoc) obj;
		if (ksx != other.ksx) {
			return false;
		}
		if (ksy != other.ksy) {
			return false;
		}
		if (moumer != other.moumer) {
			return false;
		}
		return true;
	}

	/**
	 * Konvertuje merkátorovu souřadnici na mapovou souřadnici
	 *
	 * @param mersou
	 * @return
	 */
	public int getFromSzUnsignedX() {
		return getSize() / 2 + getSignedX();
	}

	/**
	 * Konvertuje merkátorovu souřadnici na mapovou souřadnici
	 *
	 * @param mersou
	 * @return
	 */
	public int getFromSzUnsignedY() {
		if (moumer == 0) {
			return 0;
		}
		return getSize() / 2 - 1 - getSignedY();
	}

	/**
	 * Vrátí souřadnice JZ rohu.
	 *
	 * Je to potřeba jen kvůli prioritnímu stahování od středu.
	 *
	 * @return
	 */
	public Mou getMouJZ() {
		return new Mou(ksx << Coord.MOU_BITS - moumer, ksy << Coord.MOU_BITS - moumer);
	}

	public int getMoumer() {
		return moumer;
	}

	/**
	 * Vrátí souřadnice JZ rohu.
	 *
	 * Je to potřeba jen kvůli prioritnímu stahování od středu.
	 *
	 * @return
	 */
	public Mou getMouSZ() {
		return getMouJZ().add(0, 1 << Coord.MOU_BITS - moumer);
	}

	/**
	 * Xsová souřadnice kachle. U vedlejších kachlí se souřadnice liší o jednu. Jsou se zanménkkem, například pro moumer=3 jdou souřadnice -4 až 3. Nula je kachle ležící svým JZ bodem na WGS=[0,0]
	 *
	 * Kachle jdou zleva doprava a zespodu nahoru jako normální souřadnice. Kachle ze [signedMapX, signedMapY] leží v africe
	 *
	 * @return
	 */
	public int getSignedX() {
		return ksx;
	}

	public int getSignedY() {
		return ksy;
	}

	/**
	 * Počet kachlí vodorovně nebo svisle pro aktuální měřítko. Je t ovždy mocnina dvou
	 *
	 * @return
	 */
	public int getSize() {
		return 1 << moumer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ksx;
		result = prime * result + ksy;
		result = prime * result + moumer;
		return result;
	}

	@Override
	public String toString() {
		return String.format("[%d,%d,Z%d]", ksx, ksy, moumer);
	}
}
