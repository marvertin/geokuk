package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.core.program.FConst;
import cz.geokuk.core.program.UmisteniSouboru0;
import cz.geokuk.util.file.Filex;

import java.io.File;
import java.util.Objects;

public class MapyUmisteniSouboru extends UmisteniSouboru0 {
	public static final Filex MAPY_JSON_FILE = new Filex(new File(FConst.HOME_DIR, "geokuk/map-definitions.utf-8.json"), false, true);

	private Filex mapyJsonFile;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final MapyUmisteniSouboru that = (MapyUmisteniSouboru) o;
		return equalsDataLocations(that);
	}

	/** Porovná, zda se shodují umístění datových složek. */
	public boolean equalsDataLocations(final MapyUmisteniSouboru that) {
		if (!Objects.equals(mapyJsonFile, that == null ? null : that.mapyJsonFile)) {
			return false;
		}
		return true;
	}

	public Filex getMapyJsonFile() {
		check(mapyJsonFile);
		return mapyJsonFile;
	}

	public void setMapyJsonFile(Filex mapyJsonFile) {
		this.mapyJsonFile = mapyJsonFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (mapyJsonFile == null ? 0 : mapyJsonFile.hashCode());
		return result;
	}

	private void check(final Filex file) {
		if (file == null) {
			throw new RuntimeException("Jmena souboru jeste nebyla inicializovana.");
		}
	}
}
