package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.Wpti;
import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;


public abstract class KesPopiskyDef0 extends PopiskyDefBuilder0 {


	private static Kes kes(final Wpt wpt) {
		return (Kes) ((Wpti)wpt).getKoid();
	}

	@Override
	public void init() {
		def("{nazev}", (sb, wpt) -> {
			sb.append(wpt.getNazev());
		});

		def("{typ1}", (sb, wpt) -> {
			sb.append(kes(wpt).getOneLetterType());
		});

		def("{velikost}", (sb, wpt) -> {
			sb.append(kes(wpt).getSize());
		});

		def("{velikost1}", (sb, wpt) -> {
			sb.append(kes(wpt).getOneLetterSize());
		});

		def("{obtiznost}", (sb, wpt) -> {
			sb.append(kes(wpt).getDifficulty());
		});

		def("{obtiznost1}", (sb, wpt) -> {
			sb.append(kes(wpt).getOneLetterDifficulty());
		});

		def("{teren}", (sb, wpt) -> {
			sb.append(kes(wpt).getTerrain());
		});

		def("{teren1}", (sb, wpt) -> {
			sb.append(kes(wpt).getOneLetterTerrain());
		});

		def("{info}", (sb, wpt) -> {
			sb.append(kes(wpt).getInfo());
		});

	}


}
