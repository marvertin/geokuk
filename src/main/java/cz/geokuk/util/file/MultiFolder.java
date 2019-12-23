/**
 *
 */
package cz.geokuk.util.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/**
 * Nese informace z několika složek, ať resourcových nebo normálních. Informace se překrývají.
 *
 * @author Martin Veverka
 */
public class MultiFolder {
	private static final String REMOVE_SUFFIX = ".remove";

	private static final String RESOURCE_FOLDER_IMG = "img";
	private static final String RESOURCE_FOLDER_MAP = "map";


	private final KeyTree<String, LamUrl> tree = new KeyTree<>();

//	public static void main(final String[] args) {
//
//		final MultiFolder mf = new MultiFolder();
//		mf.addResourceTree("geokuk/image");
//		mf.addFolderTree(new File("img2"));
//		mf.addFolderTree(new File("img3"));
//		mf.print();
//	}

	public void addFolderTree(final File dir) {
		final List<String> emptyList = Collections.emptyList();
		addOneFord(dir, emptyList);
	}
	/**
	 * Přidá strom resourců
	 *
	 * @param rootresource
	 */
	public void addResourceTree() {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final Set<String> obrazky = obohatOAdresare(new Reflections(RESOURCE_FOLDER_IMG + "." + RESOURCE_FOLDER_MAP, new ResourcesScanner())
				.getResources(Pattern.compile(".*")));
		obrazky.remove(RESOURCE_FOLDER_IMG); // ten tam dělal problém
		obrazky.forEach(System.out::println);

		obohatOAdresare(obrazky);

		for (final String s : obrazky) {
			final String line = s.substring(RESOURCE_FOLDER_IMG.length() + 1);
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
	}



	private Set<String> obohatOAdresare(final Set<String> obrazky) {
		return obrazky.stream()
				.map(s -> Arrays.stream(s.split("/")))
				.flatMap(stm -> {
					final StringBuilder sb = new StringBuilder();
					return stm.map(s1 -> {
						if (sb.length() > 0) {
							sb.append("/");
						}
						sb.append(s1);
						return sb.toString();
					});
				}).collect(Collectors.toSet());
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
