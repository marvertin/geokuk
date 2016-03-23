package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.util.*;

import cz.geokuk.util.file.KeFile;

public class InformaceOZdroji {
	private static class FileDirComparator implements Comparator<File> {
		private static final FileDirComparator INSTANCE = new FileDirComparator();

		@Override
		public int compare(final File file1, final File file2) {
			if (file1.isDirectory() && file2.isFile()) {
				return -1;
			} else if (file1.isFile() && file2.isDirectory()) {
				return 1;
			} else {
				return compareNames(file1, file2);
			}
		}

		private int compareNames(final File file1, final File file2) {
			return file1.getName().compareToIgnoreCase(file2.getName());
		}
	}

	private static class InformaceOZdrojiComparator implements Comparator<InformaceOZdroji> {
		private static final InformaceOZdrojiComparator INSTANCE = new InformaceOZdrojiComparator();

		@Override
		public int compare(final InformaceOZdroji o1, final InformaceOZdroji o2) {
			return FileDirComparator.INSTANCE.compare(o1.jmenoZdroje.getFile(), o2.jmenoZdroje.getFile());
		}
	}

	public final KeFile jmenoZdroje;
	public int pocetWaypointuCelkem;
	public int pocetWaypointuBranych;
	public int pocetWaypointuCelkemChildren;
	public int pocetWaypointuBranychChildren;
	public final boolean nacteno;

	private final List<InformaceOZdroji> children = new ArrayList<>();

	public InformaceOZdroji parent;

	public InformaceOZdroji(final KeFile jmenoZdroje, final boolean nacteno) {
		this.jmenoZdroje = jmenoZdroje;
		this.nacteno = nacteno;
	}

	public void addChild(final InformaceOZdroji child) {
		// TODO : use a sane data structure for this
		children.add(child);
		Collections.sort(children, InformaceOZdrojiComparator.INSTANCE);
	}

	public String getDisplayName() {
		if (parent.parent != null) {
			return parent.jmenoZdroje.getFile().toPath().relativize(jmenoZdroje.getFile().toPath()).toString();
		} else {
			return jmenoZdroje.getFile().getAbsolutePath();
		}
	}

	public List<InformaceOZdroji> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public long getLastModified() {
		return jmenoZdroje.getLastModified();
	}

	public int getPocetWaypointuBranychSDetmi() {
		return pocetWaypointuBranych + pocetWaypointuBranychChildren;
	}

	public int getPocetWaypointuCelkemSDetmi() {
		return pocetWaypointuCelkem + pocetWaypointuCelkemChildren;
	}

	/**
	 * Rekuzivně vypíše intormace
	 *
	 * @param maToBytParent
	 */
	public void print(final String odsazovac, final InformaceOZdroji maToBytParent) {
		final String parentProblem = parent == maToBytParent ? "" : "!!!  NESEDI PARENT: " + parent + " <> " + maToBytParent;
		System.out.println(odsazovac + jmenoZdroje.getFile() + parentProblem);
		for (final InformaceOZdroji info : getChildren()) {
			info.print(odsazovac + "  ", this);
		}
	}

	public InformaceOZdroji removeChild(final int index) {
		final InformaceOZdroji odebranec = children.remove(index);
		odebranec.parent = null;
		return odebranec;
	}

	public int remplaceChild(final InformaceOZdroji iozOld, final InformaceOZdroji iozNew) {
		final int index = children.indexOf(iozOld);
		children.set(index, iozNew);
		return index;
	}

	@Override
	public String toString() {
		return jmenoZdroje.toString();
	}

	void spocitejSiPocetWaipointuChildren() {
		pocetWaypointuBranychChildren = 0;
		pocetWaypointuCelkemChildren = 0;
		for (final InformaceOZdroji ioz : children) {
			ioz.spocitejSiPocetWaipointuChildren();
			pocetWaypointuBranychChildren += ioz.getPocetWaypointuBranychSDetmi();
			pocetWaypointuCelkemChildren += ioz.getPocetWaypointuCelkemSDetmi();
		}

	}
}