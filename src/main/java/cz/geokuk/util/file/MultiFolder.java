/**
 *
 */
package cz.geokuk.util.file;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Nese informace z několika složek, ať resourcových nebo normálních. Informace se překrývají.
 *
 * @author Martin Veverka
 */
public class MultiFolder {
	private static final String REMOVE_SUFFIX = ".remove";
	private static final String CONTENT_TXT = "content.txt";

	private final KeyTree<String, LamUrl> tree = new KeyTree<>();

	public static void main(final String[] args) {

		final MultiFolder mf = new MultiFolder();
		mf.addResourceTree("geokuk/image");
		mf.addFolderTree(new File("img2"));
		mf.addFolderTree(new File("img3"));
		mf.print();
	}

	public void addFolderTree(final File dir) {
		final List<String> emptyList = Collections.emptyList();
		addOneFord(dir, emptyList);
	}

	/**
	 * Přidá strom resourců
	 *
	 * @param rootresource
	 */
	public void addResourceTree(final String rootresource) {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream is = classLoader.getResourceAsStream(rootresource + "/" + CONTENT_TXT);
		if (is != null) {
			final BufferedReader bis = new BufferedReader(new InputStreamReader(is));
			String line;
			try {
				while ((line = bis.readLine()) != null) {
					line = line.trim();
					if (line.length() == 0) {
						continue;
					}
					final String s = rootresource + "/" + line;
					final URL url = classLoader.getResource(s);
					final String[] jmena = line.split("/");
					if (jmena[jmena.length - 1].endsWith(REMOVE_SUFFIX)) {
						jmena[jmena.length - 1] = jmena[jmena.length - 1].substring(0, jmena[jmena.length - 1].length() - REMOVE_SUFFIX.length());
						tree.remove(jmena);
					} else {
						final LamUrl lamUrl = new LamUrl();
						lamUrl.url = url;
						lamUrl.name = jmena[jmena.length - 1];
						tree.add(lamUrl, jmena);
					}
				}
				bis.close();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
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
		final MultiFolder other = (MultiFolder) obj;
		if (tree == null) {
			if (other.tree != null) {
				return false;
			}
		} else if (!tree.equals(other.tree)) {
			return false;
		}
		return true;
	}

	public KeyNode<String, LamUrl> getNode(final String key) {
		final String[] ss = key.split("/");
		return tree.locate(Arrays.asList(ss));
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
		result = prime * result + (tree == null ? 0 : tree.hashCode());
		return result;
	}

	public void print() {
		tree.print();
	}

	private void addOneFord(final File ford, final List<String> names) {
		final LamUrl lamUrl = new LamUrl();
		lamUrl.url = fileToUrl(ford);
		lamUrl.lastModified = ford.lastModified();
		lamUrl.name = ford.getName();
		tree.add(lamUrl, names);
		if (ford.isDirectory()) {
			for (final String s : ford.list()) {
				final List<String> list = new ArrayList<>(names.size() + 1);
				list.addAll(names);
				if (s.endsWith(REMOVE_SUFFIX)) {
					list.add(s.substring(0, s.length() - REMOVE_SUFFIX.length()));
					tree.remove(list);
				} else {
					list.add(s);
					addOneFord(new File(ford, s), list);
				}
			}
		}
	}

	/**
	 * @param ford
	 * @return
	 * @throws MalformedURLException
	 */
	private URL fileToUrl(final File ford) {
		try {
			return ford.toURI().toURL();
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
