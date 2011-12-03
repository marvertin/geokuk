package novacachenakachle;
import java.io.IOException;
import java.io.RandomAccessFile;
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
public class MemSoubory {

  private static final long N = Integer.MAX_VALUE / 2;
  //private static final long N = 2000000000;

  public static void main(String[] args) throws IOException, InterruptedException {

    RandomAccessFile raf = new RandomAccessFile("raf.txt", "rw");
    FileChannel fc = raf.getChannel();

    MappedByteBuffer buf = fc.map(MapMode.READ_WRITE, 0, N);
    raf.close();

    String s = "xxxxxxxxxxfrrrrrrrabcdef0123456 - fffffffffff\r\n";
    //    int j = 0;
    //    for (int i=0; i<N; i ++) {
    //      buf.put((byte) s.charAt(j));
    //      j ++;
    //      if (j >= s.length()) j = 0;
    //    }
    for (int k=0; k<100; k++) {
      int j = 0;
      for (int i=0; i<N; i+= 1) {
        @SuppressWarnings("unused")
        byte b = buf.get(i);
        buf.put(i, (byte) s.charAt(j));
        j ++;
        if (j >= s.length()) {
          j = 0;
        }
      }
      System.out.println("Projeto " + k);
      //      Thread.sleep(100);
    }

    for (;;) {
      Thread.sleep(5000);
    }

    //    buf.force();

  }

}
