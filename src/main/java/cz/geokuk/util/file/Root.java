package cz.geokuk.util.file;

import java.io.File;
import java.util.regex.Pattern;

public class Root {
	public static class Def {
		final int		maxDepth;
		final Pattern	patternIncludes;
		final Pattern	patternExcludes;

		public Def(final int aMaxDepth, final Pattern aPatternIncludes, final Pattern aPatternExcludes) {
			super();
			maxDepth = aMaxDepth;
			patternIncludes = aPatternIncludes;
			patternExcludes = aPatternExcludes;
		}

		@Override
		public String toString() {
			return "Def [maxDepth=" + maxDepth + ", patternIncludes=" + patternIncludes + ", patternExcludes=" + patternExcludes + "]";
		}

	}

	public final File	dir;

	public final Def	def;

	public Root(final File aRoot, final Def aDef) {
		super();
		dir = aRoot;
		def = aDef;
		if (def == null) {
			throw new NullPointerException();
		}
		if (dir == null) {
			throw new NullPointerException();
		}
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
		final Root other = (Root) obj;
		if (dir == null) {
			if (other.dir != null) {
				return false;
			}
		} else if (!dir.equals(other.dir)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (dir == null ? 0 : dir.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Root [dir=" + dir + ", def=" + def + "]";
	}

}