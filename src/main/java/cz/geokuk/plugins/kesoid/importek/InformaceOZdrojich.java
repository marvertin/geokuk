package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.util.*;

import cz.geokuk.util.file.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InformaceOZdrojich {
	public class Builder {
		/**
		 * Tady postupně budujeme stromy načítaných zdrojů.
		 *
		 * @param aJmenoZdroje
		 * @param nacteno
		 * @return
		 */
		public InformaceOZdroji add(final KeFile aJmenoZdroje, final boolean nacteno) {
			// TODO : perhaps it's not needed to bother with the whole path to root...
			// Maybe canonical path and longest
			// common prefix will do?
			Strom strom = stromy.get(aJmenoZdroje.root);
			if (strom == null) {
				strom = new Strom();
				stromy.put(aJmenoZdroje.root, strom);
			}
			InformaceOZdroji ioz = strom.map.get(aJmenoZdroje);
			if (ioz == null) {
				ioz = new InformaceOZdroji(aJmenoZdroje, nacteno);
				strom.map.put(aJmenoZdroje, ioz);
				InformaceOZdroji last = ioz;
				KeFile parent = aJmenoZdroje.getParent();
				while (parent != null) {
					InformaceOZdroji parentIoz = strom.map.get(parent);
					if (parentIoz != null) {
						parentIoz.addChild(last);
						last.parent = parentIoz;
						break;
					}
					parentIoz = new InformaceOZdroji(parent, false);
					parentIoz.addChild(last);
					last.parent = parentIoz;
					strom.map.put(parent, parentIoz);
					last = parentIoz;
					parent = parent.getParent();
				}
				if (parent == null) {
					strom.root = last;
				}
			}
			return ioz;
		}

		/** Objekt je hotov */
		public InformaceOZdrojich done() {
			// našvindlený root
			final File pseudoFile = new File("[gc]");
			root = new InformaceOZdroji(new KeFile(new FileAndTime(pseudoFile, 0), new Root(pseudoFile, new Root.Def(0, null, null))), false);
			for (final Strom strom : stromy.values()) {
				root.addChild(strom.root);
				strom.root.parent = root;
			}
			setřepáníNevětvenýchCest(root);
			root.spocitejSiPocetWaipointuChildren();
			print();
			return InformaceOZdrojich.this;

		}

		private void setřepáníNevětvenýchCest(final InformaceOZdroji aIoz) {
			if (aIoz.getChildren().size() == 1 && aIoz.parent != null) {
				final InformaceOZdroji jedinacek = aIoz.getChildren().get(0);
				aIoz.parent.remplaceChild(aIoz, jedinacek);
				jedinacek.parent = aIoz.parent;
			}
			// po sesypání nebo bez sesypání, děti sesypeme
			for (final InformaceOZdroji ioz : aIoz.getChildren()) {
				setřepáníNevětvenýchCest(ioz); // pro 0 se nedělá nic a pro více než 1 se nesetřepává
			}
		}

	}

	private static class Strom {
		InformaceOZdroji root;
		Map<KeFile, InformaceOZdroji> map = new LinkedHashMap<>();
	}


	private final Map<Root, Strom> stromy = new LinkedHashMap<>();

	private InformaceOZdroji root;

	public static Builder builder() {
		return new InformaceOZdrojich().new Builder();
	}

	private InformaceOZdrojich() {}

	public InformaceOZdroji get(final KeFile key) {
		final InformaceOZdroji informaceOZdroji = stromy.get(key.root).map.get(key);
		return informaceOZdroji;
	}

	public Set<File> getJmenaZdroju() {
		final Set<File> files = new LinkedHashSet<>();
		for (final KeFile kefile : getKeJmenaZdroju()) {
			files.add(kefile.getFile());
		}
		return files;
	}

	public Set<KeFile> getKeJmenaZdroju() {
		final Set<KeFile> files = new LinkedHashSet<>();
		for (final Strom strom : stromy.values()) {
			files.addAll(strom.map.keySet());
		}
		return files;
	}

	public InformaceOZdroji getRoot() {
		return root;
	}

	public Set<InformaceOZdroji> getSetInformaciOZdrojich() {
		final Set<InformaceOZdroji> infas = new LinkedHashSet<>();
		for (final Strom strom : stromy.values()) {
			infas.addAll(strom.map.values());
		}
		return infas;
	}

	public int getSourceCount(final boolean loaded) {
		int loadedCount = 0;
		int notLoadedCount = 0;
		for (final InformaceOZdroji ioz : getSetInformaciOZdrojich()) {
			if (ioz.getChildren().isEmpty()) {
				if (ioz.nacteno) {
					++loadedCount;
				} else {
					++notLoadedCount;
				}
			}
		}
		return loaded ? loadedCount : notLoadedCount;
	}

	public Collection<InformaceOZdroji> getSubtree(final KeFile subTreeRoot) {
		final List<InformaceOZdroji> toReturn = new ArrayList<>();
		return getSubtree(get(subTreeRoot), toReturn);
	}

	public long getYungest() {
		long x = 0;
		for (final InformaceOZdroji info : getSetInformaciOZdrojich()) {
			if (info.nacteno) {
				x = Math.max(x, info.getLastModified());
			}
		}
		log.info("Spocitano nejmladsi datum: {}", x);
		return x;
	}

	public void print() {
		if (log.isDebugEnabled()) {
			System.out.println("================= prin strom - START");
			root.print(":: ", null);
			System.out.println("================= prin strom - END");
		}
	}

	private Collection<InformaceOZdroji> getSubtree(final InformaceOZdroji subTreeRoot, final Collection<InformaceOZdroji> buffer) {
		final List<InformaceOZdroji> children = subTreeRoot.getChildren();
		buffer.add(subTreeRoot);
		for (final InformaceOZdroji child : children) {
			getSubtree(child, buffer);
		}
		return buffer;
	}
}