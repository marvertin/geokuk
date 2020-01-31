package cz.geokuk.plugins.kesoid.importek;

import java.util.*;

import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.GpxToWptContext;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;

public class GpxToWptContextIml implements GpxToWptContext {


	private final Genom genom;
	private final GccomNick gccomNick;

	public GpxToWptContextIml(final Genom genom, final GccomNick gccomNick) {
		this.genom = genom;
		this.gccomNick = gccomNick;
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


}
