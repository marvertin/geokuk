package cz.geokuk.util.file;

import java.io.File;
import java.nio.file.Path;

public class KeFile {
	public static class XRelativizeDubleDot extends RuntimeException {
		private static final long serialVersionUID = 7735880115004590777L;

		private XRelativizeDubleDot(final String aMessage) {
			super(aMessage);
		}

	}

	public final Root			root;

	public final FileAndTime	fat;

	public KeFile(final FileAndTime aFat, final Root aRoot) {
		super();
		fat = aFat;
		root = aRoot;
		getRelativePath(); // jen kvůli vyhození výjimky, aby nešel stvořit obje,kt, který lze relativizovat
	}

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
		final KeFile other = (KeFile) obj;
		return getFile().equals(other.getFile()) && root.dir.equals(other.root.dir);
	}

	public File getFile() {
		return fat.file;
	}

	public long getLastModified() {
		return fat.lastmodify;
	}

	public KeFile getParent() {
		final Path relativePath = getRelativePath();
		if (relativePath.toString().isEmpty()) {
			return null;
		}
		final File parent = getFile().getParentFile();
		return new KeFile(new FileAndTime(parent, 0), root);

	}

	public Path getRelativePath() {
		final Path relativeFile = root.dir.toPath().relativize(fat.file.toPath());
		if (relativeFile.startsWith("..")) {
			throw new XRelativizeDubleDot("Vysledek relativizace leze zpet: " + this + " => " + relativeFile);
		}
		return relativeFile;
	}

	@Override
	public int hashCode() {
		return getFile().hashCode() + root.dir.hashCode();
	}

	@Override
	public String toString() {
		return "KeFile [fat=" + fat + ", root=" + root + "]";
	}

	// public static void main(String[] args) {
	// System.out.println(Paths.get("C:/aa/bb/xx").relativize(Paths.get("D:/aa/bb/cc/dd")));
	// }

}