package cz.geokuk.core.napoveda;

import java.util.List;

class ZpravyAVerze {
	final List<ZpravaUzivateli>	zpravy;
	final String				lastVersion;

	public ZpravyAVerze(List<ZpravaUzivateli> zpravy, String lastVersion) {
		this.zpravy = zpravy;
		this.lastVersion = lastVersion;
	}
}
