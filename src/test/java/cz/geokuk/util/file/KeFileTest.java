package cz.geokuk.util.file;

import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.util.file.KeFile.XRelativizeDubleDot;

public class KeFileTest {

  private static long NOW = System.currentTimeMillis();
  private static Pattern PATTERN = Pattern.compile(".");
  private static Pattern PATTERN2 = Pattern.compile("2");
  
  private KeFile create(String file, String root) {
    return create(file, root, NOW, PATTERN);
  }

  private KeFile create(String file, String root, long cas, Pattern pattern) {
    return new KeFile(new FileAndTime(new File(file), cas), new Root(new File(root), pattern));
  }

  private void assertFile(String fileName, File file) {
    Assert.assertEquals(new File(fileName), file);
  }
  
  @Test
  public void testKeFile1() {
    KeFile k = create("c:\\aa\\bb\\ccc", "c:\\aa\\bb");
    assertFile("c:/aa/bb/ccc", k.getFile());
  }

  @Test
  public void testKeFile2() {
    KeFile k = create("c:/aa/bb/ccc", "c:/aa/bb");
    assertFile("c:/aa/bb/ccc", k.getFile());
  }

  @Test
  public void testKeFile3() {
    KeFile k = create("c:/aa/bb/ccc", "c:/aa/bb");
    assertFile("c:/aa/bb", k.root.dir);
  }

  @Test
  public void testGetParent1() {
    KeFile k = create("c:\\aa\\bb\\ccc/ddd", "c:\\aa\\bb");
    assertFile("c:/aa/bb/ccc", k.getParent().getFile());
  }

  @Test
  public void testGetParent2() {
    KeFile k = create("c:\\aa\\bb", "c:\\aa\\bb");
    Assert.assertNull(k.getParent());
  }

  @Test(expected=XRelativizeDubleDot.class)
  public void testGetParent3() {
    create("c:\\jedna\\bb", "c:\\dva\\bb");
  }

  @Test(expected=XRelativizeDubleDot.class)
  public void testGetParent4() {
    create("c:\\aa\\bb", "c:\\aa\\bb/cc/dd");
  }


  @Test(expected=IllegalArgumentException.class)
  public void testGetParent5() {
    create("c:\\jedna\\bb", "d:\\dva\\bb");
  }


  @Test
  public void testGetLastModified() {
    KeFile k = create("c:\\aa\\bb\\ccc/ddd", "c:\\aa\\bb");
    Assert.assertEquals(NOW, k.getLastModified());
  }


  @Test
  public void testGetRelativePath() {
    KeFile k = create("c:\\aa\\bb\\ccc/ddd", "c:\\aa\\bb");
    Assert.assertEquals(Paths.get("ccc\\ddd"), k.getRelativePath());
  }

  @Test(expected=NullPointerException.class)
  public void tesNull1() {
    new KeFile(null, new Root(new File("roo"), PATTERN));
  }

  @Test(expected=NullPointerException.class)
  public void tesNull11() {
    new KeFile(new FileAndTime(null, NOW), new Root(new File("roo"), PATTERN));
  }

  @Test(expected=NullPointerException.class)
  public void tesNull111() {
    new KeFile(new FileAndTime(new File((String)null), NOW), new Root(new File("roo"), PATTERN));
  }

  @Test(expected=NullPointerException.class)
  public void tesNull2() {
    new KeFile(new FileAndTime(new File("aaa"), NOW), null);
  }

  @Test(expected=NullPointerException.class)
  public void tesNull21() {
    new KeFile(new FileAndTime(new File("aaa"), NOW), new Root(null, PATTERN));
  }

  @Test(expected=NullPointerException.class)
  public void tesNull211() {
    new KeFile(new FileAndTime(new File("aaa"), NOW), new Root(new File((String)null), PATTERN));
  }

  @Test(expected=NullPointerException.class)
  public void tesNull22() {
    new KeFile(new FileAndTime(new File("aaa"), NOW), new Root(new File("aaa"), null));
  }

  @Test(expected=NullPointerException.class)
  public void tesNullRoot1() {
    new Root(null, PATTERN);
  }

  @Test(expected=NullPointerException.class)
  public void tesNullRoot11() {
    new Root(new File((String)null), PATTERN);
  }

  @Test(expected=NullPointerException.class)
  public void tesNullRoot2() {
    new Root(new File("aaa"), null);
  }


  @Test
  public void testEquals1() {
    KeFile k1 = create("c:\\aa\\bb\\ccc/ddd", "c:\\aa\\bb");
    KeFile k2 = create("c:/aa\\bb\\ccc/ddd", "c:/aa\\bb");
    Assert.assertTrue(k1.equals(k2));
    Assert.assertTrue(k2.equals(k1));
    Assert.assertEquals(k1.hashCode(), k2.hashCode());
  }

  @Test
  public void testNotEquals2() {
    KeFile k1 = create("c:\\aa\\bb\\ccc/ddd", "c:\\aa");
    KeFile k2 = create("c:/aa\\bb\\ccc/ddd", "c:/aa\\bb");
    Assert.assertFalse(k1.equals(k2));
    Assert.assertFalse(k2.equals(k1));
  }

  @Test
  public void testEquals3() {
    KeFile k1 = create("c:\\aa\\bb\\ccc/ddd", "c:\\aa\\bb", NOW, PATTERN);
    KeFile k2 = create("c:/aa\\bb\\ccc/ddd", "c:/aa\\bb", 45456564, PATTERN);
    Assert.assertTrue(k1.equals(k2));
    Assert.assertTrue(k2.equals(k1));
    Assert.assertEquals(k1.hashCode(), k2.hashCode());
  }

  @Test
  public void testEquals4() {
    KeFile k1 = create("c:\\aa\\bb\\ccc/ddd", "c:\\aa\\bb", NOW, PATTERN);
    KeFile k2 = create("c:/aa\\bb\\ccc/ddd", "c:/aa\\bb", NOW, PATTERN2);
    Assert.assertTrue(k1.equals(k2));
    Assert.assertTrue(k2.equals(k1));
    Assert.assertEquals(k1.hashCode(), k2.hashCode());
  }

  @Test
  public void testNotEquals1() {
    KeFile k1 = create("c:\\aa\\bb\\ccXc/ddd", "c:\\aa\\bb");
    KeFile k2 = create("c:/aa\\bb\\ccc/ddd", "c:/aa\\bb");
    Assert.assertFalse(k1.equals(k2));
    Assert.assertFalse(k2.equals(k1));
  }


  @Test
  public void testNotEquals3() {
    KeFile k1 = create("c:\\aa\\bb\\ccXc/ddd1", "c:\\aa\\bb");
    KeFile k2 = create("c:\\aa\\bb\\ccXc/ddd2", "c:\\aa\\bb");
    Assert.assertFalse(k1.equals(k2));
    Assert.assertFalse(k2.equals(k1));
  }


}
