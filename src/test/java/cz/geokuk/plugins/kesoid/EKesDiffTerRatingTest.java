package cz.geokuk.plugins.kesoid;

import org.junit.Assert;
import org.junit.Test;

public class EKesDiffTerRatingTest {

  @Test
  public void testUpravCislo0() {
    Assert.assertEquals("0", EKesDiffTerRating.UNKNOWN.to2DigitNumberString());
  }

  @Test
  public void testUpravCislo10() {
    Assert.assertEquals("10", EKesDiffTerRating.ONE.to2DigitNumberString());
  }

  @Test
  public void testUpravCislo15() {
    Assert.assertEquals("15", EKesDiffTerRating.ONE_HALF.to2DigitNumberString());
  }

  @Test
  public void testUpravCislo20() {
    Assert.assertEquals("20", EKesDiffTerRating.TWO.to2DigitNumberString());
  }

  @Test
  public void testUpravCislo25() {
    Assert.assertEquals("25", EKesDiffTerRating.TWO_HALF.to2DigitNumberString());
  }
  
  @Test
  public void testUpravCislo30() {
    Assert.assertEquals("30", EKesDiffTerRating.THREE.to2DigitNumberString());
  }
  
  @Test
  public void testUpravCislo35() {
    Assert.assertEquals("35", EKesDiffTerRating.THREE_HALF.to2DigitNumberString());
  }
  
  @Test
  public void testUpravCislo40() {
    Assert.assertEquals("40", EKesDiffTerRating.FOUR.to2DigitNumberString());
  }
  
  @Test
  public void testUpravCislo45() {
    Assert.assertEquals("45", EKesDiffTerRating.FOUR_HALF.to2DigitNumberString());
  }

  @Test
  public void testUpravCislo50() {
    Assert.assertEquals("50", EKesDiffTerRating.FIVE.to2DigitNumberString());
  }
  
}
