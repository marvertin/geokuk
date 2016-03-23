package cz.geokuk.util.file;

import java.io.File;
import java.util.Stack;
import java.util.StringTokenizer;

import cz.geokuk.core.program.FConst;

public class Filex {
	private final File		file;
	private final boolean	relativeToProgram;
	private final boolean	active;

	public Filex(File file, boolean relativeToProgram, boolean active) {
		super();
		this.file = file;
		this.relativeToProgram = relativeToProgram;
		this.active = active;
	}

	public File getFile() {
		return file;
	}

	public boolean isRelativeToProgram() {
		return relativeToProgram;
	}

	/**
	 * Pokud je nastaven adresář jako aktivní, tak bere efektivní soubor, jinak null.
	 * 
	 * @return
	 */
	public File getEffectiveFileIfActive() {
		return isActive() ? getEffectiveFile() : null;
	}

	public File getEffectiveFile() {
		File f = file;
		if (!file.isAbsolute()) {
			if (relativeToProgram) {
				f = new File(FConst.JAR_DIR, file.getPath());
			} else {
				f = new File(new File("").getAbsoluteFile(), file.getPath());
			}
		}
		f = canonize(f);
		return f;
	}

	/**
	 * Kanonizuj danou cestu. Pokud je cesta relativní, nejdříve ji zabsolutni.
	 *
	 * <p>
	 * This includes:
	 * <ul>
	 * <li>Uppercase the drive letter if there is one.</li>
	 * <li>Remove redundant slashes after the drive spec.</li>
	 * <li>resolve all ./, .\, ../ and ..\ sequences.</li>
	 * <li>DOS style paths that start with a drive letter will have \ as the separator.</li>
	 * </ul>
	 *
	 * @param aPath
	 *            Cesta, jenž má být kanonizována.
	 * @return Kanonizovaná cesta. Nikdy nevrací null.
	 * @throws java.lang.NullPointerException
	 *             if the file path is equal to null.
	 */
	public static File canonize(File aPath) {
		// Nechť jsou v cestě lomítka dle operačního systému
		String path = aPath.getAbsolutePath().replace('/', File.separatorChar).replace('\\', File.separatorChar);

		// Ujistěme se, že máme absolutní cestu. Relativní cesty není dobré normalizovat.
		int colon = path.indexOf(':');

		if (!path.startsWith(File.separator) && !(path.length() >= 2 && Character.isLetter(path.charAt(0)) && colon == 1)) {
			throw new IllegalArgumentException(path + " is not an absolute path");
		}

		boolean dosWithDrive = false;
		String root = null;
		// Eliminate consecutive slashes after the drive spec
		if ((path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':')) {

			dosWithDrive = true;

			char[] ca = path.replace('/', '\\').toCharArray();
			StringBuilder sbRoot = new StringBuilder();
			for (int i = 0; i < colon; i++) {
				sbRoot.append(Character.toUpperCase(ca[i]));
			}
			sbRoot.append(':');
			if (colon + 1 < path.length()) {
				sbRoot.append(File.separatorChar);
			}
			root = sbRoot.toString();

			// Eliminate consecutive slashes after the drive spec
			StringBuilder sbPath = new StringBuilder();
			for (int i = colon + 1; i < ca.length; i++) {
				if ((ca[i] != '\\') || (ca[i] == '\\' && ca[i - 1] != '\\')) {
					sbPath.append(ca[i]);
				}
			}
			path = sbPath.toString().replace('\\', File.separatorChar);

		} else {
			if (path.length() == 1) {
				root = File.separator;
				path = "";
			} else if (path.charAt(1) == File.separatorChar) {
				// UNC drive
				root = File.separator + File.separator;
				path = path.substring(2);
			} else {
				root = File.separator;
				path = path.substring(1);
			}
		}

		Stack<String> s = new Stack<>();
		s.push(root);
		StringTokenizer tok = new StringTokenizer(path, File.separator);
		while (tok.hasMoreTokens()) {
			String thisToken = tok.nextToken();
			if (!".".equals(thisToken)) {
				if ("..".equals(thisToken)) {
					if (s.size() >= 2) {
						s.pop();
					}
				} else { // plain component
					s.push(thisToken);
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.size(); i++) {
			if (i > 1) {
				// not before the filesystem root and not after it, since root
				// already contains one
				sb.append(File.separatorChar);
			}
			sb.append(s.elementAt(i));
		}

		path = sb.toString();
		if (dosWithDrive) {
			path = path.replace('/', '\\');
		}
		return new File(path);
	}

	@Override
	public String toString() {
		return getEffectiveFile().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + (relativeToProgram ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Filex other = (Filex) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (relativeToProgram != other.relativeToProgram)
			return false;
		return true;
	}

	public boolean isActive() {
		return active;
	}

}
