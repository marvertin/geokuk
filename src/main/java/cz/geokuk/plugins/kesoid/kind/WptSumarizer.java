package cz.geokuk.plugins.kesoid.kind;

import cz.geokuk.plugins.kesoid.importek.WptReceiver;

public interface WptSumarizer extends WptReceiver {

	static final WptSumarizer EMPTY = wpt -> {
	};

}
