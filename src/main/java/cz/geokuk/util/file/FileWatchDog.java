package cz.geokuk.util.file;

import java.io.*;
import java.nio.charset.Charset;

public class FileWatchDog<R> {

	private final File		file;

	private long			lastmodified;

	private WatchDogGroup	watchDogGroup;

	public FileWatchDog(final File file) {
		super();
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public void forceLoad() {
		lastmodified = 0;
	}

	public void nastavPrecteno() {
		lastmodified = wasModified();
	}

	private BufferedReader open() {
		try {
			Reader r;
			if (file.canRead()) {
				r = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF8"));
			} else {
				r = new StringReader("");
			}
			return new BufferedReader(r);
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized R readIfModified(final ReaderCallback<R> callback) throws IOException {
		if (watchDogGroup != null) {
			watchDogGroup.forceIfAnyModified();
		}
		BufferedReader rdr = null;
		try {
			final long lm = wasModified();
			if (lm < 0)
				return null;
			rdr = open();
			final R result = callback.load(rdr);
			lastmodified = lm; // tepre až se povede načíst, můžeme změni
			return result;
		} finally {
			try {
				if (rdr != null)
					rdr.close();
			} catch (final IOException e) { // s tím nic nenadělám
			}
		}
	}

	/**
	 * Vrátí čas modifikace souboru pokud byl modifikován a -1 když nebyl
	 *
	 * @return
	 */
	public synchronized long wasModified() {
		long lm;
		if (file.canRead()) {
			lm = file.lastModified();
		} else {
			lm = 0;
		}
		if (lm == lastmodified)
			return -1;
		return lm;
	}

	public static interface ReaderCallback<R> {
		R load(BufferedReader reader) throws IOException;
	}

	public void setGroup(final WatchDogGroup watchDogGroup) {
		if (this.watchDogGroup == watchDogGroup)
			return;
		this.watchDogGroup = watchDogGroup;
		watchDogGroup.add(this);
	}
}
