package novacachenakachle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * 
 */

/**
 * @author veverka
 *
 */
public class KachInd {

  int PAGE_SIZE = 256; // poèet položek stránky
  int PAGE_BYTES = PAGE_SIZE * 4; // poèet bytù stránky
  int MAX_FILE_SIZE = 1024 * 1024 * 1024; // vìtší než gigové indexy nebereme
  byte[] NULY = new byte[PAGE_BYTES];

  private int nextPosition = 0;
  @SuppressWarnings("unused")
  private MappedByteBuffer buf;


  public void _openFile(File file, boolean aReadWrite, int aPrirustek) throws IOException {
    buf = null; // aby GC buffer odstranil
    long actualLength = file.length();
    nextPosition = (int) Math.min(actualLength, MAX_FILE_SIZE);
    nextPosition = nextPosition / PAGE_BYTES * PAGE_BYTES;
    RandomAccessFile raf = new RandomAccessFile(file, aReadWrite ? "rw" : "r");
    try {
      FileChannel fc = raf.getChannel();
      try {
        if (nextPosition != actualLength)
        {
          fc.truncate(nextPosition);  // zarovnání na pozici
        }

        int length = zarovnejDelku(nextPosition + aPrirustek);
        int prirustekPages = (length - nextPosition) / PAGE_SIZE;
        ByteBuffer bb = ByteBuffer.wrap(NULY);
        for (int i=0; i < prirustekPages; i++) {
          bb.rewind();
          fc.write(bb);
        }

        buf = fc.map(MapMode.READ_WRITE, 0, length);
        fc.lock();
      } finally {
        fc.close();
      }
      //for (int i =0; i 1buf.
    } finally {
      raf.close();
    }
  }

  private int zarovnejDelku(long delka) {
    int l = (int) Math.min(delka, MAX_FILE_SIZE);
    l = l / PAGE_BYTES * PAGE_BYTES;
    return l;
  }
}
