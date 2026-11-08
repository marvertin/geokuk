package cz.geokuk.plugins.challenge;

import java.util.Optional;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Kde leží ikona keše vůči jejímu bodu na mapě, aby štítek kandidáta mohl stát těsně vedle ní. */
final class IkonaKese {

	/** Polovina ikony, když se skutečná ještě nezná (sklivec se plní postupně); ve značkách při zvětšení 1. */
	private static final int NEZNAMA_POLOVINA = 8;

	private IkonaKese() {
	}

	/** Pravý okraj ikony vůči bodu keše a svislý střed ikony vůči bodu, ve značkách při daném zvětšení ikon; {@code vysledek[0]} je pravý okraj, {@code vysledek[1]} svislý střed; má-li pole třetí prvek, je v něm horní okraj ikony a ve čtvrtém levý okraj. */
	static void okraje(final Indexator<Wpt> indexator, final Mou mou, final double meritko, final int[] vysledek) {
		vysledek[0] = (int) Math.ceil(NEZNAMA_POLOVINA * meritko);
		vysledek[1] = 0;
		nastavNahore(vysledek, -(int) Math.ceil(NEZNAMA_POLOVINA * meritko));
		nastavVlevo(vysledek, -(int) Math.ceil(NEZNAMA_POLOVINA * meritko));
		if (indexator == null) {
			return;
		}
		final Optional<Wpt> nejblizsi = indexator.bound(new BoundingRect(mou.xx - 2, mou.yy - 2, mou.xx + 2, mou.yy + 2)).locateNearestOne(mou.xx, mou.yy);
		if (nejblizsi.isPresent()) {
			okraje(nejblizsi.get(), meritko, vysledek);
		}
	}

	/** Totéž pro známou keš; bez hotového sklivce platí odhad. */
	static void okraje(final Wpt wpt, final double meritko, final int[] vysledek) {
		vysledek[0] = (int) Math.ceil(NEZNAMA_POLOVINA * meritko);
		vysledek[1] = 0;
		nastavNahore(vysledek, -(int) Math.ceil(NEZNAMA_POLOVINA * meritko));
		nastavVlevo(vysledek, -(int) Math.ceil(NEZNAMA_POLOVINA * meritko));
		final Sklivec sklivec = wpt.getSklivec();
		if (sklivec == null) {
			return;
		}
		int pravy = Integer.MIN_VALUE;
		int levy = Integer.MAX_VALUE;
		int nahore = Integer.MAX_VALUE;
		int dole = Integer.MIN_VALUE;
		for (final Imagant i : sklivec.imaganti) {
			if (i == null) {
				continue;
			}
			final Imagant z = i.zvetseny(meritko);
			pravy = Math.max(pravy, z.getXpos() + z.getImage().getWidth());
			levy = Math.min(levy, z.getXpos());
			nahore = Math.min(nahore, z.getYpos());
			dole = Math.max(dole, z.getYpos() + z.getImage().getHeight());
		}
		if (pravy != Integer.MIN_VALUE) {
			vysledek[0] = pravy;
			vysledek[1] = nahore + (dole - nahore) / 2;
			nastavNahore(vysledek, nahore);
			nastavVlevo(vysledek, levy);
		}
	}

	private static void nastavVlevo(final int[] vysledek, final int vlevo) {
		if (vysledek.length > 3) {
			vysledek[3] = vlevo;
		}
	}

	private static void nastavNahore(final int[] vysledek, final int nahore) {
		if (vysledek.length > 2) {
			vysledek[2] = nahore;
		}
	}
}
