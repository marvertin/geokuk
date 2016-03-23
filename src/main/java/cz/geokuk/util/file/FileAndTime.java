package cz.geokuk.util.file;

import java.io.File;

public class FileAndTime implements Comparable<FileAndTime> {

	final File	file;
	final long	lastmodify;

	public FileAndTime(final File aFile) {
		this(aFile, aFile.lastModified());
	}

	public FileAndTime(final File aFile, final long aLastmodify) {
		file = aFile;
		lastmodify = aLastmodify;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (file == null ? 0 : file.hashCode());
		result = prime * result + (int) (lastmodify ^ lastmodify >>> 32);
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FileAndTime other = (FileAndTime) obj;
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		} else if (!file.equals(other.file)) {
			return false;
		}
		if (lastmodify != other.lastmodify) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileAndTime [file=" + file + ", lastmodify=" + lastmodify + "]";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final FileAndTime fat) {
		if (lastmodify == fat.lastmodify) {
			return 0;
		}
		if (lastmodify < fat.lastmodify) {
			return -1;
		} else {
			return 1;
		}
	}
}