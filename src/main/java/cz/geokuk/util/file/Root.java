package cz.geokuk.util.file;

import java.io.File;
import java.util.regex.Pattern;

public class Root {
  public final File dir;
  public final Pattern pattern;

  public Root(File aRoot, Pattern aPattern) {
    super();
    dir = aRoot;
    pattern = aPattern;
    if (pattern == null) {
      throw new NullPointerException();
    }
    if (dir == null) {
      throw new NullPointerException();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
    result = prime * result + ((dir == null) ? 0 : dir.hashCode());
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
    Root other = (Root) obj;
    if (pattern == null) {
      if (other.pattern != null)
        return false;
    } else if (!pattern.equals(other.pattern))
      return false;
    if (dir == null) {
      if (other.dir != null)
        return false;
    } else if (!dir.equals(other.dir))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Root [dir=" + dir + ", pattern=" + pattern + "]";
  }
  
  
}