package cz.geokuk.util.file;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Třída je zodpověd na projití zadaných rootu a držení informací o tom, zda nedošlo ke změně.
 */
public class DirScanner {

	// case insensitive, TODO : other image formats than JPG, raw and tif

	// TODO : Use file watchers
	private List<Root>		roots;
	private List<KeFile>	lastScaned	= null;

	public void seRootDirs(final boolean prenacti, final Root... roots) {
		final List<Root> newRoots = Arrays.asList(roots);
		if (!prenacti && newRoots.equals(this.roots)) {
			return;
		}
		this.roots = newRoots;
		nulujLastScaned();
	}

	public synchronized void nulujLastScaned() {
		lastScaned = null;
	}

	/**
	 * Vrátí null, pokud není co načítat, protože nedošlo ke změně. Prázdný seznam je něco jiného, to ke změně došlo takové, že zmizely všechny soubory. Když se změní byť jediný soubor, je to změna a načítá se.
	 *
	 * @return
	 */
	public synchronized List<KeFile> coMamNacist() {
		final Set<KeFile> set = new HashSet<>();
		for (final Root dir : roots) {
			final List<KeFile> li = scanDir(dir);
			set.addAll(li);
		}
		final List<KeFile> list = new ArrayList<>(set);
		if (list.equals(lastScaned)) {
			return null; // nezměnilo se nic
		}
		lastScaned = list;
		return list;
	}

	private boolean matches(final String fileName, final Root.Def def) {
		if (def.patternExcludes != null) {
			if (def.patternExcludes.matcher(fileName).matches()) {
				return false;
			}
		}
		if (def.patternIncludes != null) {
			return def.patternIncludes.matcher(fileName).matches();
		} else {
			return true; // není matcher tak mečuje vše
		}
	}

	private List<KeFile> scanDir(final Root root) {
		if (!root.dir.exists()) {
			return Collections.emptyList();
		}
		try {
			final List<KeFile> list = new ArrayList<>();
			Files.walkFileTree(root.dir.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), root.def.maxDepth, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path path, final BasicFileAttributes aAttrs) throws IOException {

					if (matches(path.getFileName().toString(), root.def)) {
						list.add(new KeFile(new FileAndTime(path.toFile()), root));
					}
					return FileVisitResult.CONTINUE;
				}
			});
			return list;
		} catch (final Exception e) {
			throw new RuntimeException("Skenovani od " + root, e);
		}
	}
}
