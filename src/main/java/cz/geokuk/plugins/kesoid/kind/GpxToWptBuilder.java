package cz.geokuk.plugins.kesoid.kind;

import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.Wpti;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;

public interface GpxToWptBuilder {

	Wpti createWpt(GpxWpt gpxwpt, Kepodr kepodr);

}
