package cz.geokuk.util.file;

import java.util.ArrayList;
import java.util.List;


public class WatchDogGroup {

  private final List<FileWatchDog<?>> list = new ArrayList<FileWatchDog<?>>();

  void forceIfAnyModified() {
    boolean zmena = false;
    for (FileWatchDog<?> fwd : list) {
      if (fwd.wasModified() > 0) {
        zmena = true;
      }
    }
    if (zmena) {
      for (FileWatchDog<?> fwd : list) {
        fwd.forceLoad();
      }
    }
  }

  void add(FileWatchDog<?> fwd) {
    list.add(fwd);
    fwd.setGroup(this);
  }

}
