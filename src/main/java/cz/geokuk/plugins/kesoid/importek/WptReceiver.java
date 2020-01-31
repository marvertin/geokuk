package cz.geokuk.plugins.kesoid.importek;

import java.util.*;

import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.GpxToWptContext;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;

public class WptReceiver implements GpxToWptContext {


	private final Genom genom;
	private final KesBag kesBag;

	private final GccomNick gccomNick;

	public WptReceiver(final Genom genom, final GccomNick gccomNick, final KesBag kesBag) {
		this.genom = genom;
		this.gccomNick = gccomNick;
		this.kesBag = kesBag;
	}


	@Override
	public Set<Alela> definujUzivatslskeAlely(final GpxWpt gpxwpt) {
		final Set<Alela> alely = new HashSet<>();

		for (final Map.Entry<String, String> entry : gpxwpt.gpxg.userTags.entrySet()) {
			final String alelaName = entry.getValue();
			final String genName = entry.getKey();
			final Alela alela = genom.gen(genName).alela(alelaName);
			if (alela == null) {
				continue;
			}
			alely.add(alela);
			genom.UNIVERZALNI_DRUH.addGen(alela.getGen());
		}

		return alely;
	}


	@Override
	public Genom getGenom() {
		return genom;
	}

	@Override
	public GccomNick getGccomNick() {
		return gccomNick;
	}


	/**
	 * Hlavní metoda, kterou přijímáme hotové waypointy.
	 */
	@Override
	public void expose(final Wpt wpt) {
		kesBag.add(wpt);
	}


}
