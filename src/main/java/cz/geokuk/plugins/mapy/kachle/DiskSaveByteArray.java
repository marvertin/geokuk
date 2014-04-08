package cz.geokuk.plugins.mapy.kachle;

import java.io.IOException;
import java.io.OutputStream;

class DiskSaveByteArray implements ImageSaver {

  private final byte[] data;

  public DiskSaveByteArray(byte[] data) {
    this.data = data;
  }

  @Override
  public void save(OutputStream ostm) throws IOException {
    ostm.write(data);
  }
}
