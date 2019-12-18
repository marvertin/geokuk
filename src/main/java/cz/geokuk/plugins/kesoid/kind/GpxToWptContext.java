package cz.geokuk.plugins.kesoid.kind;

import java.util.Set;

import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;

public interface GpxToWptContext {

	Genom getGenom();

	GccomNick getGccomNick();

	Set<Alela> definujUzivatslskeAlely(final GpxWpt gpxwpt);

	Wpt createWpt(GpxWpt gpxwpt);
}
