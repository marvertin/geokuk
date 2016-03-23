package cz.geokuk.util.file;

import java.util.ArrayList;
import java.util.List;

public class WatchDogGroup {

	private final List<FileWatchDog<?>> list = new ArrayList<>();

	void add(final FileWatchDog<?> fwd) {
		list.add(fwd);
		fwd.setGroup(this);
	}

	void forceIfAnyModified() {
		boolean zmena = false;
		for (final FileWatchDog<?> fwd : list) {
			if (fwd.wasModified() > 0) {
				zmena = true;
			}
		}
		if (zmena) {
			for (final FileWatchDog<?> fwd : list) {
				fwd.forceLoad();
			}
		}
	}

}
