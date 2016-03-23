package cz.geokuk.core.napoveda;

import java.util.List;

class ZpravyAVerze {
	final List<ZpravaUzivateli>	zpravy;
	final String				lastVersion;

	public ZpravyAVerze(final List<ZpravaUzivateli> zpravy, final String lastVersion) {
		this.zpravy = zpravy;
		this.lastVersion = lastVersion;
	}
}
