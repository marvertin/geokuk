package cz.geokuk.plugins.kesoid.kind;

import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;

public interface GpxToWptBuilder {

	Wpt createWpt(GpxWpt gpxwpt, Kepodr kepodr);

}
